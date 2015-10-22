package cr0s.warpdrive.block.detection;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractInterfaced;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityMonitor extends TileEntityAbstractInterfaced {
	private int videoChannel = -1;
	
	private final static int PACKET_SEND_INTERVAL_TICKS = 60 * 20;
	private int packetSendTicks = 20;
	
	public TileEntityMonitor() {
		super();
		
		peripheralName = "warpdriveMonitor";
		addMethods(new String[] {
			"videoChannel"
		});
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			packetSendTicks--;
			if (packetSendTicks <= 0) {
				packetSendTicks = PACKET_SEND_INTERVAL_TICKS;
				PacketHandler.sendFreqPacket(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, videoChannel);
			}
			return;
		}
	}
	
	public int getVideoChannel() {
		return videoChannel;
	}
	
	public void setVideoChannel(int parVideoChannel) {
		if (videoChannel != parVideoChannel) {
			videoChannel = parVideoChannel;
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info(this + " Monitor frequency set to " + videoChannel);
			}
			// force update through main thread since CC runs on server as 'client'
			packetSendTicks = 0;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		videoChannel = tag.getInteger("frequency") + tag.getInteger("videoChannel");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("videoChannel", videoChannel);
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] videoChannel(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			setVideoChannel(arguments.checkInteger(0));
		}
		return new Integer[] { videoChannel };
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = getMethodName(method);
		
		if (methodName.equals("videoChannel")) {
			if (arguments.length == 1) {
				setVideoChannel(toInt(arguments[0]));
			}
			return new Integer[] { videoChannel };
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	@Override
	public String toString() {
		return String.format("%s \'%d\' @ \'%s\' %d, %d, %d", new Object[] {
				getClass().getSimpleName(),
				videoChannel,
				worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
						xCoord, yCoord, zCoord});
	}
}