package cr0s.warpdrive.data;

import java.util.LinkedList;
import java.util.List;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.network.PacketHandler;
import cr0s.warpdrive.render.EntityFXBeam;

public class CloakedArea {
	public int dimensionId = -666;
	public int coreX, coreY, coreZ;
	public int minX, minY, minZ;
	public int maxX, maxY, maxZ;
	private LinkedList<String> playersInArea;
	public byte tier = 0;
	public Block fogBlock;
	public int fogMetadata;
	
	public CloakedArea(World worldObj,
			final int dimensionId, final int x, final int y, final int z, final byte tier,
			final int minX, final int minY, final int minZ,
			final int maxX, final int maxY, final int maxZ) {
		this.dimensionId = dimensionId;
		this.coreX = x;
		this.coreY = y;
		this.coreZ = z;
		this.tier = tier;
		
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		
		this.playersInArea = new LinkedList<String>();
		
		if (worldObj != null) {
			try {
				// Add all players currently inside the field
				List<EntityPlayer> list = worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
				for (EntityPlayer player : list) {
					addPlayer(player.getCommandSenderName());
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		
		if (tier == 1) {
			fogBlock = WarpDrive.blockGas;
			fogMetadata = 5;
		} else {
			fogBlock = Blocks.air;
			fogMetadata = 0;
		}
	}
	
	public boolean isPlayerListedInArea(String username) {
		for (String playerInArea : playersInArea) {
			if (playerInArea.equals(username)) {
				return true;
			}
		}
		
		return false;
	}
	
	private void removePlayer(String username) {
		for (int i = 0; i < playersInArea.size(); i++) {
			if (playersInArea.get(i).equals(username)) {
				playersInArea.remove(i);
				return;
			}
		}
	}
	
	private void addPlayer(String username) {
		if (!isPlayerListedInArea(username)) {
			playersInArea.add(username);
		}
	}
	
	public boolean isEntityWithinArea(EntityLivingBase entity) {
		return (minX <= entity.posX && (maxX + 1) > entity.posX
			 && minY <= (entity.posY + entity.height) && (maxY + 1) > entity.posY
			 && minZ <= entity.posZ && (maxZ + 1) > entity.posZ);
	}
	
	public boolean isBlockWithinArea(final int x, final int y, final int z) {
		return (minX <= x && (maxX + 1) > x
			 && minY <= y && (maxY + 1) > y
			 && minZ <= z && (maxZ + 1) > z);
	}
	
	// Sending only if field changes: sets up or collapsing
	@SideOnly(Side.SERVER)
	public void sendCloakPacketToPlayersEx(final boolean decloak) {
		if (WarpDriveConfig.LOGGING_CLOAKING) {
			WarpDrive.logger.info("sendCloakPacketToPlayersEx " + decloak);
		}
		final int RADIUS = 250;
		
		double midX = minX + (Math.abs(maxX - minX) / 2.0D);
		double midY = minY + (Math.abs(maxY - minY) / 2.0D);
		double midZ = minZ + (Math.abs(maxZ - minZ) / 2.0D);
		
		for (int j = 0; j < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); j++) {
			EntityPlayerMP entityPlayerMP = (EntityPlayerMP) MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(j);
			
			if (entityPlayerMP.dimension == dimensionId) {
				double dX = midX - entityPlayerMP.posX;
				double dY = midY - entityPlayerMP.posY;
				double dZ = midZ - entityPlayerMP.posZ;
				
				if (Math.abs(dX) < RADIUS && Math.abs(dY) < RADIUS && Math.abs(dZ) < RADIUS) {
					if (decloak) {
						revealChunksToPlayer(entityPlayerMP);
						revealEntitiesToPlayer(entityPlayerMP);
					}
					
					if (!isEntityWithinArea(entityPlayerMP) && !decloak) {
						PacketHandler.sendCloakPacket(entityPlayerMP, this, false);
					} else if (decloak) {
						PacketHandler.sendCloakPacket(entityPlayerMP, this, true);
					}
				}
			}
		}
	}
	
	public void updatePlayer(EntityPlayer player) {
		if (isEntityWithinArea(player)) {
			if (!isPlayerListedInArea(player.getCommandSenderName())) {
				if (WarpDriveConfig.LOGGING_CLOAKING) {
					WarpDrive.logger.info(this + " Player " + player.getCommandSenderName() + " has entered");
				}
				addPlayer(player.getCommandSenderName());
				revealChunksToPlayer(player);
				revealEntitiesToPlayer(player);
				PacketHandler.sendCloakPacket(player, this, false);
			}
		} else {
			if (isPlayerListedInArea(player.getCommandSenderName())) {
				if (WarpDriveConfig.LOGGING_CLOAKING) {
					WarpDrive.logger.info(this + " Player " + player.getCommandSenderName() + " has left");
				}
				removePlayer(player.getCommandSenderName());
				MinecraftServer
						.getServer()
						.getConfigurationManager()
						.sendToAllNearExcept(player, player.posX, player.posY, player.posZ, 100, player.worldObj.provider.dimensionId,
								PacketHandler.getPacketForThisEntity(player));
				PacketHandler.sendCloakPacket(player, this, false);
			}
		}
	}
	
	public void revealChunksToPlayer(EntityPlayer player) {
		if (WarpDriveConfig.LOGGING_CLOAKING) {
			 WarpDrive.logger.info(this + " Revealing cloaked blocks to player " + player.getCommandSenderName());
		}
		int minYclamped = Math.max(0, minY);
		int maxYclamped = Math.min(255, maxY);
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minYclamped; y <= maxYclamped; y++) {
					if (!player.worldObj.getBlock(x, y, z).isAssociatedBlock(Blocks.air)) {
						player.worldObj.markBlockForUpdate(x, y, z);
						
						JumpBlock.refreshBlockStateOnClient(player.worldObj, x, y, z);
					}
				}
			}
		}
		
		/*
		ArrayList<Chunk> chunksToSend = new ArrayList<Chunk>();
		
		for (int x = minX >> 4; x <= maxX >> 4; x++) {
			for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
				chunksToSend.add(p.worldObj.getChunkFromChunkCoords(x, z));
			}
		}
		
		//System.outprintln("[Cloak] Sending " + chunksToSend.size() + " chunks to player " + p.username);
		((EntityPlayerMP) p).playerNetServerHandler.sendPacketToPlayer(new Packet56MapChunks(chunksToSend));
		
		//System.outprintln("[Cloak] Sending decloak packet to player " + p.username);
		area.sendCloakPacketToPlayer(p, true);
		// decloak = true
		
		/**/
	}
	
	public void revealEntitiesToPlayer(EntityPlayer player) {
		List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
		
		for (Entity entity : list) {
			Packet packet = PacketHandler.getPacketForThisEntity(entity);
			if (packet != null) {
				if (WarpDriveConfig.LOGGING_CLOAKING) {
					WarpDrive.logger.warn("Revealing entity " + entity + " with packet " + packet);
				}
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
			} else if (WarpDriveConfig.LOGGING_CLOAKING) {
				WarpDrive.logger.warn("Revealing entity " + entity + " fails: null packet");
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void clientCloak() {
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		
		// Hide the blocks within area
		if (WarpDriveConfig.LOGGING_CLOAKING) { WarpDrive.logger.info("Refreshing cloaked blocks..."); }
		World worldObj = player.worldObj;
		int minYmap = Math.max(0, minY);
		int maxYmap = Math.min(255, maxY);
		for (int y = minYmap; y <= maxYmap; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block block = worldObj.getBlock(x, y, z);
					if (!block.isAssociatedBlock(Blocks.air)) {
						worldObj.setBlock(x, y, z, fogBlock, fogMetadata, 4);
					}
				}
			}
		}
		
		// Hide any entities inside area
		if (WarpDriveConfig.LOGGING_CLOAKING) { WarpDrive.logger.info("Refreshing cloaked entities..."); }
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
		List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb);
		for (Entity entity : list) {
			worldObj.removeEntity(entity);
			((WorldClient) worldObj).removeEntityFromWorld(entity.getEntityId());
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void clientDecloak() {
		World worldObj = Minecraft.getMinecraft().theWorld;
		worldObj.markBlockRangeForRenderUpdate(minX - 1, Math.max(0, minY - 1), minZ - 1, maxX + 1, Math.min(255, maxY + 1), maxZ + 1);

		// Make some graphics
		int numLasers = 80 + worldObj.rand.nextInt(50);
		
		double centerX = (minX + maxX) / 2.0D;
		double centerY = (minY + maxY) / 2.0D;
		double centerZ = (minZ + maxZ) / 2.0D;
		double radiusX = (maxX - minX) / 2.0D + 5.0D;
		double radiusY = (maxY - minY) / 2.0D + 5.0D;
		double radiusZ = (maxZ - minZ) / 2.0D + 5.0D;
		
		for (int i = 0; i < numLasers; i++) {
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(new EntityFXBeam(worldObj,
				new Vector3(
					centerX + radiusX * worldObj.rand.nextGaussian(),
					centerY + radiusY * worldObj.rand.nextGaussian(),
					centerZ + radiusZ * worldObj.rand.nextGaussian()),
				new Vector3(
					centerX + radiusX * worldObj.rand.nextGaussian(),
					centerY + radiusY * worldObj.rand.nextGaussian(),
					centerZ + radiusZ * worldObj.rand.nextGaussian()),
				worldObj.rand.nextFloat(), worldObj.rand.nextFloat(), worldObj.rand.nextFloat(),
				60 + worldObj.rand.nextInt(60), 100));
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s @ DIM%d %d, %d, %d (%d %d %d) -> (%d %d %d)", new Object[] {
				getClass().getSimpleName(), Integer.valueOf(dimensionId),
				Integer.valueOf(coreX), Integer.valueOf(coreY), Integer.valueOf(coreZ),
				Integer.valueOf(minX), Integer.valueOf(minY), Integer.valueOf(minZ),
				Integer.valueOf(maxX), Integer.valueOf(maxY), Integer.valueOf(maxZ) });
	}
}
