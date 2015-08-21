package cr0s.warpdrive.conf.structures;

import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cr0s.warpdrive.conf.InvalidXmlException;
import cr0s.warpdrive.conf.MetaBlock;
import cr0s.warpdrive.conf.XmlRepresentable;

public class FillerSet implements XmlRepresentable {

	private MetaBlock[] weightedFillerBlocks;

	public FillerSet() {
		//To prevent getting rand.nextInt(0)
		weightedFillerBlocks = new MetaBlock[1];
	}

	public MetaBlock getRandomBlock(Random rand) {
		return weightedFillerBlocks[rand.nextInt(weightedFillerBlocks.length)];
	}

	@Override
	public void loadFromXmlElement(Element e) throws InvalidXmlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveToXmlElement(Element e, Document d) throws InvalidXmlException {
		// TODO Not really needed
		
	}
	

}
