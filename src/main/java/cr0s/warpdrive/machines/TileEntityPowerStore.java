package cr0s.warpdrive.machines;

import net.minecraftforge.common.util.ForgeDirection;
import cr0s.warpdrive.WarpDriveConfig;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityPowerStore extends WarpEnergyTE {

	public TileEntityPowerStore() {
		super();
		peripheralName = "warpdrivePowerStore";
		methodsArray = new String[] {
				"getEnergyLevel"
		};
	}

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

	// ComputerCraft IPeripheral methods implementation
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = methodsArray[method];
		if (methodName == "getEnergyLevel") {
			return getEnergyLevel();
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
	public int getSinkTier() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSourceTier() {
		// TODO Auto-generated method stub
		return 0;
	}
}