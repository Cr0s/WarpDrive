package cr0s.WarpDrive.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.LinkedList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cr0s.WarpDrive.WarpDrive;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class CloakedArea {
	public int dimensionId = -666;
	public int coreX, coreY, coreZ;
	public AxisAlignedBB aabb;
	private LinkedList<String> playersInArea;
	public byte tier = 0;
	
	public boolean isPlayerListedInArea(String username) {
		for (String playerInArea : playersInArea) {
			// WarpDrive.debugPrint("" + this + " Checking player: " + p.username + "(" + p.entityId + ")" + " =? " + player.username + " (" + p.entityId + ")");
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
		return (aabb.minX <= entity.posX                   && (aabb.maxX + 1) > entity.posX
			 && aabb.minY <= (entity.posY + entity.height) && (aabb.maxY + 1) > entity.posY
			 && aabb.minZ <= entity.posZ                   && (aabb.maxZ + 1) > entity.posZ);
	}
	
	public CloakedArea(World worldObj, int x, int y, int z, AxisAlignedBB aabb, byte tier) {
		this.coreX = x;
		this.coreY = y;
		this.coreZ = z;
		this.aabb = aabb;
		this.tier = tier;
		this.playersInArea = new LinkedList<String>();
		
		if (worldObj == null || aabb == null) {
			return;
		}
		
		this.dimensionId = worldObj.provider.dimensionId;
		
		try {
			// Add all players currently inside the field
			List<Entity> list = worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, this.aabb);
			for (Entity e : list) {
				if (e instanceof EntityPlayer) {
					addPlayer(((EntityPlayer)e).username);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}		
	
	// Sending only if field changes: sets up or collapsing
	public void sendCloakPacketToPlayersEx(boolean decloak) {
		final int RADIUS = 250;
		
		double midX = this.aabb.minX + (Math.abs(this.aabb.maxX - this.aabb.minX) / 2);
		double midY = this.aabb.minY + (Math.abs(this.aabb.maxY - this.aabb.minY) / 2);
		double midZ = this.aabb.minZ + (Math.abs(this.aabb.maxZ - this.aabb.minZ) / 2);
		
		for (int j = 0; j < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); j++) {
			EntityPlayerMP entityPlayerMP = (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(j);

			if (entityPlayerMP.dimension == dimensionId) {
				double d4 = midX - entityPlayerMP.posX;
				double d5 = midY - entityPlayerMP.posY;
				double d6 = midZ - entityPlayerMP.posZ;

				if (Math.abs(d4) < RADIUS && Math.abs(d5) < RADIUS && Math.abs(d6) < RADIUS) {
					if (decloak) {
						revealChunksToPlayer(entityPlayerMP);
						revealEntityToPlayer(entityPlayerMP);
					}
					
					if (!isEntityWithinArea(entityPlayerMP) && !decloak) {
						sendCloakPacketToPlayer(entityPlayerMP, false);
					} else if (decloak) {
						sendCloakPacketToPlayer(entityPlayerMP, true);
					}
				}
			}
		}
	}
	
	public void sendCloakPacketToPlayer(EntityPlayer player, boolean decloak) {
		if (isPlayerListedInArea(player.username)) {
			WarpDrive.debugPrint("" + this + " Player " + player.username + " is inside, no cloak packet to send");
			return;
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
		DataOutputStream outputStream = new DataOutputStream(bos);
		
		try {
			outputStream.writeInt((int) this.aabb.minX);
			outputStream.writeInt((int) this.aabb.minY);
			outputStream.writeInt((int) this.aabb.minZ);
			
			outputStream.writeInt((int) this.aabb.maxX);
			outputStream.writeInt((int) this.aabb.maxY);
			outputStream.writeInt((int) this.aabb.maxZ);
			
			outputStream.writeBoolean(decloak);
			
			outputStream.writeByte(this.tier);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// WarpDrive.debugPrint("" + this + " Sending cloak packet to player " + player.username);
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "WarpDriveCloaks";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		
		((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(packet);
	}
	
	public void updatePlayer(EntityPlayer player) {
		if (isEntityWithinArea(player)) {
			if (!isPlayerListedInArea(player.username)) {
				// WarpDrive.debugPrint("" + this + " Player " + player.username + " has entered");
				addPlayer(player.username);
				revealChunksToPlayer(player);
				revealEntityToPlayer(player);
				sendCloakPacketToPlayer(player, false);
			}
		} else {
			if (isPlayerListedInArea(player.username)) {
				// WarpDrive.debugPrint("" + this + " Player " + player.username + " has left");
				removePlayer(player.username);
				MinecraftServer.getServer().getConfigurationManager().sendToAllNearExcept(player, player.posX, player.posY, player.posZ, 100, player.worldObj.provider.dimensionId, CloakManager.getPacketForThisEntity(player));
				sendCloakPacketToPlayer(player, false);
			}
		}
	}

	public void revealChunksToPlayer(EntityPlayer p) {
		// WarpDrive.debugPrint("" + this + " Revealing cloaked blocks to player " + p.username);
		int minY = (int) Math.max(  0, aabb.minY);
		int maxY = (int) Math.min(255, aabb.maxY);
		for (int x = (int)aabb.minX; x <= (int)aabb.maxX; x++) {
			for (int z = (int)aabb.minZ; z <= (int)aabb.maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					if (p.worldObj.getBlockId(x, y, z) != 0) {
						p.worldObj.markBlockForUpdate(x, y, z);
					}
				}
			}
		}
		/*ArrayList<Chunk> chunksToSend = new ArrayList<Chunk>();
		
		for (int x = (int)aabb.minX >> 4; x <= (int)aabb.maxX >> 4; x++)
			for (int z = (int)aabb.minZ >> 4; z <= (int)aabb.maxZ >> 4; z++) {
				chunksToSend.add(p.worldObj.getChunkFromChunkCoords(x, z));
			}
		
		//System.outprintln("[Cloak] Sending " + chunksToSend.size() + " chunks to player " + p.username);
		((EntityPlayerMP)p).playerNetServerHandler.sendPacketToPlayer(new Packet56MapChunks(chunksToSend));
		
		//System.outprintln("[Cloak] Sending decloak packet to player " + p.username);
		area.sendCloakPacketToPlayer(p, true); // decloak = true
		*/
	}
	
	public void revealEntityToPlayer(EntityPlayer p) {
		List<Entity> list = p.worldObj.getEntitiesWithinAABBExcludingEntity(p, aabb);
		
		for (Entity e : list) {
			((EntityPlayerMP)p).playerNetServerHandler.sendPacketToPlayer(CloakManager.getPacketForThisEntity(e));
		}
	}
	
	@Override
	public String toString() {
        return String.format("%s @ DIM%d %d, %d, %d %s", new Object[] {
           		getClass().getSimpleName(),
           		Integer.valueOf(dimensionId),
           		Integer.valueOf(coreX), Integer.valueOf(coreY), Integer.valueOf(coreZ),
           		aabb.toString()});
	}
}
