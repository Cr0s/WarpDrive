package cr0s.warpdriveCore;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion(value = "1.6.4")
public class WCFMLLoadingPlugin implements IFMLLoadingPlugin, IFMLCallHook {
	public static File location;

	@Override
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { WCClassTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass() {
		return WCDummyContainer.class.getName();
	}

	@Override
	public String getSetupClass() {
		return getClass().getName();
	}

	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
		System.out.println("*** Transformer jar location location.getName: " + location.getName());
	}

	@Override
	public Void call() throws Exception {
		System.out.println("[WCD] call()");
		return null;
	}
}