package cr0s.warpdrive.block;

import java.util.LinkedList;
import java.util.List;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.config.WarpDriveConfig;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


// Abstract class to manage laser mediums
public abstract class TileEntityAbstractLaser extends TileEntityAbstractInterfaced {
	// direction of the laser medium stack
	protected ForgeDirection directionLaserMedium = ForgeDirection.UNKNOWN;
	protected ForgeDirection[] directionsValidLaserMedium = ForgeDirection.VALID_DIRECTIONS;
	protected int countMaxLaserMediums = 0;
	
	private final int updateInterval_ticks = 20 * WarpDriveConfig.SHIP_CONTROLLER_UPDATE_INTERVAL_SECONDS;
	private int updateTicks = updateInterval_ticks;
	private int bootTicks = 20;
	
	public TileEntityAbstractLaser() {
		super();
		
		addMethods(new String[] {
				"energy",
				"laserMediumDirection"
		});
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		
		// accelerate update ticks during boot
		if (bootTicks > 0) {
			bootTicks--;
			if (directionLaserMedium == ForgeDirection.UNKNOWN) {
				updateTicks = 1;
			}
		}
		updateTicks--;
		if (updateTicks <= 0) {
			updateTicks = updateInterval_ticks;
			
			updateLaserMediumDirection();
		}
	}
	
	private void updateLaserMediumDirection() {
		for(ForgeDirection direction : directionsValidLaserMedium) {
			TileEntity  tileEntity = worldObj.getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
			if (tileEntity != null && tileEntity instanceof TileEntityLaserMedium) {
				directionLaserMedium = direction;
				return;
			}
		}
		directionLaserMedium = ForgeDirection.UNKNOWN;
		return;
	}
	
	protected int getEnergyStored() {
		return consumeCappedEnergyFromLaserMediums(Integer.MAX_VALUE, true);
	}
	
	protected boolean consumeEnergyFromLaserMediums(final int amount, final boolean simulate) {
		return amount <= consumeCappedEnergyFromLaserMediums(amount, simulate);
	}
	
	protected int consumeCappedEnergyFromLaserMediums(final int amount, final boolean simulate) {
		if (directionLaserMedium == ForgeDirection.UNKNOWN) {
			return 0;
		}
		
		// Primary scan of all laser mediums
		int totalEnergy = 0;
		int count = 1;
		List<TileEntityLaserMedium> laserMediums = new LinkedList();
		for (; count <= countMaxLaserMediums; count++) {
			TileEntity tileEntity = worldObj.getTileEntity(
					xCoord + count * directionLaserMedium.offsetX,
					yCoord + count * directionLaserMedium.offsetY,
					zCoord + count * directionLaserMedium.offsetZ);
			if (!(tileEntity instanceof TileEntityLaserMedium)) {
				break;
			}
			laserMediums.add((TileEntityLaserMedium) tileEntity);
			totalEnergy += ((TileEntityLaserMedium) tileEntity).getEnergyStored();
		}
		count--;
		if (count == 0) {
			return 0;
		}
		if (simulate) {
			return totalEnergy;
		}
		
		// Compute average energy to get per laser medium, capped at its capacity
		int energyAverage = amount / count;
		int energyLeftOver = amount - energyAverage * count;
		if (energyAverage >= WarpDriveConfig.LASER_MEDIUM_MAX_ENERGY_STORED) {
			energyAverage = WarpDriveConfig.LASER_MEDIUM_MAX_ENERGY_STORED;
			energyLeftOver = 0;
		}
		
		// Secondary scan for laser medium below the required average
		for (TileEntityLaserMedium laserMedium : laserMediums) {
			int energyStored = laserMedium.getEnergyStored();
			if (energyStored < energyAverage) {
				energyLeftOver += energyAverage - energyStored;
			}
		}
		
		// Third and final pass for energy consumption
		int energyTotalConsumed = 0;
		for (TileEntityLaserMedium laserMedium : laserMediums) {
			int energyStored = laserMedium.getEnergyStored();
			int energyToConsume = Math.min(energyStored, energyAverage + energyLeftOver);
			energyLeftOver -= Math.max(0, energyToConsume - energyAverage);
			laserMedium.consumeEnergy(energyToConsume, simulate);
			energyTotalConsumed += energyToConsume;
		}
		return energyTotalConsumed;
	}
	
	protected Object[] energy() {
		if (directionLaserMedium == ForgeDirection.UNKNOWN) {
			return new Object[] { 0, 0 };
		} else {
			int energyStored = 0;
			int energyStoredMax = 0;
			int count = 1;
			List<TileEntityLaserMedium> laserMediums = new LinkedList();
			for (; count <= countMaxLaserMediums; count++) {
				TileEntity tileEntity = worldObj.getTileEntity(
						xCoord + count * directionLaserMedium.offsetX,
						yCoord + count * directionLaserMedium.offsetY,
						zCoord + count * directionLaserMedium.offsetZ);
				if (!(tileEntity instanceof TileEntityLaserMedium)) {
					break;
				}
				laserMediums.add((TileEntityLaserMedium) tileEntity);
				energyStored += ((TileEntityLaserMedium) tileEntity).getEnergyStored();
				energyStoredMax += ((TileEntityLaserMedium) tileEntity).getMaxEnergyStored();
			}
			return new Object[] { energyStored, energyStoredMax };
		}
	}
	
	protected Object[] laserMediumDirection() {
		return new Object[] { directionLaserMedium.name(), directionLaserMedium.offsetX, directionLaserMedium.offsetY, directionLaserMedium.offsetZ };
	}
	
	// OpenComputers callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] energy(Context context, Arguments arguments) {
		return energy();
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] laserMediumDirection(Context context, Arguments arguments) {
		return laserMediumDirection();
	}
	
	// ComputerCraft methods
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = getMethodName(method);
		
		if (methodName.equals("energy")) {
			return energy();
		} else if (methodName.equals("laserMediumDirection")) {
			return laserMediumDirection();
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
}
