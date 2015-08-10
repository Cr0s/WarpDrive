package cr0s.warpdrive.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import cr0s.warpdrive.conf.WarpDriveConfig;

public class WorldGenStructure {
	public static Block getStoneBlock(boolean corrupted, Random rand) {
		if (corrupted && (rand.nextInt(15) == 1))
			return Blocks.air;

		if (WarpDriveConfig.isIndustrialCraft2loaded) {
			return Block.getBlockFromItem(WarpDriveConfig.getIC2Item("blockAlloy").getItem());
		} else {
			return Blocks.stone;
		}
	}

	public static Block getGlassBlock(boolean corrupted, Random rand) {
		if (corrupted && (rand.nextInt(30) == 1))
			return Blocks.air;

		if (WarpDriveConfig.isIndustrialCraft2loaded) {
			return Block.getBlockFromItem(WarpDriveConfig.getIC2Item("blockAlloyGlass").getItem());
		} else {
			return Blocks.glass;
		}
	}
}
