package cr0s.warpdrive.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.conf.WarpDriveConfig;

public class TileEntityLaserMedium extends TileEntityAbstractEnergy {
	private int ticks = 0;

	public TileEntityLaserMedium() {
		peripheralName = "warpdriveLaserMedium";
		methodsArray = new String[] {
			"getEnergyLevel"
		};
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();

		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}

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
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}
}
