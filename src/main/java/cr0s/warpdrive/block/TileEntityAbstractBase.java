package cr0s.warpdrive.block;

import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityAbstractBase extends TileEntity
{
	protected static int toInt(double d) {
		return (int) Math.round(d);
	}
	
	protected static int toInt(Object o) {
		return toInt(toDouble(o));
	}
	
	protected static double toDouble(Object o) {
		return Double.parseDouble(o.toString());
	}

	protected static float toFloat(Object o) {
		return Float.parseFloat(o.toString());
	}
	
	protected static boolean toBool(Object o) {
		if (o == null) {
			 return false;
		}
		if (o instanceof Boolean) {
			 return ((Boolean) o);
		}
		if (o.toString() == "true" || o.toString() == "1.0" || o.toString() == "1" || o.toString() == "y" || o.toString() == "yes") {
			return true;
		}
		return false;
	}
	
	protected static int clamp(final int min, final int max, final int value) {
		return Math.min(max, Math.max(value, min));
	}
	
	protected static double clamp(final double min, final double max, final double value) {
		return Math.min(max, Math.max(value, min));
	}
}
