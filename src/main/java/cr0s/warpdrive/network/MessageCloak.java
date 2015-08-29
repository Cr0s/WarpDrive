package cr0s.warpdrive.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CloakedArea;


public class MessageCloak implements IMessage, IMessageHandler<MessageCloak, IMessage> {
	private int coreX;
	private int coreY;
	private int coreZ;
	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;
	private byte tier;
	private boolean decloak;
	
	public MessageCloak() {
		// required on receiving side
	}
	
	public MessageCloak(CloakedArea area, final boolean decloak) {
		this.coreX = area.coreX;
		this.coreY = area.coreY;
		this.coreZ = area.coreZ;
		this.minX = area.minX;
		this.minY = area.minY;
		this.minZ = area.minZ;
		this.maxX = area.maxX;
		this.maxY = area.maxY;
		this.maxZ = area.maxZ;
		this.tier = area.tier;
		this.decloak = decloak;
	}
	
	@Override
	public void fromBytes(ByteBuf buffer) {
		coreX = buffer.readInt();
		coreY = buffer.readInt();
		coreZ = buffer.readInt();
		minX = buffer.readInt();
		minY = buffer.readInt();
		minZ = buffer.readInt();
		maxX = buffer.readInt();
		maxY = buffer.readInt();
		maxZ = buffer.readInt();
		decloak = buffer.readBoolean();
		tier = buffer.readByte();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(coreX);
		buffer.writeInt(coreY);
		buffer.writeInt(coreZ);
		buffer.writeInt(minX);
		buffer.writeInt(minY);
		buffer.writeInt(minZ);
		buffer.writeInt(maxX);
		buffer.writeInt(maxY);
		buffer.writeInt(maxZ);
		buffer.writeBoolean(decloak);
		buffer.writeByte(tier);
	}
	
	@SideOnly(Side.CLIENT)
	private void handle(EntityClientPlayerMP player) {
		if (decloak) {
			// reveal the area
			WarpDrive.cloaks.removeCloakedArea(player.worldObj.provider.dimensionId, coreX, coreY, coreZ);
		} else { 
			// Hide the area
			WarpDrive.cloaks.updateCloakedArea(player.worldObj, player.worldObj.provider.dimensionId, coreX, coreY, coreZ, tier, minX, minY, minZ, maxX, maxY, maxZ);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageCloak cloakMessage, MessageContext context) {
		// skip in case player just logged in
		if (Minecraft.getMinecraft().theWorld == null) {
			WarpDrive.logger.error("WorldObj is null, ignoring cloak packet");
			return null;
		}
		
		if (WarpDriveConfig.LOGGING_CLOAKING) {
			WarpDrive.logger.info("Received cloak packet: " + ((cloakMessage.decloak) ? "DEcloaked" : "cloaked")
				+ "area: (" + cloakMessage.minX + "; " + cloakMessage.minY + "; " + cloakMessage.minZ
				+ ") -> (" + cloakMessage.maxX + "; " + cloakMessage.maxY + "; " + cloakMessage.maxZ + ") tier " + cloakMessage.tier);
		}
		
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		if ( cloakMessage.minX <= player.posX && (cloakMessage.maxX + 1) > player.posX
		  && cloakMessage.minY <= player.posY && (cloakMessage.maxY + 1) > player.posY
		  && cloakMessage.minZ <= player.posZ && (cloakMessage.maxZ + 1) > player.posZ) {
			return null;
		}
		cloakMessage.handle(player);
		
		return null;	// no response
	}
}
