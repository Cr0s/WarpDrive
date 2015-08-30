package cr0s.warpdrive.config;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cpw.mods.fml.common.Loader;
import cr0s.warpdrive.WarpDrive;


public class XmlPreprocessor {

	/**
	 * Will check the given element for a mod attribute and return a string of all the ones that are not loaded, separated by commas
	 *
	 * @param e
	 *            Element to check
	 * @return A string, which is empty if all the mods are loaded.
	 * @throws InvalidXmlException
	 */
	public static ModCheckResults checkModRequirements(Element e) {

		ModCheckResults modErrors = new ModCheckResults();

		for (String mod : e.getAttribute("mods").split(",")) {

			//TODO: add version check


			if (mod.isEmpty())
				continue;

			if (mod.startsWith("!")) {

				if (Loader.isModLoaded(mod.substring(1)))
					modErrors.addMod(mod, "loaded");

			} else if (!Loader.isModLoaded(mod))
				modErrors.addMod(mod, "not loaded");

		}

		return modErrors;
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

				ModCheckResults res = checkModRequirements((Element) child);

				if (!res.isEmpty()) {
					base.removeChild(child);
					WarpDrive.logger.info("Removed child element " + child.getBaseURI() + " of element " + base.getBaseURI() + ", results: " + res);
				} else {

					doModReqSanitation(child);

				}

			}
		}

	}

	public static void doLogicPreprocessing(Element root) {

	}

	public static class ModCheckResults {

		private TreeMap<String, String> mods;

		public ModCheckResults() {
			mods = new TreeMap<String, String>();
		}

		public void addMod(String name, String error) {
			mods.put(name, error);
		}

		public boolean isEmpty() {
			return mods.isEmpty();
		}

		@Override
		public String toString() {
			String s = "{";

			for (Entry<String, String> e : mods.entrySet())
				s = s + e.getKey() + ": " + e.getValue() + ", ";

			return s + "}";

		}

	}

}
