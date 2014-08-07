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
	
	public static Packet getPacketForThisEntity(Entity e) {
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
