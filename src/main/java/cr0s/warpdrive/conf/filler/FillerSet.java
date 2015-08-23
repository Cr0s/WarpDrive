package cr0s.warpdrive.conf.filler;

import java.util.Random;

import net.minecraft.block.Block;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cr0s.warpdrive.conf.InvalidXmlException;
import cr0s.warpdrive.conf.MetaBlock;
import cr0s.warpdrive.conf.XmlRepresentable;

/**
 * Represents a set of fillers. Before using after construction, finishContruction() must be called.
 *
 * If FillerSet(blocks[]) is called, that is not neccessary.
 *
 * @author TheNumenorean
 *
 */
public class FillerSet implements XmlRepresentable, Comparable {

	private MetaBlock[] weightedFillerBlocks;
	private FillerFactory factory;
	private String name;

	public FillerSet(MetaBlock[] blocks) {
		weightedFillerBlocks = blocks;
	}

	public FillerSet(String name) {

		this.name = name;

		//To prevent getting rand.nextInt(0)
		weightedFillerBlocks = new MetaBlock[1];
		factory = new FillerFactory();

	}

	public MetaBlock getRandomBlock(Random rand) {
		return weightedFillerBlocks[rand.nextInt(weightedFillerBlocks.length)];
	}

	@Override
	public void loadFromXmlElement(Element e) throws InvalidXmlException {

		NodeList fillers = e.getElementsByTagName("filler");
		for (int i = 0; i < fillers.getLength(); i++) {

			Element filler = (Element) fillers.item(i);

			//Check there is a block name
			if (!filler.hasAttribute("block"))
				throw new InvalidXmlException("Filler " + filler.getBaseURI() + " must contain a block tag!");

			Block block = Block.getBlockFromName(filler.getAttribute("block"));
			if (block == null)
				throw new InvalidXmlException("Filler " + filler.getBaseURI() + " names a nonexistant block! Did you forget a mods attribute?");

			//Get metadata attribute. If absent, set to 0.
			int meta = 0;
			String metaString = filler.getAttribute("metadata");
			try {
				if (!metaString.isEmpty())
					meta = Integer.parseInt(metaString);
			} catch (NumberFormatException ex) {
				throw new InvalidXmlException("Filler " + filler.getBaseURI() + " metadata attribute is NaN!");
			}

			boolean atLeastOne = false;

			//It is intentional that a filler could have both a ratio and a weight
			
			//Check for a weight and add it to the factory
			String weightStr = filler.getAttribute("weight");
			int weight;

			if (!weightStr.isEmpty()) {
				atLeastOne = true;

				try {
					weight = Integer.parseInt(weightStr);

					factory.addWeightedBlock(block, meta, weight);

				} catch (NumberFormatException ex) {
					throw new InvalidXmlException("Filler " + filler.getBaseURI() + " weight is NaN!");
				} catch (IllegalArgumentException ex) {
					throw new InvalidXmlException(ex.getMessage());
				}

			}

			//Check for a ratio attribute, and add it to the factory
			String ratio = filler.getAttribute("ratio");
			if (!ratio.isEmpty()) {
				atLeastOne = true;

				try {

					factory.addRatioBlock(block, meta, ratio);

				} catch (IllegalArgumentException ex) {
					throw new InvalidXmlException(ex.getMessage());
				}

			}
			
			if (!atLeastOne)
				throw new InvalidXmlException("Filler " + filler.getBaseURI() + " doesnt declare a weight or a ratio!");

		}

	}

	@Override
	public void saveToXmlElement(Element e, Document d) throws InvalidXmlException {
		// Unneeded

	}

	/**
	 * Uses the data that has been loaded thusfar to constructed the array in order to make the FillerSet functional. Must be called before calling getRandomBlock()
	 *
	 * Clears the memory used for construction
	 */
	public void finishContruction() {
		weightedFillerBlocks = factory.constructWeightedMetaBlockList();
		factory = null;
	}

	@Override
	public int compareTo(Object obj) {
		return name.compareTo(((FillerSet) obj).name);
	}

	@Override
	public String toString() {
		return name;
	}

}
