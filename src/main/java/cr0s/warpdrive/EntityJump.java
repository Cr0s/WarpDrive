package cr0s.warpdrive;

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
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.conf.WarpDriveConfig;
import cr0s.warpdrive.data.JumpBlock;
import cr0s.warpdrive.data.MovingEntity;
import cr0s.warpdrive.data.Planet;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.world.SpaceTeleporter;

public class EntityJump extends Entity {
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
	private TileEntityShipCore shipCore;
	
	private final static int STATE_IDLE = 0;
	private final static int STATE_JUMPING = 1;
	private final static int STATE_REMOVING = 2;
	private int state = STATE_IDLE;
	private int currentIndexInShip = 0;
	
	private List<MovingEntity> entitiesOnShip;
	
	private boolean betweenWorlds;
	
	private int destX, destY, destZ;
	private boolean isCoordJump;
	
	private long msCounter = 0;
	private int ticks = 0;
	
	public EntityJump(World world) {
		super(world);
		targetWorld = worldObj;
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Entity created (empty) in dimension " + worldObj.getProviderName() + " - " + worldObj.getWorldInfo().getWorldName());
		}
	}
	
	public EntityJump(World world, int x, int y, int z, int _dx, int _dz, TileEntityShipCore _reactor, boolean _isHyperspaceJump, int _distance, int _direction,
			boolean _isCoordJump, int _destX, int _destY, int _destZ) {
		super(world);
		this.posX = x + 0.5D;
		this.posY = y + 0.5D;
		this.posZ = z + 0.5D;
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
		this.dx = _dx;
		this.dz = _dz;
		this.shipCore = _reactor;
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
		
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Entity created");
		}
	}
	
	public void killEntity(String reason) {
		if (!on) {
			return;
		}
		
		on = false;
		
		if (WarpDriveConfig.LOGGING_JUMP) {
			if (reason == null || reason.isEmpty()) {
				WarpDrive.logger.info(this + " Killing jump entity...");
			} else {
				WarpDrive.logger.info(this + " Killing jump entity... (" + reason + ")");
			}
		}
		
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
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " Removing from onUpdate...");
			}
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
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " Preparing to jump...");
			}
			prepareToJump();
			if (on) {
				state = STATE_JUMPING;
			}
		} else if (state == STATE_JUMPING) {
			if (currentIndexInShip < ship.length - 1) {
				// moveEntities(true);
				moveShip();
			} else {
				moveEntities(false);
				currentIndexInShip = 0;
				state = STATE_REMOVING;
			}
		} else if (state == STATE_REMOVING) {
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
	
	private boolean forceChunks(StringBuilder reason) {
		LocalProfiler.start("EntityJump.forceChunks");
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Forcing chunks in " + worldObj.provider.getDimensionName() + " and " + targetWorld.provider.getDimensionName());
		}
		sourceWorldTicket = ForgeChunkManager.requestTicket(WarpDrive.instance, worldObj, Type.NORMAL); // Type.ENTITY);
		if (sourceWorldTicket == null) {
			reason.append("Chunkloading rejected in S:" + worldObj.getWorldInfo().getWorldName() + ". Aborting.");
			return false;
		}
		targetWorldTicket = ForgeChunkManager.requestTicket(WarpDrive.instance, targetWorld, Type.NORMAL);
		if (targetWorldTicket == null) {
			reason.append("Chunkloading rejected in T:" + worldObj.getWorldInfo().getWorldName() + ". Aborting.");
			return false;
		}
		// sourceWorldTicket.bindEntity(this);
		int x1 = minX >> 4;
		int x2 = maxX >> 4;
		int z1 = minZ >> 4;
		int z2 = maxZ >> 4;
		int chunkCount = 0;
		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
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
		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
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
	
	private void unforceChunks() {
		LocalProfiler.start("EntityJump.unforceChunks");
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Unforcing chunks");
		}
		
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
			shipCore.messageToAllPlayersOnShip(msg);
		} else {
			WarpDrive.logger.info(this + " messageToAllPlayersOnShip: " + msg);
			for (MovingEntity me : entitiesOnShip) {
				if (me.entity instanceof EntityPlayer) {
					WarpDrive.addChatMessage((EntityPlayer) me.entity, "["
							+ ((shipCore != null && shipCore.shipName.length() > 0) ? shipCore.shipName : "WarpCore") + "] " + msg);
				}
			}
		}
	}
	
	public static String getDirectionLabel(int direction) {
		switch (direction) {
		case -1:
			return "UP";
		case -2:
			return "DOWN";
		case 0:
			return "FRONT";
		case 180:
			return "BACK";
		case 90:
			return "LEFT";
		case 255:
			return "RIGHT";
		default:
			return direction + " degrees";
		}
	}
	
	private void prepareToJump() {
		StringBuilder reason = new StringBuilder();
		
		LocalProfiler.start("EntityJump.prepareToJump");
		boolean isInSpace = (worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID);
		boolean isInHyperSpace = (worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID);
		
		boolean toSpace = (direction == -1) && (maxY + distance > 255) && (!isInSpace) && (!isInHyperSpace);
		boolean fromSpace = (direction == -2) && (minY - distance < 0) && isInSpace;
		betweenWorlds = fromSpace || toSpace || isHyperspaceJump;
		moveX = moveY = moveZ = 0;
		
		if (toSpace) {
			Boolean planeFound = false;
			Boolean planeValid = false;
			int closestPlaneDistance = Integer.MAX_VALUE;
			Planet closestTransitionPlane = null;
			for (int iPlane = 0; (!planeValid) && iPlane < WarpDriveConfig.PLANETS.length; iPlane++) {
				Planet transitionPlane = WarpDriveConfig.PLANETS[iPlane];
				if (worldObj.provider.dimensionId == transitionPlane.dimensionId) {
					planeFound = true;
					int planeDistance = transitionPlane.isValidToSpace(new Vector3(this));
					if (planeDistance == 0) {
						planeValid = true;
						moveX = transitionPlane.spaceCenterX - transitionPlane.dimensionCenterX;
						moveZ = transitionPlane.spaceCenterZ - transitionPlane.dimensionCenterZ;
						targetWorld = DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID);
					} else if (closestPlaneDistance > planeDistance) {
						closestPlaneDistance = planeDistance;
						closestTransitionPlane = transitionPlane;
					}
				}
			}
			if (!planeFound) {
				LocalProfiler.stop();
				String msg = "Unable to reach space!\nThere's no valid transition plane for current dimension " + worldObj.provider.getDimensionName() + " ("
						+ worldObj.provider.dimensionId + ")";
				messageToAllPlayersOnShip(msg);
				killEntity(msg);
				return;
			}
			if (!planeValid) {
				LocalProfiler.stop();
				assert(closestTransitionPlane != null);
				@SuppressWarnings("null") // Eclipse derp, don't remove
				String msg = "Ship is outside border, unable to reach space!\nClosest transition plane is ~" + closestPlaneDistance + " m away ("
						+ (closestTransitionPlane.dimensionCenterX - closestTransitionPlane.borderSizeX) + ", 250,"
						+ (closestTransitionPlane.dimensionCenterZ - closestTransitionPlane.borderSizeZ) + ") to ("
						+ (closestTransitionPlane.dimensionCenterX + closestTransitionPlane.borderSizeX) + ", 255,"
						+ (closestTransitionPlane.dimensionCenterZ + closestTransitionPlane.borderSizeZ) + ")";
				messageToAllPlayersOnShip(msg);
				killEntity(msg);
				return;
			}
		} else if (fromSpace) {
			Boolean planeFound = false;
			int closestPlaneDistance = Integer.MAX_VALUE;
			Planet closestTransitionPlane = null;
			for (int iPlane = 0; (!planeFound) && iPlane < WarpDriveConfig.PLANETS.length; iPlane++) {
				Planet transitionPlane = WarpDriveConfig.PLANETS[iPlane];
				int planeDistance = transitionPlane.isValidFromSpace(new Vector3(this));
				if (planeDistance == 0) {
					planeFound = true;
					moveX = transitionPlane.dimensionCenterX - transitionPlane.spaceCenterX;
					moveZ = transitionPlane.dimensionCenterZ - transitionPlane.spaceCenterZ;
					targetWorld = DimensionManager.getWorld(transitionPlane.dimensionId);
				} else if (closestPlaneDistance > planeDistance) {
					closestPlaneDistance = planeDistance;
					closestTransitionPlane = transitionPlane;
				}
			}
			if (!planeFound) {
				LocalProfiler.stop();
				String msg = "";
				if (closestTransitionPlane == null) {
					msg = "No transition plane defined, unable to enter atmosphere!";
				} else {
					msg = "No planet in range, unable to enter atmosphere!\nClosest transition plane is " + closestPlaneDistance + " m away ("
							+ (closestTransitionPlane.dimensionCenterX - closestTransitionPlane.borderSizeX) + ", 250,"
							+ (closestTransitionPlane.dimensionCenterZ - closestTransitionPlane.borderSizeZ) + ") to ("
							+ (closestTransitionPlane.dimensionCenterX + closestTransitionPlane.borderSizeX) + ", 255,"
							+ (closestTransitionPlane.dimensionCenterZ + closestTransitionPlane.borderSizeZ) + ")";
				}
				messageToAllPlayersOnShip(msg);
				killEntity(msg);
				return;
			}
		} else if (isHyperspaceJump && isInHyperSpace) {
			targetWorld = DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID);
		} else if (isHyperspaceJump && isInSpace) {
			targetWorld = DimensionManager.getWorld(WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID);
		} else {
			targetWorld = worldObj;
		}
		
		// Calculate jump vector
		if (isCoordJump) {
			moveX = destX - xCoord;
			moveZ = destZ - zCoord;
			moveY = destY - yCoord;
			distance = 0; // FIXME: check collision in straight path, starting with getPossibleJumpDistance() ?
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
		
		if (betweenWorlds && WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Worlds: " + worldObj.provider.getDimensionName() + " -> " + targetWorld.provider.getDimensionName());
		}
		
		// Validate positions aren't overlapping
		if (!betweenWorlds) {
			if (Math.abs(moveX) <= (maxX - minX + 1) && Math.abs(moveY) <= (maxY - minY + 1) && Math.abs(moveZ) <= (maxZ - minZ + 1)) {
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
		// lockWorlds();
		saveEntities();
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Saved " + entitiesOnShip.size() + " entities from ship");
		}
		
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
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info("Removing TE duplicates: tileEntities in target world before jump: " + targetWorld.loadedTileEntityList.size());
		}
	}
	
	/**
	 * Finish jump: move entities, unlock worlds and delete self
	 */
	private void finishJump() {
		// FIXME TileEntity duplication workaround
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Jump done in " + ((System.currentTimeMillis() - msCounter) / 1000F) + " seconds and " + ticks + " ticks");
			WarpDrive.logger.info("Removing TE duplicates: tileEntities in target world after jump, before cleanup: " + targetWorld.loadedTileEntityList.size());
		}
		LocalProfiler.start("EntityJump.removeDuplicates()");
		
		try {
			targetWorld.loadedTileEntityList = this.removeDuplicates(targetWorld.loadedTileEntityList);
		} catch (Exception exception) {
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info("TE Duplicates removing exception: " + exception.getMessage());
			}
		}
		
		doCollisionDamage(true);
		
		LocalProfiler.stop();
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info("Removing TE duplicates: tileEntities in target world after jump, after cleanup: " + targetWorld.loadedTileEntityList.size());
		}
		killEntity("Jump done");
	}
	
	/**
	 * Removing ship from world
	 *
	 */
	private void removeShip() {
		LocalProfiler.start("EntityJump.removeShip");
		int blocksToMove = Math.min(WarpDriveConfig.G_BLOCKS_PER_TICK, ship.length - currentIndexInShip);
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Removing ship blocks " + currentIndexInShip + " to " + (currentIndexInShip + blocksToMove - 1) + " / " + (ship.length - 1));
		}
		for (int index = 0; index < blocksToMove; index++) {
			if (currentIndexInShip >= ship.length) {
				break;
			}
			JumpBlock jb = ship[ship.length - currentIndexInShip - 1];
			if (jb == null) {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info(this + " Removing ship part: unexpected null found at ship[" + currentIndexInShip + "]");
				}
				currentIndexInShip++;
				continue;
			}
			
			if (jb.blockTileEntity != null) {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info("Removing tile entity at " + jb.x + ", " + jb.y + ", " + jb.z);
				}
				worldObj.removeTileEntity(jb.x, jb.y, jb.z);
			}
			worldObj.setBlock(jb.x, jb.y, jb.z, Blocks.air, 0, 2);
			
			JumpBlock.refreshBlockStateOnClient(targetWorld, jb.x + moveX, jb.y + moveY, jb.z + moveZ);
			
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
		try {
			ship = new JumpBlock[shipSize];
			JumpBlock placeAfter[] = new JumpBlock[shipSize]; // blocks and tile entities to be placed at the end, and removed first
			
			int indexPlaceNormal = 0;
			int indexPlaceAfter = 0;
			int xc1 = minX >> 4;
			int xc2 = maxX >> 4;
			int zc1 = minZ >> 4;
			int zc2 = maxZ >> 4;
			
			for (int xc = xc1; xc <= xc2; xc++) {
				int x1 = Math.max(minX, xc << 4);
				int x2 = Math.min(maxX, (xc << 4) + 15);
				
				for (int zc = zc1; zc <= zc2; zc++) {
					int z1 = Math.max(minZ, zc << 4);
					int z2 = Math.min(maxZ, (zc << 4) + 15);
					
					for (int y = minY; y <= maxY; y++) {
						for (int x = x1; x <= x2; x++) {
							for (int z = z1; z <= z2; z++) {
								Block block = worldObj.getBlock(x, y, z);
								
								// Skip air blocks
								if (worldObj.isAirBlock(x, y, z) && (!block.isAssociatedBlock(WarpDrive.blockAir))) {
									continue;
								}
								
								int blockMeta = worldObj.getBlockMetadata(x, y, z);
								TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
								JumpBlock jumpBlock = new JumpBlock(block, blockMeta, tileEntity, x, y, z);
								
								if (tileEntity == null || false /* TODO: implement latePlacementBlockList configuration, including IC2 reactor chambers */ ) {
									ship[indexPlaceNormal] = jumpBlock;
									indexPlaceNormal++;
								} else {
									placeAfter[indexPlaceAfter] = jumpBlock;
									indexPlaceAfter++;
								}
							}
						}
					}
				}
			}
			
			for (int index = 0; index < indexPlaceAfter; index++) {
				ship[indexPlaceNormal] = placeAfter[index];
				indexPlaceNormal++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			killEntity("Exception during jump preparation (saveShip)!");
			LocalProfiler.stop();
			return;
		}
		
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Ship saved as " + ship.length + " blocks");
		}
		LocalProfiler.stop();
	}
	
	/**
	 * Ship moving
	 */
	private void moveShip() {
		LocalProfiler.start("EntityJump.moveShip");
		int blocksToMove = Math.min(WarpDriveConfig.G_BLOCKS_PER_TICK, ship.length - currentIndexInShip);
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Moving ship blocks " + currentIndexInShip + " to " + (currentIndexInShip + blocksToMove - 1) + " / " + (ship.length - 1));
		}
		
		for (int index = 0; index < blocksToMove; index++) {
			if (currentIndexInShip >= ship.length) {
				break;
			}
			
			JumpBlock jb = ship[currentIndexInShip];
			if (jb != null) {
				jb.deploy(targetWorld, moveX, moveY, moveZ);
				// 1.6.4 required to keep tile entities for CC_peripheral:2 or :4
				worldObj.removeTileEntity(jb.x, jb.y, jb.z);
			}
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
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Calculating possible jump distance...");
		}
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
		
		if (distance != testDistance && WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Jump distance adjusted to " + testDistance + " after " + blowPoints + " collisions");
		}
		
		// Register explosion(s) at collision point
		if (blowPoints > WarpDriveConfig.SHIP_COLLISION_TOLERANCE_BLOCKS) {
			result = checkMovement(Math.max(1, testDistance + 1), true);
			if (result != null) {
				/*
				 * Strength scaling: Creeper = 3 or 6 Wither skull = 1 Wither
				 * boom = 5 Endercrystal = 6 TNTcart = 4 to 11.5 TNT = 4
				 */
				float massCorrection = 0.5F + (float) Math
						.sqrt(Math.min(1.0D, Math.max(0.0D, shipCore.shipVolume - WarpDriveConfig.SHIP_VOLUME_MAX_ON_PLANET_SURFACE)
								/ WarpDriveConfig.SHIP_VOLUME_MIN_FOR_HYPERSPACE));
				collisionDetected = true;
				collisionStrength = (4.0F + blowPoints - WarpDriveConfig.SHIP_COLLISION_TOLERANCE_BLOCKS) * massCorrection;
				collisionAtSource = result.atSource;
				collisionAtTarget = result.atTarget;
				WarpDrive.logger.info(this + " Reporting " + collisionAtTarget.size() + " collisions coordinates " + blowPoints
							+ " blowPoints with massCorrection of " + String.format("%.2f", massCorrection) + " => strength "
							+ String.format("%.2f", collisionStrength));
			} else {
				WarpDrive.logger.error("WarpDrive error: unable to compute collision points, ignoring...");
			}
		}
		
		return testDistance;
	}
	
	private void doCollisionDamage(boolean atTarget) {
		if (!collisionDetected) {
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " doCollisionDamage No collision detected...");
			}
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
		double rx = Math.round(min.x + worldObj.rand.nextInt(Math.max(1, (int) (max.x - min.x))));
		double ry = Math.round(min.y + worldObj.rand.nextInt(Math.max(1, (int) (max.y - min.y))));
		double rz = Math.round(min.z + worldObj.rand.nextInt(Math.max(1, (int) (max.z - min.z))));
		messageToAllPlayersOnShip("Ship collision detected around " + (int) rx + ", " + (int) ry + ", " + (int) rz + ". Damage report pending...");
		
		// randomize if too many collision points
		int nbExplosions = Math.min(5, collisionPoints.size());
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info("doCollisionDamage nbExplosions " + nbExplosions + "/" + collisionPoints.size());
		}
		for (int i = 0; i < nbExplosions; i++) {
			// get location
			Vector3 current;
			if (nbExplosions < collisionPoints.size()) {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info("doCollisionDamage random #" + i);
				}
				current = collisionPoints.get(worldObj.rand.nextInt(collisionPoints.size()));
			} else {
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info("doCollisionDamage get " + i);
				}
				current = collisionPoints.get(i);
			}
			
			// compute explosion strength with a jitter, at least 1 TNT
			float strength = Math.max(4.0F, collisionStrength / nbExplosions - 2.0F + 2.0F * worldObj.rand.nextFloat());
			
			(atTarget ? targetWorld : worldObj).newExplosion((Entity) null, current.x, current.y, current.z, strength, atTarget, atTarget);
			WarpDrive.logger.info("Ship collision caused explosion at " + current.x + ", " + current.y + ", " + current.z + " with strength " + strength);
		}
	}
	
	private int getRealShipVolume_checkBedrock(StringBuilder reason) {
		LocalProfiler.start("EntityJump.getRealShipVolume_checkBedrock");
		int shipVolume = 0;
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					Block block = worldObj.getBlock(x, y, z);
					
					// Skipping vanilla air & WarpDrive gas blocks, keep WarpDrive air block
					if (worldObj.isAirBlock(x, y, z) || block.isAssociatedBlock(WarpDrive.blockAir)) {// whitelist
						continue;
					}
					
					shipVolume++;
					
					if (WarpDriveConfig.LOGGING_JUMP) {
						WarpDrive.logger.info("Block(" + x + ", " + y + ", " + z + ") is " + block.getUnlocalizedName() + "@" + worldObj.getBlockMetadata(x, y, z));
					}
					
					if (block.isAssociatedBlock(Blocks.bedrock)) {// Blacklist
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
					
					Block block = worldObj.getBlock(x, y, z);
					
					// Skipping air blocks
					if (worldObj.isAirBlock(x, y, z)) {
						continue;
					}
					
					// Skipping unmovable blocks
					if (block.isAssociatedBlock(Blocks.bedrock)) {// Blacklist
						continue;
					}
					
					TileEntity te = worldObj.getTileEntity(x, y, z);
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
			
			Entity entity = (Entity) o;
			MovingEntity movingEntity = new MovingEntity(entity);
			entitiesOnShip.add(movingEntity);
		}
	}
	
	private boolean moveEntities(boolean restorePositions) {
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Moving entities");
		}
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
				
				if (WarpDriveConfig.LOGGING_JUMP) {
					WarpDrive.logger.info("Entity moving: old (" + oldEntityX + " " + oldEntityY + " " + oldEntityZ + ") -> new (" + newEntityX + " " + newEntityY + " " + newEntityZ);
				}
				
				// Travel to another dimension if needed
				if (betweenWorlds && !restorePositions) {
					MinecraftServer server = MinecraftServer.getServer();
					WorldServer from = server.worldServerForDimension(worldObj.provider.dimensionId);
					WorldServer to = server.worldServerForDimension(targetWorld.provider.dimensionId);
					SpaceTeleporter teleporter = new SpaceTeleporter(to, 0,
							MathHelper.floor_double(newEntityX),
							MathHelper.floor_double(newEntityY),
							MathHelper.floor_double(newEntityZ));
					
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
					
					if (bedLocation != null && minX <= bedLocation.posX && maxX >= bedLocation.posX && minY <= bedLocation.posY && maxY >= bedLocation.posY
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
	
	public int[] getVector(int i) {
		int v[] = { 0, 0, 0 };
		
		switch (i) {
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
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info("CheckMovementResult " + sx + ", " + sy + ", " + sz + " -> " + tx + ", " + ty + ", " + tz + " " + isCollision + " '" + reason + "'");
			}
		}
	};
	
	private CheckMovementResult checkMovement(int testDistance, boolean fullCollisionDetails) {
		CheckMovementResult result = new CheckMovementResult();
		if ((direction == -1 && maxY + testDistance > 255) && !betweenWorlds) {
			result.add(xCoord, maxY + testDistance, zCoord, xCoord + 0.5D, maxY + testDistance + 1.0D, zCoord + 0.5D, false,
					"Reactor will blow due +high limit");
			return result;
		}
		
		if ((direction == -2 && minY - testDistance <= 8) && !betweenWorlds) {
			result.add(xCoord, minY - testDistance, zCoord, xCoord + 0.5D, maxY - testDistance, zCoord + 0.5D, false, "Reactor will blow due -low limit");
			return result;
		}
		
		int movementVector[] = getVector(direction);
		int lmoveX = movementVector[0] * testDistance;
		int lmoveY = movementVector[1] * testDistance;
		int lmoveZ = movementVector[2] * testDistance;
		
		int x, y, z, newX, newY, newZ;
		Block block;
		for (y = minY; y <= maxY; y++) {
			newY = y + lmoveY;
			for (x = minX; x <= maxX; x++) {
				newX = x + lmoveX;
				for (z = minZ; z <= maxZ; z++) {
					newZ = z + lmoveZ;
					
					block = worldObj.getBlock(newX, newY, newZ);
					if (block.isAssociatedBlock(Blocks.bedrock)) {// Blacklist
						result.add(x, y, z,
							newX + 0.5D - movementVector[0] * 1.0D,
							newY + 0.5D - movementVector[1] * 1.0D,
							newZ + 0.5D - movementVector[2] * 1.0D,
							true, "Unpassable block " + block + " detected at destination (" + newX + ";" + newY + ";" + newZ + ")");
						if (!fullCollisionDetails) {
							return result;
						}
					}
					
					if ( !worldObj.isAirBlock(x, y, z)
					  && !worldObj.isAirBlock(newX, newY, newZ)
					  && !block.isAssociatedBlock(WarpDrive.blockAir)
					  && !block.isAssociatedBlock(WarpDrive.blockGas)
					  && !block.isAssociatedBlock(Blocks.leaves)) {
						result.add(x, y, z,
							newX + 0.5D + movementVector[0] * 0.1D,
							newY + 0.5D + movementVector[1] * 0.1D,
							newZ + 0.5D + movementVector[2]	* 0.1D,
							true, "Obstacle block #" + block + " detected at (" + newX + ", " + newY + ", " + newZ + ")");
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
	
	private static ArrayList<Object> removeDuplicates(List<TileEntity> l) {
		Set<TileEntity> s = new TreeSet<TileEntity>(new Comparator<TileEntity>() {
			@Override
			public int compare(TileEntity o1, TileEntity o2) {
				if (o1.xCoord == o2.xCoord && o1.yCoord == o2.yCoord && o1.zCoord == o2.zCoord) {
					if (WarpDriveConfig.LOGGING_JUMP) {
						WarpDrive.logger.info("Removed duplicated TE: " + o1 + ", " + o2);
					}
					return 0;
				} else {
					return 1;
				}
			}
		});
		s.addAll(l);
		return new ArrayList<Object>(Arrays.asList(s.toArray()));
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		WarpDrive.logger.error(this + " readEntityFromNBT()");
	}
	
	@Override
	protected void entityInit() {
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.warn(this + " entityInit()");
		}
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound var1) {
		WarpDrive.logger.error(this + " writeEntityToNBT()");
	}
	
	public void setMinMaxes(int minXV, int maxXV, int minYV, int maxYV, int minZV, int maxZV) {
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
			getClass().getSimpleName(), Integer.valueOf(getEntityId()),
			shipCore == null ? "~NULL~" : (shipCore.uuid + ":" + shipCore.shipName),
			worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
			Double.valueOf(posX), Double.valueOf(posY), Double.valueOf(posZ) });
	}
}