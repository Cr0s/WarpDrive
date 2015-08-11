package cr0s.warpdrive.conf.structures;

import java.util.Random;

import net.minecraft.world.World;
import cr0s.warpdrive.world.EntityStarCore;

public class Sun extends Orb {
	
	public Sun(int diameter) {
		super(diameter);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean generate(World p_76484_1_, Random p_76484_2_, int p_76484_3_, int p_76484_4_, int p_76484_5_) {
		boolean success = super.generate(p_76484_1_, p_76484_2_, p_76484_3_, p_76484_4_, p_76484_5_);
		
		if (success)
			p_76484_1_.spawnEntityInWorld(new EntityStarCore(p_76484_1_, p_76484_3_, p_76484_4_, p_76484_5_, super.getHeight() / 2));
		return false;
	}
	
}
