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

import cpw.mods.fml.common.Loader;
import cr0s.warpdrive.WarpDrive;

public class OreManager {


	public enum OreSpawnCategory {
		RARE, COMMON;

		
		private TreeMap<Block, Integer> ores;
		public int totWeight;

		private OreSpawnCategory() {
			ores = new TreeMap<Block, Integer>(new BlockComparator());
		}

		public void addOre(Block b, int weight) {
			
			if (b == null || weight < 1)
				throw new IllegalArgumentException();
			
			ores.put(b, weight);
			totWeight += weight;
		}

		public Set<Entry<Block, Integer>> getOres() {
			return ores.entrySet();
		}

		public Integer getOreWeight(Block b) {
			return ores.get(b);
		}
		
		public int getTotalWeight() {
			return totWeight;
		}
		
		public static OreSpawnCategory getOreSpawnValue(String s) {
			return OreSpawnCategory.valueOf(s.trim().toUpperCase());
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
				return name.startsWith("ores") && name.endsWith(".xml");
			}
			
		});
		
		for(File f : files) {
			
			try {
				
				WarpDrive.logger.info("Loading ore data file " + f.getPath());
				
				loadXmlOreFile(f);
				
				WarpDrive.logger.info("Finished loading ore data file " + f.getPath());
				
			} catch (Exception e) {
				WarpDrive.logger.error("Error loading file " + f.getPath() + ": " + e.getMessage());
				e.printStackTrace();
			}
			
		}
		

	}
	
	private static void loadXmlOreFile(File f) throws InvalidXmlException, SAXException, IOException {

		Document base = WarpDriveConfig.getXmlDocumentBuilder().parse(f);

		NodeList modReqList = base.getElementsByTagName("ModRequirements");
		for (int i = 0; i < modReqList.getLength(); i++) {
			NodeList mods = ((Element) modReqList.item(i)).getElementsByTagName("mod");
			for (int j = 0; j < mods.getLength(); j++) {
				Element mod = (Element) mods.item(j);
				
				String name = mod.getTextContent();
				if (name.isEmpty())
					throw new InvalidXmlException("A mod requirement at " + i + ":" + j + " is empty!");

				if (!Loader.isModLoaded(name)) {
					WarpDrive.logger.info("Skippping ore data file " + f.getPath() + " because the mod " + name + " is not loaded");
				}

			}
		}

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
			
			OreSpawnCategory cat =  OreSpawnCategory.getOreSpawnValue(category);
			if(cat == null)
				throw new InvalidXmlException();

			Block b = Block.getBlockFromName(oreName);
			if(b == null)
				throw new InvalidXmlException("Ore name at " + i + " is not a valid block!");

			//Everything is fine
			cat.addOre(b, weight);
		}
	}
	
	public static class BlockComparator implements Comparator<Block> {
		
		@Override
		public int compare(Block arg0, Block arg1) {
			return arg0.getUnlocalizedName().compareTo(arg1.getUnlocalizedName());
		}

	}
	
}
