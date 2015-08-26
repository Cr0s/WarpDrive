package cr0s.warpdrive.config.filler;

import java.util.Random;

import net.minecraft.block.Block;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.MetaBlock;
import cr0s.warpdrive.config.XmlRepresentable;

/**
 * Represents a set of fillers. Before using after construction, finishContruction() must be called.
 *
 * If FillerSet(blocks[]) is called, that is not necessary.
 *
 *
 */
public class FillerSet implements XmlRepresentable, Comparable {

	private MetaBlock[] weightedFillerBlocks;
	private FillerFactory factory;
	private String name;


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public FillerSet(MetaBlock[] blocks) {
		weightedFillerBlocks = blocks;
	}

	public FillerSet(String name) {

		this.name = name;

		weightedFillerBlocks = new MetaBlock[1];
		factory = new FillerFactory();
	}

	public MetaBlock getRandomBlock(Random rand) {
		return weightedFillerBlocks[rand.nextInt(weightedFillerBlocks.length)];
	}

	@Override
	public void loadFromXmlElement(Element element) throws InvalidXmlException {

		NodeList fillers = element.getElementsByTagName("filler");
		for (int i = 0; i < fillers.getLength(); i++) {

			Element filler = (Element) fillers.item(i);

			// Check there is a block name
			if (!filler.hasAttribute("block")) {
				throw new InvalidXmlException("Filler " + filler.getBaseURI() + " is missing a block tag!");
			}

			String blockName = filler.getAttribute("block");
			Block block = Block.getBlockFromName(blockName);
			if (block == null) {
				WarpDrive.logger.warn("Filler " + filler.getBaseURI() + " refers to missing block " + blockName + ": ignoring that entry...");
				continue;
			}

			// Get metadata attribute, defaults to 0
			int metadata = 0;
			String metaString = filler.getAttribute("metadata");
			if (!metaString.isEmpty()) {
				try {
					metadata = Integer.parseInt(metaString);
				} catch (NumberFormatException exception) {
					throw new InvalidXmlException("Filler " + filler.getBaseURI() + " metadata attribute is NaN!");
				}
			}

			boolean hasWeightOrRatio = false;

			// It is intentional that a filler could have both a ratio and a weight

			// Check for a weight and add it to the factory
			String stringWeight = filler.getAttribute("weight");
			int weight;

			if (!stringWeight.isEmpty()) {
				hasWeightOrRatio = true;

				try {
					weight = Integer.parseInt(stringWeight);

					factory.addWeightedBlock(block, metadata, weight);

				} catch (NumberFormatException exception) {
					throw new InvalidXmlException("Filler " + filler.getBaseURI() + " weight is NaN!");
				} catch (IllegalArgumentException exception) {
					throw new InvalidXmlException(exception.getMessage());
				}
			}

			// Check for a ratio attribute, and add it to the factory
			String stringRatio = filler.getAttribute("ratio");
			if (!stringRatio.isEmpty()) {
				hasWeightOrRatio = true;

				try {
					factory.addRatioBlock(block, metadata, stringRatio);

				} catch (IllegalArgumentException ex) {
					throw new InvalidXmlException(ex.getMessage());
				}
			}

			if (!hasWeightOrRatio) {
				throw new InvalidXmlException("Filler " + filler.getBaseURI() + " is missing a weight or a ratio!");
			}
		}
	}

	@Override
	public void saveToXmlElement(Element e, Document d) throws InvalidXmlException {
		throw new InvalidXmlException("Not supported");
	}

	/**
	 * Uses the data that has been loaded thus far to construct the array in order to make the FillerSet functional. Must be called before calling getRandomBlock()
	 *
	 * Clears the memory used for construction
	 */
	public void finishContruction() {
		WarpDrive.logger.info("Finishing construction of FillerSet " + name);
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

	/**
	 * Adds the blocks from the given fillerSet into this one. Must be pre-finishConstruction()
	 *
	 * @param fillerSet
	 *            The fillerset to add from
	 */
	public void loadFrom(FillerSet fillerSet) {
		factory.addFromFactory(fillerSet.factory);
	}
}
