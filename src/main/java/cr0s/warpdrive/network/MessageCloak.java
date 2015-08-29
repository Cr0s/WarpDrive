package cr0s.warpdrive.network;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.render.EntityFXBeam;


public class MessageCloak implements IMessage, IMessageHandler<MessageCloak, IMessage> {
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
	
	public MessageCloak(final AxisAlignedBB aabb, final byte tier, final boolean decloak) {
		this.minX = (int)aabb.minX;
		this.minY = (int)aabb.minY;
		this.minZ = (int)aabb.minZ;
		this.maxX = (int)aabb.maxX;
		this.maxY = (int)aabb.maxY;
		this.maxZ = (int)aabb.maxZ;
		this.tier = tier;
		this.decloak = decloak;
	}
	
	@Override
	public void fromBytes(ByteBuf buffer) {
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
		// Hide the area
		if (!decloak) {
			if (WarpDriveConfig.LOGGING_CLOAKING) {
				WarpDrive.logger.info("Received cloak packet: Removing blocks...");
			}
			// Hide the blocks within area
			World worldObj = player.worldObj;
			Block cloakBlockID = (tier == 1) ? WarpDrive.blockGas : Blocks.air;
			int cloakBlockMetadata = (tier == 1) ? 5 : 0;
			int minYmap = Math.max(0, minY);
			int maxYmap = Math.min(255, maxY);
			for (int y = minYmap; y <= maxYmap; y++) {
				for (int x = minX; x <= maxX; x++) {
					for (int z = minZ; z <= maxZ; z++) {
						Block block = worldObj.getBlock(x, y, z);
						if (!block.isAssociatedBlock(Blocks.air)) {
							// 1.6.4 was skipping CC peripherals with metadata 2 and 4 here...
							worldObj.setBlock(x, y, z, cloakBlockID, cloakBlockMetadata, 4);
						}
					}
				}
			}

			if (WarpDriveConfig.LOGGING_CLOAKING) {
				WarpDrive.logger.info("Received cloak packet: Removing entities...");
			}
			// Hide any entities inside area
			AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
			List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb);
			for (Entity e : list) {
				worldObj.removeEntity(e);
				((WorldClient) worldObj).removeEntityFromWorld(e.getEntityId());
			}
			
		} else { // reveal the area
			player.worldObj.markBlockRangeForRenderUpdate(minX - 1, Math.max(0, minY - 1), minZ - 1, maxX + 1, Math.min(255, maxY + 1), maxZ + 1);

			// Make some graphics
			int numLasers = 80 + player.worldObj.rand.nextInt(50);
			
			double centerX = (minX + maxX) / 2.0D;
			double centerY = (minY + maxY) / 2.0D;
			double centerZ = (minZ + maxZ) / 2.0D;
			double radiusX = (maxX - minX) / 2.0D + 5.0D;
			double radiusY = (maxY - minY) / 2.0D + 5.0D;
			double radiusZ = (maxZ - minZ) / 2.0D + 5.0D;
			
			for (int i = 0; i < numLasers; i++) {
				FMLClientHandler.instance().getClient().effectRenderer.addEffect(new EntityFXBeam(player.worldObj,
					new Vector3(
						centerX + radiusX * player.worldObj.rand.nextGaussian(),
						centerY + radiusY * player.worldObj.rand.nextGaussian(),
						centerZ + radiusZ * player.worldObj.rand.nextGaussian()),
					new Vector3(
						centerX + radiusX * player.worldObj.rand.nextGaussian(),
						centerY + radiusY * player.worldObj.rand.nextGaussian(),
						centerZ + radiusZ * player.worldObj.rand.nextGaussian()),
					player.worldObj.rand.nextFloat(), player.worldObj.rand.nextFloat(), player.worldObj.rand.nextFloat(),
					60 + player.worldObj.rand.nextInt(60), 100));
			}
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
