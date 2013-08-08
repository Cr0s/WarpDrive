package shipmod.util;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class AABBRotator
{
    private static Vec3 vec00 = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
    private static Vec3 vec01 = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
    private static Vec3 vec10 = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
    private static Vec3 vec11 = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);

    public static void rotateAABBAroundY(AxisAlignedBB aabb, double xoff, double zoff, float ang)
    {
        double y0 = aabb.minY;
        double y1 = aabb.maxY;
        vec00.xCoord = aabb.minX - xoff;
        vec00.zCoord = aabb.minZ - zoff;
        vec01.xCoord = aabb.minX - xoff;
        vec01.zCoord = aabb.maxZ - zoff;
        vec10.xCoord = aabb.maxX - xoff;
        vec10.zCoord = aabb.minZ - zoff;
        vec11.xCoord = aabb.maxX - xoff;
        vec11.zCoord = aabb.maxZ - zoff;
        vec00.rotateAroundY(ang);
        vec01.rotateAroundY(ang);
        vec10.rotateAroundY(ang);
        vec11.rotateAroundY(ang);
        aabb.setBounds(minX(), y0, minZ(), maxX(), y1, maxZ()).offset(xoff, 0.0D, zoff);
    }

    private static double minX()
    {
        return Math.min(Math.min(Math.min(vec00.xCoord, vec01.xCoord), vec10.xCoord), vec11.xCoord);
    }

    private static double minZ()
    {
        return Math.min(Math.min(Math.min(vec00.zCoord, vec01.zCoord), vec10.zCoord), vec11.zCoord);
    }

    private static double maxX()
    {
        return Math.max(Math.max(Math.max(vec00.xCoord, vec01.xCoord), vec10.xCoord), vec11.xCoord);
    }

    private static double maxZ()
    {
        return Math.max(Math.max(Math.max(vec00.zCoord, vec01.zCoord), vec10.zCoord), vec11.zCoord);
    }
}
