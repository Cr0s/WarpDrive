package cr0s.WarpDrive;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cpw.mods.fml.common.network.FMLNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet20NamedEntitySpawn;
import net.minecraft.network.packet.Packet23VehicleSpawn;
import net.minecraft.network.packet.Packet24MobSpawn;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet25EntityPainting;
import net.minecraft.network.packet.Packet26EntityExpOrb;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * Cloak manager stores cloaking devices covered areas
 * @author Cr0s
 *
 */

public class CloakManager {

	private LinkedList<CloakedArea> cloaks;
	
	public CloakManager() {
		this.cloaks = new LinkedList<CloakedArea>();
	}

	public boolean isInCloak(int dimensionID, int x, int y, int z, boolean chunk) {
		for (int i = 0; i < this.cloaks.size(); i++) {
			if (this.cloaks.get(i).dimensionId != dimensionID)
				continue;
			
			AxisAlignedBB axisalignedbb = this.cloaks.get(i).aabb;
			
			if (axisalignedbb.minX <= x && axisalignedbb.maxX >= x && (chunk || (axisalignedbb.minY <= y && axisalignedbb.maxY >= y)) && axisalignedbb.minZ <= z && axisalignedbb.maxZ >= z) {
				return true;
			}
		}
		
		return false;
	}
	
	public ArrayList<CloakedArea> getCloaksForPoint(int dimensionID, int x, int y, int z, boolean chunk) {
		ArrayList<CloakedArea> res = new ArrayList<CloakedArea>();
		
		for (int i = 0; i < this.cloaks.size(); i++) {
			if (this.cloaks.get(i).dimensionId != dimensionID)
				continue;
			
			AxisAlignedBB axisalignedbb = this.cloaks.get(i).aabb;
			if (axisalignedbb.minX <= x && axisalignedbb.maxX >= x && (chunk || (axisalignedbb.minY <= y && axisalignedbb.maxY >= y)) && axisalignedbb.minZ <= z && axisalignedbb.maxZ >= z) {
				res.add(cloaks.get(i));
			}
		}		
		
		return res;
	}
	
	public boolean isAreaExists(World worldObj, int x, int y, int z) {
		return (getCloakedArea(worldObj, x, y, z) != null);		
	}
	
	public void addCloakedAreaWorld(World worldObj, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int x, int y, int z, byte tier) { 
		cloaks.add(new CloakedArea(worldObj, x, y, z, AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ), tier));
	}	
	
	public void removeCloakedArea(World worldObj, int x, int y, int z) {
		int index = 0;
		for (int i = 0; i < this.cloaks.size(); i++){
			if (this.cloaks.get(i).coreX == x && this.cloaks.get(i).coreY == y && this.cloaks.get(i).coreZ == z && this.cloaks.get(i).dimensionId == worldObj.provider.dimensionId) {
				this.cloaks.get(i).sendCloakPacketToPlayersEx(true); // send info about collapsing cloaking field
				index = i;
				break;
			}
		}		
		
		cloaks.remove(index);
	}
	
	public CloakedArea getCloakedArea(World worldObj, int x, int y, int z) {
		for (CloakedArea area : this.cloaks) {
			if (area.coreX == x && area.coreY == y && area.coreZ == z && area.dimensionId == worldObj.provider.dimensionId)
				return area;
		}
		
		return null;
	}
	
	public static void playerEnteringCloakedArea(CloakedArea area, EntityPlayer player) {
		area.addPlayer(player.username);
		revealChunksToPlayer(area, player);
		revealEntityToPlayer(area, player);
		area.sendCloakPacketToPlayer(player, false);
	}
	
	public static void revealEntityToPlayer(CloakedArea area, EntityPlayer p) {
		List<Entity> list = p.worldObj.getEntitiesWithinAABBExcludingEntity(p, area.aabb);
		
		for (Entity e : list) {
			((EntityPlayerMP)p).playerNetServerHandler.sendPacketToPlayer(getPacketForThisEntity(e));
		}
	}
	
	public void checkPlayerLeftArea(EntityPlayer player) {
		for (CloakedArea area : this.cloaks) {
			if (!area.isEntityWithinArea(player) && area.isPlayerInArea(player.username)) {
				area.removePlayer(player.username);
				//System.outprintln("[Cloak] Player " + p.username + " has leaved cloaked area " + area.frequency);
				MinecraftServer.getServer().getConfigurationManager().sendToAllNearExcept(player, player.posX, player.posY, player.posZ, 100, player.worldObj.provider.dimensionId, getPacketForThisEntity(player));
				area.sendCloakPacketToPlayer(player, false);
			}
		}
	}
	
	public static void revealChunksToPlayer(CloakedArea area, EntityPlayer p) {
		//System.outprintln("[Cloak] Revealing cloaked chunks in area " + area.frequency + " to player " + p.username);
		for (int x = (int)area.aabb.minX; x <= (int)area.aabb.maxX; x++) {
			for (int z = (int)area.aabb.minZ; z <= (int)area.aabb.maxZ; z++) {
				for (int y = (int)area.aabb.minY; y <= (int)area.aabb.maxY; y++) {
					if (p.worldObj.getBlockId(x, y, z) != 0) {
						p.worldObj.markBlockForUpdate(x, y, z);
					}
				}
			}
		}
		/*ArrayList<Chunk> chunksToSend = new ArrayList<Chunk>();
		
		for (int x = (int)area.aabb.minX >> 4; x <= (int)area.aabb.maxX >> 4; x++)
			for (int z = (int)area.aabb.minZ >> 4; z <= (int)area.aabb.maxZ >> 4; z++) {
				chunksToSend.add(p.worldObj.getChunkFromChunkCoords(x, z));
			}
		
		//System.outprintln("[Cloak] Sending " + chunksToSend.size() + " chunks to player " + p.username);
		((EntityPlayerMP)p).playerNetServerHandler.sendPacketToPlayer(new Packet56MapChunks(chunksToSend));
		
		//System.outprintln("[Cloak] Sending decloak packet to player " + p.username);
		area.sendCloakPacketToPlayer(p, true); // decloak = true
		*/
	}
	
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
	                		revealChunksToPlayer(this, entityPlayerMP);
	                		revealEntityToPlayer(this, entityPlayerMP);
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
	}
	
	private static Packet getPacketForThisEntity(Entity e) {
        if (e.isDead) {
            e.worldObj.getWorldLogAgent().logWarning("Fetching addPacket for removed entity");
        }

        Packet pkt = FMLNetworkHandler.getEntitySpawningPacket(e);
        if (pkt != null) {
            return pkt;
        }

        if (e instanceof EntityItem) {
            return new Packet23VehicleSpawn(e, 2, 1);
        } else if (e instanceof EntityPlayerMP) {
            return new Packet20NamedEntitySpawn((EntityPlayer)e);
        } else if (e instanceof EntityMinecart) {
            EntityMinecart entityminecart = (EntityMinecart)e;
            return new Packet23VehicleSpawn(e, 10, entityminecart.getMinecartType());
        } else if (e instanceof EntityBoat) {
            return new Packet23VehicleSpawn(e, 1);
        } else if (!(e instanceof IAnimals) && !(e instanceof EntityDragon)) {
            if (e instanceof EntityFishHook) {
                EntityPlayer entityplayer = ((EntityFishHook)e).angler;
                return new Packet23VehicleSpawn(e, 90, entityplayer != null ? entityplayer.entityId : e.entityId);
            } else if (e instanceof EntityArrow) {
                Entity entity = ((EntityArrow)e).shootingEntity;
                return new Packet23VehicleSpawn(e, 60, entity != null ? entity.entityId : e.entityId);
            } else if (e instanceof EntitySnowball) {
                return new Packet23VehicleSpawn(e, 61);
            } else if (e instanceof EntityPotion) {
                return new Packet23VehicleSpawn(e, 73, ((EntityPotion)e).getPotionDamage());
            } else if (e instanceof EntityExpBottle) {
                return new Packet23VehicleSpawn(e, 75);
            } else if (e instanceof EntityEnderPearl) {
                return new Packet23VehicleSpawn(e, 65);
            } else if (e instanceof EntityEnderEye) {
                return new Packet23VehicleSpawn(e, 72);
            } else if (e instanceof EntityFireworkRocket) {
                return new Packet23VehicleSpawn(e, 76);
            } else {
                Packet23VehicleSpawn packet23vehiclespawn;

                if (e instanceof EntityFireball) {
                    EntityFireball entityfireball = (EntityFireball)e;
                    packet23vehiclespawn = null;
                    byte b0 = 63;

                    if (e instanceof EntitySmallFireball) {
                        b0 = 64;
                    } else if (e instanceof EntityWitherSkull) {
                        b0 = 66;
                    }

                    if (entityfireball.shootingEntity != null) {
                        packet23vehiclespawn = new Packet23VehicleSpawn(e, b0, ((EntityFireball)e).shootingEntity.entityId);
                    } else {
                        packet23vehiclespawn = new Packet23VehicleSpawn(e, b0, 0);
                    }

                    packet23vehiclespawn.speedX = (int)(entityfireball.accelerationX * 8000.0D);
                    packet23vehiclespawn.speedY = (int)(entityfireball.accelerationY * 8000.0D);
                    packet23vehiclespawn.speedZ = (int)(entityfireball.accelerationZ * 8000.0D);
                    return packet23vehiclespawn;
                } else if (e instanceof EntityEgg) {
                    return new Packet23VehicleSpawn(e, 62);
                } else if (e instanceof EntityTNTPrimed) {
                    return new Packet23VehicleSpawn(e, 50);
                } else if (e instanceof EntityEnderCrystal) {
                    return new Packet23VehicleSpawn(e, 51);
                } else if (e instanceof EntityFallingSand) {
                    EntityFallingSand entityfallingsand = (EntityFallingSand)e;
                    return new Packet23VehicleSpawn(e, 70, entityfallingsand.blockID | entityfallingsand.metadata << 16);
                } else if (e instanceof EntityPainting) {
                    return new Packet25EntityPainting((EntityPainting)e);
                } else if (e instanceof EntityItemFrame) {
                    EntityItemFrame entityitemframe = (EntityItemFrame)e;
                    packet23vehiclespawn = new Packet23VehicleSpawn(e, 71, entityitemframe.hangingDirection);
                    packet23vehiclespawn.xPosition = MathHelper.floor_float(entityitemframe.xPosition * 32);
                    packet23vehiclespawn.yPosition = MathHelper.floor_float(entityitemframe.yPosition * 32);
                    packet23vehiclespawn.zPosition = MathHelper.floor_float(entityitemframe.zPosition * 32);
                    return packet23vehiclespawn;
                } else if (e instanceof EntityLeashKnot) {
                    EntityLeashKnot entityleashknot = (EntityLeashKnot)e;
                    packet23vehiclespawn = new Packet23VehicleSpawn(e, 77);
                    packet23vehiclespawn.xPosition = MathHelper.floor_float(entityleashknot.xPosition * 32);
                    packet23vehiclespawn.yPosition = MathHelper.floor_float(entityleashknot.yPosition * 32);
                    packet23vehiclespawn.zPosition = MathHelper.floor_float(entityleashknot.zPosition * 32);
                    return packet23vehiclespawn;
                } else if (e instanceof EntityXPOrb) {
                    return new Packet26EntityExpOrb((EntityXPOrb)e);
                } else {
                    throw new IllegalArgumentException("Don\'t know how to add " + e.getClass() + "!");
                }
            }
        } else {
            return new Packet24MobSpawn((EntityLivingBase)e);
        }
    }	
}
