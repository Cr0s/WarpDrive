package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public final class EntitySphereGen extends Entity
{
	public int xCoord;
	public int yCoord;
	public int zCoord;

	private int radius;
	private int[] block;
	public int[] defaultBlock;
	private boolean hollow;
	private boolean fillingSphere; // new sphere blocks does not replace existing blocks (for gases)
	private boolean surfaceSphere; // generate random surface blocks or fixed blockID

	private final int BLOCKS_PER_TICK = 10000;

	private final int STATE_SAVING = 0;
	private final int STATE_SETUP = 1;
	private final int STATE_STOP = 2;
	private final int STATE_DELETE = 3;
	private int state = STATE_DELETE;

	private int currentIndex = 0;

	private ArrayList<JumpBlock> blocks;

	public EntitySphereGen(World world)
	{
		super(world);
System.out.println("ZLO EntitySphereGen THE FUCK create");
	}

	public EntitySphereGen(World world, int x, int y, int z, int radius, int blockID, int blockMeta, boolean hollow, boolean fillingSphere)
	{
		super(world);
		this.xCoord = x;
		this.posX = (double) x;
		this.yCoord = y;
		this.posY = (double) y;
		this.zCoord = z;
		this.posZ = (double) z;
		this.radius = radius;
		this.hollow = hollow;
		this.fillingSphere = fillingSphere;
		this.surfaceSphere = (blockID == 0);
		this.state = STATE_SAVING;
		blocks = new ArrayList<JumpBlock>();
		if (surfaceSphere)
			defaultBlock = WarpDriveConfig.i.getDefaultSurfaceBlock(world.rand, world.rand.nextInt(10) > 8, true);
		else
			this.block = new int[] {blockID, blockMeta};
	}

	public void killEntity()
	{
		this.state = STATE_STOP;
		worldObj.removeEntity(this);
	}

	@Override
	public void onUpdate()
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			return;
		}

		switch (this.state)
		{
			case STATE_SAVING:
				System.out.println("[ESG] Saving blocks...");
				saveSphereBlocks();
				this.state = STATE_SETUP;
				break;
			case STATE_SETUP:
				if (currentIndex >= blocks.size() - 1)
					this.state = STATE_DELETE;
				else
					setupBlocksTick();
				break;
			case STATE_DELETE:
				currentIndex = 0;
				killEntity();
				break;
		}
	}

	private void setupBlocksTick()
	{
		LocalProfiler.start("EntitySphereGen.setupBlocksTick");
		int blocksToMove = Math.min(BLOCKS_PER_TICK, blocks.size() - currentIndex);
		System.out.println("[ESG] Setting up blocks: " + currentIndex + "/" + blocks.size() + " [bts: " + blocksToMove + "]");
		int notifyFlag;

		for (int index = 0; index < blocksToMove; index++)
		{
			if (currentIndex >= blocks.size())
				break;
			notifyFlag = (currentIndex % 1000 == 0 ? 2 : 0);
			JumpBlock jb = blocks.get(currentIndex);
			mySetBlock(worldObj, jb.x, jb.y, jb.z, jb.blockID, jb.blockMeta, notifyFlag);
			currentIndex++;
		}

		LocalProfiler.stop();
	}

	private void saveSphereBlocks()
	{
		radius += 0.5D; // Radius from center of block
		double radiusSq = radius * radius; // Optimization to avoid square roots
		double radius1Sq = (radius - 1.0D) * (radius - 1.0D); // for hollow sphere
		int ceilRadius = (int) Math.ceil(radius);

		// Pass the cube and check points for sphere equation x^2 + y^2 + z^2 = r^2
		for (int x = 0; x <= ceilRadius; x++)
		{
			for (int y = 0; y <= ceilRadius; y++)
			{
				for (int z = 0; z <= ceilRadius; z++)
				{
					double dSq = lengthSq(x, y, z); // Distance from current position to center

					// Skip too far blocks
					if (dSq > radiusSq)
						continue;
					// Hollow sphere condition
					if ((hollow) && ((dSq < radius1Sq) || ((lengthSq(x + 1, y, z) <= radiusSq) && (lengthSq(x, y + 1, z) <= radiusSq) && (lengthSq(x, y, z + 1) <= radiusSq))))
						continue;
					if (surfaceSphere)
						block = WarpDriveConfig.i.getRandomSurfaceBlock(worldObj.rand, defaultBlock[0], defaultBlock[1], true);
					// Add blocks to memory
					addBlock(new JumpBlock(block[0], block[1], xCoord + x, yCoord + y, zCoord + z));
					if (surfaceSphere)
						block = WarpDriveConfig.i.getRandomSurfaceBlock(worldObj.rand, defaultBlock[0], defaultBlock[1], true);
					addBlock(new JumpBlock(block[0], block[1], xCoord - x, yCoord + y, zCoord + z));
					if (surfaceSphere)
						block = WarpDriveConfig.i.getRandomSurfaceBlock(worldObj.rand, defaultBlock[0], defaultBlock[1], true);
					addBlock(new JumpBlock(block[0], block[1], xCoord + x, yCoord - y, zCoord + z));
					if (surfaceSphere)
						block = WarpDriveConfig.i.getRandomSurfaceBlock(worldObj.rand, defaultBlock[0], defaultBlock[1], true);
					addBlock(new JumpBlock(block[0], block[1], xCoord + x, yCoord + y, zCoord - z));
					if (surfaceSphere)
						block = WarpDriveConfig.i.getRandomSurfaceBlock(worldObj.rand, defaultBlock[0], defaultBlock[1], true);
					addBlock(new JumpBlock(block[0], block[1], xCoord - x, yCoord - y, zCoord + z));
					if (surfaceSphere)
						block = WarpDriveConfig.i.getRandomSurfaceBlock(worldObj.rand, defaultBlock[0], defaultBlock[1], true);
					addBlock(new JumpBlock(block[0], block[1], xCoord + x, yCoord - y, zCoord - z));
					if (surfaceSphere)
						block = WarpDriveConfig.i.getRandomSurfaceBlock(worldObj.rand, defaultBlock[0], defaultBlock[1], true);
					addBlock(new JumpBlock(block[0], block[1], xCoord - x, yCoord + y, zCoord - z));
					if (surfaceSphere)
						block = WarpDriveConfig.i.getRandomSurfaceBlock(worldObj.rand, defaultBlock[0], defaultBlock[1], true);
					addBlock(new JumpBlock(block[0], block[1], xCoord - x, yCoord - y, zCoord - z));
				}
			}
		}
		if (blocks != null)
			System.out.println("[ESG] Saved " + blocks.size() + " blocks");
	}

	private void addBlock(JumpBlock jb)
	{
		// Do not replace exitsting blocks if fillingSphere is true
		if (fillingSphere && !worldObj.isAirBlock(jb.x, jb.y, jb.z))
			return;
		if (blocks == null)
			return;
		blocks.add(jb);
	}

	private static double lengthSq(double x, double y, double z)
	{
		return (x * x) + (y * y) + (z * z);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag)
	{
	}

	@Override
	protected void entityInit()
	{
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tag)
	{
	}

	// Own implementation of setting blocks withow light recalculation in optimization purposes
	public boolean mySetBlock(World w, int x, int y, int z, int blockId, int blockMeta, int par6)
	{
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000)
		{
			if (y < 0)
				return false;
			else if (y >= 256)
				return false;
			w.markBlockForUpdate(x, y, z);
			Chunk chunk = w.getChunkFromChunkCoords(x >> 4, z >> 4);
			return myChunkSBIDWMT(chunk, x & 15, y, z & 15, blockId, blockMeta);
		}
		else
			return false;
	}

	public boolean myChunkSBIDWMT(Chunk c, int x, int y, int z, int blockId, int blockMeta)
	{
		int j1 = z << 4 | x;
		if (y >= c.precipitationHeightMap[j1] - 1)
			c.precipitationHeightMap[j1] = -999;
		int l1 = c.getBlockID(x, y, z);
		int i2 = c.getBlockMetadata(x, y, z);
		if (l1 == blockId && i2 == blockMeta)
			return false;
		ExtendedBlockStorage[] storageArrays = c.getBlockStorageArray();
		ExtendedBlockStorage extendedblockstorage = storageArrays[y >> 4];
		if (extendedblockstorage == null)
		{
			if (blockId == 0)
				return false;
			extendedblockstorage = storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !c.worldObj.provider.hasNoSky);
		}
		int j2 = c.xPosition * 16 + x;
		int k2 = c.zPosition * 16 + z;
		extendedblockstorage.setExtBlockID(x, y & 15, z, blockId);
		if (extendedblockstorage.getExtBlockID(x, y & 15, z) != blockId)
			return false;
		extendedblockstorage.setExtBlockMetadata(x, y & 15, z, blockMeta);
		if (blockId != 0)
			if (Block.blocksList[blockId] != null && Block.blocksList[blockId].hasTileEntity(blockMeta))
			{
				TileEntity tileentity = c.getChunkBlockTileEntity(x, y, z);
				if (tileentity == null)
				{
					tileentity = Block.blocksList[blockId].createTileEntity(c.worldObj, blockMeta);
					c.worldObj.setBlockTileEntity(j2, y, k2, tileentity);
				}
				else
				{
					tileentity.updateContainingBlockInfo();
					tileentity.blockMetadata = blockMeta;
				}
			}
		c.isModified = true;
		return true;
	}
	
    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return false;
    }	
}