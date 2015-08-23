package cr0s.warpdrive.conf.structures;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.World;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.InvalidXmlException;
import cr0s.warpdrive.conf.XmlRepresentable;
import cr0s.warpdrive.conf.filler.FillerManager;
import cr0s.warpdrive.conf.filler.FillerSet;
import cr0s.warpdrive.world.EntitySphereGen;

public abstract class Orb extends DeployableStructure implements XmlRepresentable {
	
	private OrbShell[] shellRelative;
	private ArrayList<OrbShell> shells;
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Orb(int diameter) {
		super(diameter, diameter, diameter);
		
	}
	
	@Override
	public void loadFromXmlElement(Element e) throws InvalidXmlException {
		
		this.name = e.getAttribute("name");
		
		ArrayList<OrbShell> newShells = new ArrayList<OrbShell>();
		int totalThickness = 0;
		
		NodeList shells = e.getElementsByTagName("shell");
		for (int i = 0; i < shells.getLength(); i++) {
			Element tmp = (Element) shells.item(i);
			
			OrbShell shell = new OrbShell();
			shell.loadFromXmlElement(tmp);
			totalThickness += shell.thickness;
			newShells.add(shell);
			
		}
		
		int index = 0;
		shellRelative = new OrbShell[totalThickness];
		
		for (OrbShell shell : newShells) {
			
			for (int i = 0; i < shell.thickness; i++)
				shellRelative[index++] = shell;
		}
		
		
	}
	
	@Override
	public void saveToXmlElement(Element e, Document d) {
		for (OrbShell shell : shells) {
			Element tmp = d.createElement("shell");
			shell.saveToXmlElement(tmp, d);
			e.appendChild(tmp);
		}
		
	}
	
	@Override
	public boolean generate(World world, Random p_76484_2_, int x, int y, int z) {
		EntitySphereGen entitySphereGen = new EntitySphereGen(world, x, y, z, getHeight() / 2, this, true);
		world.spawnEntityInWorld(entitySphereGen);
		return false;
	}

	public boolean generate(World world, Random p_76484_2_, int x, int y, int z, final int radius) {
		EntitySphereGen entitySphereGen = new EntitySphereGen(world, x, y, z, radius, this, true);
		world.spawnEntityInWorld(entitySphereGen);
		return false;
	}
	
	public OrbShell getShellForRadius(int r) {
		return shellRelative[r];
	}
	
	public class OrbShell extends FillerSet {
		
		private int thickness;
		
		/**
		 * @return the thickness
		 */
		public int getThickness() {
			return thickness;
		}

		/**
		 * @param thickness
		 *            the thickness to set
		 */
		public void setThickness(int thickness) {
			this.thickness = thickness;
		}

		public OrbShell() {

			super("");
		}
		
		@Override
		public void loadFromXmlElement(Element e) throws InvalidXmlException {
			
			WarpDrive.logger.info("Loading shell " + e.getAttribute("name"));
			name = e.getAttribute("name");

			super.loadFromXmlElement(e);

			if (e.hasAttribute("fillerSets")) {
				String[] imports = e.getAttribute("fillerSets").split(",");
				for (String imp : imports) {
					super.loadFrom(FillerManager.getFillerSet(imp));
				}
			}
			
			thickness = Integer.parseInt(e.getAttribute("thicknessMin"));
			
			//TODO: Implement random thickness
			
		}
		
		@Override
		public void saveToXmlElement(Element e, Document d) {
			//Not needed
		}
		
	}
	
}
