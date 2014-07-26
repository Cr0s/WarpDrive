package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import cr0s.WarpDrive.*;

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
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, Math.max(0, Math.min(10, Math.round((getEnergyStored() * 10) / WarpDriveConfig.PB_MAX_ENERGY_VALUE))), 2);
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
}
