package cr0s.WarpDrive.machines;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import cofh.api.energy.IEnergyHandler;

public abstract class WarpEnergyTE extends TileEntity implements IEnergyHandler
{
	protected int energyStoredAmount=0;
	
	
	public int getEnergyStored()
	{
		return energyStoredAmount;
	}
	
	public int getMaxEnergyStored()
	{
		return 0;
	}
	
	protected boolean removeEnergy(int amount,boolean simulate)
	{
		if(getEnergyStored() >= amount)
		{
			if(!simulate)
				energyStoredAmount -= amount;
			return true;
		}
		return false;
	}
	
	public Object[] getEnergyObject()
	{
		return new Object[]{ getEnergyStored(),getMaxEnergyStored() };
	}
	
	protected int removeAllEnergy()
	{
		int temp = energyStoredAmount;
		energyStoredAmount = 0;
		return temp;
	}
	
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return getEnergyStored();
	}
	
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return getMaxEnergyStored();
	}
	
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		int maxStored = getMaxEnergyStored();
		if(maxStored == 0)
			return 0;
		
		int toAdd = Math.min(maxReceive, maxStored - getEnergyStored());
		if(!simulate)
			energyStoredAmount += toAdd;
		
		return toAdd;
	}
	
	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}
	
	@Override
	public boolean canInterface(ForgeDirection from)
	{
		return (getMaxEnergyStored()!=0);
	}
	
	//Better save and load the amounts of energy stored
	@Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        this.energyStoredAmount = tag.getInteger("energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger("energy", this.energyStoredAmount);
    }
}
