package cr0s.warpdrive.conf;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cr0s.warpdrive.WarpDrive;

public abstract class Orb extends DeployableStructure implements XmlRepresentable {

	public Orb(int diameter) {
		super(diameter, diameter, diameter);
	}

	@Override
	public void loadFromXmlElement(Element e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToXmlElement(Element e, Document d) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean generate(World p_76484_1_, Random p_76484_2_, int p_76484_3_, int p_76484_4_, int p_76484_5_) {
		// TODO Auto-generated method stub
		return false;
	}

	public class OrbShell implements XmlRepresentable {

		private int radiusInner, radiusOuter;

		private HashMap<String, Integer> compositionBlocks;

		private Block[] blockWeights;

		private String name;

		private int totalWeight;

		public OrbShell() {
			compositionBlocks = new HashMap<String, Integer>();
			totalWeight = 1;
			blockWeights = new Block[0];
		}

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

				NodeList compBlocks = e.getElementsByTagName("CompositionBlock");
				for (int i = 0; i < compBlocks.getLength(); i++) {
					Element tmp = (Element) compBlocks.item(i);

					if (!tmp.hasAttribute("weight"))
						throw new InvalidXmlException("Shell is missing weight at place " + i + "!");

					if (!tmp.hasAttribute("block"))
						throw new InvalidXmlException("Shell is missing block at place " + i + "!");

					int tmpWeight = Integer.parseInt(tmp.getAttribute("weight"));

					if (tmpWeight < 1)
						throw new InvalidXmlException("Weight is less than 1 at place " + i + "!");

					String tmpBlock = tmp.getAttribute("block");

					if (Block.getBlockFromName(tmpBlock) == null)
						throw new InvalidXmlException("Shell has unknown block at place " + i + "!");

					totalWeight += tmpWeight;

					compositionBlocks.put(tmpBlock, tmpWeight);
				}

				int index = 0;
				blockWeights = new Block[totalWeight];

				for (Entry<String, Integer> compBlock : compositionBlocks.entrySet()) {

					Block bl = Block.getBlockFromName(compBlock.getKey());

					for (int i = 0; i < compBlock.getValue(); i++) {
						blockWeights[index++] = bl;
					}

				}

			} catch (NumberFormatException ex) {
				throw new InvalidXmlException("Invalid integer in shell " + name + "!");
			}

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
