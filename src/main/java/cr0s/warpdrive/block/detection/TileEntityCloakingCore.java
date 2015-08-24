package cr0s.warpdrive.block.detection;

import java.util.Arrays;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CloakedArea;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityCloakingCore extends TileEntityAbstractEnergy {
	public boolean isEnabled = false;
	public byte tier = 1; // cloaking field tier, 1 or 2
	
	// inner coils color map
	final float[] innerCoilColor_r = { 1.00f, 1.00f, 1.00f, 1.00f, 0.75f, 0.25f, 0.00f, 0.00f, 0.00f, 0.00f, 0.50f, 1.00f }; 
	final float[] innerCoilColor_g = { 0.00f, 0.25f, 0.75f, 1.00f, 1.00f, 1.00f, 1.00f, 1.00f, 0.50f, 0.25f, 0.00f, 0.00f }; 
	final float[] innerCoilColor_b = { 0.25f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.50f, 1.00f, 1.00f, 1.00f, 1.00f, 0.75f }; 
	
	// Spatial cloaking field parameters
	private final int innerCoilsDistance = 2; // Step length from core block to main coils
	private final byte[] dx = { -1,  1,  0,  0,  0,  0 };	// validateAssembly() is coded ordering, do not change! 
	private final byte[] dy = {  0,  0, -1,  1,  0,  0 };	// validateAssembly() is coded ordering, do not change!
	private final byte[] dz = {  0,  0,  0,  0, -1,  1 };	// validateAssembly() is coded ordering, do not change!
	
	private int[] outerCoilsDistance = {0, 0, 0, 0, 0, 0};
	public int minX = 0;
	public int minY = 0;
	public int minZ = 0;
	public int maxX = 0;
	public int maxY = 0;
	public int maxZ = 0;
	
	public boolean isValid = false;
	public boolean isCloaking = false;
	public int volume = 0;
	private int updateTicks = 0;
	private int laserDrawingTicks = 0;
	
	private boolean soundPlayed = false;
	private int soundTicks = 0;

	public TileEntityCloakingCore() {
		super();
		peripheralName = "warpdriveCloakingCore";
		methodsArray = new String[] {
			"tier",				// set field tier to 1 or 2, return field tier
			"isAssemblyValid",	// returns true or false
			"getEnergyLevel", 
			"enable"			// set field enable state (true or false), return true if enabled
		};
		CC_scripts = Arrays.asList("cloak1", "cloak2", "uncloak");
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		
		// Reset sound timer
		soundTicks++;
		if (soundTicks >= 40) {
			soundTicks = 0;
			soundPlayed = false;
		}
		
		updateTicks--;
		if (updateTicks <= 0) {
			if (WarpDriveConfig.LOGGING_CLOAKING) {
				WarpDrive.logger.info(this + " Updating cloaking state...");
			}
			updateTicks = ((tier == 1) ? 20 : (tier == 2) ? 10 : 20) * WarpDriveConfig.CLOAKING_FIELD_REFRESH_INTERVAL_SECONDS; // resetting timer
			
			isValid = validateAssembly();
			isCloaking = WarpDrive.cloaks.isAreaExists(worldObj, xCoord, yCoord, zCoord); 
			if (!isEnabled) {// disabled
				if (isCloaking) {// disabled, cloaking => stop cloaking
					if (WarpDriveConfig.LOGGING_CLOAKING) {
						WarpDrive.logger.info(this + " Disabled, cloak field going down...");
					}
					disableCloakingField();
				} else {// disabled, no cloaking
					// IDLE
				}
			} else {// isEnabled
				boolean hasEnoughPower = countBlocksAndConsumeEnergy();
				if (!isCloaking) {// enabled, not cloaking
					if (hasEnoughPower && isValid) {// enabled, can cloak and able to
						setCoilsState(true);
						
						// Register cloak
						WarpDrive.cloaks.addCloakedAreaWorld(worldObj, minX, minY, minZ, maxX, maxY, maxZ, xCoord, yCoord, zCoord, tier);
						if (!soundPlayed) {
							soundPlayed = true;
							worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:cloak", 4F, 1F);
						}
						
						// Refresh the field
						CloakedArea area = WarpDrive.cloaks.getCloakedArea(worldObj, xCoord, yCoord, zCoord);
						if (area != null) {
							area.sendCloakPacketToPlayersEx(false); // recloak field
						} else {
							if (WarpDriveConfig.LOGGING_CLOAKING) {
								WarpDrive.logger.info("getCloakedArea1 returned null for " + worldObj + " " + xCoord + "," + yCoord + "," + zCoord);
							}
						}
					} else {// enabled, not cloaking but not able to
						// IDLE
					}
				} else {// enabled & cloaked
					if (!isValid) {// enabled, cloaking but invalid
						if (WarpDriveConfig.LOGGING_CLOAKING) {
							WarpDrive.logger.info(this + " Coil(s) lost, cloak field is collapsing...");
						}
						consumeAllEnergy();
						disableCloakingField();				
					} else {// enabled, cloaking and valid
						if (hasEnoughPower) {// enabled, cloaking and able to
							// IDLE
							// Refresh the field FIXME: workaround to re-synchronize players
							CloakedArea area = WarpDrive.cloaks.getCloakedArea(worldObj, xCoord, yCoord, zCoord);
							if (area != null) {
								area.sendCloakPacketToPlayersEx(false); // recloak field
							} else {
								if (WarpDriveConfig.LOGGING_CLOAKING) {
									WarpDrive.logger.info("getCloakedArea2 returned null for " + worldObj + " " + xCoord + "," + yCoord + "," + zCoord);
								}
							}
							setCoilsState(true);
						} else {// loosing power
							if (WarpDriveConfig.LOGGING_CLOAKING) {
								WarpDrive.logger.info(this + " Low power, cloak field is collapsing...");
							}
							disableCloakingField();
						}
					}
				}
			}
		}
		
		if (laserDrawingTicks++ > 100) {
			laserDrawingTicks = 0;
			
			if (isEnabled && isValid) {
				drawLasers();
			}
		}
	}
	
	private void setCoilsState(final boolean enabled) {
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (enabled) ? 1 : 0, 2);
		
		for (int direction = 0; direction < 6; direction++) {
			setCoilState(innerCoilsDistance, direction, enabled);
			setCoilState(outerCoilsDistance[direction], direction, enabled);
		}
	}
	
	private void setCoilState(final int distance, final int direction, final boolean enabled) {
		int x = xCoord + distance * dx[direction];
		int y = yCoord + distance * dy[direction];
		int z = zCoord + distance * dz[direction];
		if (worldObj.getBlock(x, y, z).isAssociatedBlock(WarpDrive.blockCloakingCoil)) {
			worldObj.setBlockMetadataWithNotify(x, y, z, (enabled) ? 1 : 0, 2);
		}
	}

	private void drawLasers() {
		float r = 0.0f;
		float g = 1.0f;
		float b = 0.0f;
		if (!isCloaking) {// out of energy
			r = 0.75f;
			g = 0.50f;
			b = 0.50f;
		} else if (tier == 1) {
			r = 0.25f;
			g = 1.00f;
			b = 0.00f;
		} else if (tier == 2) {
			r = 0.00f;
			g = 0.25f;
			b = 1.00f;
		}
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		for (int direction = 0; direction < 6; direction++) {
			PacketHandler.sendBeamPacketToPlayersInArea(worldObj,
					new Vector3(
						xCoord + innerCoilsDistance * dx[direction],
						yCoord + innerCoilsDistance * dy[direction],
						zCoord + innerCoilsDistance * dz[direction]).translate(0.5),
					new Vector3(
						xCoord + outerCoilsDistance[direction] * dx[direction],
						yCoord + outerCoilsDistance[direction] * dy[direction],
						zCoord + outerCoilsDistance[direction] * dz[direction]).translate(0.5),
					r, g, b, 110, 0,
					AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
		}
		
		// draw connecting coils
		for (int i = 0; i < 5; i++) {		
			for (int j = i + 1; j < 6; j++) {
				// skip mirrored coils (removing the inner lines)
				if (dx[i] == -dx[j] && dy[i] == -dy[j] && dz[i] == -dz[j]) {
					continue;
				}
				
				// draw a random colored beam
				int mapIndex = worldObj.rand.nextInt(innerCoilColor_b.length);
				r = innerCoilColor_r[mapIndex];
				g = innerCoilColor_g[mapIndex];
				b = innerCoilColor_b[mapIndex];
				
				PacketHandler.sendBeamPacketToPlayersInArea(worldObj,
					new Vector3(xCoord + innerCoilsDistance * dx[i], yCoord + innerCoilsDistance * dy[i], zCoord + innerCoilsDistance * dz[i]).translate(0.5),
					new Vector3(xCoord + innerCoilsDistance * dx[j], yCoord + innerCoilsDistance * dy[j], zCoord + innerCoilsDistance * dz[j]).translate(0.5),
					r, g, b, 110, 0,
					AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
			}
		}
	}

	public void disableCloakingField() {
		setCoilsState(false);
		if (WarpDrive.cloaks.isAreaExists(worldObj, xCoord, yCoord, zCoord)) {
			WarpDrive.cloaks.removeCloakedArea(worldObj, xCoord, yCoord, zCoord);
			
			if (!soundPlayed) {
				soundPlayed = true;
				worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:decloak", 4F, 1F);
			}
		}
	}
	
	public boolean countBlocksAndConsumeEnergy() {
		int x, y, z, energyToConsume = 0;
		volume = 0;
		if (tier == 1) {// tier1 = gaz and air blocks don't count
			for (y = minY; y <= maxY; y++) {
				for (x = minX; x <= maxX; x++) {
					for(z = minZ; z <= maxZ; z++) {
						if (!worldObj.isAirBlock(x, y, z)) {
							volume++;
						} 
					}
				}
			}
			energyToConsume = volume * WarpDriveConfig.CLOAKING_TIER1_ENERGY_PER_BLOCK;
		} else {// tier2 = everything counts
			for (y = minY; y <= maxY; y++) {
				for (x = minX; x <= maxX; x++) {
					for(z = minZ; z <= maxZ; z++) {
						if (!worldObj.getBlock(x, y, z) .isAssociatedBlock(Blocks.air)) {
							volume++;
						} 
					}
				}
			}
			energyToConsume = volume * WarpDriveConfig.CLOAKING_TIER2_ENERGY_PER_BLOCK;
		}
		
		// WarpDrive.logger.info(this + " Consuming " + energyToConsume + " eU for " + blocksCount + " blocks");
		return consumeEnergy(energyToConsume, false);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.tier = tag.getByte("tier");
		this.isEnabled = tag.getBoolean("enabled");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("tier", tier);
		tag.setBoolean("enabled", isEnabled);
	}
	
	public boolean validateAssembly() {
		final int maxOuterCoilDistance = WarpDriveConfig.CLOAKING_MAX_FIELD_RADIUS - WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS; 
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		for (int direction = 0; direction < 6; direction++) {
			// check validity of inner coil
			int x = xCoord + innerCoilsDistance * dx[direction];
			int y = yCoord + innerCoilsDistance * dy[direction];
			int z = zCoord + innerCoilsDistance * dz[direction];
			if (!worldObj.getBlock(x, y, z).isAssociatedBlock(WarpDrive.blockCloakingCoil)) {
				return false;
			}
			
			// find closest outer coil
			int newCoilDistance = 0;
			for (int distance = 3; distance < maxOuterCoilDistance; distance++) {
				x += dx[direction];
				y += dy[direction];
				z += dz[direction];
				
				if (worldObj.getBlock(x, y, z).isAssociatedBlock(WarpDrive.blockCloakingCoil)) {
					newCoilDistance = distance;
					break;
				}
			}
			
			// disable previous outer coil, in case a different one was found
			if ( newCoilDistance != outerCoilsDistance[direction]
			  && outerCoilsDistance[direction] > 0) {
				int oldX = xCoord + outerCoilsDistance[direction] * dx[direction];
				int oldY = yCoord + outerCoilsDistance[direction] * dy[direction];
				int oldZ = zCoord + outerCoilsDistance[direction] * dz[direction];
				if (worldObj.getBlock(oldX, oldY, oldZ).isAssociatedBlock(WarpDrive.blockCloakingCoil)) {
					worldObj.setBlockMetadataWithNotify(oldX, oldY, oldZ, 0, 2);
				}
			}
			
			// check validity and save new coil position
			if (newCoilDistance <= 0) {
				outerCoilsDistance[direction] = 0;
				if (WarpDriveConfig.LOGGING_CLOAKING) {
					WarpDrive.logger.info("Invalid outercoil assembly at " + direction);
				}
				return false;
			}
			outerCoilsDistance[direction] = newCoilDistance;
		}
		
		// Check cloaking field parameters defining coils		
		minX =               xCoord - outerCoilsDistance[0] - WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS;
		maxX =               xCoord + outerCoilsDistance[1] + WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS;
		minY = Math.max(  0, yCoord - outerCoilsDistance[2] - WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS);
		maxY = Math.min(255, yCoord + outerCoilsDistance[3] + WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS);
		minZ =               zCoord - outerCoilsDistance[4] - WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS;
		maxZ =               zCoord + outerCoilsDistance[5] + WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS;
		return true;
	}

	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] tier(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			if (arguments.checkInteger(0) == 2) {
				tier = 2;
			} else {
				tier = 1;
			}
		}
		return new Integer[] { (int)tier };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] isAssemblyValid(Context context, Arguments arguments) {
		return new Object[] { (boolean)validateAssembly() };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] enable(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			isEnabled = arguments.checkBoolean(0);
		}
		return new Object[] { isEnabled };
	}

	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
    	String methodName = methodsArray[method];
    	if (methodName.equals("tier")) {
			if (arguments.length == 1) {
				if (toInt(arguments[0]) == 2) {
					tier = 2;
				} else {
					tier = 1;
				}
			}
			return new Integer[] { (int)tier };
			
    	} else if (methodName.equals("isAssemblyValid")) {
			return new Object[] { (boolean)validateAssembly() };
			
    	} else if (methodName.equals("getEnergyLevel")) {
			return getEnergyLevel();
			
    	} else if (methodName.equals("enable")) {
			if (arguments.length == 1) {
				isEnabled = toBool(arguments[0]);
			}
			return new Object[] { isEnabled };
		}
		
		return null;
	}
	
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.CLOAKING_MAX_ENERGY_STORED;
	}
    
    @Override
    public boolean canInputEnergy(ForgeDirection from) {
    	return true;
    }
}
