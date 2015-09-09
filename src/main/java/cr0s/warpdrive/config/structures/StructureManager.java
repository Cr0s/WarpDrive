package cr0s.warpdrive.config.structures;

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
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.config.XmlPreprocessor;
import cr0s.warpdrive.config.XmlPreprocessor.ModCheckResults;


public class StructureManager {
	
	private static ArrayList<Star> stars = new ArrayList<Star>();
	private static ArrayList<Planetoid> moons = new ArrayList<Planetoid>();
	private static ArrayList<Planetoid> gasClouds = new ArrayList<Planetoid>();
	private static ArrayList<Asteroid> asteroids = new ArrayList<Asteroid>();
	
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
				
				WarpDrive.logger.info("Loading structure data file " + file.getName());
				
				loadXmlStructureFile(file);
				
				WarpDrive.logger.info("Finished loading structure data file " + file.getName());
				
			} catch (Exception e) {
				WarpDrive.logger.error("Error loading file " + file.getName() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	private static void loadXmlStructureFile(File f) throws SAXException, IOException, InvalidXmlException {
		Document base = WarpDriveConfig.getXmlDocumentBuilder().parse(f);
		
		ModCheckResults res = XmlPreprocessor.checkModRequirements(base.getDocumentElement());
		
		if (!res.isEmpty()) {
			WarpDrive.logger.info("Skippping structure data file " + f.getName() + " because of: " + res);
			return;
		}
		
		XmlPreprocessor.doModReqSanitation(base);
		XmlPreprocessor.doLogicPreprocessing(base);
		
		NodeList structures = base.getElementsByTagName("structure");
		for (int i = 0; i < structures.getLength(); i++) {
			
			Element struct = (Element) structures.item(i);
			
			String group = struct.getAttribute("group");
			String name = struct.getAttribute("name");
			
			WarpDrive.logger.info("Loading structure " + name);
			
			if (group.isEmpty())
				throw new InvalidXmlException("Structure must have a group!");
			
			int radius = 0;
			
			if (group.equalsIgnoreCase("star")) {
				Star s = new Star(radius);
				s.loadFromXmlElement(struct);
				stars.add(s);
			} else if (group.equalsIgnoreCase("moon")) {
				Planetoid pl = new Planetoid(radius);
				pl.loadFromXmlElement(struct);
				moons.add(pl);
			} else if (group.equalsIgnoreCase("asteroid")) {
				Asteroid as = new Asteroid();
				as.loadFromXmlElement(struct);
				asteroids.add(as);
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
			} else if (type.equalsIgnoreCase("asteroid")) {
				return asteroids.get(random.nextInt(asteroids.size()));
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
	
	public static DeployableStructure getAsteroid(Random random, final String name) {
		return getStructure(random, name, "asteroid");
	}
	
	public static DeployableStructure getGasCloud(Random random, final String name) {
		return getStructure(random, name, "cloud");
	}
}
