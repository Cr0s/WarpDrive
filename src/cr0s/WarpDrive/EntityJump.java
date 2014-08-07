package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.machines.TileEntityReactor;
import dan200.computercraft.api.peripheral.IPeripheral;
import ic2.api.network.NetworkHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public class EntityJump extends Entity
{
	// Jump vector
	private int moveX;
	private int moveY;
	private int moveZ;

	private int xCoord;
	private int yCoord;
	private int zCoord;
	private int dx;
	private int dz;
	private int distance;
	private int direction;
	public int shipLength;
	public int maxX;
	public int maxZ;
	public int maxY;
	public int minX;
	public int minZ;
	public int minY;

	private boolean isHyperspaceJump;

	private World targetWorld;
	private Ticket sourceWorldTicket;
	private Ticket targetWorldTicket;

	private boolean collisionDetected = false;
	private ArrayList<Vector3> collisionAtSource;
	private ArrayList<Vector3> collisionAtTarget;
	private float collisionStrength = 0; 

	public boolean on = false;
	private JumpBlock ship[];
	private TileEntityReactor reactor;

	private final static int STATE_IDLE = 0;
	private final static int STATE_JUMPING = 1;
	private final static int STATE_REMOVING = 2;
	private int state = STATE_IDLE;
	private int currentIndexInShip = 0;

	private final int BLOCKS_PER_TICK = 3500;

	private List<MovingEntity> entitiesOnShip;
	private List<TileEntity> ASTurbines;

	private boolean betweenWorlds;

	private int destX, destY, destZ;
	private boolean isCoordJump;

	private long msCounter = 0;
	private int ticks = 0;

	public EntityJump(World world)
	{
		super(world);
		targetWorld = worldObj;
		WarpDrive.debugPrint("" + this + " Entity created (empty) in dimension " + worldObj.getProviderName() + " - " + worldObj.getWorldInfo().getWorldName()
					+ " " + (FMLCommonHandler.instance().getEffectiveSide().isClient() ? "Client":"Server"));
	}

	public EntityJump(World world, int x, int y, int z, int _dx, int _dz, TileEntityReactor _reactor,
			boolean _isHyperspaceJump, int _distance, int _direction, boolean _isCoordJump, int _destX, int _destY, int _destZ) {
		super(world);
		this.posX = x + 0.5D;
		this.posY = y + 0.5D;
		this.posZ = z + 0.5D;
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
		this.dx = _dx;
		this.dz = _dz;
		this.reactor = _reactor;
        this.isHyperspaceJump = _isHyperspaceJump;
		this.distance = _distance;
		this.direction = _direction;
		this.isCoordJump = _isCoordJump;
		this.destX = _destX;
		this.destY = _destY;
		this.destZ = _destZ;

		// set by reactor
		maxX = maxZ = maxY = minX = minZ = minY = 0;
		shipLength = 0;
		
		// set when preparing jump
		targetWorld = null;
		
		WarpDrive.debugPrint("" + this + " Entity created");
	}

	public void killEntity(String reason) {
		if (!on) {
			return;
		}

		on = false;

		if (reason == null || reason.isEmpty()) {
			WarpDrive.debugPrint("" + this + " Killing jump entity...");
		} else {
			WarpDrive.debugPrint("" + this + " Killing jump entity... (" + reason + ")");
		}

//		unlockWorlds();
		unforceChunks();
		worldObj.removeEntity(this);
	}
	
	@Override
    public boolean isEntityInvulnerable() {
        return true;
    }
	
	@Override
	public void onUpdate() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}

		if (!on) {
			System.out.println("" + this + " Removing from onUpdate...");
			worldObj.removeEntity(this);
			return;
		}

		if (minY < 0 || maxY > 256) {
			String msg = "Invalid Y coordinate(s), check ship dimensions...";
			messageToAllPlayersOnShip(msg);
			killEntity(msg);
			return;
		}

		ticks++;
		if (state == STATE_IDLE) {
			WarpDrive.debugPrint("" + this + " Preparing to jump...");
			prepareToJump();
			if (on) {
				state = STATE_JUMPING;
			}
		} else if (state == STATE_JUMPING) {
			if (currentIndexInShip < ship.length - 1) {
				//moveEntities(true);
				moveShip();
			} else {
				moveEntities(false);
				currentIndexInShip = 0;
				state = STATE_REMOVING;
			}
		} else if (state == STATE_REMOVING) {
			ASTurbines = new ArrayList<TileEntity>();
			removeShip();

			if (currentIndexInShip >= ship.length - 1) {
				finishJump();
				state = STATE_IDLE;
			}
		} else {
			String msg = "Invalid state, aborting jump...";
			messageToAllPlayersOnShip(msg);
			killEntity(msg);
			return;
		}
	}

	private boolean forceChunks(StringBuilder reason)
	{
		LocalProfiler.start("EntityJump.forceChunks");
		WarpDrive.debugPrint("" + this + " Forcing chunks in " + worldObj.provider.getDimensionName() + " and " + targetWorld.provider.getDimensionName());
		sourceWorldTicket = ForgeChunkManager.requestTicket(WarpDrive.instance, worldObj, Type.NORMAL);	// Type.ENTITY);
		if (sourceWorldTicket == null) {
			reason.append("Chunkloading rejected in S:" + worldObj.getWorldInfo().getWorldName() + ". Aborting.");
			return false;
		}
		targetWorldTicket = ForgeChunkManager.requestTicket(WarpDrive.instance, targetWorld, Type.NORMAL);
		if (targetWorldTicket == null) {
			reason.append("Chunkloading rejected in T:" + worldObj.getWorldInfo().getWorldName() + ". Aborting.");
			return false;
		}
//		sourceWorldTicket.bindEntity(this);
		int x1 = minX >> 4;
		int x2 = maxX >> 4;
		int z1 = minZ >> 4;
		int z2 = maxZ >> 4;
		int chunkCount = 0;
		for (int x = x1; x <= x2; x++)
		{
			for (int z = z1; z <= z2; z++)
			{
				chunkCount++;
				if (chunkCount > sourceWorldTicket.getMaxChunkListDepth()) {
					reason.append("Ship is extending over too many chunks, max is S:" + sourceWorldTicket.getMaxChunkListDepth() + ". Aborting.");
					return false;
				}
				ForgeChunkManager.forceChunk(sourceWorldTicket, new ChunkCoordIntPair(x, z));
			}
		}

		x1 = (minX + moveX) >> 4;
		x2 = (maxX + moveX) >> 4;
		z1 = (minZ + moveZ) >> 4;
		z2 = (maxZ + moveZ) >> 4;
		chunkCount = 0;
		for (int x = x1; x <= x2; x++)
		{
			for (int z = z1; z <= z2; z++)
			{
				chunkCount++;
				if (chunkCount > targetWorldTicket.getMaxChunkListDepth()) {
					reason.append("Ship is extending over too many chunks, max is T:" + sourceWorldTicket.getMaxChunkListDepth() + ". Aborting.");
					return false;
				}
				ForgeChunkManager.forceChunk(targetWorldTicket, new ChunkCoordIntPair(x, z));
			}
		}
		LocalProfiler.stop();
		return true;
	}

	private void unforceChunks()
	{
		LocalProfiler.start("EntityJump.unforceChunks");
		WarpDrive.debugPrint("" + this + " Unforcing chunks");

		int x1, x2, z1, z2;
		if (sourceWorldTicket != null) {
			x1 = minX >> 4;
			x2 = maxX >> 4;
			z1 = minZ >> 4;
			z2 = maxZ >> 4;
			for (int x = x1; x <= x2; x++) {
				for (int z = z1; z <= z2; z++) {
					ForgeChunkManager.unforceChunk(sourceWorldTicket, new ChunkCoordIntPair(x, z));
				}
			}
			ForgeChunkManager.releaseTicket(sourceWorldTicket);
			sourceWorldTicket = null;
		}

		if (targetWorldTicket != null) {
			x1 = (minX + moveX) >> 4;
			x2 = (maxX + moveX) >> 4;
			z1 = (minZ + moveZ) >> 4;
			z2 = (maxZ + moveZ) >> 4;
			for (int x = x1; x <= x2; x++) {
				for (int z = z1; z <= z2; z++) {
					ForgeChunkManager.unforceChunk(targetWorldTicket, new ChunkCoordIntPair(x, z));
				}
			}
			ForgeChunkManager.releaseTicket(targetWorldTicket);
			targetWorldTicket = null;
		}
		
		LocalProfiler.stop();
	}

	private void messageToAllPlayersOnShip(String msg) {
		if (entitiesOnShip == null) {
			reactor.messageToAllPlayersOnShip(msg);
		} else {
			System.out.println("" + this + " messageToAllPlayersOnShip: " + msg);
			for (MovingEntity me : entitiesOnShip) {
				if (me.entity instanceof EntityPlayer) {
					((EntityPlayer)me.entity).addChatMessage("[" + ((reactor != null && reactor.coreFrequency.length() > 0) ? reactor.coreFrequency : "WarpCore") + "] " + msg);
				}
			}
		}
	}

	public static String getDirectionLabel(int direction) {
		switch (direction) {
			case -1: return "UP";
			case -2: return "DOWN";
			case 0: return "FRONT";
			case 180: return "BACK";
			case 90: return "LEFT";
			case 255: return "RIGHT";
			default: return direction + " degrees";
		}
	}
	
	private void prepareToJump() {
		StringBuilder reason = new StringBuilder();

		LocalProfiler.start("EntityJump.prepareToJump");
		boolean isInSpace = (worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID);
		boolean isInHyperSpace = (worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID);

		boolean toSpace   = (direction == -1) && (maxY + distance > 255) && (!isInSpace) && (!isInHyperSpace);
		boolean fromSpace = (direction == -2) && (minY - distance < 0) && isInSpace;
		betweenWorlds = fromSpace || toSpace || isHyperspaceJump;
		moveX = moveY = moveZ = 0;
		
		TransitionPlane overworld = new TransitionPlane(0, 0, 0, 5000, 5000, 0, 0);
		Vector3 exit; //FIXME
		if (toSpace) {
			if (worldObj.provider.dimensionId == overworld.dimensionId) {
				if (!overworld.isValidToSpace(new Vector3(this))) {// invalid transition, cancel transition
					LocalProfiler.stop();
					String msg = "Ship is outside worldborder, unable to transition to space!";
					messageToAllPlayersOnShip(msg);
					killEntity(msg);
					return;
				}
				moveX = overworld.spaceCenterX - overworld.dimensionCenterX;
				moveZ = overworld.spaceCenterZ - overworld.dimensionCenterZ;
			} else {
				moveX = 0;
				moveZ = 0;
			}
			targetWorld = DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID);
		} else if (fromSpace) {
			if (!overworld.isValidFromSpace(new Vector3(this))) {// invalid transition, cancel transition
				LocalProfiler.stop();
				String msg = "Ship is outside worldborder, unable to transition from space!";
				messageToAllPlayersOnShip(msg);
				killEntity(msg);
				return;
			}
			moveX = overworld.dimensionCenterX - overworld.spaceCenterX;
			moveZ = overworld.dimensionCenterZ - overworld.spaceCenterZ;
			targetWorld = DimensionManager.getWorld(0);
		} else if (isHyperspaceJump && isInHyperSpace) {
			targetWorld = DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID);
		} else if (isHyperspaceJump && isInSpace) {
			targetWorld = DimensionManager.getWorld(WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID);
		} else {
			targetWorld = worldObj;
		}

		// FIXME
		//turnOffModems();

		// Calculate jump vector
		if (isCoordJump) {
			moveX = destX - xCoord;
			moveZ = destZ - zCoord;
			moveY = destY - yCoord;
			distance = 0;	// FIXME: check collision in straight path, starting with getPossibleJumpDistance() ?
		} else if (isHyperspaceJump) {
			distance = 0;
		} else {
			if (toSpace) {
				// enter space at current altitude
				moveY = 0;
			} else if (fromSpace) {
				// re-enter atmosphere at max altitude
				moveY = 245 - maxY;
			} else {
				// Do not check in long jumps
				if (distance < 256) {
					distance = getPossibleJumpDistance();
				}

				int movementVector[] = getVector(direction);
				moveX = movementVector[0] * distance;
				moveY = movementVector[1] * distance;
				moveZ = movementVector[2] * distance;

				if ((maxY + moveY) > 255) {
					moveY = 255 - maxY;
				}

				if ((minY + moveY) < 5) {
					moveY = 5 - minY;
				}
			}
		}

		if (betweenWorlds) {
			WarpDrive.debugPrint("" + this + " Worlds: " + worldObj.provider.getDimensionName() + " -> " + targetWorld.provider.getDimensionName());
		}
		
		// Validate positions aren't overlapping
		if (!betweenWorlds) {
			if ( Math.abs(moveX) <= (maxX - minX + 1) && Math.abs(moveY) <= (maxY - minY + 1) && Math.abs(moveZ) <= (maxZ - minZ + 1) ) {
				// render fake explosions
				doCollisionDamage(false);

				// cancel jump
				LocalProfiler.stop();
				String msg = "Not enough space for jump!";
				messageToAllPlayersOnShip(msg);
				killEntity(msg);
				return;
			}
		}

		if (!forceChunks(reason)) {
			String msg = reason.toString();
			killEntity(msg);
			messageToAllPlayersOnShip(msg);
			LocalProfiler.stop();
			return;
		}
//		lockWorlds();
		saveEntities();
		WarpDrive.debugPrint("" + this + " Saved " + entitiesOnShip.size() + " entities from ship");

		if (isHyperspaceJump && isInSpace) {
			messageToAllPlayersOnShip("Entering HYPERSPACE...");
		} else if (isHyperspaceJump && isInHyperSpace) {
			messageToAllPlayersOnShip("Leaving HYPERSPACE..");
		} else if (isCoordJump) {
			messageToAllPlayersOnShip("Jumping to coordinates (" + destX + "; " + yCoord + "; " + destZ + ")!");
		} else {
			messageToAllPlayersOnShip("Jumping " + getDirectionLabel(direction) + " by " + distance + " blocks");
		}

		// validate ship content
		int shipVolume = getRealShipVolume_checkBedrock(reason);
		if (shipVolume == -1) {
			String msg = reason.toString();
			killEntity(msg);
			messageToAllPlayersOnShip(msg);
			LocalProfiler.stop();
			return;
		}

		saveShip(shipVolume);
		this.currentIndexInShip = 0;
		msCounter = System.currentTimeMillis();
		LocalProfiler.stop();
		WarpDrive.debugPrint("Removing TE duplicates: tileEntities in target world before jump: " + targetWorld.loadedTileEntityList.size());
	}

	/**
	 * Finish jump: move entities, unlock worlds and delete self
	 */
	private void finishJump() {
		WarpDrive.debugPrint("" + this + " Jump done in " + ((System.currentTimeMillis() - msCounter) / 1000F) + " seconds and " + ticks + " ticks");
		//FIXME TileEntity duplication workaround
		WarpDrive.debugPrint("Removing TE duplicates: tileEntities in target world after jump, before cleanup: " + targetWorld.loadedTileEntityList.size());
		LocalProfiler.start("EntityJump.removeDuplicates()");

		try {
			targetWorld.loadedTileEntityList = this.removeDuplicates(targetWorld.loadedTileEntityList);
		} catch (Exception e) {
			WarpDrive.debugPrint("TE Duplicates removing exception: " + e.getMessage());
		}
		FixASTurbines();
		doCollisionDamage(true);

		LocalProfiler.stop();
		WarpDrive.debugPrint("Removing TE duplicates: tileEntities in target world after jump, after cleanup: " + targetWorld.loadedTileEntityList.size());
		killEntity("Jump done");
	}

	/**
	 * Removing ship from world
	 *
	 */
	private void removeShip() {
		LocalProfiler.start("EntityJump.removeShip");
		int blocksToMove = Math.min(BLOCKS_PER_TICK, ship.length - currentIndexInShip);
		WarpDrive.debugPrint("" + this + " Removing ship blocks " + currentIndexInShip + " to " + (currentIndexInShip + blocksToMove - 1) + " / " + (ship.length - 1));
		TileEntity te;
		Class<?> c;
		for (int index = 0; index < blocksToMove; index++) {
			if (currentIndexInShip >= ship.length) {
				break;
			}
			JumpBlock jb = ship[ship.length - currentIndexInShip - 1];
			if (jb == null) {
				WarpDrive.debugPrint("" + this + " Removing ship part: unexpected null found at ship[" + currentIndexInShip + "]");
				currentIndexInShip++;
				continue;
			}
			
			if (jb.blockTileEntity != null) {
				// WarpDrive.debugPrint("Removing tile entity at " + jb.x + ", " + jb.y + ", " + jb.z);
				worldObj.removeBlockTileEntity(jb.x, jb.y, jb.z);
			}
			worldObj.setBlock(jb.x, jb.y, jb.z, 0, 0, 2);
			
			te = targetWorld.getBlockTileEntity(jb.x + moveX, jb.y + moveY, jb.z + moveZ);
			if (te != null) {
				c = te.getClass();
				if (c.getName().equals("atomicscience.jiqi.TTurbine")) {
					try
					{
						if (c.getField("shiDa").getBoolean(te))
							ASTurbines.add(te);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				} else if (te instanceof TileEntityReactor) {
					WarpDrive.instance.warpCores.removeFromRegistry((TileEntityReactor)te);
				}
				
				c = c.getSuperclass();
				if (c.getName().equals("ic2.core.block.wiring.TileEntityElectricBlock") || c.getName().equals("ic2.core.block.TileEntityBlock") || c.getName().contains("ic2.core.block.generator")) {
					try
					{
						Method method;
						method = c.getDeclaredMethod("onUnloaded", (Class<?>[])null);
						method.invoke(te, (Object[])null);
						method = c.getDeclaredMethod("onLoaded", (Class<?>[])null);
						method.invoke(te, (Class<?>)null);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					te.updateContainingBlockInfo();
					try
					{
						NetworkHelper.updateTileEntityField(te, "facing");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

			currentIndexInShip++;
		}
		LocalProfiler.stop();
	}

	/**
	 * Saving ship to memory
	 *
	 * @param shipSize
	 */
	private void saveShip(int shipSize) {
		LocalProfiler.start("EntityJump.saveShip");
		ship = new JumpBlock[shipSize];

		if (ship == null) {
			killEntity("Unable to allocate memory (ship is null!)");
			LocalProfiler.stop();
			return;
		}

		int index = 0;
		int xc1 = minX >> 4;
		int xc2 = maxX >> 4;
		int zc1 = minZ >> 4;
		int zc2 = maxZ >> 4;

		for (int xc = xc1; xc <= xc2; xc++)
		{
			int x1 = Math.max(minX, xc << 4);
			int x2 = Math.min(maxX, (xc << 4) + 15);

			for (int zc = zc1; zc <= zc2; zc++)
			{
				int z1 = Math.max(minZ, zc << 4);
				int z2 = Math.min(maxZ, (zc << 4) + 15);

				for (int y = minY; y <= maxY; y++)
				{
					for (int x = x1; x <= x2; x++)
					{
						for (int z = z1; z <= z2; z++)
						{
							int blockID = worldObj.getBlockId(x, y, z);

							// Skip air blocks
							if (worldObj.isAirBlock(x, y, z) && (blockID != WarpDriveConfig.airID))
							{
								continue;
							}

							int blockMeta = worldObj.getBlockMetadata(x, y, z);
							TileEntity tileentity = worldObj.getBlockTileEntity(x, y, z);
							ship[index] = new JumpBlock(blockID, blockMeta, tileentity, x, y, z);
							if (ship[index] == null)
							{
								WarpDrive.debugPrint("" + this + " Unable to allocate memory (ship[" + index + "] is null!)");
							}
					
							index++;
						}
					}
				}
			}
		}

		WarpDrive.debugPrint("" + this + " Ship saved as " + ship.length + " blocks");
		LocalProfiler.stop();
	}

	/**
	 *Ship moving
	 */
	private void moveShip() {
		LocalProfiler.start("EntityJump.moveShip");
		int blocksToMove = Math.min(BLOCKS_PER_TICK, ship.length - currentIndexInShip);
		WarpDrive.debugPrint("" + this + " Moving ship blocks " + currentIndexInShip + " to " + (currentIndexInShip + blocksToMove - 1) + " / " + (ship.length - 1));

		for (int index = 0; index < blocksToMove; index++) {
			if (currentIndexInShip >= ship.length) {
				break;
			}

			moveBlockSimple(currentIndexInShip);
			currentIndexInShip++;
		}

		LocalProfiler.stop();
	}

	/**
	 * Checking jump possibility
	 *
	 * @return possible jump distance or -1
	 */
	private int getPossibleJumpDistance() {
		WarpDrive.debugPrint("" + this + " Calculating possible jump distance...");
		int testDistance = this.distance;
		int blowPoints = 0;
		collisionDetected = false;
		
		CheckMovementResult result = null;
		while (testDistance >= 0) {
			// Is there enough space in destination point?
			result = checkMovement(testDistance, false);

			if (result == null) {
				break;
			}
			
			if (result.isCollision) {
				blowPoints++;
			}
			testDistance--;
		}
		
		if (distance != testDistance) {
			WarpDrive.debugPrint("" + this + " Jump distance adjusted to " + testDistance + " after " + blowPoints + " collisions");
		}

		// Register explosion(s) at collision point
		if (blowPoints > WarpDriveConfig.WC_COLLISION_TOLERANCE_BLOCKS) {
			result = checkMovement(Math.max(1, testDistance + 1), true);
			if (result != null) {
				/*
				 * Strength scaling:
				 * Creeper = 3 or 6
				 * Wither skull = 1
				 * Wither boom = 5
				 * Endercrystal = 6
				 * TNTcart = 4 to 11.5
				 * TNT = 4
				 */
				float massCorrection = 0.5F + (float)Math.sqrt(
						Math.min(1.0D, Math.max(0.0D, reactor.shipVolume - WarpDriveConfig.WC_MAX_SHIP_VOLUME_ON_SURFACE) / WarpDriveConfig.WC_MIN_SHIP_VOLUME_FOR_HYPERSPACE));
				collisionDetected = true;
				collisionStrength = (4.0F + blowPoints - WarpDriveConfig.WC_COLLISION_TOLERANCE_BLOCKS) * massCorrection;
				collisionAtSource = result.atSource;
				collisionAtTarget = result.atTarget;
				WarpDrive.debugPrint("" + this + " Reporting " + collisionAtTarget.size() + " collisions coordinates "
						+ blowPoints + " blowPoints with massCorrection of " + String.format("%.2f", massCorrection) + " => strength " + String.format("%.2f", collisionStrength) );
			} else {
				System.out.println("WarpDrive error: unable to compute collision points, ignoring...");
			}
		}

		return testDistance;
	}
	
	private void doCollisionDamage(boolean atTarget) {
		if (!collisionDetected) {
			WarpDrive.debugPrint("" + this + " doCollisionDamage No collision detected...");
			return;
		}
		ArrayList<Vector3> collisionPoints = atTarget ? collisionAtTarget : collisionAtSource;
		Vector3 min = collisionPoints.get(0);
		Vector3 max = collisionPoints.get(0);
		for (Vector3 v : collisionPoints) {
			if (min.x > v.x) {
				min.x = v.x;
			} else if (max.x < v.x) {
				max.x = v.x;
			}
			if (min.y > v.y) {
				min.y = v.y;
			} else if (max.y < v.y) {
				max.y = v.y;
			}
			if (min.z > v.z) {
				min.z = v.z;
			} else if (max.z < v.z) {
				max.z = v.z;
			}
		}
		
		// inform players on board
		double rx = Math.round(min.x + worldObj.rand.nextInt( Math.max(1, (int) (max.x - min.x)) ));
		double ry = Math.round(min.y + worldObj.rand.nextInt( Math.max(1, (int) (max.y - min.y)) ));
		double rz = Math.round(min.z + worldObj.rand.nextInt( Math.max(1, (int) (max.z - min.z)) ));
//		WarpDrive.debugPrint("doCollisionDamage msg " + rx + ", " + ry + ", " + rz + " atTarget " + atTarget + " min " + min + " max " + max);
		messageToAllPlayersOnShip("Ship collision detected around " + (int)rx + ", " + (int)ry + ", " + (int)rz + ". Damage report pending...");
		
		// randomize if too many collision points
		int nbExplosions = Math.min(5, collisionPoints.size());
		WarpDrive.debugPrint("doCollisionDamage nbExplosions " + nbExplosions + "/" + collisionPoints.size());
		for (int i = 0; i < nbExplosions; i++) {
			// get location
			Vector3 current;
			if (nbExplosions < collisionPoints.size()) {
				WarpDrive.debugPrint("doCollisionDamage random #" + i);
				current = collisionPoints.get(worldObj.rand.nextInt(collisionPoints.size()));
			} else {
				WarpDrive.debugPrint("doCollisionDamage get " + i);
				current = collisionPoints.get(i);
			}
			
			// compute explosion strength with a jitter, at least 1 TNT
			float strength = Math.max(4.0F, collisionStrength / nbExplosions - 2.0F + 2.0F * worldObj.rand.nextFloat());
			
			(atTarget ? targetWorld : worldObj).newExplosion((Entity) null, current.x, current.y, current.z, strength, atTarget, atTarget);
			WarpDrive.debugPrint("doCollisionDamage explosion at " + current.x + ", " + current.y + ", " + current.z + " with strength " + strength);
		}
	}

	private int getRealShipVolume_checkBedrock(StringBuilder reason) {
		LocalProfiler.start("EntityJump.getRealShipVolume_checkBedrock");
		int shipVolume = 0;

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					int blockID = worldObj.getBlockId(x, y, z);

					// Skipping vanilla air & WarpDrive gas blocks, keep WarpDrive air block
					if (worldObj.isAirBlock(x, y, z) && (blockID != WarpDriveConfig.airID)) {// whitelist
						continue;
					}

					shipVolume++;
					
					/*
					Item item = Item.itemsList[blockID];
					if (item == null)
						WarpDrive.debugPrint("Block(" + x + ", " + y + ", " + z + ") is undefined#" + blockID + ":" + worldObj.getBlockMetadata(x, y, z));
					else
						WarpDrive.debugPrint("Block(" + x + ", " + y + ", " + z + ") is " + item.getUnlocalizedName() + ":" + worldObj.getBlockMetadata(x, y, z));
					 /**/
					
					if ((blockID == Block.bedrock.blockID) || (blockID == 2702)) {// Blacklist
						reason.append("Bedrock detected onboard at " + x + ", " + y + ", " + z + ". Aborting.");
						LocalProfiler.stop();
						return -1;
					}
				}
			}
		}

		// Lem: abort jump if blocks with TE are connecting to the ship (avoid crash when splitting multi-blocks)
		for (int x = minX - 1; x <= maxX + 1; x++) {
			boolean xBorder = (x == minX - 1) || (x == maxX + 1); 
			for (int z = minZ - 1; z <= maxZ + 1; z++) {
				boolean zBorder = (z == minZ - 1) || (z == maxZ + 1); 
				for (int y = minY - 1; y <= maxY + 1; y++) {
					boolean yBorder = (y == minY - 1) || (y == maxY + 1); 
					if ((y < 0) || (y > 255))
						continue;
					if (!(xBorder || yBorder || zBorder))
						continue;

					int blockID = worldObj.getBlockId(x, y, z);

					// Skipping air blocks
					if (worldObj.isAirBlock(x, y, z)) {
						continue;
					}

					// Skipping unmovable blocks
					if ((blockID == Block.bedrock.blockID) || (blockID == 2702)) {// Blacklist
						continue;
					}
					
					TileEntity te = worldObj.getBlockTileEntity(x, y, z);
					if (te == null) {
						 continue;
					}
					
					reason.append("Ship snagged at " + x + ", " + y + ", " + z + ". Damage report pending...");
					worldObj.createExplosion((Entity) null, x, y, z, Math.min(4F * 30, 4F * (shipVolume / 50)), false);
					LocalProfiler.stop();
					return -1;
				}
			}
		}

		LocalProfiler.stop();
		return shipVolume;
	}
	

	private void saveEntities() {
		entitiesOnShip = new ArrayList<MovingEntity>();
		
		AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX + 0.99D, maxY + 0.99D, maxZ + 0.99D);
		
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		
		for (Object o : list) {
			if (o == null || !(o instanceof Entity) || (o instanceof EntityJump)) {
				continue;
			}

			Entity entity = (Entity)o;
			MovingEntity movingEntity = new MovingEntity(entity);
			entitiesOnShip.add(movingEntity);
		}
	}
	

	private boolean moveEntities(boolean restorePositions) {
		WarpDrive.debugPrint("" + this + " Moving entities");
		LocalProfiler.start("EntityJump.moveEntities");

		if (entitiesOnShip != null) {
			for (MovingEntity me : entitiesOnShip) {
				Entity entity = me.entity;

				if (entity == null) {
					continue;
				}

				double oldEntityX = me.oldX;
				double oldEntityY = me.oldY;
				double oldEntityZ = me.oldZ;
				double newEntityX;
				double newEntityY;
				double newEntityZ;

				if (restorePositions) {
					newEntityX = oldEntityX;
					newEntityY = oldEntityY;
					newEntityZ = oldEntityZ;
				} else {
					newEntityX = oldEntityX + moveX;
					newEntityY = oldEntityY + moveY;
					newEntityZ = oldEntityZ + moveZ;
				}

				//WarpDrive.debugPrint("Entity moving: old (" + oldEntityX + " " + oldEntityY + " " + oldEntityZ + ") -> new (" + newEntityX + " " + newEntityY + " " + newEntityZ);

				// Travel to another dimension if needed
				if (betweenWorlds && !restorePositions) {
					MinecraftServer server = MinecraftServer.getServer();
					WorldServer from = server.worldServerForDimension(worldObj.provider.dimensionId);
					WorldServer to = server.worldServerForDimension(targetWorld.provider.dimensionId);
					SpaceTeleporter teleporter = new SpaceTeleporter(to, 0, MathHelper.floor_double(newEntityX), MathHelper.floor_double(newEntityY), MathHelper.floor_double(newEntityZ));

					if (entity instanceof EntityPlayerMP) {
						EntityPlayerMP player = (EntityPlayerMP) entity;
						server.getConfigurationManager().transferPlayerToDimension(player, targetWorld.provider.dimensionId, teleporter);
					} else {
						server.getConfigurationManager().transferEntityToWorld(entity, worldObj.provider.dimensionId, from, to, teleporter);
					}
				}

				// Update position
				if (entity instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) entity;

					ChunkCoordinates bedLocation = player.getBedLocation(player.worldObj.provider.dimensionId);

					if (bedLocation != null
						&& minX <= bedLocation.posX && maxX >= bedLocation.posX
						&& minY <= bedLocation.posY && maxY >= bedLocation.posY
						&& minZ <= bedLocation.posZ && maxZ >= bedLocation.posZ) {
						bedLocation.posX = bedLocation.posX + moveX;
						bedLocation.posY = bedLocation.posY + moveY;
						bedLocation.posZ = bedLocation.posZ + moveZ;
						player.setSpawnChunk(bedLocation, false);
					}

					player.setPositionAndUpdate(newEntityX, newEntityY, newEntityZ);
				} else {
					entity.setPosition(newEntityX, newEntityY, newEntityZ);
				}
			}
		}

		LocalProfiler.stop();
		return true;
	}
	


	public int[] getVector(int i)
	{
		int v[] =
		{
			0, 0, 0
		};

		switch (i)
		{
			case -1:
				v[1] = 1;
				break;

			case -2:
				v[1] = -1;
				break;

			case 0:
				v[0] = dx;
				v[2] = dz;
				break;

			case 180:
				v[0] = -dx;
				v[2] = -dz;
				break;

			case 90:
				v[0] = dz;
				v[2] = -dx;
				break;

			case 270:
				v[0] = -dz;
				v[2] = dx;
		}

		return v;
	}

	class CheckMovementResult {
		public ArrayList<Vector3> atSource;
		public ArrayList<Vector3> atTarget;
		public boolean isCollision = false;
		public String reason = "";
		
		CheckMovementResult() {
			this.atSource = new ArrayList<Vector3>(1);
			this.atTarget = new ArrayList<Vector3>(1);
			this.isCollision = false;
			this.reason = "Unknown reason";
		}
		
		public void add(double sx, double sy, double sz, double tx, double ty, double tz, boolean pisCollision, String preason) {
			atSource.add(new Vector3(sx, sy, sz));
			atTarget.add(new Vector3(tx, ty, tz));
			isCollision = isCollision || pisCollision;
			reason = preason;
			WarpDrive.debugPrint("CheckMovementResult " + sx + ", " + sy + ", " + sz + " -> " + tx + ", " + ty + ", " + tz + " " + isCollision + " '" + reason + "'");
		}
	};
	private CheckMovementResult checkMovement(int testDistance, boolean fullCollisionDetails) {
		CheckMovementResult result = new CheckMovementResult();
		if ((direction == -1 && maxY + testDistance > 255) && !betweenWorlds) {
			result.add(xCoord, maxY + testDistance, zCoord, xCoord + 0.5D, maxY + testDistance + 1.0D, zCoord + 0.5D,
					false, "Reactor will blow due +high limit");
			return result;
		}

		if ((direction == -2 && minY - testDistance <= 8) && !betweenWorlds) {
			result.add(xCoord, minY - testDistance, zCoord, xCoord + 0.5D, maxY - testDistance, zCoord + 0.5D,
					false, "Reactor will blow due -low limit");
			return result;
		}

		int movementVector[] = getVector(direction);
		int lmoveX = movementVector[0] * testDistance;
		int lmoveY = movementVector[1] * testDistance;
		int lmoveZ = movementVector[2] * testDistance;
		
		int x, y, z, newX, newY, newZ, blockOnShipID, blockID;
		for (y = minY; y <= maxY; y++) {
			newY = y + lmoveY;
			for (x = minX; x <= maxX; x++) {
				newX = x + lmoveX;
				for (z = minZ; z <= maxZ; z++) {
					newZ = z + lmoveZ;

					blockID = worldObj.getBlockId(newX, newY, newZ);
					if ((blockID == Block.bedrock.blockID) || (blockID == 2702)) {// Blacklist
						result.add(x, y, z,
								newX + 0.5D - movementVector[0] * 1.0D,
								newY + 0.5D - movementVector[1] * 1.0D,
								newZ + 0.5D - movementVector[2] * 1.0D,
								true, "Unpassable block " + blockID + " detected at destination (" + newX + ";" + newY + ";" + newZ + ")");
						if (!fullCollisionDetails) {
							return result;
						}
					}

					blockOnShipID = worldObj.getBlockId(x, y, z);
					if (blockOnShipID != 0 && blockID != 0 && blockID != WarpDriveConfig.airID && blockID != WarpDriveConfig.gasID && blockID != 18) {
						result.add(x, y, z,
								newX + 0.5D + movementVector[0] * 0.1D,
								newY + 0.5D + movementVector[1] * 0.1D,
								newZ + 0.5D + movementVector[2] * 0.1D,
								true, "Obstacle block #" + blockID + " detected at (" + newX + ", " + newY + ", " + newZ + ")");
						if (!fullCollisionDetails) {
							return result;
						}
					}
				}
			}
		}

		if (fullCollisionDetails && result.isCollision) {
			return result;
		} else {
			return null;
		}
	}
/*
	private void turnOffModem(IPeripheral p)
	{
		// FIXME
		if (p.getType() == "modem") {
			String[] methods = p.getMethodNames();
			for(int i = 0; i < methods.length; i++) {
				if (methods[i] == "closeAll") {
					try {
						p.callMethod(null, i, null); // FIXME
					} catch (Exception e) {
						// ignore iy
					}
					return;
				}
			}
		}
	}

	private void turnOffModems()
	{
		// FIXME
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					int blockID = worldObj.getBlockId(x, y, z);
					if (blockID == 0 || blockID == WarpDriveConfig.airID || blockID == WarpDriveConfig.gasID) {
						continue;
					}

					TileEntity tileEntity = worldObj.getBlockTileEntity(x, y, z);
					if (tileEntity == null) continue;

					if (tileEntity instanceof IPeripheral) {
						IPeripheral p = (IPeripheral)tileEntity;
						turnOffModem(p);
					}
					if (tileEntity instanceof ITurtleAccess) {
						ITurtleAccess a = (ITurtleAccess)tileEntity;
						IPeripheral pl = a.getPeripheral(TurtleSide.Left);
						if (pl != null) turnOffModem(pl);
						IPeripheral pr = a.getPeripheral(TurtleSide.Right);
						if (pr != null) turnOffModem(pr);
					}
				}
			}
		}
	}/**/

	private boolean moveBlockSimple(int indexInShip)
	{
		try
		{
			JumpBlock shipBlock = ship[indexInShip];

			if (shipBlock == null) {
				return false;
			}

			int oldX = shipBlock.x;
			int oldY = shipBlock.y;
			int oldZ = shipBlock.z;
			int newX = oldX + moveX;
			int newY = oldY + moveY;
			int newZ = oldZ + moveZ;
			int blockID = shipBlock.blockID;
			int blockMeta = shipBlock.blockMeta;
			mySetBlock(targetWorld, newX, newY, newZ, blockID, blockMeta, 2);

			// Re-schedule air blocks update
			if (blockID == WarpDriveConfig.airID) {
				targetWorld.markBlockForUpdate(newX, newY, newZ);
				targetWorld.scheduleBlockUpdate(newX, newY, newZ, blockID, 40 + targetWorld.rand.nextInt(20));
			}

			NBTTagCompound oldnbt = new NBTTagCompound();
			boolean unlockToValidate = false;
			// 145 Anvil, 146 Trapped chest, 149 inactive redstone comparator, 156 Quartz stair, 159 Stained clay
			if (shipBlock.blockTileEntity != null && blockID != 159 && blockID != 149 && blockID != 156 && blockID != 146 && blockID != 145)
			{
				shipBlock.blockTileEntity.writeToNBT(oldnbt);
				oldnbt.setInteger("x", newX);
				oldnbt.setInteger("y", newY);
				oldnbt.setInteger("z", newZ);
				
				if (oldnbt.hasKey("mainX") && oldnbt.hasKey("mainY") && oldnbt.hasKey("mainZ"))	// Mekanism 6.0.4.44
				{
					WarpDrive.debugPrint("[JUMP] moveBlockSimple: TileEntity from Mekanism detected");
					oldnbt.setInteger("mainX", oldnbt.getInteger("mainX") + moveX);
					oldnbt.setInteger("mainY", oldnbt.getInteger("mainY") + moveY);
					oldnbt.setInteger("mainZ", oldnbt.getInteger("mainZ") + moveZ);
					unlockToValidate = true;
				} else if (oldnbt.hasKey("id") && oldnbt.getString("id") == "savedMultipart")
				{
					WarpDrive.debugPrint("[JUMP] moveBlockSimple: TileEntity from Forge multipart detected at " + oldX + ", " + oldY + ", " + oldZ);
					unlockToValidate = true;
				} else {
//					WarpDrive.debugPrint("[JUMP] moveBlockSimple: TileEntity from other detected");
				}
				TileEntity newTileEntity = null;
				if (blockID == WarpDriveConfig.CC_Computer || blockID == WarpDriveConfig.CC_peripheral || blockID == WarpDriveConfig.CCT_Turtle || blockID == WarpDriveConfig.CCT_Upgraded || blockID == WarpDriveConfig.CCT_Advanced)
				{
					newTileEntity = TileEntity.createAndLoadEntity(oldnbt);
					newTileEntity.invalidate();
				}
				else if (blockID == WarpDriveConfig.AS_Turbine)
				{
					if (oldnbt.hasKey("zhuYao"))
					{
						NBTTagCompound nbt1 = oldnbt.getCompoundTag("zhuYao");
						nbt1.setDouble("x", newX);
						nbt1.setDouble("y", newY);
						nbt1.setDouble("z", newZ);
						oldnbt.setTag("zhuYao", nbt1);
					}
					newTileEntity = TileEntity.createAndLoadEntity(oldnbt);
				}
				
				if (newTileEntity == null)
					newTileEntity = TileEntity.createAndLoadEntity(oldnbt);
				
				newTileEntity.worldObj = targetWorld;
				if (unlockToValidate)
				{
//					targetWorld.isRemote = false;
					newTileEntity.validate();
//					targetWorld.isRemote = true;
				}
				else
				{
					newTileEntity.validate();
				}
				
				worldObj.removeBlockTileEntity(oldX, oldY, oldZ);
				targetWorld.setBlockTileEntity(newX, newY, newZ, newTileEntity);
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			WarpDrive.debugPrint("[JUMP] moveBlockSimple exception Idx " + indexInShip);
			return false;
		}

		return true;
	}

	private static ArrayList<Object> removeDuplicates(List<TileEntity> l)
	{
		Set<TileEntity> s = new TreeSet<TileEntity>(new Comparator<TileEntity>()
		{
			@Override
			public int compare(TileEntity o1, TileEntity o2)
			{
				if (o1.xCoord == o2.xCoord && o1.yCoord == o2.yCoord && o1.zCoord == o2.zCoord)
				{
					WarpDrive.debugPrint("Removed duplicated TE: " + o1 + ", " + o2);
					return 0;
				}
				else
				{
					return 1;
				}
			}
		});
		s.addAll(l);
		return new ArrayList<Object>(Arrays.asList(s.toArray()));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		//WarpDrive.debugPrint("" + this + " readEntityFromNBT()");
	}

	@Override
	protected void entityInit()
	{
		//WarpDrive.debugPrint("" + this + " entityInit()");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound var1)
	{
		//WarpDrive.debugPrint("" + this + " writeEntityToNBT()");
	}

	// Own implementation of setting blocks without light recalculation in optimization purposes
	private boolean mySetBlock(World w, int x, int y, int z, int blockId, int blockMeta, int par6)
	{
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000)
		{
			if (y < 0)
			{
				return false;
			}
			else if (y >= 256)
			{
				return false;
			}
			else
			{
				w.markBlockForUpdate(x, y, z);
				Chunk chunk = w.getChunkFromChunkCoords(x >> 4, z >> 4);
				return myChunkSBIDWMT(chunk, x & 15, y, z & 15, blockId, blockMeta);
			}
		}
		else
		{
			return false;
		}
	}

	private boolean myChunkSBIDWMT(Chunk c, int x, int y, int z, int blockId, int blockMeta)
	{
		int j1 = z << 4 | x;

		if (y >= c.precipitationHeightMap[j1] - 1)
		{
			c.precipitationHeightMap[j1] = -999;
		}

		//int k1 = c.heightMap[j1];
		int l1 = c.getBlockID(x, y, z);
		int i2 = c.getBlockMetadata(x, y, z);

		if (l1 == blockId && i2 == blockMeta)
		{
			return false;
		}
		else
		{
			ExtendedBlockStorage[] storageArrays = c.getBlockStorageArray();
			ExtendedBlockStorage extendedblockstorage = storageArrays[y >> 4];

			if (extendedblockstorage == null)
			{
				if (blockId == 0)
				{
					return false;
				}

				extendedblockstorage = storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !c.worldObj.provider.hasNoSky);
			}

			int j2 = c.xPosition * 16 + x;
			int k2 = c.zPosition * 16 + z;
			extendedblockstorage.setExtBlockID(x, y & 15, z, blockId);

			if (l1 != 0)
			{
				if (!c.worldObj.isRemote)
				{
					Block.blocksList[l1].breakBlock(c.worldObj, j2, y, k2, l1, i2);
				}
				else if (Block.blocksList[l1] != null && Block.blocksList[l1].hasTileEntity(i2))
				{
					TileEntity te = worldObj.getBlockTileEntity(j2, y, k2);

					if (te != null && te.shouldRefresh(l1, blockId, i2, blockMeta, worldObj, j2, y, k2))
					{
						c.worldObj.removeBlockTileEntity(j2, y, k2);
					}
				}
			}

			if (extendedblockstorage.getExtBlockID(x, y & 15, z) != blockId)
			{
				return false;
			}
			else
			{
				extendedblockstorage.setExtBlockMetadata(x, y & 15, z, blockMeta);
				// Removed light recalculations
				/*if (flag)
				{
					c.generateSkylightMap();
				}
				else
				{
					if (c.getBlockLightOpacity(par1, par2, par3) > 0)
					{
						if (par2 >= k1)
						{
							c.relightBlock(par1, par2 + 1, par3);
						}
					}
					else if (par2 == k1 - 1)
					{
						c.relightBlock(par1, par2, par3);
					}

					c.propagateSkylightOcclusion(par1, par3);
				}*/
				TileEntity tileentity;

				if (blockId != 0)
				{
					if (Block.blocksList[blockId] != null && Block.blocksList[blockId].hasTileEntity(blockMeta))
					{
						tileentity = c.getChunkBlockTileEntity(x, y, z);

						if (tileentity == null)
						{
							tileentity = Block.blocksList[blockId].createTileEntity(c.worldObj, blockMeta);
							c.worldObj.setBlockTileEntity(j2, y, k2, tileentity);
						}

						if (tileentity != null)
						{
							tileentity.updateContainingBlockInfo();
							tileentity.blockMetadata = blockMeta;
						}
					}
				}

				c.isModified = true;
				return true;
			}
		}
	}

	private void FixASTurbines()
	{
		Class<?> c;
		for (TileEntity t : ASTurbines)
			try
			{
				c = t.getClass();
				Method method = c.getDeclaredMethod("bianDa", (Class<?>[])null);
				method.invoke(t, (Object[])null);
				method.invoke(t, (Object[])null);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	}
	
	public void setMinMaxes(int minXV,int maxXV,int minYV,int maxYV,int minZV,int maxZV)
	{
		minX = minXV;
		maxX = maxXV;
		minY = minYV;
		maxY = maxYV;
		minZ = minZV;
		maxZ = maxZV;
	}

	@Override
	public String toString() {
        return String.format("%s/%d \'%s\' @ \'%s\' %.2f, %.2f, %.2f", new Object[] {
       		getClass().getSimpleName(),
       		Integer.valueOf(entityId),
       		reactor == null ? "~NULL~" : reactor.coreFrequency,
       		worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
       		Double.valueOf(posX), Double.valueOf(posY), Double.valueOf(posZ)});
	}
}