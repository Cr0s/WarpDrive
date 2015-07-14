package cr0s.warpdrive.world;

import java.util.Random;

import net.minecraft.block.Block;
import cr0s.warpdrive.WarpDriveConfig;

public class WorldGenStructure
{
	public static int getStoneBlock(boolean corrupted, Random rand)
	{
		if (corrupted && (rand.nextInt(15) == 1))
			return 0;
		
		if (WarpDriveConfig.isICLoaded) {
			return WarpDriveConfig.getIC2Item("reinforcedStone").itemID;
		} else {
			return Block.stone.blockID;
		}
	}

	public static int getGlassBlock(boolean corrupted, Random rand)
	{
		if (corrupted && (rand.nextInt(30) == 1))
			return 0;
		
		if (WarpDriveConfig.isICLoaded) {
			return WarpDriveConfig.getIC2Item("reinforcedGlass").itemID;
		} else {
			return Block.glass.blockID;
		}
	}
}
