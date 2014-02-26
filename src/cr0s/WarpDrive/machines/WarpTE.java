package cr0s.WarpDrive.machines;

import net.minecraft.tileentity.TileEntity;

public abstract class WarpTE extends TileEntity
{
	protected int toInt(double d)
	{
		return (int) Math.round(d);
	}
	
	protected int toInt(Object o)
	{
		return toInt(toDouble(o));
	}
	
	protected double toDouble(Object o)
	{
		return Double.parseDouble(o.toString());
	}
	
	protected boolean toBool(Object o)
	{
		if(o.toString() == "true" || o.toString() == "1.0" || o.toString() == "1")
			return true;
		return false;
	}
	
	protected int clamp(int a,int min,int max)
	{
		return Math.min(max, Math.max(a,min));
	}
	
	protected double clamp(double a,double min,double max)
	{
		return Math.min(max, Math.max(a,min));
	}
}
