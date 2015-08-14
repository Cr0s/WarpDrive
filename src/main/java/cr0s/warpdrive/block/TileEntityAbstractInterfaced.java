package cr0s.warpdrive.block;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import li.cil.oc.api.FileSystem;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.WarpDriveConfig;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

// OpenComputer API: https://github.com/MightyPirates/OpenComputers/tree/master-MC1.7.10/src/main/java/li/cil/oc/api

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers"),
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
})
public abstract class TileEntityAbstractInterfaced extends TileEntityAbstractBase implements IPeripheral, Environment {
	// Common computer properties
	private boolean interfacedFirstTick = true;
	protected String peripheralName = null;
	protected String[] methodsArray = {};
	
	// pre-loaded scripts support
	private volatile ManagedEnvironment OC_fileSystem = null;
	private volatile boolean CC_hasResource = false;
	private volatile boolean OC_hasResource = false;
	protected volatile List<String> CC_scripts = null;
	
	// OpenComputer specific properties
	protected Node		OC_node = null;
	protected boolean	OC_addedToNetwork = false;
	
	// ComputerCraft specific properties
	protected HashMap<Integer, IComputerAccess> connectedComputers = new HashMap<Integer, IComputerAccess>();
	
	private boolean assetExist(final String resourcePath) {
		URL url = getClass().getResource(resourcePath);
		return (url != null);
	}
	
	// TileEntity overrides, notably for OpenComputer
	@Override
 	public void updateEntity() {
		super.updateEntity();
		
		if (interfacedFirstTick) {
			if (WarpDriveConfig.isComputerCraftLoaded) {
				String CC_path = "/assets/" + WarpDrive.MODID.toLowerCase() + "/lua.ComputerCraft/" + peripheralName;
				CC_hasResource = assetExist(CC_path);
			}
			if (Loader.isModLoaded("OpenComputers")) {
				String OC_path = "/assets/" + WarpDrive.MODID.toLowerCase() + "/lua.OpenComputers/" + peripheralName;
				OC_hasResource = assetExist(OC_path);
			}
			
			// deferred constructor so the derived class can finish it's initialization first
			if (Loader.isModLoaded("OpenComputers")) {
				OC_constructor();
			}
			interfacedFirstTick = false;
			return;
		}
		
		if (Loader.isModLoaded("OpenComputers")) {
			if (!OC_addedToNetwork) {
				OC_addedToNetwork = true;
				Network.joinOrCreateNetwork(this);
			}
		}
	}

	@Override
	public void invalidate() {
		if (Loader.isModLoaded("OpenComputers")) {
			if (OC_node != null) {
				OC_node.remove();
				OC_node = null;
			}
		}
		super.invalidate();
	}
	
	@Override
	public void onChunkUnload() {
		if (Loader.isModLoaded("OpenComputers")) {
			if (OC_node != null) {
				OC_node.remove();
				OC_node = null;
			}
		}
		super.onChunkUnload();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if (Loader.isModLoaded("OpenComputers")) {
			if (OC_node != null && OC_node.host() == this) {
				OC_node.load(tag.getCompoundTag("oc:node"));
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if (Loader.isModLoaded("OpenComputers")) {
			if (OC_node != null && OC_node.host() == this) {
				final NBTTagCompound nodeNbt = new NBTTagCompound();
				OC_node.save(nodeNbt);
				tag.setTag("oc:node", nodeNbt);
			}
		}
	}
	
	@Override
	public int hashCode() {
		return (((((super.hashCode() + worldObj.provider.dimensionId << 4) + xCoord) << 4) + yCoord) << 4) + zCoord;
	}
	
	// Dirty cheap conversion methods
	@Optional.Method(modid = "OpenComputers")
	protected Object[] argumentsOCtoCC(Arguments args) {
		Object[] arguments = new Object[args.count()];
		int index = 0;
		for (Object arg:args) {
			arguments[index] = arg;
			index++;
		}
		return arguments;
	}
	
	// ComputerCraft methods
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public String getType() {
		return peripheralName;
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public String[] getMethodNames() {
		return methodsArray;
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		// empty stub
		return null;
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {
		int id = computer.getID();
		connectedComputers.put(id, computer);
		if (CC_hasResource && WarpDriveConfig.G_LUA_SCRIPTS != WarpDriveConfig.LUA_SCRIPTS_NONE) {
			computer.mount("/" + peripheralName, ComputerCraftAPI.createResourceMount(WarpDrive.class, WarpDrive.MODID.toLowerCase(), "lua.ComputerCraft/" + peripheralName));
	        computer.mount("/warpupdater", ComputerCraftAPI.createResourceMount(WarpDrive.class, WarpDrive.MODID.toLowerCase(), "lua.ComputerCraft/common/updater"));
			if (WarpDriveConfig.G_LUA_SCRIPTS == WarpDriveConfig.LUA_SCRIPTS_ALL) {
				for(String script : CC_scripts) {
					computer.mount("/" + script, ComputerCraftAPI.createResourceMount(WarpDrive.class, WarpDrive.MODID.toLowerCase(), "lua.ComputerCraft/" + peripheralName + "/" + script));
				}
			}
		}
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {
		int id = computer.getID();
		if (connectedComputers.containsKey(id)) {
			connectedComputers.remove(id);
		}
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other) {
		// WarpDrive.debugPrint("WarpInterfacedTE.equals");
		return other.hashCode() == hashCode();
	}
	
	// Computer abstraction methods
	protected void sendEvent(String eventName, Object[] arguments) {
		if (WarpDriveConfig.LOGGING_LUA) {
			WarpDrive.logger.info(this + " Sending event '" + eventName + "'");
		}
		if (WarpDriveConfig.isComputerCraftLoaded) {
			Set<Integer> keys = connectedComputers.keySet();
			for(Integer key:keys) {
				IComputerAccess comp = connectedComputers.get(key);
				comp.queueEvent(eventName, arguments);
			}
		}
	}
	
	// OpenComputers methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] interfaced(Context context, Arguments arguments) {
		return new String[] { "This is a WarpDrive computer interfaced tile entity." };
	}
	
	@Optional.Method(modid = "OpenComputers")
	private void OC_constructor() {
		OC_node = Network.newNode(this, Visibility.Network).withComponent(peripheralName).create();
		if (OC_hasResource && WarpDriveConfig.G_LUA_SCRIPTS != WarpDriveConfig.LUA_SCRIPTS_NONE) {
			OC_fileSystem = FileSystem.asManagedEnvironment(FileSystem.fromClass(getClass(), WarpDrive.MODID.toLowerCase(), "lua.OpenComputers/" + peripheralName), peripheralName);
		}
	}
	
	@Override
	@Optional.Method(modid = "OpenComputers")
	public Node node() {
		return OC_node;
	}
	
	@Override
	@Optional.Method(modid = "OpenComputers")
	public void onConnect(Node node) {
		if (node.host() instanceof Context) {
			// Attach our file system to new computers we get connected to.
			// Note that this is also called for all already present computers
			// when we're added to an already existing network, so we don't
			// have to loop through the existing nodes manually.
			if (OC_fileSystem != null) {
				node.connect(OC_fileSystem.node());
			}
		}
	}
	
	@Override
	@Optional.Method(modid = "OpenComputers")
	public void onDisconnect(Node node) {
		if (OC_fileSystem != null) {
			if (node.host() instanceof Context) {
				// Disconnecting from a single computer
				node.disconnect(OC_fileSystem.node());
			} else if (node == OC_node) {
				// Disconnecting from the network
				OC_fileSystem.node().remove();
			}
		}
	}
	
	@Override
	@Optional.Method(modid = "OpenComputers")
	public void onMessage(Message message) {
		// nothing special
	}
}
