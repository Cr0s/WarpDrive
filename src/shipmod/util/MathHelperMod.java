package shipmod.util;

import net.minecraft.util.MathHelper;

public class MathHelperMod extends MathHelper
{
    public static double clamp_double(double d, double lowerbound, double upperbound)
    {
        return d < lowerbound ? lowerbound : (d > upperbound ? upperbound : d);
    }

    public static int round_double(double d)
    {
        return (int)Math.round(d);
    }
}
