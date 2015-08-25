package cr0s.warpdrive.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cpw.mods.fml.common.Loader;
import cr0s.warpdrive.WarpDrive;


public class ModRequirementChecker {

	/**
	 * Will check the given element for a mod attribute and return a string of all the ones that are not loaded, separated by commas
	 *
	 * @param e
	 *            Element to check
	 * @return A string, which is empty if all the mods are loaded.
	 * @throws InvalidXmlException
	 */
	public static String checkModRequirements(Element e) {

		String missingMods = "";

		for (String mod : e.getAttribute("mods").split(",")) {

			//TODO: add version check


			if (mod.isEmpty())
				continue;

			if (!Loader.isModLoaded(mod)) {
				missingMods = missingMods + mod + ", ";
			}

		}

		return missingMods;
	}

	/**
	 * Goes through every child node of the given node, and if it is an element and fails checkModRequirements() it is removed
	 *
	 * @param base
	 * @throws InvalidXmlException
	 */
	public static void doModReqSanitation(Node base) {

		NodeList children = base.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if (child instanceof Element) {

				String res = checkModRequirements((Element) child);

				if (!res.isEmpty()) {
					base.removeChild(child);
					WarpDrive.logger.info("Removed child element " + child.getBaseURI() + " of element " + base.getBaseURI() + " because these mods arent loaded: " + res);
				} else {

					doModReqSanitation(child);

				}

			}
		}

	}

}
