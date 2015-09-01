package cr0s.warpdrive.block.movement;

import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.EntityJump;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Jumpgate;
import cr0s.warpdrive.data.StarMapEntry;
import cr0s.warpdrive.world.SpaceTeleporter;

/**
 * @author Cr0s
 */
public class TileEntityShipCore extends TileEntityAbstractEnergy {
	public Boolean ready;
	
	public Boolean launchState = false;
	
	public final int JUMP_UP = -1;
	public final int JUMP_DOWN = -2;
	public int dx, dz;
	private int direction;
	
	public int maxX, maxY, maxZ;
	public int minX, minY, minZ;
	
	public int shipFront, shipBack;
	public int shipLeft, shipRight;
	public int shipUp, shipDown;
	public int shipLength;
	public int shipMass;
	public int shipVolume;
	private ShipCoreMode currentMode = ShipCoreMode.IDLE;
	
	public enum ShipCoreMode {
		IDLE(0),
		BASIC_JUMP(1),		// 0-128
		LONG_JUMP(2),		// 0-12800
		TELEPORT(3),
		BEACON_JUMP(4),		// Jump ship by beacon
		HYPERSPACE(5),		// Jump to/from Hyperspace
		GATE_JUMP(6);		// Jump via jumpgate
		
		private final int code;
		
		ShipCoreMode(int code) {
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}
	}

	private int warmupTime = 0;
	private int cooldownTime = 0;
	public int randomWarmupAddition = 0;

	private int chestTeleportUpdateTicks = 0;
    private final int registryUpdateInterval_ticks = 20 * WarpDriveConfig.SHIP_CORE_REGISTRY_UPDATE_INTERVAL_SECONDS;
	private int registryUpdateTicks = 0;
    private int bootTicks = 20;
    
	public UUID uuid = null;
	public String shipName = "default";

	public int isolationBlocksCount = 0;
	public double isolationRate = 0.0D;
	public int isolationUpdateTicks = 0;

	public TileEntityShipController controller;

	private boolean soundPlayed = false;

	public TileEntityShipCore() {
		super();
		peripheralName = "warpdriveShipCore";
		// methodsArray = Arrays.asList("", "");;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		
		// Always cooldown
		if (cooldownTime > 0) {
			cooldownTime--;
			warmupTime = 0;
		}
		
		// Update state
		if (cooldownTime > 0) { // cooling down (2)
			if (getBlockMetadata() != 2) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 1 + 2);
			}
		} else if (controller == null) { // not connected (0)
			if (getBlockMetadata() != 0) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);
			}
		} else if (controller.isJumpFlag() || this.controller.isSummonAllFlag() || !this.controller.getToSummon().isEmpty()) { // active
			// (1)
			if (getBlockMetadata() != 1) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
			}
		} else { // inactive
			if (getBlockMetadata() != 0) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);
			}
		}
		
		// Update warp core in cores registry
		// accelerate update ticks during boot
		if (bootTicks > 0) {
			bootTicks--;
			if (controller == null) {
				registryUpdateTicks = 1;
			}
		}
		registryUpdateTicks--;
		if (registryUpdateTicks <= 0) {
			registryUpdateTicks = registryUpdateInterval_ticks;
			if (uuid == null || (uuid.getMostSignificantBits() == 0 && uuid.getLeastSignificantBits() == 0)) {
				uuid = UUID.randomUUID();
			}
			// recovery registration, shouldn't be need, in theory...
			WarpDrive.starMap.updateInRegistry(new StarMapEntry(this));
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.starMap.printRegistry();
				WarpDrive.logger.info(this + " controller is " + controller + ", warmupTime " + warmupTime + ", currentMode " + currentMode + ", jumpFlag "
						+ (controller == null ? "NA" : controller.isJumpFlag()) + ", cooldownTime " + cooldownTime);
			}
			
			TileEntity controllerFound = findControllerBlock();
			if (controllerFound == null) {
				controller = null;
				warmupTime = 0;
				soundPlayed = false;
				return;
			}
			controller = (TileEntityShipController) controllerFound;
		}
		
		isolationUpdateTicks++;
		if (isolationUpdateTicks > WarpDriveConfig.SHIP_CORE_ISOLATION_UPDATE_INTERVAL_SECONDS * 20) {
			isolationUpdateTicks = 0;
			updateIsolationState();
		}
		
		if (controller == null) {
			return;
		}
		
		currentMode = controller.getMode();
		
		StringBuilder reason = new StringBuilder();
		
		if ((controller.isJumpFlag() && (isolationUpdateTicks == 1)) || this.controller.isSummonAllFlag() || !this.controller.getToSummon().isEmpty()) {
			if (!validateShipSpatialParameters(reason)) {
				if (controller.isJumpFlag()) {
					controller.setJumpFlag(false);
					messageToAllPlayersOnShip(reason.toString());
				}
				warmupTime = 0;
				soundPlayed = false;
				return;
			}
			
			if (this.controller.isSummonAllFlag()) {
				summonPlayers();
				controller.setSummonAllFlag(false);
			} else if (!this.controller.getToSummon().isEmpty()) {
				summonSinglePlayer(this.controller.getToSummon());
				this.controller.setToSummon("");
			}
		}
		
		switch (currentMode) {
		case TELEPORT:
			if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
				if (isChestSummonMode()) {
					chestTeleportUpdateTicks++;
					if (chestTeleportUpdateTicks >= 20) {
						summonPlayersByChestCode();
						chestTeleportUpdateTicks = 0;
					}
				} else {
					teleportPlayersToSpace();
				}
			} else {
				chestTeleportUpdateTicks = 0;
			}
			break;
			
		case BASIC_JUMP:
		case LONG_JUMP:
		case BEACON_JUMP:
		case HYPERSPACE:
		case GATE_JUMP:
			if (controller.isJumpFlag()) {
				// Compute warm-up time
				int targetWarmup = 0;
				switch (currentMode) {
				case BASIC_JUMP:
				case LONG_JUMP:
					if (controller.getDistance() < 50) {
						targetWarmup = WarpDriveConfig.SHIP_SHORTJUMP_WARMUP_SECONDS * 20;
					} else {
						targetWarmup = WarpDriveConfig.SHIP_LONGJUMP_WARMUP_SECONDS * 20;
					}
					break;
					
				case BEACON_JUMP:
				case HYPERSPACE:
				case GATE_JUMP:
				default:
					targetWarmup = WarpDriveConfig.SHIP_LONGJUMP_WARMUP_SECONDS * 20;
					break;
				}
				// Select best sound file and adjust offset
				int soundThreshold = 0;
				String soundFile = "";
				if (targetWarmup < 10 * 20) {
					soundThreshold = targetWarmup - 4 * 20;
					soundFile = "warpdrive:warp_4s";
				} else if (targetWarmup > 29 * 20) {
					soundThreshold = targetWarmup - 30 * 20;
					soundFile = "warpdrive:warp_30s";
				} else {
					soundThreshold = targetWarmup - 10 * 20;
					soundFile = "warpdrive:warp_10s";
				}
				// Add random duration
				soundThreshold += randomWarmupAddition;
				
				// Check cooldown time
				if (cooldownTime > 0) {
					if (cooldownTime % 20 == 0) {
						int seconds = cooldownTime / 20;
						if ((seconds < 5) || ((seconds < 30) && (seconds % 5 == 0)) || (seconds % 10 == 0)) {
							messageToAllPlayersOnShip("Warp core is cooling down... " + cooldownTime / 20 + "s to go...");
						}
					}
					return;
				}
				
				// Set up activated animation
				if (warmupTime == 0) {
					messageToAllPlayersOnShip("Running pre-jump checklist...");
					
					// update ship parameters
					if (!validateShipSpatialParameters(reason)) {
						controller.setJumpFlag(false);
						messageToAllPlayersOnShip(reason.toString());
						return;
					}
					if (WarpDriveConfig.SHIP_WARMUP_SICKNESS) {
						if (WarpDriveConfig.LOGGING_JUMP) {
							WarpDrive.logger.info(this + " Giving warp sickness targetWarmup " + targetWarmup + " distance " + controller.getDistance());
						}
						makePlayersOnShipDrunk(targetWarmup + WarpDriveConfig.SHIP_WARMUP_RANDOM_TICKS);
					}
				}
				
				if (!soundPlayed && (soundThreshold > warmupTime)) {
					if (WarpDriveConfig.LOGGING_JUMP) {
						WarpDrive.logger.info(this + " Playing sound effect '" + soundFile + "' soundThreshold " + soundThreshold + " warmupTime " + warmupTime);
					}
					worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, soundFile, 4F, 1F);
					soundPlayed = true;
				}
				
				// Awaiting cool-down time
				if (warmupTime < (targetWarmup + randomWarmupAddition)) {
					warmupTime++;
					return;
				}
				
				warmupTime = 0;
				soundPlayed = false;
				
				if (!validateShipSpatialParameters(reason)) {
					controller.setJumpFlag(false);
					messageToAllPlayersOnShip(reason.toString());
					return;
				}
				
				if (WarpDrive.starMap.isWarpCoreIntersectsWithOthers(this)) {
					controller.setJumpFlag(false);
					messageToAllPlayersOnShip("Warp field intersects with other ship's field. Cannot jump.");
					return;
				}
				
				if (WarpDrive.cloaks.isCloaked(worldObj.provider.dimensionId, xCoord, yCoord, zCoord)) {
					controller.setJumpFlag(false);
					messageToAllPlayersOnShip("Core is inside a cloaking field. Aborting. Disable cloaking field to jump!");
					return;
				}
				
				doJump();
				cooldownTime = WarpDriveConfig.SHIP_COOLDOWN_INTERVAL_SECONDS * 20;
				controller.setJumpFlag(false);
			} else {
				warmupTime = 0;
			}
			break;
		default:
			break;
		}
	}
	
	public void messageToAllPlayersOnShip(String msg) {
		AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX + 0.99D, this.maxY + 0.99D, this.maxZ + 0.99D);
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

		WarpDrive.logger.info(this + " messageToAllPlayersOnShip: " + msg);
		for (Object object : list) {
			if (object == null || !(object instanceof EntityPlayer)) {
				continue;
			}
			
			WarpDrive.addChatMessage((EntityPlayer) object, "[" + (shipName.length() > 0 ? shipName : "WarpCore") + "] " + msg);
		}
	}

	private void updateIsolationState() {
		// Search block in cube around core
		int xmax, ymax, zmax;
		int xmin, ymin, zmin;
		xmin = xCoord - WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		xmax = xCoord + WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;

		zmin = zCoord - WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;
		zmax = zCoord + WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE;

		// scan 1 block higher to encourage putting isolation block on both
		// ground and ceiling
		ymin = Math.max(0, yCoord - WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE + 1);
		ymax = Math.min(255, yCoord + WarpDriveConfig.RADAR_MAX_ISOLATION_RANGE + 1);

		int newCount = 0;

		// Search for warp isolation blocks
		for (int y = ymin; y <= ymax; y++) {
			for (int x = xmin; x <= xmax; x++) {
				for (int z = zmin; z <= zmax; z++) {
					if (worldObj.getBlock(x, y, z).isAssociatedBlock(WarpDrive.blockWarpIsolation)) {
						newCount++;
					}
				}
			}
		}
		isolationBlocksCount = newCount;
		if (isolationBlocksCount >= WarpDriveConfig.RADAR_MIN_ISOLATION_BLOCKS) {
			isolationRate = WarpDriveConfig.RADAR_MIN_ISOLATION_EFFECT
					+ (isolationBlocksCount - WarpDriveConfig.RADAR_MIN_ISOLATION_BLOCKS) // bonus
					// blocks
					* (WarpDriveConfig.RADAR_MAX_ISOLATION_EFFECT - WarpDriveConfig.RADAR_MIN_ISOLATION_EFFECT)
					/ (WarpDriveConfig.RADAR_MAX_ISOLATION_BLOCKS - WarpDriveConfig.RADAR_MIN_ISOLATION_BLOCKS);
		} else {
			isolationRate = 0.0D;
		}
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Isolation updated to " + isolationBlocksCount + " (" + String.format("%.1f", isolationRate * 100) + "%)");
		}
	}

	private void makePlayersOnShipDrunk(int tickDuration) {
		AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

		for (Object o : list) {
			if (o == null || !(o instanceof EntityPlayer)) {
				continue;
			}

			// Set "drunk" effect
			((EntityPlayer) o).addPotionEffect(new PotionEffect(Potion.confusion.id, tickDuration, 0, true));
		}
	}

	private void summonPlayers() {
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);

		for (int i = 0; i < controller.players.size(); i++) {
			String nick = controller.players.get(i);
			EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(nick);

			if (player != null
					&& !testBB(aabb, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ))) {
				summonPlayer(player, xCoord + dx, yCoord, zCoord + dz);
			}
		}
	}

	private void summonSinglePlayer(String nickname) {
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);

		for (int i = 0; i < controller.players.size(); i++) {
			String nick = controller.players.get(i);
			EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(nick);

			if (player != null && nick.equals(nickname)
					&& !testBB(aabb, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ))) {
				summonPlayer(player, xCoord + dx, yCoord, zCoord + dz);
				return;
			}
		}
	}

	private void summonPlayer(EntityPlayerMP player, int x, int y, int z) {
		if (consumeEnergy(WarpDriveConfig.SHIP_TELEPORT_ENERGY_PER_ENTITY, false)) {
			if (player.dimension != worldObj.provider.dimensionId) {
				player.mcServer.getConfigurationManager().transferPlayerToDimension(
					player,
					worldObj.provider.dimensionId,
					new SpaceTeleporter(
						DimensionManager.getWorld(worldObj.provider.dimensionId),
						0,
						MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ)));
				player.setPositionAndUpdate(x, y, z);
				player.sendPlayerAbilities();
			} else {
				player.setPositionAndUpdate(x, y, z);
			}
		}
	}

	public boolean validateShipSpatialParameters(StringBuilder reason) {
		if (controller == null) {
			reason.append("TileEntityReactor.validateShipSpatialParameters: no controller detected!");
			return false;
		}
		direction = controller.getDirection();
		shipFront = controller.getFront();
		shipRight = controller.getRight();
		shipUp = controller.getUp();
		shipBack = controller.getBack();
		shipLeft = controller.getLeft();
		shipDown = controller.getDown();
		
		int x1 = 0, x2 = 0, z1 = 0, z2 = 0;
		
		if (Math.abs(dx) > 0) {
			if (dx == 1) {
				x1 = xCoord - shipBack;
				x2 = xCoord + shipFront;
				z1 = zCoord - shipLeft;
				z2 = zCoord + shipRight;
			} else {
				x1 = xCoord - shipFront;
				x2 = xCoord + shipBack;
				z1 = zCoord - shipRight;
				z2 = zCoord + shipLeft;
			}
		} else if (Math.abs(dz) > 0) {
			if (dz == 1) {
				z1 = zCoord - shipBack;
				z2 = zCoord + shipFront;
				x1 = xCoord - shipRight;
				x2 = xCoord + shipLeft;
			} else {
				z1 = zCoord - shipFront;
				z2 = zCoord + shipBack;
				x1 = xCoord - shipLeft;
				x2 = xCoord + shipRight;
			}
		}

		if (x1 < x2) {
			minX = x1;
			maxX = x2;
		} else {
			minX = x2;
			maxX = x1;
		}

		if (z1 < z2) {
			minZ = z1;
			maxZ = z2;
		} else {
			minZ = z2;
			maxZ = z1;
		}

		minY = yCoord - shipDown;
		maxY = yCoord + shipUp;
		shipLength = 0;

		switch (direction) {
		case 0:
		case 180:
			shipLength = shipBack + shipFront;
			break;

		case 90:
		case 270:
			shipLength = shipLeft + shipRight;
			break;

		case -1:
		case -2:
			shipLength = shipDown + shipUp;
			break;

		default:
			reason.append("Invalid jump direction " + direction);
			return false;
		}

		// Ship side is too big
		if ( (shipBack + shipFront) > WarpDriveConfig.SHIP_MAX_SIDE_SIZE
		  || (shipLeft + shipRight) > WarpDriveConfig.SHIP_MAX_SIDE_SIZE
		  || (shipDown + shipUp) > WarpDriveConfig.SHIP_MAX_SIDE_SIZE) {
			reason.append("Ship is too big (max is " + WarpDriveConfig.SHIP_MAX_SIDE_SIZE + " per side)");
			return false;
		}

		boolean isUnlimited = false;
		AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX + 0.99D, maxY + 0.99D, maxZ + 0.99D);
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
		for (Object o : list) {
			if (o == null || !(o instanceof EntityPlayer)) {
				continue;
			}

			String playerName = ((EntityPlayer) o).getDisplayName();
			for (String unlimiteName : WarpDriveConfig.SHIP_VOLUME_UNLIMITED_PLAYERNAMES) {
				isUnlimited = isUnlimited || unlimiteName.equals(playerName);
			}
		}

		updateShipMassAndVolume();
		if (!isUnlimited && shipMass > WarpDriveConfig.SHIP_VOLUME_MAX_ON_PLANET_SURFACE && worldObj.provider.dimensionId == 0) {
			reason.append("Ship is too big for the overworld (max is " + WarpDriveConfig.SHIP_VOLUME_MAX_ON_PLANET_SURFACE + " blocks)");
			return false;
		}

		return true;
	}

	private void doBeaconJump() {
		// Search beacon coordinates
		String freq = controller.getBeaconFrequency();
		int beaconX = 0, beaconZ = 0;
		boolean isBeaconFound = false;
		EntityPlayerMP player;

		for (int i = 0; i < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); i++) {
			player = (EntityPlayerMP) MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(i);

			// Skip players from other dimensions
			if (player.dimension != worldObj.provider.dimensionId) {
				continue;
			}

			TileEntity tileEntity = worldObj.getTileEntity(
					MathHelper.floor_double(player.posX),
					MathHelper.floor_double(player.posY) - 1,
					MathHelper.floor_double(player.posZ));

			if (tileEntity != null && (tileEntity instanceof TileEntityShipController)) {
				if (((TileEntityShipController) tileEntity).getBeaconFrequency().equals(freq)) {
					beaconX = tileEntity.xCoord;
					beaconZ = tileEntity.zCoord;
					isBeaconFound = true;
					break;
				}
			}
		}
		
		// Now make jump to a beacon
		if (isBeaconFound) {
			// Consume energy
			if (consumeEnergy(calculateRequiredEnergy(currentMode, shipMass, controller.getDistance()), false)) {
				WarpDrive.logger.info(this + " Moving ship to beacon (" + beaconX + "; " + yCoord + "; " + beaconZ + ")");
				EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, dx, dz, this, false, 1, 0, true, beaconX, yCoord, beaconZ);
				jump.maxX = maxX;
				jump.minX = minX;
				jump.maxZ = maxZ;
				jump.minZ = minZ;
				jump.maxY = maxY;
				jump.minY = minY;
				jump.shipLength = shipLength;
				jump.on = true;
				worldObj.spawnEntityInWorld(jump);
			} else {
				messageToAllPlayersOnShip("Insufficient energy level");
			}
		} else {
			WarpDrive.logger.info(this + " Beacon '" + freq + "' is unknown.");
		}
	}
	
	private boolean isShipInJumpgate(Jumpgate jumpgate, StringBuilder reason) {
		AxisAlignedBB aabb = jumpgate.getGateAABB();
		if (WarpDriveConfig.LOGGING_JUMP) {
			WarpDrive.logger.info(this + " Jumpgate " + jumpgate.name + " AABB is " + aabb);
		}
		int countBlocksInside = 0;
		int countBlocksTotal = 0;
		
		if ( aabb.isVecInside(Vec3.createVectorHelper(minX, minY, minZ))
		  && aabb.isVecInside(Vec3.createVectorHelper(maxX, maxY, maxZ)) ) {
			// fully inside
			return true;
		}
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					Block block = worldObj.getBlock(x, y, z);
					
					// Skipping vanilla air & ignored blocks
					if (block == Blocks.air || WarpDriveConfig.BLOCKS_LEFTBEHIND.contains(block)) {
						continue;
					}
					if (WarpDriveConfig.BLOCKS_NOMASS.contains(block)) {
						continue;
					}
					
					if (aabb.minX <= x && aabb.maxX >= x && aabb.minY <= y && aabb.maxY >= y && aabb.minZ <= z && aabb.maxZ >= z) {
						countBlocksInside++;
					}
					countBlocksTotal++;
				}
			}
		}
		
		float percent = 0F;
		if (shipMass != 0) {
			percent = Math.round((((countBlocksInside * 1.0F) / shipMass) * 100.0F) * 10.0F) / 10.0F;
		}
		
		if (WarpDriveConfig.LOGGING_JUMP) {
			if (shipMass != countBlocksTotal) {
				WarpDrive.logger.info(this + " Ship mass has changed from " + shipMass + " to " + countBlocksTotal + " blocks");
			}
			WarpDrive.logger.info(this + "Ship has " + countBlocksInside + " / " + shipMass + " blocks (" + percent + "%) in jumpgate '" + jumpgate.name + "'");
		}
		
		// At least 80% of ship must be inside jumpgate
		if (percent > 80F) {
			return true;
		} else if (percent <= 0.001) {
			reason.append("Ship is not inside a jumpgate. Jump rejected. Nearest jumpgate is " + jumpgate.toNiceString());
			return false;
		} else {
			reason.append("Ship is only " + percent + "% inside a jumpgate. Sorry, we'll loose too much crew as is, jump rejected.");
			return false;
		}
	}
	
	private boolean isFreePlaceForShip(int destX, int destY, int destZ) {
		int newX, newY, newZ;
		
		if (destY + shipUp > 255 || destY - shipDown < 5) {
			return false;
		}
		
		int moveX = destX - xCoord;
		int moveY = destY - yCoord;
		int moveZ = destZ - zCoord;
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					Block blockSource = worldObj.getBlock(x, y, z);
					Block blockTarget = worldObj.getBlock(moveX + x, moveY + y, moveZ + z);
					
					// not vanilla air nor ignored blocks at source
					// not vanilla air nor expandable blocks are target location
					if ( blockSource != Blocks.air
					  && !WarpDriveConfig.BLOCKS_EXPANDABLE.contains(blockSource)
					  && blockTarget != Blocks.air
					  && !WarpDriveConfig.BLOCKS_EXPANDABLE.contains(blockTarget)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	private void doGateJump() {
		// Search nearest jump-gate
		String gateName = controller.getTargetJumpgateName();
		Jumpgate targetGate = WarpDrive.jumpgates.findGateByName(gateName);
		
		if (targetGate == null) {
			messageToAllPlayersOnShip("Destination jumpgate '" + gateName + "' is unknown. Check jumpgate name.");
			this.controller.setJumpFlag(false);
			return;
		}
		
		// Now make jump to a beacon
		int gateX = targetGate.xCoord;
		int gateY = targetGate.yCoord;
		int gateZ = targetGate.zCoord;
		int destX = gateX;
		int destY = gateY;
		int destZ = gateZ;
		Jumpgate nearestGate = WarpDrive.jumpgates.findNearestGate(xCoord, yCoord, zCoord);
		
		StringBuilder reason = new StringBuilder();
		if (!isShipInJumpgate(nearestGate, reason)) {
			messageToAllPlayersOnShip(reason.toString());
			this.controller.setJumpFlag(false);
			return;
		}
		
		// If gate is blocked by obstacle
		if (!isFreePlaceForShip(gateX, gateY, gateZ)) {
			// Randomize destination coordinates and check for collision with obstacles around jumpgate
			// Try to find good place for ship
			int numTries = 10; // num tries to check for collision
			boolean placeFound = false;
			
			for (; numTries > 0; numTries--) {
				// randomize destination coordinates around jumpgate
				destX = gateX + ((worldObj.rand.nextBoolean()) ? -1 : 1) * (20 + worldObj.rand.nextInt(100));
				destZ = gateZ + ((worldObj.rand.nextBoolean()) ? -1 : 1) * (20 + worldObj.rand.nextInt(100));
				destY = gateY + ((worldObj.rand.nextBoolean()) ? -1 : 1) * (20 + worldObj.rand.nextInt(50));
				
				// check for collision
				if (isFreePlaceForShip(destX, destY, destZ)) {
					placeFound = true;
					break;
				}
			}
			
			if (!placeFound) {
				messageToAllPlayersOnShip("Destination gate is blocked by obstacles. Aborting...");
				this.controller.setJumpFlag(false);
				return;
			}
			
			WarpDrive.logger.info("[GATE] Place found over " + (10 - numTries) + " tries.");
		}
		
		// Consume energy
		if (consumeEnergy(calculateRequiredEnergy(currentMode, shipMass, controller.getDistance()), false)) {
			WarpDrive.logger.info(this + " Moving ship to a place around gate '" + targetGate.name + "' (" + destX + "; " + destY + "; " + destZ + ")");
			EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, dx, dz, this, false, 1, 0, true, destX, destY, destZ);
			jump.maxX = maxX;
			jump.minX = minX;
			jump.maxZ = maxZ;
			jump.minZ = minZ;
			jump.maxY = maxY;
			jump.minY = minY;
			jump.shipLength = shipLength;
			jump.on = true;
			worldObj.spawnEntityInWorld(jump);
		} else {
			messageToAllPlayersOnShip("Insufficient energy level");
		}
	}
	
	private void doJump() {
		int distance = controller.getDistance();
		int requiredEnergy = calculateRequiredEnergy(currentMode, shipMass, distance);
		
		if (!consumeEnergy(requiredEnergy, true)) {
			messageToAllPlayersOnShip("Insufficient energy to jump! Core is currently charged with " + getEnergyStored() + " EU while jump requires "
					+ requiredEnergy + " EU");
			this.controller.setJumpFlag(false);
			return;
		}
		
		String shipInfo = "" + shipVolume + " blocks inside (" + minX + ", " + minY + ", " + minZ + ") to (" + maxX + ", " + maxY + ", " + maxZ + ") with an actual mass of " + shipMass + " blocks";
		if (currentMode == ShipCoreMode.GATE_JUMP) {
			WarpDrive.logger.info(this + " Performing gate jump of " + shipInfo);
			doGateJump();
			return;
		} else if (currentMode == ShipCoreMode.BEACON_JUMP) {
			WarpDrive.logger.info(this + " Performing beacon jump of " + shipInfo);
			doBeaconJump();
			return;
		} else if (currentMode == ShipCoreMode.HYPERSPACE) {
			WarpDrive.logger.info(this + " Performing hyperspace jump of " + shipInfo);
			
			// Check ship size for hyper-space jump
			if (shipMass < WarpDriveConfig.SHIP_VOLUME_MIN_FOR_HYPERSPACE) {
				Jumpgate nearestGate = null;
				if (WarpDrive.jumpgates == null) {
					WarpDrive.logger.warn(this + " WarpDrive.instance.jumpGates is NULL!");
				} else {
					nearestGate = WarpDrive.jumpgates.findNearestGate(xCoord, yCoord, zCoord);
				}
				
				StringBuilder reason = new StringBuilder();
				if (nearestGate == null || !isShipInJumpgate(nearestGate, reason)) {
					this.messageToAllPlayersOnShip("Ship is too small (" + shipMass + "/" + WarpDriveConfig.SHIP_VOLUME_MIN_FOR_HYPERSPACE
							+ "). Insufficient ship mass to open hyperspace portal. Use a jumpgate to reach or exit hyperspace.");
					this.controller.setJumpFlag(false);
					return;
				}
			}
		} else if (currentMode == ShipCoreMode.BASIC_JUMP) {
			WarpDrive.logger.info(this + " Performing basic jump of " + shipInfo + " toward direction " + direction + " over " + distance + " blocks.");
		} else if (currentMode == ShipCoreMode.LONG_JUMP) {
			WarpDrive.logger.info(this + " Performing long jump of " + shipInfo + " toward direction " + direction + " over " + distance + " blocks.");
		} else {
			WarpDrive.logger.info(this + " Performing some jump #" + currentMode + " of " + shipInfo);
		}
		
		if (currentMode == ShipCoreMode.BASIC_JUMP || currentMode == ShipCoreMode.LONG_JUMP || currentMode == ShipCoreMode.HYPERSPACE) {
			if (!consumeEnergy(requiredEnergy, false)) {
				messageToAllPlayersOnShip("Insufficient energy level");
				return;
			}
			
			if (this.currentMode == ShipCoreMode.BASIC_JUMP) {
				distance += shipLength;
			}
			
			if (currentMode == ShipCoreMode.LONG_JUMP && (direction != -1) && (direction != -2)) {
				if (worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) {
					distance *= 100;
				}
			}
			
			if (WarpDriveConfig.LOGGING_JUMP) {
				WarpDrive.logger.info(this + " Distance adjusted to " + distance + " blocks.");
			}
			EntityJump jump = new EntityJump(worldObj, xCoord, yCoord, zCoord, dx, dz, this, (currentMode == ShipCoreMode.HYPERSPACE), distance, direction,
					false, 0, 0, 0);
			jump.maxX = maxX;
			jump.minX = minX;
			jump.maxZ = maxZ;
			jump.minZ = minZ;
			jump.maxY = maxY;
			jump.minY = minY;
			jump.shipLength = shipLength;
			jump.on = true;
			worldObj.spawnEntityInWorld(jump);
		}
	}
	
	private void teleportPlayersToSpace() {
		if (worldObj.provider.dimensionId != WarpDriveConfig.G_SPACE_DIMENSION_ID) {
			AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(xCoord - 2, yCoord - 1, zCoord - 2, xCoord + 2, yCoord + 4, zCoord + 2);
			List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
			
			WorldServer spaceWorld = DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID);
			for (Object o : list) {
				if (!consumeEnergy(WarpDriveConfig.SHIP_TELEPORT_ENERGY_PER_ENTITY, false)) {
					return;
				}
				
				Entity entity = (Entity) o;
				int x = MathHelper.floor_double(entity.posX);
				int z = MathHelper.floor_double(entity.posZ);
				// int y = MathHelper.floor_double(entity.posY);
				int newY;
				
				for (newY = 254; newY > 0; newY--) {
					if (spaceWorld.getBlock(x, newY, z).isAssociatedBlock(Blocks.wool)) {
						break;
					}
				}
				
				if (newY <= 0) {
					newY = 254;
				}
				
				if (entity instanceof EntityPlayerMP) {
					((EntityPlayerMP) entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity),
							WarpDriveConfig.G_SPACE_DIMENSION_ID,
							new SpaceTeleporter(DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID), 0, x, 256, z));
					
					if (spaceWorld.isAirBlock(x, newY, z)) {
						spaceWorld.setBlock(x, newY, z, Blocks.stone, 0, 2);
						spaceWorld.setBlock(x + 1, newY, z, Blocks.stone, 0, 2);
						spaceWorld.setBlock(x - 1, newY, z, Blocks.stone, 0, 2);
						spaceWorld.setBlock(x, newY, z + 1, Blocks.stone, 0, 2);
						spaceWorld.setBlock(x, newY, z - 1, Blocks.stone, 0, 2);
						spaceWorld.setBlock(x + 1, newY, z + 1, Blocks.stone, 0, 2);
						spaceWorld.setBlock(x - 1, newY, z - 1, Blocks.stone, 0, 2);
						spaceWorld.setBlock(x + 1, newY, z - 1, Blocks.stone, 0, 2);
						spaceWorld.setBlock(x - 1, newY, z + 1, Blocks.stone, 0, 2);
					}
					
					((EntityPlayerMP) entity).setPositionAndUpdate(x + 0.5D, newY + 2.0D, z + 0.5D);
					((EntityPlayerMP) entity).sendPlayerAbilities();
				}
			}
		}
	}
	
	private void summonPlayersByChestCode() {
		if (worldObj.getTileEntity(xCoord, yCoord + 1, zCoord) == null) {
			return;
		}
		
		TileEntityChest chest = (TileEntityChest) worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
		EntityPlayerMP player;
		
		for (int i = 0; i < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); i++) {
			player = (EntityPlayerMP) MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(i);
			
			if (checkPlayerInventory(chest, player)) {
				WarpDrive.logger.info(this + " Summoning " + player.getDisplayName());
				summonPlayer(player, xCoord, yCoord + 2, zCoord);
			}
		}
	}
	
	private static boolean checkPlayerInventory(TileEntityChest chest, EntityPlayerMP player) {
		Boolean result = false;
		final int MIN_KEY_LENGTH = 5;
		int keyLength = 0;
		
		for (int index = 0; index < chest.getSizeInventory(); index++) {
			ItemStack chestItem = chest.getStackInSlot(index);
			ItemStack playerItem = player.inventory.getStackInSlot(9 + index);
			
			if (chestItem == null) {
				continue;
			}
			
			if (playerItem == null || chestItem != playerItem
			  || chestItem.getItemDamage() != playerItem.getItemDamage()
			  || chestItem.stackSize != playerItem.stackSize) {
				return false;
			} else {
				result = true;
			}
			
			keyLength++;
		}
		
		if (keyLength < MIN_KEY_LENGTH) {
			WarpDrive.logger.info("[ChestCode] Key is too short: " + keyLength + " < " + MIN_KEY_LENGTH);
			return false;
		}
		
		return result;
	}
	
	private Boolean isChestSummonMode() {
		TileEntity te = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
		
		if (te != null) {
			return (te instanceof TileEntityChest);
		}
		
		return false;
	}
	
	private static boolean testBB(AxisAlignedBB axisalignedbb, int x, int y, int z) {
		return axisalignedbb.minX <= x && axisalignedbb.maxX >= x
			&& axisalignedbb.minY <= y && axisalignedbb.maxY >= y
			&& axisalignedbb.minZ <= z && axisalignedbb.maxZ >= z;
	}
	
	@Override
	public String getStatus() {
		return getBlockType().getLocalizedName()
			+ String.format(" '%s' energy level is %.0f/%.0f EU.",
				shipName,
				convertInternalToEU(getEnergyStored()),
				convertInternalToEU(getMaxEnergyStored()))
			+ ((cooldownTime > 0) ? ("\n" + (cooldownTime / 20) + " s left of cooldown.")
				: ((isolationBlocksCount > 0) ? ("\n" + isolationBlocksCount + " active isolation blocks") : ""));
	}
	
	public static int calculateRequiredEnergy(ShipCoreMode shipCoreMode, int shipVolume, int jumpDistance) {
		switch (shipCoreMode) {
		case TELEPORT:
			return WarpDriveConfig.SHIP_TELEPORT_ENERGY_PER_ENTITY;
			
		case BASIC_JUMP:
			return (WarpDriveConfig.SHIP_NORMALJUMP_ENERGY_PER_BLOCK * shipVolume) + (WarpDriveConfig.SHIP_NORMALJUMP_ENERGY_PER_DISTANCE * jumpDistance);
			
		case LONG_JUMP:
			return (WarpDriveConfig.SHIP_HYPERJUMP_ENERGY_PER_BLOCK * shipVolume) + (WarpDriveConfig.SHIP_HYPERJUMP_ENERGY_PER_DISTANCE * jumpDistance);
			
		case HYPERSPACE:
			return WarpDriveConfig.SHIP_MAX_ENERGY_STORED / 10; // 10% of maximum
			
		case BEACON_JUMP:
			return WarpDriveConfig.SHIP_MAX_ENERGY_STORED / 2; // half of maximum
			
		case GATE_JUMP:
			return 2 * shipVolume;
		default:
			break;
		}
		
		return WarpDriveConfig.SHIP_MAX_ENERGY_STORED;
	}
	
	private void updateShipMassAndVolume() {
		int newMass = 0;
		int newVolume = 0;
		
		try {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					for (int y = minY; y <= maxY; y++) {
						Block block = worldObj.getBlock(x, y, z);
						
						// Skipping vanilla air & ignored blocks
						if (block == Blocks.air || WarpDriveConfig.BLOCKS_LEFTBEHIND.contains(block)) {
							continue;
						}
						newVolume++;
						
						if (WarpDriveConfig.BLOCKS_NOMASS.contains(block)) {
							continue;
						}
						newMass++;
					}
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		shipMass = newMass;
		shipVolume = newVolume;
	}
	
	private TileEntity findControllerBlock() {
		TileEntity result;
		result = worldObj.getTileEntity(xCoord + 1, yCoord, zCoord);
		
		if (result != null && result instanceof TileEntityShipController) {
			dx = 1;
			dz = 0;
			return result;
		}
		
		result = worldObj.getTileEntity(xCoord - 1, yCoord, zCoord);
		
		if (result != null && result instanceof TileEntityShipController) {
			dx = -1;
			dz = 0;
			return result;
		}
		
		result = worldObj.getTileEntity(xCoord, yCoord, zCoord + 1);
		
		if (result != null && result instanceof TileEntityShipController) {
			dx = 0;
			dz = 1;
			return result;
		}
		
		result = worldObj.getTileEntity(xCoord, yCoord, zCoord - 1);
		
		if (result != null && result instanceof TileEntityShipController) {
			dx = 0;
			dz = -1;
			return result;
		}
		
		return null;
	}
	
	public int getCooldown() {
		return cooldownTime;
	}
	
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.SHIP_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		uuid = new UUID(tag.getLong("uuidMost"), tag.getLong("uuidLeast"));
		if (uuid.getMostSignificantBits() == 0 && uuid.getLeastSignificantBits() == 0) {
			uuid = UUID.randomUUID();
		}
		shipName = tag.getString("corefrequency") + tag.getString("shipName");	// coreFrequency is the legacy tag name
		isolationBlocksCount = tag.getInteger("isolation");
		cooldownTime = tag.getInteger("cooldownTime");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if (uuid != null) {
			tag.setLong("uuidMost", uuid.getMostSignificantBits());
			tag.setLong("uuidLeast", uuid.getLeastSignificantBits());
		}
		tag.setString("shipName", shipName);
		tag.setInteger("isolation", isolationBlocksCount);
		tag.setInteger("cooldownTime", cooldownTime);
	}
	
	@Override
	public void validate() {
		super.validate();
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		
		WarpDrive.starMap.updateInRegistry(new StarMapEntry(this));
	}
	
	@Override
	public void invalidate() {
		if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			WarpDrive.starMap.removeFromRegistry(new StarMapEntry(this));
		}
		super.invalidate();
	}
	
	@Override
	public String toString() {
		return String.format(
				"%s \'%s\' @ \'%s\' %d, %d, %d",
				new Object[] { getClass().getSimpleName(), shipName, worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
						Integer.valueOf(xCoord), Integer.valueOf(yCoord), Integer.valueOf(zCoord) });
	}
}
