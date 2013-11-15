package cr0s.WarpDrive;

import ic2.api.item.Items;
import java.util.Random;

public class WorldGenStructure
{
    public static int getStoneBlock(boolean corrupted, Random rand)
    {
        int res = Items.getItem("reinforcedStone").itemID;

        if (corrupted && (rand.nextInt(15) == 1))
        {
            res = 0;
        }

        return res;
    }

    public static int getGlassBlock(boolean corrupted, Random rand)
    {
        int res = Items.getItem("reinforcedGlass").itemID;

        if (corrupted && (rand.nextInt(30) == 1))
        {
            res = 0;
        }

        return res;
    }
}
