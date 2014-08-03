package cr0s.WarpDrive;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cr0s.WarpDrive.machines.TileEntityCamera;
import cr0s.WarpDrive.machines.TileEntityLaser;
import cr0s.WarpDrive.machines.TileEntityMonitor;
import net.minecraft.client.multiplayer.WorldClient;

public class PacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        if (packet.channel.equals("WarpDriveBeam"))
        {
            handleBeam(packet, (EntityPlayer)player);
        }
        else if (packet.channel.equals("WarpDriveFreq"))
        {
            handleFreqUpdate(packet, (EntityPlayer)player);
        }
        else if (packet.channel.equals("WarpDriveLaserT"))
        {
            handleLaserTargeting(packet, (EntityPlayer)player);
        } 
        else if (packet.channel.equals("WarpDriveCloaks")) 
        {
        	handleCloak(packet, (EntityPlayer)player);
        }
    }

    public static void handleCloak(Packet250CustomPayload packet, EntityPlayer player) {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));

        try {   
            // Read cloaked area parameters
            int minX = inputStream.readInt();
            int minY = inputStream.readInt();
            int minZ = inputStream.readInt();
            
            int maxX = inputStream.readInt();
            int maxY = inputStream.readInt();
            int maxZ = inputStream.readInt(); 
            
            boolean decloak = inputStream.readBoolean();
            
            byte tier = inputStream.readByte();
            
            //WarpDrive.debugPrint("[Cloak Packet] Received " + ((decloak) ? "DEcloaked" : "cloaked") + "area: (" + minX + "; " + minY + "; " + minZ + ") -> (" + maxX + "; " + maxY + "; " + maxZ + ")");            
            
            if (minX <= player.posX && maxX >= player.posY && minY <= player.posZ && maxY >= player.posX  && minZ <= player.posY && maxZ >= player.posZ)
            	return;
            
            // Hide the area
            if (!decloak) {
	            //WarpDrive.debugPrint("[Cloak Packet] Removing " + size + " blocks...");
	            
	            // Now hide the blocks within area
	            World worldObj = player.worldObj;
	            int cloakBlockID = (tier == 1) ? WarpDriveConfig.gasID : 0;
	            int cloakBlockMetadata = (tier == 1) ? 5 : 0;
	    		for (int y = minY; y <= maxY; y++) {
	    			for (int x = minX; x <= maxX; x++) {
	    				for(int z = minZ; z <= maxZ; z++) {
	            			if (worldObj.getBlockId(x, y, z) != 0) {
	            				worldObj.setBlock(x, y, z, cloakBlockID, cloakBlockMetadata, 4);
	            			}
	    				}
	    			}
	    		}
	            
	    		//WarpDrive.debugPrint("[Cloak Packet] Removing entity...");
	            // Hide any entities inside area
	            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb);
	            for (Entity e : list) {
	            	worldObj.removeEntity(e);
	            	((WorldClient)worldObj).removeEntityFromWorld(e.entityId);
	            }
            } else { // reveal the area
            	player.worldObj.markBlockRangeForRenderUpdate(minX + 1, minY + 1, minZ + 1, maxX + 1, maxY + 1, maxZ + 1);
            	
            	// Make some graphics
            	int numLasers = 80 + player.worldObj.rand.nextInt(50);
            	
            	double centerX = (minX + maxX) / 2.0D; 
            	double centerY = (minY + maxY) / 2.0D; 
            	double centerZ = (minZ + maxZ) / 2.0D;
            	double radiusX = (maxX - minX) / 2.0D + 5.0D; 
            	double radiusY = (maxY - minY) / 2.0D + 5.0D; 
            	double radiusZ = (maxZ - minZ) / 2.0D + 5.0D;
           
            	for (int i = 0; i < numLasers; i++) {
            		WarpDrive.proxy.renderBeam(player.worldObj,
            			new Vector3(
	        				centerX + radiusX * player.worldObj.rand.nextGaussian(),
	        				centerY + radiusY * player.worldObj.rand.nextGaussian(),
	        				centerZ + radiusZ * player.worldObj.rand.nextGaussian()),
        				new Vector3(
	        				centerX + radiusX * player.worldObj.rand.nextGaussian(),
	        				centerY + radiusY * player.worldObj.rand.nextGaussian(),
	        				centerZ + radiusZ * player.worldObj.rand.nextGaussian()),
        				player.worldObj.rand.nextFloat(), player.worldObj.rand.nextFloat(), player.worldObj.rand.nextFloat(),
        				60 + player.worldObj.rand.nextInt(60), 100);
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }    	
    }
    
    public static void handleLaserTargeting(Packet250CustomPayload packet, EntityPlayer player)
    {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));

        try
        {
            int x = inputStream.readInt();
            int y = inputStream.readInt();
            int z = inputStream.readInt();
            float yaw = inputStream.readFloat();
            float pitch = inputStream.readFloat();
            WarpDrive.debugPrint("Received target packet: (" + x + "; " + y + "; " + z + ") yaw: " + yaw + " pitch: " + pitch);
            TileEntity te = player.worldObj.getBlockTileEntity(x, y, z);
            if (te != null && te instanceof TileEntityLaser) {
                TileEntityLaser laser = (TileEntityLaser)te;
                laser.yaw = yaw;
                laser.pitch = pitch;
                laser.delayTicks = 0;
                laser.isEmitting = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void handleFreqUpdate(Packet250CustomPayload packet, EntityPlayer player) {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));

        try {
            int x = inputStream.readInt();
            int y = inputStream.readInt();
            int z = inputStream.readInt();
            int frequency = inputStream.readInt();
//            WarpDrive.debugPrint("Received frequency packet: (" + x + ", " + y + ", " + z + ") frequency '" + frequency + "'");
            TileEntity te = player.worldObj.getBlockTileEntity(x, y, z);
            if (te != null) {
                if (te instanceof TileEntityMonitor) {
                    ((TileEntityMonitor)te).setFrequency(frequency);
                } else if (te instanceof TileEntityCamera) {
                    ((TileEntityCamera)te).setFrequency(frequency);
                } else if (te instanceof TileEntityLaser) {
                    ((TileEntityLaser)te).setCameraFrequency(frequency);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void handleBeam(Packet250CustomPayload packet, EntityPlayer player)
    {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
        Vector3 source, target;
        double sx, sy, sz;
        double tx, ty, tz;
        float r, g, b;
        int age;
        int energy;
        World worldObj = player.worldObj;

        try
        {
            // Read source vector
            sx = inputStream.readDouble();
            sy = inputStream.readDouble();
            sz = inputStream.readDouble();
            source = new Vector3(sx, sy, sz);
            // Read target vector
            tx = inputStream.readDouble();
            ty = inputStream.readDouble();
            tz = inputStream.readDouble();
            target = new Vector3(tx, ty, tz);
            // Read r, g, b of laser
            r = inputStream.readFloat();
            g = inputStream.readFloat();
            b = inputStream.readFloat();
            // Read age
            age = inputStream.readByte();
            // Read energy value
            energy = inputStream.readInt();

            // Render beam
//            WarpDrive.debugPrint("Received beam packet from " + source + " to " + target + " as RGB " + r + " " + g + " " + b + " age " + age +" energy " + energy);

            // To avoid NPE at logging in
            if (worldObj == null)
            {
                WarpDrive.debugPrint("WorldObj is null");
                return;
            }

            WarpDrive.proxy.renderBeam(worldObj, source.clone(), target.clone(), r, g, b, age, energy);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
    }
}