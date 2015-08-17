package cr0s.warpdrive.conf.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.InvalidXmlException;
import cr0s.warpdrive.conf.OreManager.OreSpawnCategory;
import cr0s.warpdrive.conf.XmlRepresentable;
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

		NodeList shells = e.getElementsByTagName("shell");
		for (int i = 0; i < shells.getLength(); i++) {
			Element tmp = (Element) shells.item(i);

			OrbShell shell = new OrbShell();
			shell.loadFromXmlElement(tmp);

			setShell(shell.getRadiusInner(), shell.getRadiusOuter(), shell);

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

	public void setShell(int inner, int outer, OrbShell shell) {
		for (; inner <= outer; inner++)
			shellRelative[inner] = shell;
	}

	public OrbShell getShellForRadius(int r) {
		return shellRelative[r];
	}

	public Block getBlockForRadius(Random rand, int r) {
		return shellRelative[r].getNextBlock(rand);

	}

	public class OrbShell implements XmlRepresentable {

		private int radiusInner, radiusOuter;

		private HashMap<String, Integer> compositionBlocks;

		private Block[] blockWeights;

		private String name;

		private int totalWeight;
		
		private double genericOreChance;

		public OrbShell() {
			compositionBlocks = new HashMap<String, Integer>();
			totalWeight = 1;
			blockWeights = new Block[0];
			genericOreChance = 0;
		}

		/**
		 * Gets a block randomly chosen according to the setup of this OrbShell
		 *
		 * @param r
		 *            The Random to use
		 * @return A Non-null block instance
		 */
		public Block getNextBlock(Random r) {
			return blockWeights[r.nextInt(totalWeight)];
		}

		public int getRadiusInner() {
			return radiusInner;
		}

		public void setRadiusInner(int radiusInner) {

			this.radiusInner = radiusInner;
		}

		public int getRadiusOuter() {
			return radiusOuter;
		}

		public void setRadiusOuter(int radiusOuter) {
			this.radiusOuter = radiusOuter;
		}

		@Override
		public void loadFromXmlElement(Element e) throws InvalidXmlException {

			WarpDrive.logger.info("Loading shell " + e.getAttribute("name"));
			name = e.getAttribute("name");

			totalWeight = 0;

			try {
				radiusInner = Integer.parseInt(e.getAttribute("radiusInner"));
				radiusOuter = Integer.parseInt(e.getAttribute("radiusOuter"));

				if (radiusInner < 1 || radiusInner > radiusOuter)
					throw new InvalidXmlException("Orb creation arguments are incorrect!");

				NodeList compBlocks = e.getElementsByTagName("block");
				for (int i = 0; i < compBlocks.getLength(); i++) {
					Element tmp = (Element) compBlocks.item(i);

					if (!tmp.hasAttribute("weight"))
						throw new InvalidXmlException("Shell is missing weight at place " + i + "!");

					String blockName = tmp.getTextContent();

					if (blockName.isEmpty())
						throw new InvalidXmlException("Shell is missing block name at place " + i + "!");

					int tmpWeight = Integer.parseInt(tmp.getAttribute("weight"));

					if (tmpWeight < 1)
						throw new InvalidXmlException("Weight is less than 1 at place " + i + "!");

					if (Block.getBlockFromName(blockName) == null)
						throw new InvalidXmlException("Shell has unknown block at place " + i + "!");

					totalWeight += tmpWeight;

					compositionBlocks.put(blockName, tmpWeight);
				}
				
				HashMap<String, Integer> categoryTemp = new HashMap<String, Integer>();

				NodeList oreCategories = e.getElementsByTagName("OreCategory");
				for (int i = 0; i < oreCategories.getLength(); i++) {
					Element cat = (Element) oreCategories.item(i);

					if (!cat.hasAttribute("weight"))
						throw new InvalidXmlException("OreCategory " + i + " must have a weight");

					if (!cat.hasAttribute("category"))
						throw new InvalidXmlException("OreCategory " + i + " must have a category");

					int weight = Integer.parseInt(cat.getAttribute("weight"));
					String oreSpawnCategory = cat.getAttribute("category");
					
					if (OreSpawnCategory.getOreSpawnValue(oreSpawnCategory) == null)
						throw new InvalidXmlException("Shell has an invalid ores category!");

					if (weight < 1)
						throw new InvalidXmlException("Shell has an invalid ores probability!");
					
					totalWeight += weight;
					
					if(categoryTemp.containsKey(oreSpawnCategory))
						throw new InvalidXmlException("OreCategory used twice in same shell!");
					
					categoryTemp.put(oreSpawnCategory, weight);
					
				}

				int index = 0;
				blockWeights = new Block[totalWeight];

				for (Entry<String, Integer> compBlock : compositionBlocks.entrySet()) {

					Block bl = Block.getBlockFromName(compBlock.getKey());

					for (int i = 0; i < compBlock.getValue(); i++) {
						blockWeights[index++] = bl;
					}

				}

				for(Entry<String, Integer> category : categoryTemp.entrySet()) {
					OreSpawnCategory value = OreSpawnCategory.getOreSpawnValue(category.getKey());
					for (Entry<Block, Integer> compBlock : value.getOres()) {

						int reduced = category.getValue() * compBlock.getValue() / value.getTotalWeight();
						for (int i = 0; i < reduced; i++) {
							blockWeights[index++] = compBlock.getKey();
						}

					}
				}
				

			} catch (NumberFormatException ex) {
				throw new InvalidXmlException("Invalid integer in shell " + name + "!");
			}

			if (totalWeight < 1)
				throw new InvalidXmlException("At least one kind of block must be defined!");

		}

		@Override
		public void saveToXmlElement(Element e, Document d) {
			WarpDrive.logger.info("Saving shell " + e.getAttribute("name"));

			e.setAttribute("name", name);
			e.setAttribute("radiusInner", "" + radiusInner);
			e.setAttribute("radiusOuter", "" + radiusOuter);

			for (Entry<String, Integer> compBlock : compositionBlocks.entrySet()) {
				Element child = d.createElement("CompositionBlock");
				child.setAttribute("weight", compBlock.getValue() + "");
				child.setAttribute("block", compBlock.getKey());
				e.appendChild(child);
			}

		}

	}

}
