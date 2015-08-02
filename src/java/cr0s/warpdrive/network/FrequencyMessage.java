package cr0s.warpdrive.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.machines.TileEntityCamera;
import cr0s.warpdrive.machines.TileEntityLaser;
import cr0s.warpdrive.machines.TileEntityMonitor;


public class FrequencyMessage implements IMessage, IMessageHandler<FrequencyMessage, IMessage> {
	private int x;
	private int y;
	private int z;
	private int frequency;
	
	public FrequencyMessage() {
		// required on receiving side
	}
	
	public FrequencyMessage(final Vector3 target, final int frequency) {
		this.x = target.intX();
		this.y = target.intY();
		this.z = target.intZ();
		this.frequency = frequency;
	}
	
	public FrequencyMessage(final int x, final int y, final int z, final int frequency) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.frequency = frequency;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
		frequency = buffer.readInt();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		buffer.writeInt(frequency);
	}
	
	private void handle(World worldObj) {
		TileEntity te = worldObj.getTileEntity(x, y, z);
		if (te != null) {
			if (te instanceof TileEntityMonitor) {
				((TileEntityMonitor) te).setFrequency(frequency);
			} else if (te instanceof TileEntityCamera) {
				((TileEntityCamera) te).setFrequency(frequency);
			} else if (te instanceof TileEntityLaser) {
				((TileEntityLaser) te).setCameraFrequency(frequency);
			} else {
				WarpDrive.logger.warning("Received frequency packet: (" + x + ", " + y + ", " + z + ") is not a valid tile entity");
			}
		} else {
			WarpDrive.logger.warning("Received frequency packet: (" + x + ", " + y + ", " + z + ") has no tile entity");
		}
 	}
	
	@Override
	public IMessage onMessage(FrequencyMessage frequencyMessage, MessageContext context) {
		// skip in case player just logged in
		if (context.getServerHandler().playerEntity.worldObj == null) {
			WarpDrive.logger.severe("WorldObj is null, ignoring frequency packet");
			return null;
		}
		
		if (WarpDriveConfig.G_DEBUGMODE) {
			WarpDrive.debugPrint("Received frequency packet: (" + frequencyMessage.x + ", " + frequencyMessage.y + ", " + frequencyMessage.z + ") frequency '" + frequencyMessage.frequency + "'");
		}
		
		handle(context.getServerHandler().playerEntity.worldObj);
		
		return null;	// no response
	}
}
