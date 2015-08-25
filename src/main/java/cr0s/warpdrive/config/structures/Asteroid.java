package cr0s.warpdrive.config.structures;

import org.w3c.dom.Element;

import cr0s.warpdrive.config.InvalidXmlException;

public class Asteroid extends Orb {

	public Asteroid(int diameter) {
		super(diameter);
	}

	@Override
	public void loadFromXmlElement(Element e) throws InvalidXmlException {

		super.loadFromXmlElement(e);

	}

}
