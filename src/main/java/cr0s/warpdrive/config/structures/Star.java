package cr0s.warpdrive.config.structures;

import java.util.Random;

import net.minecraft.world.World;
import cr0s.warpdrive.world.EntityStarCore;

public class Star extends Orb {
	
	public Star(int diameter) {
		super(diameter);
	}
	
	@Override
	public boolean generate(World p_76484_1_, Random p_76484_2_, int p_76484_3_, int p_76484_4_, int p_76484_5_) {
		boolean success = super.generate(p_76484_1_, p_76484_2_, p_76484_3_, p_76484_4_, p_76484_5_);
		
		if (success)
			return placeStarCore(p_76484_1_, p_76484_3_, p_76484_4_, p_76484_5_, super.getHeight() / 2);
		return false;
	}

	public static boolean placeStarCore(World world, int x, int y, int z, int radius) {
		return world.spawnEntityInWorld(new EntityStarCore(world, x, y, z, radius));
	}
	
}
