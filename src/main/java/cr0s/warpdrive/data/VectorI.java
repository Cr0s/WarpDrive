package cr0s.warpdrive.data;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.util.ForgeDirection;


/**
 * Generic 3D vector for efficient block manipulation.
 * Loosely based on Mojang Vec3 and Calclavia Vector3. 
 *
 * @author LemADEC
 */
public class VectorI implements Cloneable {
	public int x;
	public int y;
	public int z;
	
	public VectorI() {
		this(0, 0, 0);
	}
	
	// constructor from float/double is voluntarily skipped
	// if you need it, you're probably doing something wrong :)
	
	public VectorI(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public VectorI(final Entity entity) {
		x = ((int) Math.floor(entity.posX));
		y = ((int) Math.floor(entity.posY));
		z = ((int) Math.floor(entity.posZ));
	}
	
	public VectorI(final TileEntity tileEntity) {
		x = tileEntity.xCoord;
		y = tileEntity.yCoord;
		z = tileEntity.zCoord;
	}
	
	public VectorI(final MovingObjectPosition movingObject) {
		this.x = movingObject.blockX;
		this.y = movingObject.blockY;
		this.z = movingObject.blockZ;
	}
	
	public VectorI(final ChunkCoordinates chunkCoordinates) {
		this.x = chunkCoordinates.posX;
		this.y = chunkCoordinates.posY;
		this.z = chunkCoordinates.posZ;
	}
	
	public VectorI(final ForgeDirection direction) {
		this.x = direction.offsetX;
		this.y = direction.offsetY;
		this.z = direction.offsetZ;
	}
	
	
	public Vector3 getBlockCenter() {
		return new Vector3(x + 0.5D, y + 0.5D, z + 0.5D);
	}
	
	
	@Override
	public VectorI clone() {
		return new VectorI(x, y, z);
	}
	
	public VectorI invertedClone() {
		return new VectorI(-x, -y, -z);
	}
	
	
	public Block getBlock(IBlockAccess world) {
		return world.getBlock(x, y, z);
	}
	
	public Block getBlock_noChunkLoading(IBlockAccess world) {
		return getBlock_noChunkLoading(world, x, y, z);
	}
	
	static public Block getBlock_noChunkLoading(IBlockAccess world, final int x, final int y, final int z) {
		// skip unloaded worlds
		if (world == null) {
			return null;
		}
		if (world instanceof WorldServer) {
			boolean isLoaded = false;
			if (((WorldServer)world).getChunkProvider() instanceof ChunkProviderServer) {
				ChunkProviderServer chunkProviderServer = (ChunkProviderServer) ((WorldServer)world).getChunkProvider();
				try {
					Chunk chunk = (Chunk)chunkProviderServer.loadedChunkHashMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x >> 4, z >> 4));
					if (chunk == null) {
						isLoaded = false;
					} else {
						isLoaded = chunk.isChunkLoaded;
					}
				} catch (NoSuchFieldError exception) {
					isLoaded = chunkProviderServer.chunkExists(x >> 4, z >> 4);
				}
			} else {
				isLoaded = ((WorldServer)world).getChunkProvider().chunkExists(x >> 4, z >> 4);
			}
			// skip unloaded chunks
			if (!isLoaded) {
				return null;
			}
		}
		return world.getBlock(x, y, z);
	}
	
	public TileEntity getTileEntity(IBlockAccess world) {
		return world.getTileEntity(x, y, z);
	}
	
	public int getBlockMetadata(IBlockAccess world) {
		return world.getBlockMetadata(x, y, z);
	}
	
	public void setBlock(World worldObj, final Block block) {
		worldObj.setBlock(x, y, z, block, 0, 3);
	}
	
	public void setBlock(World worldObj, final Block block, final int metadata) {
		worldObj.setBlock(x, y, z, block, metadata, 3);
	}
	
	
	// modify current vector by adding another one
	public VectorI translate(final VectorI vector) {
		x += vector.x;
		y += vector.y;
		z += vector.z;
		return this;
	}

	// modify current vector by subtracting another one
	public VectorI translateBack(final VectorI vector) {
		x -= vector.x;
		y -= vector.y;
		z -= vector.z;
		return this;
	}
	
	// modify current vector by translation of amount block in side direction
	public VectorI translate(final ForgeDirection side, final int amount) {
		switch (side) {
		case DOWN:
			y -= amount;
			break;
		case UP:
			y += amount;
			break;
		case NORTH:
			z -= amount;
			break;
		case SOUTH:
			z += amount;
			break;
		case WEST:
			x -= amount;
			break;
		case EAST:
			x += amount;
		}
		
		return this;
	}
	
	// modify current vector by translation of 1 block in side direction
	public VectorI translate(final ForgeDirection side) {
		x += side.offsetX;
		y += side.offsetY;
		z += side.offsetZ;
		return this;
	}

	
	// return a new vector adding both parts
	public static VectorI add(final VectorI vector1, final VectorI vector2) {
		return new VectorI(vector1.x + vector2.x, vector1.y + vector2.y, vector1.z + vector2.z);
	}
	
	// return a new vector adding both parts
	public VectorI add(final VectorI vector) {
		return new VectorI(x + vector.x, y + vector.y, z + vector.z);
	}
	
	// return a new vector adding both parts
	@Deprecated
	public VectorI add(final Vector3 vector) {
		x = ((int) (x + Math.round(vector.x)));
		y = ((int) (y + Math.round(vector.y)));
		z = ((int) (z + Math.round(vector.z)));
		return this;
	}
	
	
	// return a new vector subtracting both parts
	public static VectorI subtract(final VectorI vector1, final VectorI vector2) {
		return new VectorI(vector1.x - vector2.x, vector1.y - vector2.y, vector1.z - vector2.z);
	}
	
	// return a new vector subtracting the argument from current vector
	public VectorI subtract(final VectorI vector) {
		return new VectorI(x - vector.x, y - vector.y, z - vector.z);
	}
	
	
	@Deprecated
	public static VectorI set(final Vector3 vector) {
		return new VectorI((int) Math.round(vector.x), (int) Math.round(vector.y), (int) Math.round(vector.z));
	}
	
	@Override
	public int hashCode() {
		return (x + " " + y + " " + z).hashCode();
	}
	
	@Override
	public boolean equals(final Object object) {
		if (object instanceof VectorI) {
			VectorI vector = (VectorI) object;
			return (x == vector.x) && (y == vector.y) && (z == vector.z);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "VectorI [" + x + "," + y + "," + z + "]";
	}
	
	
	public static VectorI readFromNBT(NBTTagCompound nbtCompound) {
		VectorI vector = new VectorI();
		vector.x = nbtCompound.getInteger("x");
		vector.y = nbtCompound.getInteger("y");
		vector.z = nbtCompound.getInteger("z");
		return vector;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound) {
		nbtCompound.setInteger("x", x);
		nbtCompound.setInteger("y", y);
		nbtCompound.setInteger("z", z);
		return nbtCompound;
	}
	
	// Square roots are evil, avoid them at all cost
	@Deprecated
	public double distanceTo(final VectorI vector) {
		int newX = vector.x - x;
		int newY = vector.y - y;
		int newZ = vector.z - z;
		return Math.sqrt(newX * newX + newY * newY + newZ * newZ);
	}
	
	public int distance2To(final VectorI vector) {
		int newX = vector.x - x;
		int newY = vector.y - y;
		int newZ = vector.z - z;
		return (newX * newX + newY * newY + newZ * newZ);
	}
	
	public int distance2To(final Entity entity) {
		int newX = (int) (Math.round(entity.posX)) - x;
		int newY = (int) (Math.round(entity.posY)) - y;
		int newZ = (int) (Math.round(entity.posZ)) - z;
		return (newX * newX + newY * newY + newZ * newZ);
	}
	
	public int distance2To(final TileEntity tileEntity) {
		int newX = tileEntity.xCoord - x;
		int newY = tileEntity.yCoord - y;
		int newZ = tileEntity.zCoord - z;
		return (newX * newX + newY * newY + newZ * newZ);
	}
	
	static public int distance2To(final VectorI vector1, final VectorI vector2) {
		int newX = vector1.x - vector2.x;
		int newY = vector1.y - vector2.y;
		int newZ = vector1.z - vector2.z;
		return (newX * newX + newY * newY + newZ * newZ);
	}
	
	// Square roots are evil, avoid them at all cost
	@Deprecated
	public double getMagnitude() {
		return Math.sqrt(getMagnitudeSquared());
	}
	
	public int getMagnitudeSquared() {
		return x * x + y * y + z * z;
	}
}