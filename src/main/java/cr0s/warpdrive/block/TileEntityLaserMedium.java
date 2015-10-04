package cr0s.warpdrive.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.config.WarpDriveConfig;

public class TileEntityLaserMedium extends TileEntityAbstractEnergy {
	private int ticks = 0;
	
	public TileEntityLaserMedium() {
		peripheralName = "warpdriveLaserMedium";
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
			
			int metadata = Math.max(0, Math.min(7, Math.round((getEnergyStored() * 8) / getMaxEnergyStored())));
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
		return WarpDriveConfig.LASER_MEDIUM_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}
}
