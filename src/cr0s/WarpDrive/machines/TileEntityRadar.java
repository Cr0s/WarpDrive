package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.ArrayList;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import cr0s.WarpDrive.*;

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = WarpDriveConfig.modid_OpenComputers),
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = WarpDriveConfig.modid_ComputerCraft)
})
public class TileEntityRadar extends WarpEnergyTE implements IPeripheral, Environment {
	private String peripheralName = "radar";
	private String[] methodsArray =
	{
		"scanRay",			// 0
		"scanRadius",		// 1
		"getResultsCount",	// 2
		"getResult",		// 3
		"getEnergyLevel",	// 4
		"pos"				// 5
	};

	private ArrayList<TileEntityReactor> results;

	private int scanRadius = 0;
	private int cooldownTime = 0;

    protected Node node;
    protected boolean addedToNetwork = false;

    public TileEntityRadar() {
    	if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
    		initOC();
    }
    
    @Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
    private void initOC() {
    	node = Network.newNode(this, Visibility.Network).withComponent(peripheralName).create();
    }
    
    @Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
    private void addToNetwork() {
		if (!addedToNetwork) {
			addedToNetwork = true;
			Network.joinOrCreateNetwork(this);
		}
    }

	@Override
	public void updateEntity() {
		if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
			addToNetwork();
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		super.updateEntity();

		try {
			if (getBlockMetadata() == 2) {
				cooldownTime++;
				if (cooldownTime > (20 * ((scanRadius / 1000) + 1))) {
					WarpDrive.debugPrint("" + this + " Scanning over " + scanRadius + " radius...");
					results = WarpDrive.warpCores.searchWarpCoresInRadius(xCoord, yCoord, zCoord, scanRadius);
					WarpDrive.debugPrint("" + this + " Scan found " + results.size() + " results");
					worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
					cooldownTime = 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	readFromNBT_OC(tag);
		super.readFromNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	writeToNBT_OC(tag);
		super.writeToNBT(tag);
	}

	// IPeripheral methods implementation
	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public String getType() {
		return peripheralName;
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public String[] getMethodNames() {
		return methodsArray;
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
		switch (method) {
			case 0: // scanRay (toX, toY, toZ)
				return new Object[] { -1 };
				
			case 1: // scanRadius (radius)
				// always clear results
				results = null;
				
				// validate parameters
				if (arguments.length != 1) {
					return new Boolean[] { false };
				}
				int radius;
				try {
					radius = ((Double)arguments[0]).intValue();
				} catch(Exception e) {
	               	return new Boolean[] { false };
	            }
				if (radius <= 0 || radius > 10000) {
					scanRadius = 0;
					return new Boolean[] { false };
				}
				if (!consumeEnergy(Math.max(radius, 100) * Math.max(radius, 100), false)) {
					return new Boolean[] { false };
				}
				
				// Begin searching
				scanRadius = radius;
				cooldownTime = 0;
				if (getBlockMetadata() != 2) {
					worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 1 + 2);
				}
				return new Boolean[] { true };

			case 2: // getResultsCount
				if (results != null) {
					return new Integer[] { results.size() };
				}
				return new Integer[] { -1 };
				
			case 3: // getResult
				if (arguments.length == 1 && (results != null)) {
					int index;
					try {
						index = ((Double)arguments[0]).intValue();
					} catch(Exception e) {
						return new Object[] { "FAIL", 0, 0, 0 };
					}
					if (index >= 0 && index < results.size()) {
						TileEntityReactor res = results.get(index);
						if (res != null)
						{
							int yAddition = (res.worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID) ? 256 : (res.worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) ? 512 : 0;
							return new Object[] { res.coreFrequency, res.xCoord, res.yCoord + yAddition, res.zCoord };
						}
					}
				}
				return new Object[] { "FAIL", 0, 0, 0 };
				
			case 4: // getEnergyLevel
				return new Integer[] { getEnergyStored() };
				
			case 5: // Pos
				return new Integer[] { xCoord, yCoord, zCoord };
		}

		return null;
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public void attach(IComputerAccess computer) {
		if (WarpDriveConfig.G_LUA_SCRIPTS != WarpDriveConfig.LUA_SCRIPTS_NONE) {
			computer.mount("/radar", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/radar"));
	        computer.mount("/warpupdater", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/common/updater"));
			if (WarpDriveConfig.G_LUA_SCRIPTS == WarpDriveConfig.LUA_SCRIPTS_ALL) {
				computer.mount("/scan", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/radar/scan"));
				computer.mount("/ping", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/radar/ping"));
			}
		}
		if (getBlockMetadata() == 0) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
		}
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public void detach(IComputerAccess computer) {
		// worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);
	}

	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.WR_MAX_ENERGY_VALUE;
	}

	// IEnergySink methods implementation
	@Override
	public int getMaxSafeInput() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean equals(IPeripheral other) {
		return other == this;
	}
    
    @Override
    public boolean canInputEnergy(ForgeDirection from) {
    	return true;
    }

    // OpenComputers.

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Node node() {
		return node;
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onConnect(Node node) {}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onDisconnect(Node node) {}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onMessage(Message message) {}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void invalidate() {
		super.invalidate();
		if (node != null) node.remove();
	}

	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void readFromNBT_OC(final NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (node != null && node.host() == this) {
			node.load(nbt.getCompoundTag("oc:node"));
		}
	}

	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void writeToNBT_OC(final NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if (node != null && node.host() == this) {
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			node.save(nodeNbt);
			nbt.setTag("oc:node", nodeNbt);
		}
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] scanRay(Context context, Arguments args) {
		return new Object[] { -1 };
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] scanRadius(Context context, Arguments args) {
		// always clear results
		results = null;
		
		// validate parameters
		if (args.count() != 1) {
			return new Boolean[] { false };
		}
		int radius;
		try {
			radius = args.checkInteger(0);
		} catch(Exception e) {
           	return new Boolean[] { false };
        }
		if (radius <= 0 || radius > 10000) {
			scanRadius = 0;
			return new Boolean[] { false };
		}
		if (!consumeEnergy(Math.max(radius, 100) * Math.max(radius, 100), false)) {
			return new Boolean[] { false };
		}
		
		// Begin searching
		scanRadius = radius;
		cooldownTime = 0;
		if (getBlockMetadata() != 2) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 1 + 2);
		}
		return new Boolean[] { true };
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] getResultsCount(Context context, Arguments args) {
		if (results != null) {
			return new Integer[] { results.size() };
		}
		return new Integer[] { -1 };
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] getResult(Context context, Arguments args) {
		if (args.count() == 1 && (results != null)) {
			int index;
			if (args.isInteger(0))
				index = args.checkInteger(0);
			else
				return new Object[] { "FAIL", 0, 0, 0 };
			if (index >= 0 && index < results.size()) {
				TileEntityReactor res = results.get(index);
				if (res != null)
				{
					int yAddition = (res.worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID) ? 256 : (res.worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) ? 512 : 0;
					return new Object[] { res.coreFrequency, res.xCoord, res.yCoord + yAddition, res.zCoord };
				}
			}
		}
		return new Object[] { "FAIL", 0, 0, 0 };
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] getEnergyLevel(Context context, Arguments args) {
		return new Integer[] { getEnergyStored() };
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] pos(Context context, Arguments args) {
		return new Integer[] { xCoord, yCoord, zCoord };
	}
}
