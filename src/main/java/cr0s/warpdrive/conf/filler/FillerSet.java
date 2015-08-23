package cr0s.warpdrive.conf.filler;

import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cr0s.warpdrive.conf.InvalidXmlException;
import cr0s.warpdrive.conf.MetaBlock;
import cr0s.warpdrive.conf.XmlRepresentable;

public class FillerSet implements XmlRepresentable {

	private MetaBlock[] weightedFillerBlocks;

	public FillerSet(MetaBlock[] blocks) {
		weightedFillerBlocks = blocks;
	}

	public FillerSet() {
		//To prevent getting rand.nextInt(0)
		weightedFillerBlocks = new MetaBlock[1];
	}

	public MetaBlock getRandomBlock(Random rand) {
		return weightedFillerBlocks[rand.nextInt(weightedFillerBlocks.length)];
	}

	@Override
	public void loadFromXmlElement(Element e) throws InvalidXmlException {
		
		FillerFactory fact = new FillerFactory();

	}
	
	@Override
	public void saveToXmlElement(Element e, Document d) throws InvalidXmlException {
		// Unneded

	}

}
