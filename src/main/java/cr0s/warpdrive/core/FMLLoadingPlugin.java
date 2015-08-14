package cr0s.warpdrive.core;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name(value = "WarpDriveCore") // The readable mod name
@IFMLLoadingPlugin.MCVersion(value = "1.7.10")
@IFMLLoadingPlugin.TransformerExclusions(value = "cr0s.warpdrive.core.")
@IFMLLoadingPlugin.SortingIndex(value = 1001) // > 1000 to work with srg names
public class FMLLoadingPlugin implements IFMLLoadingPlugin, IFMLCallHook {
	public static File location;
	private boolean debugLog = false;
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] { ClassTransformer.class.getName() };
	}
	
	@Override
	public String getModContainerClass() {
		return MyDummyModContainer.class.getName();
	}
	
	@Override
	public String getSetupClass() {
		return getClass().getName();
	}
	
	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
		if (debugLog) {
			System.out.println("injectData:");
			for (Entry<String, Object> entry : data.entrySet()) {
				System.out.println("- " + entry.getKey() + " = " + entry.getValue());
			}
		}
	}
	
	@Override
	public Void call() throws Exception {
		if (debugLog) { System.out.println("call()"); }
		return null;
	}
	
	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}