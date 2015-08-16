package cr0s.warpdrive.conf.structures;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cpw.mods.fml.common.Loader;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.InvalidXmlException;
import cr0s.warpdrive.conf.WarpDriveConfig;


public class StructureManager {

	private static ArrayList<Star> stars = new ArrayList<Star>();
	private static ArrayList<Planetoid> moons = new ArrayList<Planetoid>();

	public static void loadStructures(String structureConfDir) {
		
		loadStructures(new File(structureConfDir));

	}

	public static void loadStructures(File dir) {
		dir.mkdir();
		
		if (!dir.isDirectory())
			throw new IllegalArgumentException("File path " + dir.getPath() + " must be a drectory!");
		
		File[] files = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("structure") && name.endsWith(".xml");
			}
			
		});
		
		for (File f : files) {
			
			try {
				
				WarpDrive.logger.info("Loading structure data file " + f.getPath());
				
				loadXmlStructureFile(f);
				
				WarpDrive.logger.info("Finished loading structure data file " + f.getPath());
				
			} catch (Exception e) {
				WarpDrive.logger.error("Error loading file " + f.getPath() + ": " + e.getMessage());
				e.printStackTrace();
			}
			
		}
	}
	
	private static void loadXmlStructureFile(File f) throws SAXException, IOException, InvalidXmlException {
		Document base = WarpDriveConfig.getXmlDocumentBuilder().parse(f);
		
		NodeList modReqList = base.getElementsByTagName("ModRequirements");
		for (int i = 0; i < modReqList.getLength(); i++) {
			NodeList mods = ((Element) modReqList.item(i)).getElementsByTagName("mod");
			for (int j = 0; j < mods.getLength(); j++) {
				Element mod = (Element) mods.item(j);
				if (!mod.hasAttribute("name"))
					throw new InvalidXmlException("A mod requirement at " + i + ":" + j + " is missing the name attribute!");

				String name = mod.getAttribute("name");
				if (!Loader.isModLoaded(name)) {
					WarpDrive.logger.info("Skippping structure data file " + f.getPath() + " because the mod " + name + " is not loaded");
				}
				
			}
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

			if(type.equalsIgnoreCase("star")) {
				Star s = new Star(radius);
				s.loadFromXmlElement(struct);
				stars.add(s);
			} else if(type.equalsIgnoreCase("moon")) {
				Planetoid pl = new Planetoid(radius);
				pl.loadFromXmlElement(struct);

			}

		}

	}

}
