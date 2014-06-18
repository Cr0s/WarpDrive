package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.WarpDriveConfig;
import net.minecraftforge.common.ForgeDirection;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityParticleBooster extends WarpTE
{

    int ticks = 0;

    @Override
    public void updateEntity()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return;
        }

        if (++ticks > 40)
        {
            ticks = 0;
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 10* (getEnergyStored() / WarpDriveConfig.PB_MAX_ENERGY_VALUE), 2);
        }
    }

    @Override
    public int getMaxEnergyStored()
    {
    	return WarpDriveConfig.PB_MAX_ENERGY_VALUE;
    }
}
