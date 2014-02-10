package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import net.minecraftforge.common.ForgeDirection;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityParticleBooster extends TileEntity implements IEnergySink
{
    public boolean addedToEnergyNet = false;

    private int currentEnergyValue = 0;

    int ticks = 0;

    @Override
    public void updateEntity()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return;
        }

        if (!addedToEnergyNet && !this.tileEntityInvalid)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }

        if (++ticks > 40)
        {
            ticks = 0;
            currentEnergyValue = Math.min(currentEnergyValue, WarpDriveConfig.i.PB_MAX_ENERGY_VALUE);
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, currentEnergyValue / 10000, 2);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        this.currentEnergyValue = tag.getInteger("energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger("energy", this.getCurrentEnergyValue());
    }

    // IEnergySink methods implementation
    @Override
    public double demandedEnergyUnits()
    {
        return (WarpDriveConfig.i.PB_MAX_ENERGY_VALUE - currentEnergyValue);
    }

    @Override
    public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
    {
        double leftover = 0;
        currentEnergyValue += Math.round(amount);

        if (getCurrentEnergyValue() > WarpDriveConfig.i.PB_MAX_ENERGY_VALUE)
        {
            leftover = (getCurrentEnergyValue() - WarpDriveConfig.i.PB_MAX_ENERGY_VALUE);
            currentEnergyValue = WarpDriveConfig.i.PB_MAX_ENERGY_VALUE;
        }

        return leftover;
    }

    @Override
    public int getMaxSafeInput()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
    {
        return true;
    }

    /**
     * @return the currentEnergyValue
     */
    public int getCurrentEnergyValue()
    {
        return currentEnergyValue;
    }

    public boolean consumeEnergy(int amount)
    {
        if (currentEnergyValue - amount < 0)
        {
            return false;
        }

        currentEnergyValue -= amount;
        return true;
    }

    public int collectAllEnergy()
    {
        int energy = currentEnergyValue;
        currentEnergyValue = 0;
        return energy;
    }

    @Override
    public void onChunkUnload()
    {
        if (addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
    }

    @Override
    public void invalidate()
    {
        if (addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }

        super.invalidate();
    }
}
