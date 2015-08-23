package cr0s.warpdrive.conf.filler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.block.Block;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import scala.actors.threadpool.Arrays;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.InvalidXmlException;
import cr0s.warpdrive.conf.ModRequirementChecker;
import cr0s.warpdrive.conf.WarpDriveConfig;

public class FillerManager {

	private static TreeMap<String, FillerSet> fillerSets = new TreeMap<String, FillerSet>();
	
	static//Stores extra dependecy information
	TreeMap<FillerSet, ArrayList<String>> fillerSetsAdditions = new TreeMap<FillerSet, ArrayList<String>>();

	//FillerSets that are guaranteed to exist
	public static final String COMMON_ORES = "commonOres";
	public static final String UNCOMMON_ORES = "uncommonOres";
	public static final String RARE_ORES = "rareOres";
	public static final String OVERWORLD = "overworld";
	public static final String NETHER = "nether";
	public static final String END = "end";

	public static void loadOres(String oreConfDirectory) {

		loadOres(new File(oreConfDirectory));
		
	}
	
	public static void loadOres(File dir) {
		
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
				
				loadXmlFillerFile(f);
				
				WarpDrive.logger.info("Finished loading filler data file " + f.getPath());
				
			} catch (Exception e) {
				WarpDrive.logger.error("Error loading file " + f.getPath() + ": " + e.getMessage());
				e.printStackTrace();
			}
			
		}
		

	}
	
	private static void loadXmlFillerFile(File f) throws InvalidXmlException, SAXException, IOException {

		Document base = WarpDriveConfig.getXmlDocumentBuilder().parse(f);

		String res = ModRequirementChecker.checkModRequirements(base.getDocumentElement());

		if (!res.isEmpty()) {

			WarpDrive.logger.info("Skippping filler data file " + f.getPath() + " because the mods " + res + " are not loaded");
			return;
		}

		//Remove elements based on mod reqs
		ModRequirementChecker.doModReqSanitation(base);

		//Initially add FillerSets
		NodeList sets = base.getElementsByTagName("FillerSet");
		for (int i = 0; i < sets.getLength(); i++) {

			Element fillerSet = (Element) sets.item(i);
			
			String name = fillerSet.getAttribute("name");
			if (name.isEmpty())
				throw new InvalidXmlException("FillerSet " + i + " is missing a name attribute!");
			
			FillerSet set;
			if (fillerSets.containsKey(name))
				set = fillerSets.get(name);
			else
				fillerSets.put(name, set = new FillerSet(name));
			
			if (fillerSet.hasAttribute("import")) {

				if (fillerSetsAdditions.containsKey(set)) {
					ArrayList<String> setUnresolvedDeps = fillerSetsAdditions.get(set);
					setUnresolvedDeps.addAll(Arrays.asList(fillerSet.getAttribute("import").split(",")));
				} else {
					ArrayList<String> setUnresolvedDeps = new ArrayList<String>();
					setUnresolvedDeps.addAll(Arrays.asList(fillerSet.getAttribute("import").split(",")));
					fillerSetsAdditions.put(set, setUnresolvedDeps);
				}
			}

			set.loadFromXmlElement(fillerSet);
			
		}
	}
	
	public static void finishLoading() {
		
		while (!fillerSetsAdditions.isEmpty()) {
			attemptDependencyFilling(fillerSetsAdditions);
		}
		
		//When everything is done, finalize
		for (FillerSet set : fillerSets.values()) {
			set.finishContruction();
		}
		
	}
	
	private static void attemptDependencyFilling(TreeMap<FillerSet, ArrayList<String>> fillerSetsDeps) {

		ArrayList<FillerSet> toRemove = new ArrayList<FillerSet>();
		
		for (Entry<FillerSet, ArrayList<String>> entry : fillerSetsDeps.entrySet()) {

			for (String dep : entry.getValue()) {

				if (!fillerSets.containsKey(dep)) {

					WarpDrive.logger.error("A fillerSet " + entry.getKey() + " has a dependency that doesnt exist!");
					fillerSets.remove(entry.getKey().getName());
					toRemove.add(entry.getKey());

				} else if (fillerSetsDeps.containsKey(fillerSets.get(dep))) {
					//Skip until it is loaded
				} else {
					
					entry.getKey().loadFrom(fillerSets.get(dep));
					toRemove.add(entry.getKey());
					
				}
				
				
			}

		}
		
		for (FillerSet set : toRemove) {
			fillerSetsDeps.remove(set);
		}
		
	}
	
	public static class BlockComparator implements Comparator<Block> {
		
		@Override
		public int compare(Block arg0, Block arg1) {
			return arg0.getUnlocalizedName().compareTo(arg1.getUnlocalizedName());
		}

	}
	
}
