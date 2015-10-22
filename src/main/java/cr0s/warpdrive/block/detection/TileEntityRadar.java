package cr0s.warpdrive.block.detection;

import java.util.ArrayList;
import java.util.Arrays;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.StarMapEntry;
import cr0s.warpdrive.data.VectorI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityRadar extends TileEntityAbstractEnergy {
	private ArrayList<StarMapEntry> results;
	
	// radius defined for next scan
	private int radius = 0;
	
	// radius for ongoing scan
	private int scanningRadius = 0;
	private int scanningDuration_ticks = 0;
	private int scanning_ticks = 0;
	
	public TileEntityRadar() {
		super();
		peripheralName = "warpdriveRadar";
		addMethods(new String[] {
				"radius",
				"start",
				"getResultsCount",
				"getResult"
			});
		CC_scripts = Arrays.asList("scan", "ping");
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		
		try {
			if (getBlockMetadata() == 2) {
				scanning_ticks++;
				if (scanning_ticks > scanningDuration_ticks) {
					results = WarpDrive.starMap.radarScan(this, scanningRadius);
					if (WarpDriveConfig.LOGGING_RADAR) {
						WarpDrive.logger.info(this + " Scan found " + results.size() + " results in " + scanningRadius + " radius...");
					}
					worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
					scanning_ticks = 0;
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
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
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] radius(Context context, Arguments arguments) {
		return radius(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] start(Context context, Arguments arguments) {
		return start(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getResultsCount(Context context, Arguments arguments) {
		if (results != null) {
			return new Integer[] { results.size() };
		}
		return new Integer[] { -1 };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getResult(Context context, Arguments arguments) {
		return getResult(argumentsOCtoCC(arguments));
	}
	
	private Object[] radius(Object[] arguments) {
		if (arguments.length == 1 && getBlockMetadata() != 2) {
			int newRadius;
			try {
				newRadius = toInt(arguments[0]);
			} catch(Exception exception) {
				return new Integer[] { radius };
			}
			radius = clamp(0, 10000, newRadius);
		}
		return new Integer[] { radius };
	}
	
	private Object[] start(Object[] arguments) {
		// always clear results
		results = null;
		
		// validate parameters
		if (radius <= 0 || radius > 10000) {
			radius = 0;
			return new Object[] { false, "Invalid radius" };
		}
		if (!consumeEnergy(Math.max(radius, 100) * Math.max(radius, 100), false)) {
			return new Object[] { false, "Insufficient energy" };
		}
		
		// Begin searching
		scanningRadius = radius;
		scanningDuration_ticks = (20 * ((scanningRadius / 1000) + 1));
		scanning_ticks = 0;
		if (getBlockMetadata() != 2) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 1 + 2);
		}
		return new Object[] { true };
	}
	
	private Object[] getResult(Object[] arguments) {
		if (arguments.length == 1 && (results != null)) {
			int index;
			try {
				index = toInt(arguments[0]);
			} catch(Exception exception) {
				return new Object[] { false, COMPUTER_ERROR_TAG, null, 0, 0, 0 };
			}
			if (index >= 0 && index < results.size()) {
				StarMapEntry result = results.get(index);
				if (result != null) {
					VectorI spaceCoordinates = result.getSpaceCoordinates();
					return new Object[] { true, "SHIP", result.name, spaceCoordinates.x, spaceCoordinates.y, spaceCoordinates.z };
				}
			}
		}
		return new Object[] { false, COMPUTER_ERROR_TAG, null, 0, 0, 0 };
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {
		super.attach(computer);
		if (getBlockMetadata() == 0) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
		}
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {
		super.detach(computer);
		// worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = getMethodName(method);
		
		if (methodName.equals("radius")) {
			return radius(arguments);
			
		} else if (methodName.equals("start")) {
			return start(arguments);
			
		} else if (methodName.equals("getResultsCount")) {
			if (results != null) {
				return new Integer[] { results.size() };
			}
			return new Integer[] { -1 };
			
		} else if (methodName.equals("getResult")) {
			return getResult(arguments);
			
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.RADAR_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}
}
