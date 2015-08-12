package cr0s.warpdrive.conf;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.block.Block;

public class OreManager {


	public enum OreSpawnValue {
		RARE, COMMON;

		
		private TreeMap<Block, Double> ores;
		public int totWeight;

		private OreSpawnValue() {
			ores = new TreeMap<Block, Double>(new BlockComparator());
		}

		public void addOre(Block b, double prob) {
			
			if (b == null || prob > 1 || prob <= 0)
				throw new IllegalArgumentException();
			
			ores.put(b, prob);
			totWeight += prob;
		}

		public Set<Entry<Block, Double>> getOres() {
			return ores.entrySet();
		}

		public Double getOreWeight(Block b) {
			return ores.get(b);
		}
		
		public int getTotalWeight() {
			return totWeight;
		}
		
		public static OreSpawnValue getOreSpawnValue(String s) {
			return OreSpawnValue.valueOf(s.trim().toUpperCase());
		}

	}

	public static void loadOres() {
		//TODO:
	}
	
	public static class BlockComparator implements Comparator<Block> {
		
		@Override
		public int compare(Block arg0, Block arg1) {
			return arg0.getUnlocalizedName().compareTo(arg1.getUnlocalizedName());
		}

	}
	
}
