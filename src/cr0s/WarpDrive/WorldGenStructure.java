package cr0s.WarpDrive;

import java.util.Random;

public class WorldGenStructure
{
	public static int getStoneBlock(boolean corrupted, Random rand)
	{
		if (corrupted && (rand.nextInt(15) == 1))
			return 0;
		return WarpDriveConfig.i.getIC2Item("reinforcedStone").itemID;
	}

	public static int getGlassBlock(boolean corrupted, Random rand)
	{
		if (corrupted && (rand.nextInt(30) == 1))
			return 0;
		return WarpDriveConfig.i.getIC2Item("reinforcedGlass").itemID;
	}
}
