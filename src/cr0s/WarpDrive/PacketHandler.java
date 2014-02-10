package cr0s.WarpDrive;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
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

    public void handleCloak(Packet250CustomPayload packet, EntityPlayer player) {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));

        try
        {   
            // Read cloaked area parameters
            int minX = inputStream.readInt();
            int minY = inputStream.readInt();
            int minZ = inputStream.readInt();
            
            int maxX = inputStream.readInt();
            int maxY = inputStream.readInt();
            int maxZ = inputStream.readInt(); 
            
            boolean decloak = inputStream.readBoolean();
            
            byte tier = inputStream.readByte();
            
            int w = Math.abs(maxX - minX);
            int h = Math.abs(maxY - minY);
            int l = Math.abs(maxZ - minZ);
            
            //(-2099; 208; 423) -> (-2069; 243; 453)
            // 
            int size = w * h * l;
            
            //System.out.println("[Cloak Packet] Received " + ((decloak) ? "DEcloaked" : "cloaked") + "area: (" + minX + "; " + minY + "; " + minZ + ") -> (" + maxX + "; " + maxY + "; " + maxZ + ")");            
            
            if (minX <= player.chunkCoordX && maxX >= player.chunkCoordY && minY <= player.chunkCoordY && maxY >= player.chunkCoordY  && minZ <= player.chunkCoordZ && maxZ >= player.chunkCoordZ)
            	return;
            
            // Hide the area
            if (!decloak) {
	            //System.out.println("[Cloak Packet] Removing " + size + " blocks...");
	            
	            // Now hide the blocks within area
	            World worldObj = player.worldObj;
	    		for (int y = minY; y <= maxY; y++)
	    			for (int x = minX; x <= maxX; x++)
	    				for(int z = minZ; z <= maxZ; z++)
	            			if (worldObj.getBlockId(x, y, z) != 0)
	            				worldObj.setBlock(x, y, z, (tier == 1) ? WarpDriveConfig.i.gasID : 0, 5, 4);
	            
	    		//System.out.println("[Cloak Packet] Removing entity...");
	            // Hide any entities inside area
	            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb);
	            for (Entity e : list) {
	            	worldObj.removeEntity(e);
	            	((WorldClient)worldObj).removeEntityFromWorld(e.entityId);
	            }
            } else { // reveal the area
            	player.worldObj.markBlockRangeForRenderUpdate(minX, minY, minZ, maxX, maxY, maxZ);
            	
            	// Make some graphics
            	int numLasers = 25 + player.worldObj.rand.nextInt(300);
            	
            	for (int i = 0; i < numLasers; i++) {
            		int randX1 = minX + player.worldObj.rand.nextInt(maxX - minX);
            		int randX2 = minX + player.worldObj.rand.nextInt(maxX - minX);
            		
            		int randY1 = minY + player.worldObj.rand.nextInt(maxY - minY);
            		int randY2 = minY + player.worldObj.rand.nextInt(maxY - minY);
            		
            		int randZ1 = minZ + player.worldObj.rand.nextInt(maxZ - minZ);
            		int randZ2 = minZ + player.worldObj.rand.nextInt(maxZ - minZ);
            		
            		float r = 0, g = 0, b = 0;
            		
    				switch (player.worldObj.rand.nextInt(6)) {
						case 0:
							r = 1.0f;
							g = b = 0;
							break;
						case 1:
							r = b = 0;
							g = 1.0f;
							break;
						case 2:
							r = g = 0;
							b = 1.0f;
							break;
						case 3:
							r = b = 0.5f;
							g = 0;
							break;
						case 4:
							r = g = 1.0f;
							b = 0;
							break;
						case 5:
							r = 1.0f; 
							b = 0.5f;
							g = 0f;
    				}            		
            		
            		WarpDrive.proxy.renderBeam(player.worldObj, new Vector3(randX1, randY1, randZ1), new Vector3(randX2, randY2, randZ2), r, g, b, 10, 100);
            	}
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }    	
    }
    
    public void handleLaserTargeting(Packet250CustomPayload packet, EntityPlayer player)
    {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));

        try
        {
            int x = inputStream.readInt();
            int y = inputStream.readInt();
            int z = inputStream.readInt();
            float yaw = inputStream.readFloat();
            float pitch = inputStream.readFloat();
            System.out.println("Got target packet: (" + x + "; " + y + "; " + z + ") | yaw: " + yaw + " | pitch: " + pitch);
            TileEntity te = player.worldObj.getBlockTileEntity(x, y, z);

            if (te != null)
            {
                System.out.println("TE is NULL");

                if (te instanceof TileEntityLaser)
                {
                    TileEntityLaser l = (TileEntityLaser)te;
                    l.yaw = yaw;
                    l.pitch = pitch;
                    l.delayTicks = 0;
                    l.isEmitting = true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void handleFreqUpdate(Packet250CustomPayload packet, EntityPlayer player)
    {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));

        try
        {
            int x = inputStream.readInt();
            int y = inputStream.readInt();
            int z = inputStream.readInt();
            int freq = inputStream.readInt();
            //System.out.println("Got freq packet: (" + x + "; " + y + "; " + z + ") | freq: " + freq);
            TileEntity te = player.worldObj.getBlockTileEntity(x, y, z);

            if (te != null)
            {
                if (te instanceof TileEntityMonitor)
                {
                    ((TileEntityMonitor)te).setFrequency(freq);
                }
                else if (te instanceof TileEntityCamera)
                {
                    ((TileEntityCamera)te).setFrequency(freq);
                    WarpDrive.instance.cams.updateInRegistry(new CamRegistryItem(freq, new ChunkPosition(x, y, z), player.worldObj).setType(0));
                }
                else if (te instanceof TileEntityLaser)
                {
                    ((TileEntityLaser)te).camFreq = freq;
                    WarpDrive.instance.cams.updateInRegistry(new CamRegistryItem(freq, new ChunkPosition(x, y, z), player.worldObj).setType(1));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void handleBeam(Packet250CustomPayload packet, EntityPlayer player)
    {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
        int dimID;
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
            /*System.out.println("sx: " + sx + " sy: " + sy + " sz: " + sz);
            System.out.println("tx: " + sx + " ty: " + sy + " tz: " + sz);

            System.out.println("source: " + source);
            System.out.println("target: " + target);
            System.out.println("r: " + r);
            System.out.println("g: " + g);
            System.out.println("b " + b);
            System.out.println("age: " + age);
            System.out.println("energy: " + energy);*/

            // To avoid NPE at logging in
            if (worldObj == null)
            {
                System.out.println("WorldObj is null");
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