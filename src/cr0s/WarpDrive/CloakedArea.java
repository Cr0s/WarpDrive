package cr0s.WarpDrive;

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

public class CloakedArea
{
	public int frequency;
	public AxisAlignedBB aabb;
	public LinkedList<EntityPlayer> playersInArea;
	public byte tier = 0;
	public World world = null;
	
	public boolean isPlayerInArea(EntityPlayer player) {
		for (EntityPlayer p : this.playersInArea) {
			//System.outprintln("[Cloak] Checking player: " + p.username + "(" + p.entityId + ")" + " =? " + player.username + " (" + p.entityId + ")");
			if (p.username.equals(player.username))
				return true;
		}

		return false;
	}
	
	public void removePlayerFromArea(EntityPlayer p) {
		for (int i = 0; i < this.playersInArea.size(); i++) {
			if (this.playersInArea.get(i).username.equals(p.username)) {
				this.playersInArea.remove(i);
				return;
			}
		}		
	}
	
	public boolean isPlayerWithinArea(EntityPlayer player) {
		return (aabb.minX <= player.posX && aabb.maxX >= player.posX && aabb.minY <= player.posY && aabb.maxY >= player.posY  && aabb.minZ <= player.posZ && aabb.maxZ >= player.posZ);
	}
			
	public CloakedArea(World worldObj, int frequency, AxisAlignedBB aabb, byte tier) {
		this.frequency = frequency;
		this.aabb = aabb;
		this.tier = tier;
		this.playersInArea = new LinkedList<EntityPlayer>();
		
		if (worldObj == null || aabb == null)
			return;
		
		this.world = worldObj;
		
		if (this.world == null)
			return;
		
		try {
			// Added all players, who inside the field
			List<Entity> list = worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, this.aabb);
			for (Entity e : list) {
				if (e instanceof EntityPlayer)
					this.playersInArea.add((EntityPlayer)e);
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
		
		for (int j = 0; j < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); ++j)
		{
			EntityPlayerMP entityplayermp = (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(j);

			if (entityplayermp.dimension == this.world.provider.dimensionId)
			{
				double d4 = midX - entityplayermp.posX;
				double d5 = midY - entityplayermp.posY;
				double d6 = midZ - entityplayermp.posZ;

				if (d4 * d4 + d5 * d5 + d6 * d6 < RADIUS * RADIUS)
				{
					if (decloak) {
						WarpDrive.instance.cloaks.revealChunksToPlayer(this, (EntityPlayer)entityplayermp);
						WarpDrive.instance.cloaks.revealEntityToPlayer(this, (EntityPlayer)entityplayermp);							
					}
					
					if (!isPlayerWithinArea((EntityPlayer)entityplayermp) && !decloak)
							sendCloakPacketToPlayer((EntityPlayer)entityplayermp, false);
					else if (decloak) {
						sendCloakPacketToPlayer((EntityPlayer)entityplayermp, true);
					}
				}
			}
		}			
	}
	
	public void sendCloakPacketToPlayer(EntityPlayer player, boolean decloak) {
		//System.outprintln("[Cloak] Sending cloak packet to player " + player.username);
		if (isPlayerInArea(player)) {
			//System.outprintln("[Cloak] Player " + player.username + " is inside cloaking field");
			return;
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
		DataOutputStream outputStream = new DataOutputStream(bos);

		try
		{
			outputStream.writeInt((int) this.aabb.minX);
			outputStream.writeInt((int) this.aabb.minY);
			outputStream.writeInt((int) this.aabb.minZ);
			
			outputStream.writeInt((int) this.aabb.maxX);
			outputStream.writeInt((int) this.aabb.maxY);
			outputStream.writeInt((int) this.aabb.maxZ);
			
			outputStream.writeBoolean(decloak);
		
			outputStream.writeByte(this.tier);
		}
		catch (Exception ex)
		{
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

			int j2 = c.xPosition * 16 + x;
			int k2 = c.zPosition * 16 + z;
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
}
