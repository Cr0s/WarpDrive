package cr0s.WarpDrive.machines;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

public abstract class WarpEnergyTE extends WarpTE implements IEnergyHandler, IEnergySink
{
	protected boolean addedToEnergyNet = false;
	protected int energyStored_internal = 0;
	private final double INTERNAL_TO_EU = 1.0D;
	private final double INTERNAL_TO_RF = 437.5D / 1800.0D;
	
	// WarpDrive methods
	public int getEnergyStored() {
		return energyStored_internal;
	}
	
	public int getMaxEnergyStored() {
		return 0;
	}
	
	protected boolean consumeEnergy(int amount, boolean simulate) {
		if(getEnergyStored() >= amount) {
			if (!simulate) {
				energyStored_internal -= amount;
			}
			return true;
		}
		return false;
	}
	
	protected int consumeAllEnergy() {
		int temp = energyStored_internal;
		energyStored_internal = 0;
		return temp;
	}
	
	public Object[] getEnergyObject() {
		return new Object[]{ getEnergyStored(), getMaxEnergyStored() };
	}

    public String getStatus() {
    	if (getMaxEnergyStored() != 0) {
    		return getBlockType().getLocalizedName() + " energy level is " + getEnergyStored() + "/" + getMaxEnergyStored() + " EU.";
    	} else {
    		return getBlockType().getLocalizedName();
    	}
    }

	// Common overrides
    @Override
    public void updateEntity() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        if (!addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }
    }

    @Override
    public void onChunkUnload() {
        if (addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
        
        super.onChunkUnload();
    }

    @Override
    public void invalidate() {
        if (addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
        
        super.invalidate();
    }

    // IndustrialCraft overrides
    @Override
    public double demandedEnergyUnits() {
        return Math.max(0.0D, getMaxEnergyStored() - energyStored_internal) * INTERNAL_TO_EU;
    }

    @Override
    public double injectEnergyUnits(ForgeDirection directionFrom, double amount) {
        double leftover = 0;
        energyStored_internal += Math.round(amount) / INTERNAL_TO_EU;

        if (energyStored_internal > getMaxEnergyStored()) {
            leftover = (energyStored_internal - getMaxEnergyStored());
            energyStored_internal = getMaxEnergyStored();
        }

        return leftover * INTERNAL_TO_EU;
    }

    @Override
    public int getMaxSafeInput() {
        return 0;
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return getMaxSafeInput() != 0;
    }

    // ThermalExpansion overrides
	@Override
	public int getEnergyStored(ForgeDirection from) {
		return (int)(getEnergyStored() * INTERNAL_TO_RF);
	}
	
	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return (int)(getMaxEnergyStored() * INTERNAL_TO_RF);
	}
	
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		int maxStored = (int)(getMaxEnergyStored() * INTERNAL_TO_RF);
		if (maxStored == 0) {
			return 0;
		}
		
		int toAdd = Math.min(maxReceive, maxStored - (int)(getEnergyStored() * INTERNAL_TO_RF));
		if (!simulate) {
			energyStored_internal += toAdd / INTERNAL_TO_RF;
		}
		
		return toAdd;
	}
	
	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		return 0;
	}
	
	@Override
	public boolean canInterface(ForgeDirection from) {
		return (getMaxEnergyStored() != 0);
	}
	
	// Forge overrides
	@Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.energyStored_internal = tag.getInteger("energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("energy", this.energyStored_internal);
    }
}