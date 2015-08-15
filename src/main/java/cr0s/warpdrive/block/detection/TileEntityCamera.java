package cr0s.warpdrive.block.detection;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkPosition;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractInterfaced;
import cr0s.warpdrive.conf.WarpDriveConfig;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityCamera extends TileEntityAbstractInterfaced {
	private int frequency = -1;	// beam frequency

	private final static int REGISTRY_UPDATE_INTERVAL_TICKS = 15 * 20;
	private final static int PACKET_SEND_INTERVAL_TICKS = 60 * 20;

	private int registryUpdateTicks = 20;
	private int packetSendTicks = 20;

	public TileEntityCamera() {
		super();
		peripheralName = "warpdriveCamera";
		methodsArray = new String[] {
			"freq"
		};
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();

		// Update frequency on clients (recovery mechanism, no need to go too fast)
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			packetSendTicks--;
			if (packetSendTicks <= 0) {
				packetSendTicks = PACKET_SEND_INTERVAL_TICKS;
				PacketHandler.sendFreqPacket(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, frequency);
			}
		} else {
			registryUpdateTicks--;
			if (registryUpdateTicks <= 0) {
				registryUpdateTicks = REGISTRY_UPDATE_INTERVAL_TICKS;
				if (WarpDriveConfig.LOGGING_FREQUENCY) {
					WarpDrive.logger.info(this + " Updating registry (" + frequency + ")");
				}
				WarpDrive.instance.cameras.updateInRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord), frequency, 0);
			}
		}
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int parFrequency) {
		if (frequency != parFrequency) {
			frequency = parFrequency;
			if (WarpDriveConfig.LOGGING_FREQUENCY) {
				WarpDrive.logger.info(this + " Camera frequency set to " + frequency);
			}
	        // force update through main thread since CC runs on server as 'client'
	        packetSendTicks = 0;
	        registryUpdateTicks = 0;
		}
	}
	
	@Override
	public void invalidate() {
		if (WarpDriveConfig.LOGGING_FREQUENCY) {
			WarpDrive.logger.info(this + " invalidated");
		}
        WarpDrive.instance.cameras.removeFromRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord));
		super.invalidate();
	}
	
    @Override
    public void onChunkUnload() {
    	if (WarpDriveConfig.LOGGING_FREQUENCY) {
    		WarpDrive.logger.info(this + " onChunkUnload");
    	}
        WarpDrive.instance.cameras.removeFromRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord));
        super.onChunkUnload();
    }
    
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		frequency = tag.getInteger("frequency");
		if (WarpDriveConfig.LOGGING_FREQUENCY) {
			WarpDrive.logger.info(this + " readFromNBT");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("frequency", frequency);
		if (WarpDriveConfig.LOGGING_FREQUENCY) {
			WarpDrive.logger.info(this + " writeToNBT");
		}
	}

	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] freq(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			setFrequency(arguments.checkInteger(0));
		}
		return new Integer[] { frequency };
	}

	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
    	String methodName = methodsArray[method];
    	if (methodName.equals("freq")) {
			if (arguments.length == 1) {
				setFrequency(toInt(arguments[0]));
			}
			return new Integer[] { frequency };
    	}
    	return null;
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
}