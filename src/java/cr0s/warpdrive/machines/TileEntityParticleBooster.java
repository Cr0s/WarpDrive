package cr0s.warpdrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import cr0s.warpdrive.*;

public class TileEntityParticleBooster extends WarpEnergyTE {
	private int ticks = 0;

	@Override
	public void updateEntity() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		super.updateEntity();

		ticks++;
		if (ticks > 20) {
			ticks = 0;

			int metadata = Math.max(0, Math.min(10, Math.round((getEnergyStored() * 10) / getMaxEnergyStored())));
			if (getBlockMetadata() != metadata) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 3);
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

	// IEnergySink methods implementation
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.PB_MAX_ENERGY_VALUE;
	}

	@Override
	public int getMaxSafeInput() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}
}
