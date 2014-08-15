package cr0s.WarpDrive.machines;

import cr0s.WarpDrive.WarpDriveConfig;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPowerStore extends WarpEnergyTE implements IPeripheral {
	private String[] methodArray = {
			"energy"
	};
	
	@Override
	public int getPotentialEnergyOutput() {
		return getEnergyStored();
	}
	
	@Override
	protected void energyOutputDone(int energyOutput) {
		consumeEnergy(energyOutput, false);
	}
	
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.PS_MAX_ENERGY;
	}
	
	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}
	
	@Override
	public boolean canOutputEnergy(ForgeDirection to) {
		return true;
	}
	
	// ComputerCraft
	@Override
	public String getType() {
		return "warpdrivePowerStore";
	}
	
	@Override
	public String[] getMethodNames() {
		return methodArray;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
		String name = methodArray[method];
		if (name == "energy") {
			return getEnergyObject();
		}
		return null;
	}
	
	@Override
	public void attach(IComputerAccess computer) {
		// nothing to see here
	}
	
	@Override
	public void detach(IComputerAccess computer) {
		// nothing to see here
	}
	
	@Override
	public boolean equals(IPeripheral other) {
		return this == other;
	}
}