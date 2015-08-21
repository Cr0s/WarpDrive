package cr0s.warpdrive.conf;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cr0s.warpdrive.WarpDrive;

public class FillerManager {
	
	
	public enum FillerCategory {
		RARE_ORES("rareOres"), COMMON_ORES("commonOres"), UNCOMMON_ORES("uncommonOres"), OVERWORLD("overworld"), NETHER("nether"), END("end");
		

		private TreeMap<Block, Integer> fillerBlocks;
		public int totWeight;
		private String name;
		
		private FillerCategory(String name) {
			this.name = name;
			fillerBlocks = new TreeMap<Block, Integer>(new BlockComparator());
		}
		
		public void addFillerBlock(Block b, int weight) {

			if (b == null || weight < 1)
				throw new IllegalArgumentException();

			fillerBlocks.put(b, weight);
			totWeight += weight;
		}
		
		public Set<Entry<Block, Integer>> getBlocks() {
			return fillerBlocks.entrySet();
		}
		
		public Integer getBlockWeight(Block b) {
			return fillerBlocks.get(b);
		}

		public int getTotalWeight() {
			return totWeight;
		}

		public static FillerCategory getOreSpawnValue(String s) {
			for (FillerCategory cat : FillerCategory.values())
				if (cat.name.equals(s))
					return cat;
			return null;
		}
		
	}
	
	public static void loadOres(String oreConfDirectory) {
		
		loadOres(new File(oreConfDirectory));

	}

	public static void loadOres(File dir) {

		String s = "These are the ores names that have been registered, and so may be used to create structures: ";

		for (String ore : OreDictionary.getOreNames()) {
			s = s + "\n" + ore;
		}

		WarpDrive.logger.info(s);

		dir.mkdir();

		if (!dir.isDirectory())
			throw new IllegalArgumentException("File path " + dir.getPath() + " must be a drectory!");

		File[] files = dir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("filler") && name.endsWith(".xml");
			}

		});

		for(File f : files) {

			try {

				WarpDrive.logger.info("Loading filler data file " + f.getPath());

				loadXmlOreFile(f);

				WarpDrive.logger.info("Finished loading filler data file " + f.getPath());

			} catch (Exception e) {
				WarpDrive.logger.error("Error loading file " + f.getPath() + ": " + e.getMessage());
				e.printStackTrace();
			}

		}

		
	}

	private static void loadXmlOreFile(File f) throws InvalidXmlException, SAXException, IOException {
		
		Document base = WarpDriveConfig.getXmlDocumentBuilder().parse(f);
		
		String res = ModRequirementChecker.checkModRequirements(base.getDocumentElement());
		
		if (!res.isEmpty()) {
			
			WarpDrive.logger.info("Skippping filler data file " + f.getPath() + " because the mods " + res + " are not loaded");
			return;
		}
		
		ModRequirementChecker.doModReqSanitation(base);
		

		NodeList ores = base.getElementsByTagName("ore");
		for (int i = 0; i < ores.getLength(); i++) {
			
			Element ore = (Element) ores.item(i);
			
			String oreName = ore.getAttribute("name");
			if (oreName.isEmpty())
				throw new InvalidXmlException("Ore " + i + "does not contain a name!");
			
			String oreWeight = ore.getAttribute("weight");
			if (oreWeight.isEmpty())
				throw new InvalidXmlException("Ore " + i + " does not contain a weight!");
			
			String category = ore.getAttribute("category");
			if (category.isEmpty())
				throw new InvalidXmlException("Ore " + i + " does not contain a category!");
			
			int weight;
			try {
				weight = Integer.parseInt(oreWeight);
			} catch (NumberFormatException e) {
				throw new InvalidXmlException("Ore weight at " + i + " is invalid!");
			}

			if(weight < 1)
				throw new InvalidXmlException("Ore weight at " + i + " is too low!");

			FillerCategory cat =  FillerCategory.getOreSpawnValue(category);
			if(cat == null)
				throw new InvalidXmlException();
			
			Block b = Block.getBlockFromName(oreName);
			if(b == null)
				throw new InvalidXmlException("Ore name at " + i + " is not a valid block!");
			
			//Everything is fine
			cat.addFillerBlock(b, weight);
		}
	}

	public static class BlockComparator implements Comparator<Block> {

		@Override
		public int compare(Block arg0, Block arg1) {
			return arg0.getUnlocalizedName().compareTo(arg1.getUnlocalizedName());
		}
		
	}

}
