package cr0s.WarpDrive.machines;

import java.util.HashMap;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDriveCore.IBlockUpdateDetector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

public abstract class WarpEnergyTE extends WarpTE implements IEnergyHandler, IEnergySink, IEnergySource, IBlockUpdateDetector {
	protected boolean addedToEnergyNet = false;
	protected int energyStored_internal = 0;
	private static final double EU_PER_INTERNAL = 1.0D;
	private static final double RF_PER_INTERNAL = 1800.0D / 437.5D;
	
	private int scanTickCount = -1;
	private IEnergyHandler[] TE_energyHandlers = new IEnergyHandler[ForgeDirection.VALID_DIRECTIONS.length];
	
	// WarpDrive methods
	protected static int convertInternalToRF(int energy) {
		return (int)Math.round(energy * RF_PER_INTERNAL);
	}
	
	protected static int convertRFtoInternal(int energy) {
		return (int)Math.round(energy / RF_PER_INTERNAL);
	}
	
	protected static double convertInternalToEU(int energy) {
		return Math.round(energy * EU_PER_INTERNAL);
	}
	
	protected static int convertEUtoInternal(double amount) {
		return (int)Math.round(amount / EU_PER_INTERNAL);
	}

	
	public int getEnergyStored() {
		return energyStored_internal;
	}
	
	// Methods to override
	public int getMaxEnergyStored() {
		return 0;
	}
	
	public int getPotentialEnergyOutput() {
		return 0;
	}
	
	protected void energyOutputDone(int energyOutput) {
		return;
	}
	
	public boolean canInputEnergy(ForgeDirection from) {
		return false;
	}
	
	public boolean canOutputEnergy(ForgeDirection to) {
		return false;
	}
	
	
	protected boolean consumeEnergy(int amount, boolean simulate) {
		if (getEnergyStored() >= amount) {
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
    
    
	// Minecraft overrides
    @Override
    public void updateEntity() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        // IndustrialCraft2
        if (!addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }
        
        // Thermal Expansion
		scanTickCount++;
		if(scanTickCount >= 20) {
			scanTickCount = 0;
			scanForEnergyHandlers();
		}
		outputEnergy();
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
    
    
    // IndustrialCraft IEnergySink interface
    @Override
    public double demandedEnergyUnits() {
        return Math.max(0.0D, convertInternalToEU(getMaxEnergyStored() - energyStored_internal));
    }
    
    @Override
    public double injectEnergyUnits(ForgeDirection from, double amount) {
        double leftover = 0;
        energyStored_internal += Math.round(amount) / EU_PER_INTERNAL;

        if (energyStored_internal > getMaxEnergyStored()) {
            leftover = (energyStored_internal - getMaxEnergyStored());
            energyStored_internal = getMaxEnergyStored();
        }

        return leftover * EU_PER_INTERNAL;
    }
    
    @Override
    public int getMaxSafeInput() {
        return 0;
    }
    
    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection from) {
        return getMaxSafeInput() != 0 && canInputEnergy(from);
    }
    
    // IndustrialCraft IEnergySource interface
	@Override
	public double getOfferedEnergy() {
		return convertEUtoInternal(getPotentialEnergyOutput());
	}
	
	@Override
	public void drawEnergy(double amount) {
		energyOutputDone(convertEUtoInternal(amount));
	}
	
	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection to) {
		return canOutputEnergy(to);
	}
	
    
    // ThermalExpansion IEnergyHandler interface
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		if (!canInputEnergy(from)) {
			return 0;
		}
		
		int maxStored = getMaxEnergyStored(from);
		if (maxStored == 0) {
			return 0;
		}
		int energyStored = getEnergyStored(from);
		
		int toAdd = Math.min(maxReceive, maxStored - energyStored);
		if (!simulate) {
			energyStored_internal = Math.min(getMaxEnergyStored(), energyStored_internal + convertInternalToRF(toAdd));
		}
		
		return toAdd;
	}
	
	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		if (!canOutputEnergy(from)) {
			return 0;
		}
		
		int energyExtracted = Math.min(convertRFtoInternal(maxExtract), getPotentialEnergyOutput());
		if (!simulate) {
			energyOutputDone(energyExtracted);
		}
		return energyExtracted;
	}
	
	@Override
	public boolean canInterface(ForgeDirection from) {
		return (getMaxEnergyStored() != 0) && (canInputEnergy(from) || canOutputEnergy(from)); // FIXME deadlock risk
	}
	
	@Override
	public int getEnergyStored(ForgeDirection from) {
		if (canInterface(from)) {
			return convertInternalToRF(getEnergyStored());
		}
		return 0;
	}
	
	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return canInterface(from) ? convertInternalToRF(getMaxEnergyStored()) : 0;
	}
	
	
	// WarpDrive overrides for Thermal Expansion FIXME: are we really supposed to do this?
	private void outputEnergy(ForgeDirection from, IEnergyHandler ieh) {
		if (ieh == null || worldObj.getBlockTileEntity(xCoord + from.offsetX, yCoord + from.offsetY, zCoord + from.offsetZ) == null) {
			return;
		}
		int potentialEnergyOutput = getPotentialEnergyOutput();
		if (potentialEnergyOutput > 0) {
			int energyToOutput = ieh.receiveEnergy(from.getOpposite(), convertInternalToRF(potentialEnergyOutput), true);
			if (energyToOutput > 0) {
				int energyOutputed = ieh.receiveEnergy(from.getOpposite(), energyToOutput, false);
				energyOutputDone(energyOutputed);
				// WarpDrive.debugPrint(this + " output " + energyOutput + " RF, down to " + containedEnergy);
			}
		}
	}
	
	private void outputEnergy() {
		for(ForgeDirection from: ForgeDirection.VALID_DIRECTIONS) {
			if (TE_energyHandlers[from.ordinal()] != null) {
				outputEnergy(from, TE_energyHandlers[from.ordinal()]);
			}
		}
	}
	
	
	// Forge overrides
	@Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energyStored_internal = tag.getInteger("energy");
        if (energyStored_internal > getMaxEnergyStored()) {
        	energyStored_internal = getMaxEnergyStored();
        }
    }
	
    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (energyStored_internal < 0) {
        	energyStored_internal = 0;
        }
        tag.setInteger("energy", this.energyStored_internal);
    }
    
    // WarpDrive overrides
	@Override
	public void updatedNeighbours() {
		scanForEnergyHandlers();
	}
	
	public void scanForEnergyHandlers() {
		for(ForgeDirection from : ForgeDirection.VALID_DIRECTIONS) {
			boolean iehFound = false;
			if (canInterface(from)) {
				TileEntity te = worldObj.getBlockTileEntity(xCoord + from.offsetX, yCoord + from.offsetY, zCoord + from.offsetZ);
				if (te != null && te instanceof IEnergyHandler) {
					IEnergyHandler ieh = (IEnergyHandler)te;
					if (ieh.canInterface(from.getOpposite())) {
						iehFound = true;
						TE_energyHandlers[from.ordinal()] = ieh;
					}
				}
			}
			if (!iehFound) {
				TE_energyHandlers[from.ordinal()] = null;
			}
		}
	}
}