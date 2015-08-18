package cr0s.warpdrive.conf;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cpw.mods.fml.common.Loader;

public class ModRequirementChecker {

	public static String getMissingMods(Element e) throws InvalidXmlException {

		String missingMods = "";

		NodeList mods = e.getElementsByTagName("mod");
		for (int j = 0; j < mods.getLength(); j++) {
			Element mod = (Element) mods.item(j);
			if (!mod.hasAttribute("name")) {
				throw new InvalidXmlException("A mod requirement is missing the name attribute!");
			}

			String name = mod.getAttribute("name");
			if (!Loader.isModLoaded(name)) {
				missingMods = missingMods + name + ", ";
			}
			
		}
		
		return missingMods;
	}

}
