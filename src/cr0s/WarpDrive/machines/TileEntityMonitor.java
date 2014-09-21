package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cr0s.WarpDrive.PacketHandler;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = WarpDriveConfig.modid_OpenComputers),
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = WarpDriveConfig.modid_ComputerCraft)
})
public class TileEntityMonitor extends TileEntity implements IPeripheral, Environment {
	private int frequency = -1;

	private String peripheralName = "monitor";
	private String[] methodsArray =
	{
		"freq"
	};

	private final static int PACKET_SEND_INTERVAL_TICKS = 60 * 20;
	private int packetSendTicks = 20;

    protected Node node;
    protected boolean addedToNetwork = false;

    public TileEntityMonitor() {
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
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			packetSendTicks--;
			if (packetSendTicks <= 0) {
				packetSendTicks = PACKET_SEND_INTERVAL_TICKS;
				PacketHandler.sendFreqPacket(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, frequency);
			}
			return;
		}
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int parFrequency) {
		if (frequency != parFrequency) {
			frequency = parFrequency;
			WarpDrive.debugPrint("" + this + " Monitor frequency set to " + frequency);
	        // force update through main thread since CC runs on server as 'client'
	        packetSendTicks = 0;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		frequency = tag.getInteger("frequency");
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	readFromNBT_OC(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("frequency", frequency);
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	writeToNBT_OC(tag);
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
		if (arguments.length == 1) {
			setFrequency(((Double)arguments[0]).intValue());
		}
		return new Integer[] { frequency };
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public void attach(IComputerAccess computer) {
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public void detach(IComputerAccess computer) {
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public boolean equals(IPeripheral other) {
		return other == this;
	}
	
	@Override
	public String toString() {
        return String.format("%s/%d \'%d\' @ \'%s\' %d, %d, %d", new Object[] {
       		getClass().getSimpleName(),
       		Integer.valueOf(hashCode()),
       		frequency,
       		worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
       		xCoord, yCoord, zCoord});
	}

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
	public Object[] freq(Context context, Arguments args) throws Exception {
		if (args.count() == 0) {
			return new Integer[] { frequency };
		} else if ((args.count() == 1) && args.isInteger(0)) {
			setFrequency(args.checkInteger(0));
			return new Integer[] { frequency };
		} else {
			throw new Exception("\"freq\" expects zero arguments or one numerical argument.");
		}
	}
}