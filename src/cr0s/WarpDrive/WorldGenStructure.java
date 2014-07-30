package cr0s.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;

public class WorldGenStructure
{
	public static int getStoneBlock(boolean corrupted, Random rand)
	{
		if (corrupted && (rand.nextInt(15) == 1))
			return 0;
		return Block.stone.blockID;
	}

	public static int getGlassBlock(boolean corrupted, Random rand)
	{
		if (corrupted && (rand.nextInt(30) == 1))
			return 0;
		return Block.glass.blockID;
	}
}
