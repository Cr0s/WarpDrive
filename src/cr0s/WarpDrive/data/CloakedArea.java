package cr0s.WarpDrive.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.LinkedList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import net.minecraft.entity.Entity;
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
	
	public boolean isPlayerInArea(String username) {
		for (String playerInArea : playersInArea) {
			//System.outprintln("[Cloak] Checking player: " + p.username + "(" + p.entityId + ")" + " =? " + player.username + " (" + p.entityId + ")");
			if (playerInArea.equals(username))
				return true;
		}
		
		return false;
	}
	
	public void removePlayer(String username) {
		for (int i = 0; i < playersInArea.size(); i++) {
			if (playersInArea.get(i).equals(username)) {
				playersInArea.remove(i);
				return;
			}
		}
	}
	
	public void addPlayer(String username) {
		if (!isPlayerInArea(username)) {
			playersInArea.add(username);
		}
	}
	
	public boolean isEntityWithinArea(Entity entity) {
		return (aabb.minX <= entity.posX && aabb.maxX >= entity.posX && aabb.minY <= entity.posY && aabb.maxY >= entity.posY  && aabb.minZ <= entity.posZ && aabb.maxZ >= entity.posZ);
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
	
	public Chunk obscureChunkBlocksWithinArea(Chunk chunk) {
		for (int x = (int)this.aabb.minX; x < (int)this.aabb.maxX; x++)
			for (int z = (int)this.aabb.minZ; z < (int)this.aabb.maxZ; z++)
				for (int y = (int)this.aabb.minY; y < (int)this.aabb.maxY; y++)	{
					myChunkSBIDWMT(chunk, x & 15, y, z & 15, 0, 0);
				}
		
		return chunk;
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
		//System.outprintln("[Cloak] Sending cloak packet to player " + player.username);
		if (isPlayerInArea(player.username)) {
			//System.outprintln("[Cloak] Player " + player.username + " is inside cloaking field");
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
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "WarpDriveCloaks";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		
		((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(packet);
	}
	
	public boolean myChunkSBIDWMT(Chunk c, int x, int y, int z, int blockId, int blockMeta)
	{
		int j1 = z << 4 | x;

		if (y >= c.precipitationHeightMap[j1] - 1)
		{
			c.precipitationHeightMap[j1] = -999;
		}

		int l1 = c.getBlockID(x, y, z);
		int i2 = c.getBlockMetadata(x, y, z);

		if (l1 == blockId && i2 == blockMeta)
		{
			return false;
		}
		else
		{
			ExtendedBlockStorage[] storageArrays = c.getBlockStorageArray();
			ExtendedBlockStorage extendedblockstorage = storageArrays[y >> 4];

			if (extendedblockstorage == null)
			{
				if (blockId == 0)
				{
					return false;
				}

				extendedblockstorage = storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !c.worldObj.provider.hasNoSky);
			}
			extendedblockstorage.setExtBlockID(x, y & 15, z, blockId);

			if (extendedblockstorage.getExtBlockID(x, y & 15, z) != blockId)
			{
				return false;
			}
			else
			{
				extendedblockstorage.setExtBlockMetadata(x, y & 15, z, blockMeta);
				c.isModified = true;
				return true;
			}
		}
	}
	
	public void playerEnteringCloakedArea(EntityPlayer player) {
		addPlayer(player.username);
		revealChunksToPlayer(player);
		revealEntityToPlayer(player);
		sendCloakPacketToPlayer(player, false);
	}

	public void revealChunksToPlayer(EntityPlayer p) {
		//System.outprintln("[Cloak] Revealing cloaked chunks in area " + area.frequency + " to player " + p.username);
		for (int x = (int)aabb.minX; x <= (int)aabb.maxX; x++) {
			for (int z = (int)aabb.minZ; z <= (int)aabb.maxZ; z++) {
				for (int y = (int)aabb.minY; y <= (int)aabb.maxY; y++) {
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
}
