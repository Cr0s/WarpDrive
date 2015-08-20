package cr0s.warpdrive.conf;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cpw.mods.fml.common.Loader;

public class ModRequirementChecker {

	/**
	 * Will check the given element for any mod tags on the first level and return a string of all the ones that are not loaded, separated by commas
	 *
	 * @param e
	 *            Element to check
	 * @return A string, which is empty if all the mods are loaded.
	 * @throws InvalidXmlException
	 */
	public static String checkModRequirements(Element e) throws InvalidXmlException {

		String missingMods = "";

		NodeList mods = e.getElementsByTagName("mod");
		for (int j = 0; j < mods.getLength(); j++) {
			Element mod = (Element) mods.item(j);
			
			if (!mod.hasAttribute("name")) {
				throw new InvalidXmlException("A mod requirement is missing the name attribute!");
			}

			//TODO: add version check

			String name = mod.getAttribute("name");
			if (!Loader.isModLoaded(name)) {
				missingMods = missingMods + name + ", ";
			}
			
		}
		
		return missingMods;
	}

}
