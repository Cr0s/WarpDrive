package cr0s.warpdrive.conf.structures;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.InvalidXmlException;
import cr0s.warpdrive.conf.ModRequirementChecker;
import cr0s.warpdrive.conf.WarpDriveConfig;


public class StructureManager {
	
	private static ArrayList<Star> stars = new ArrayList<Star>();
	private static ArrayList<Planetoid> moons = new ArrayList<Planetoid>();
	private static ArrayList<Planetoid> gasClouds = new ArrayList<Planetoid>();
	
	public static void loadStructures(String structureConfDir) {
		loadStructures(new File(structureConfDir));
	}

	public static void loadStructures(File dir) {

		dir.mkdir();

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("File path " + dir.getPath() + " must be a directory!");
		}

		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file_notUsed, String name) {
				return name.startsWith("structure") && name.endsWith(".xml");
			}
		});

		for (File file : files) {
			try {

				WarpDrive.logger.info("Loading structure data file " + file.getPath());

				loadXmlStructureFile(file);

				WarpDrive.logger.info("Finished loading structure data file " + file.getPath());

			} catch (Exception e) {
				WarpDrive.logger.error("Error loading file " + file.getPath() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private static void loadXmlStructureFile(File f) throws SAXException, IOException, InvalidXmlException {
		Document base = WarpDriveConfig.getXmlDocumentBuilder().parse(f);

		String res = ModRequirementChecker.checkModRequirements(base.getDocumentElement());
		
		if (!res.isEmpty()) {
			WarpDrive.logger.info("Skippping structure data file " + f.getPath() + " because the mods " + res + " are not loaded");
			return;
		}
		

		
		NodeList structures = base.getElementsByTagName("structure");
		for (int i = 0; i < structures.getLength(); i++) {
			
			Element struct = (Element) structures.item(i);
			
			String type = struct.getAttribute("type");
			String name = struct.getAttribute("name");
			
			if(type.isEmpty())
				throw new InvalidXmlException("Structure must have a type!");
			
			int radius;
			try {
				radius = Integer.parseInt(struct.getAttribute("radius"));
			} catch(NumberFormatException e) {
				throw new InvalidXmlException("Structure radius is invalid!");
			}
			
			if (type.equalsIgnoreCase("star")) {
				Star s = new Star(radius);
				s.loadFromXmlElement(struct);
				stars.add(s);
			} else if(type.equalsIgnoreCase("moon")) {
				Planetoid pl = new Planetoid(radius);
				pl.loadFromXmlElement(struct);
			}
		}
	}
	
	public static DeployableStructure getStructure(Random random, final String name, final String type) {
		if (name == null || name.length() == 0) {
			if (type == null || type.length() == 0) {
				return stars.get(random.nextInt(stars.size()));
			} else if (type.equalsIgnoreCase("star")) {
				return stars.get(random.nextInt(stars.size()));
			} else if (type.equalsIgnoreCase("moon")) {
				return moons.get(random.nextInt(moons.size()));
			}
		} else {
			for (Star star : stars) {
				if (star.getName().equals(name))
					return star;
			}
		}

		// not found or nothing defined => return null
		return null;
	}
	
	public static DeployableStructure getStar(Random random, final String name) {
		return getStructure(random, name, "star");
	}
	
	public static DeployableStructure getMoon(Random random, final String name) {
		return getStructure(random, name, "moon");
	}
	
	public static DeployableStructure getGasCloud(Random random, final String name) {
		return getStructure(random, name, "cloud");
	}
}
