package cr0s.warpdrive.conf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XmlRepresentable {

	public void loadFromXmlElement(Element e) throws InvalidXmlException;

	public void saveToXmlElement(Element e, Document d) throws InvalidXmlException;

}
