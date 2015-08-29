package cr0s.warpdrive.config.filler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.config.XmlPreprocessor;
import cr0s.warpdrive.config.XmlPreprocessor.ModCheckResults;

public class FillerManager {

	private static TreeMap<String, FillerSet> fillerSets = new TreeMap<String, FillerSet>();

	// Stores extra dependency information
	static TreeMap<FillerSet, ArrayList<String>> fillerSetsAdditions = new TreeMap<FillerSet, ArrayList<String>>();

	/* TODO dead code?
	// FillerSets that are guaranteed to exist
	public static final String COMMON_ORES = "commonOres";
	public static final String UNCOMMON_ORES = "uncommonOres";
	public static final String RARE_ORES = "rareOres";
	public static final String OVERWORLD = "overworld";
	public static final String NETHER = "nether";
	public static final String END = "end";
	/**/

	public static void loadOres(String oreConfDirectory) {
		loadOres(new File(oreConfDirectory));
	}

	public static void loadOres(File dir) {
		// directory is created by caller, so it can copy default files if any

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("File path " + dir.getPath() + " must be a directory!");
		}

		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file_notUsed, String name) {
				return name.startsWith("filler") && name.endsWith(".xml");
			}
		});

		for(File file : files) {
			try {
				WarpDrive.logger.info("Loading filler data file " + file.getPath() + "...");
				loadXmlFillerFile(file);
				WarpDrive.logger.info("Loading filler data file " + file.getPath() + " done");
			} catch (Exception exception) {
				WarpDrive.logger.error("Error loading file " + file.getPath() + ": " + exception.getMessage());
				exception.printStackTrace();
			}
		}
	}

	private static void loadXmlFillerFile(File file) throws InvalidXmlException, SAXException, IOException {

		Document base = WarpDriveConfig.getXmlDocumentBuilder().parse(file);

		ModCheckResults res = XmlPreprocessor.checkModRequirements(base.getDocumentElement());

		if (!res.isEmpty()) {
			WarpDrive.logger.info("Skippping filler data file " + file.getPath() + " because of: " + res);
			return;
		}

		// Remove elements based on mod reqs sanitation
		XmlPreprocessor.doModReqSanitation(base);

		// Initially add FillerSets
		NodeList nodesFillerSet = base.getElementsByTagName("FillerSet");
		for (int i = 0; i < nodesFillerSet.getLength(); i++) {

			Element elementFillerSet = (Element) nodesFillerSet.item(i);

			String group = elementFillerSet.getAttribute("group");
			if (group.isEmpty()) {
				throw new InvalidXmlException("FillerSet " + i + " is missing a group attribute!");
			}

			FillerSet fillerSet = fillerSets.get(group);
			if (fillerSet == null) {
				fillerSet = new FillerSet(group);
				fillerSets.put(group, fillerSet);
			}

			if (elementFillerSet.hasAttribute("fillerSets")) {
				ArrayList<String> setUnresolvedDeps = fillerSetsAdditions.get(fillerSet);
				if (setUnresolvedDeps == null) {
					setUnresolvedDeps = new ArrayList<String>();
					fillerSetsAdditions.put(fillerSet, setUnresolvedDeps);
				}
				setUnresolvedDeps.addAll(Arrays.asList(elementFillerSet.getAttribute("import").split(",")));
			}

			fillerSet.loadFromXmlElement(elementFillerSet);
		}
	}

	public static void finishLoading() {

		while (!fillerSetsAdditions.isEmpty()) {
			attemptDependencyFilling(fillerSetsAdditions);
		}

		// When everything is done, finalize
		for (FillerSet fillerSet : fillerSets.values()) {
			fillerSet.finishContruction();
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

	public static FillerSet getFillerSet(String name) {
		return fillerSets.get(name);
	}

	/* TODO dead code?
	public static class BlockComparator implements Comparator<Block> {

		@Override
		public int compare(Block arg0, Block arg1) {
			return arg0.getUnlocalizedName().compareTo(arg1.getUnlocalizedName());
		}
	}
	/**/
}
