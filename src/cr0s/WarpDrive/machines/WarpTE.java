package cr0s.WarpDrive.machines;

import net.minecraft.world.World;
import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;

public abstract class WarpTE extends WarpEnergyTE
{
	
	private static final Vector3[] adjacentSideOffsets = new Vector3[6];
	static
	{
		adjacentSideOffsets[0] = new Vector3(0,0, 1);
		adjacentSideOffsets[1] = new Vector3(0,0,-1);
		adjacentSideOffsets[2] = new Vector3(0, 1,0);
		adjacentSideOffsets[3] = new Vector3(0,-1,0);
		adjacentSideOffsets[4] = new Vector3( 1,0,0);
		adjacentSideOffsets[5] = new Vector3(-1,0,0);
	}
	
	public static final Vector3[] getAdjacentSideOffsets()
	{
		return adjacentSideOffsets;
	}
	
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
		if(o == null)
			return false;
		
		if(o instanceof Boolean)
			return ((Boolean) o);
		
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
	
	protected boolean isSpaceDim()
	{
		return worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID;
	}
	
	protected boolean isHyperSpaceDim()
	{
		 return worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID;
	}
	
	protected boolean isASpaceDim()
	{
		return isSpaceDim() || isHyperSpaceDim();
	}
	
	protected boolean isAir(World wo,int x, int y, int z)
	{
		int b = wo.getBlockId(x, y, z);
		if(b == 0 || b == WarpDrive.airBlock.blockID)
			return true;
		return false;
	}
}
