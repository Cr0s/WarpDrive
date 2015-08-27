package cr0s.warpdrive.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityLaser;
import cr0s.warpdrive.block.detection.TileEntityCamera;
import cr0s.warpdrive.block.detection.TileEntityMonitor;
import cr0s.warpdrive.config.WarpDriveConfig;


public class MessageFrequency implements IMessage, IMessageHandler<MessageFrequency, IMessage> {
	private int x;
	private int y;
	private int z;
	private int frequency;
	
	public MessageFrequency() {
		// required on receiving side
	}
	
	public MessageFrequency(final int x, final int y, final int z, final int frequency) {
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
	
	@SideOnly(Side.CLIENT)
	private void handle(World worldObj) {
		TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
		if (tileEntity != null) {
			if (tileEntity instanceof TileEntityMonitor) {
				((TileEntityMonitor) tileEntity).setFrequency(frequency);
			} else if (tileEntity instanceof TileEntityCamera) {
				((TileEntityCamera) tileEntity).setFrequency(frequency);
			} else if (tileEntity instanceof TileEntityLaser) {
				((TileEntityLaser) tileEntity).setCameraFrequency(frequency);
			} else {
				WarpDrive.logger.error("Received frequency packet: (" + x + ", " + y + ", " + z + ") is not a valid tile entity");
			}
		} else {
			WarpDrive.logger.error("Received frequency packet: (" + x + ", " + y + ", " + z + ") has no tile entity");
		}
 	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageFrequency frequencyMessage, MessageContext context) {
		// skip in case player just logged in
		if (Minecraft.getMinecraft().theWorld == null) {
			WarpDrive.logger.error("WorldObj is null, ignoring frequency packet");
			return null;
		}
		
		if (WarpDriveConfig.LOGGING_FREQUENCY) {
			WarpDrive.logger.info("Received frequency packet: (" + frequencyMessage.x + ", " + frequencyMessage.y + ", " + frequencyMessage.z + ") frequency '" + frequencyMessage.frequency + "'");
		}
		
		frequencyMessage.handle(Minecraft.getMinecraft().theWorld);
		
		return null;	// no response
	}
}
