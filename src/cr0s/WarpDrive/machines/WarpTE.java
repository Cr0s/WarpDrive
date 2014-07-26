package cr0s.WarpDrive.machines;

import cr0s.WarpDrive.Vector3;
import net.minecraft.tileentity.TileEntity;

public abstract class WarpTE extends TileEntity
{
 	public static final Vector3[] adjacentSideOffsets = new Vector3[6];
 	static
 	{
 		adjacentSideOffsets[0] = new Vector3( 0, 0, 1);
 		adjacentSideOffsets[1] = new Vector3( 0, 0,-1);
 		adjacentSideOffsets[2] = new Vector3( 0, 1, 0);
 		adjacentSideOffsets[3] = new Vector3( 0,-1, 0);
 		adjacentSideOffsets[4] = new Vector3( 1, 0, 0);
 		adjacentSideOffsets[5] = new Vector3(-1, 0, 0);
 	}
 	
	protected int toInt(double d) {
		return (int) Math.round(d);
	}
	
	protected int toInt(Object o) {
		return toInt(toDouble(o));
	}
	
	protected double toDouble(Object o) {
		return Double.parseDouble(o.toString());
	}
	
	protected boolean toBool(Object o) {
		if(o.toString() == "true" || o.toString() == "1.0" || o.toString() == "1")
			return true;
		return false;
	}
	
	protected int clamp(int a,int min,int max) {
		return Math.min(max, Math.max(a,min));
	}
	
	protected double clamp(double a,double min,double max) {
		return Math.min(max, Math.max(a,min));
	}
}
