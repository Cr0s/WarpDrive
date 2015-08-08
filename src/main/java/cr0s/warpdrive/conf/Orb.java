package cr0s.warpdrive.conf;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Orb extends DeployableStructure implements XmlRepresentable {

	public Orb(int diameter) {
		super(diameter, diameter, diameter);
	}

	@Override
	public void loadFromXmlElement(Element e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToXmlElement(Element e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean generate(World p_76484_1_, Random p_76484_2_, int p_76484_3_, int p_76484_4_, int p_76484_5_) {
		// TODO Auto-generated method stub
		return false;
	}

	public class OrbShell implements XmlRepresentable {

		private int radiusInner, radiusOuter;

		private HashMap<Integer, String> compositionBlocks;

		public OrbShell() {
			compositionBlocks = new HashMap<Integer, String>();
		}

		public Block getNextBlock() {
			return null;

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
		public void loadFromXmlElement(Element e) {

			radiusInner = Integer.parseInt(e.getAttribute("radiusInner"));
			radiusOuter = Integer.parseInt(e.getAttribute("radiusOuter"));

			if (radiusInner < 1 || radiusInner > radiusOuter)
				throw new IllegalArgumentException("Orb creation arguments are incorrect!");

			NodeList compBlocks = e.getElementsByTagName("CompositionBlock");
			for (int i = 0; i < compBlocks.getLength(); i++) {
				Element tmp = (Element) compBlocks.item(i);
				compositionBlocks.put(Integer.parseInt(tmp.getAttribute("weight")), tmp.getAttribute("block"));
			}

		}

		@Override
		public void saveToXmlElement(Element e) {
			e.setAttribute("radiusInner", "" + radiusInner);
			e.setAttribute("radiusOuter", "" + radiusOuter);

		}

	}

}
