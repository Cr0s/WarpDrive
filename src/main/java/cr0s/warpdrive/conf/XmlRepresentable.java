package cr0s.warpdrive.conf;

import org.w3c.dom.Element;

public interface XmlRepresentable {

	public void loadFromXmlElement(Element e);

	public void saveToXmlElement(Element e);

}
