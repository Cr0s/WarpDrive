package cr0s.WarpDrive;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

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