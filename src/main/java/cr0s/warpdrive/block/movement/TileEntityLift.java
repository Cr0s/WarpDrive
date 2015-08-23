package cr0s.warpdrive.block.movement;

import java.util.List;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.conf.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityLift extends TileEntityAbstractEnergy {
	private static final int MODE_REDSTONE = -1;
	private static final int MODE_INACTIVE = 0;
	private static final int MODE_UP = 1;
	private static final int MODE_DOWN = 2;
	
	private int firstUncoveredY;
	private int mode = MODE_INACTIVE;
	private boolean isEnabled = false;
	private boolean computerEnabled = true;
	private int computerMode = MODE_REDSTONE;
	
	private int tickCount = 0;
	
	public TileEntityLift() {
		super();
		IC2_sinkTier = 2;
		IC2_sourceTier = 2;
		peripheralName = "warpdriveLift";
		methodsArray = new String[] {
				"getEnergyLevel",
				"mode",
				"active" };
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		
		tickCount++;
		if (tickCount >= WarpDriveConfig.LIFT_UPDATE_INTERVAL_TICKS) {
			tickCount = 0;
			
			// Switching mode
			if (  computerMode == MODE_DOWN
			  || (computerMode == MODE_REDSTONE && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))) {
				mode = MODE_DOWN;
			} else {
				mode = MODE_UP;
			}
			
			isEnabled = computerEnabled && isPassableBlock(yCoord + 1)
					&& isPassableBlock(yCoord + 2)
					&& isPassableBlock(yCoord - 1)
					&& isPassableBlock(yCoord - 2);
			
			if (getEnergyStored() < WarpDriveConfig.LIFT_ENERGY_PER_ENTITY
					|| !isEnabled) {
				mode = MODE_INACTIVE;
				if (getBlockMetadata() != 0) {
					worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord,
							0, 2); // disabled
				}
				return;
			}
			
			if (getBlockMetadata() != mode) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord,
						mode, 2); // current mode
			}
			
			// Launch a beam: search non-air blocks under lift
			for (int ny = yCoord - 2; ny > 0; ny--) {
				if (!isPassableBlock(ny)) {
					firstUncoveredY = ny + 1;
					break;
				}
			}
			
			if (yCoord - firstUncoveredY >= 2) {
				if (mode == MODE_UP) {
					PacketHandler.sendBeamPacket(worldObj, new Vector3(
							xCoord + 0.5D, firstUncoveredY, zCoord + 0.5D),
							new Vector3(xCoord + 0.5D, yCoord, zCoord + 0.5D),
							0f, 1f, 0f, 40, 0, 100);
				} else if (mode == MODE_DOWN) {
					PacketHandler.sendBeamPacket(worldObj, new Vector3(
							xCoord + 0.5D, yCoord, zCoord + 0.5D), new Vector3(
							xCoord + 0.5D, firstUncoveredY, zCoord + 0.5D), 0f,
							0f, 1f, 40, 0, 100);
				}
				
				liftEntity();
			}
		}
	}
	
	private boolean isPassableBlock(int yPosition) {
		Block block = worldObj.getBlock(xCoord, yPosition, zCoord);
		//TODO: Make configurable or less specific
		return block.isAssociatedBlock(Blocks.air)
			|| block.isAssociatedBlock(Blocks.wall_sign)
			|| block.isAssociatedBlock(Blocks.standing_sign)
			|| worldObj.isAirBlock(xCoord, yPosition, zCoord);
	}
	
	private void liftEntity() {
		final double CUBE_RADIUS = 0.4;
		double xmax, zmax;
		double xmin, zmin;
		
		xmin = xCoord + 0.5 - CUBE_RADIUS;
		xmax = xCoord + 0.5 + CUBE_RADIUS;
		zmin = zCoord + 0.5 - CUBE_RADIUS;
		zmax = zCoord + 0.5 + CUBE_RADIUS;
		
		// Lift up
		if (mode == MODE_UP) {
			AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xmin, firstUncoveredY, zmin, xmax, yCoord, zmax);
			List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);
			if (list != null) {
				for (Object o : list) {
					if ( o != null
					  && o instanceof EntityLivingBase
					  && consumeEnergy(WarpDriveConfig.LIFT_ENERGY_PER_ENTITY, true)) {
						((EntityLivingBase) o).setPositionAndUpdate(xCoord + 0.5D, yCoord + 1.0D, zCoord + 0.5D);
						PacketHandler.sendBeamPacket(worldObj,
								new Vector3(xCoord + 0.5D, firstUncoveredY, zCoord + 0.5D),
								new Vector3(xCoord + 0.5D, yCoord, zCoord + 0.5D), 1F, 1F, 0F, 40, 0, 100);
						worldObj.playSoundEffect(xCoord + 0.5D, yCoord, zCoord + 0.5D, "warpdrive:hilaser", 4F, 1F);
						consumeEnergy(WarpDriveConfig.LIFT_ENERGY_PER_ENTITY, false);
					}
				}
			}
		} else if (mode == MODE_DOWN) {
			AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xmin,
					Math.min(firstUncoveredY + 4.0D, yCoord), zmin, xmax, yCoord + 2.0D, zmax);
			List list = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);
			if (list != null) {
				for (Object o : list) {
					if ( o != null
					  && o instanceof EntityLivingBase
					  && consumeEnergy(WarpDriveConfig.LIFT_ENERGY_PER_ENTITY, true)) {
						((EntityLivingBase) o).setPositionAndUpdate(xCoord + 0.5D, firstUncoveredY, zCoord + 0.5D);
						PacketHandler.sendBeamPacket(worldObj,
								new Vector3(xCoord + 0.5D, yCoord, zCoord + 0.5D),
								new Vector3(xCoord + 0.5D, firstUncoveredY, zCoord + 0.5D), 1F, 1F, 0F, 40, 0, 100);
						worldObj.playSoundEffect(xCoord + 0.5D, yCoord, zCoord + 0.5D, "warpdrive:hilaser", 4F, 1F);
						consumeEnergy(WarpDriveConfig.LIFT_ENERGY_PER_ENTITY, false);
					}
				}
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
	}
	
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.LIFT_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] mode(Context context, Arguments arguments) {
		return mode(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] active(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			computerEnabled = arguments.checkBoolean(0);
		}
		return new Object[] { computerEnabled ? false : isEnabled };
	}
	
	private Object[] mode(Object[] arguments) {
		if (arguments.length == 1) {
			if (arguments[0].toString().equals("up")) {
				computerMode = MODE_UP;
			} else if (arguments[0].toString().equals("down")) {
				computerMode = MODE_DOWN;
			} else {
				computerMode = MODE_REDSTONE;
			}
		}
		switch (computerMode) {
		case -1:
			return new Object[] { "redstone" };
		case 1:
			return new Object[] { "up" };
		case 2:
			return new Object[] { "down" };
		}
		return null;
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = methodsArray[method];
		if (methodName.equals("getEnergyLevel")) {
			return getEnergyLevel();
			
		} else if (methodName.equals("mode")) {
			return mode(arguments);
			
		} else if (methodName.equals("active")) {
			if (arguments.length == 1) {
				computerEnabled = toBool(arguments);
			}
			return new Object[] { computerEnabled ? false : isEnabled };
		}
		return null;
	}
}
