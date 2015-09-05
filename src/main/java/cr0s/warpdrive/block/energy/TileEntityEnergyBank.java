package cr0s.warpdrive.block.energy;

import net.minecraftforge.common.util.ForgeDirection;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;

public class TileEntityEnergyBank extends TileEntityAbstractEnergy {

	public TileEntityEnergyBank() {
		super();
		IC2_sinkTier = 0;
		IC2_sourceTier = 0;
		peripheralName = "warpdriveEnergyBank";
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
		return WarpDriveConfig.ENERGY_BANK_MAX_ENERGY_STORED;
	}

	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public boolean canOutputEnergy(ForgeDirection to) {
		return true;
	}
}