package shipmod;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import shipmod.entity.EntityShip;

public class PacketHandler implements IPacketHandler
{
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        ShipMod.modLogger.finest("Received packet " + packet.channel + " with player " + player.toString());
        EntityPlayer entityplayer = (EntityPlayer)player;
        ByteArrayDataInput in;
        int id;
        Entity entity;

        if (packet.channel.equals("shipControl"))
        {
            in = ByteStreams.newDataInput(packet.data);
            id = in.readInt();
            entity = entityplayer.worldObj.getEntityByID(id);

            if (entity instanceof EntityShip)
            {
                byte signs = in.readByte();
                EntityShip i = (EntityShip)entity;
                i.getController().updateControl(i, entityplayer, signs);
            }
        }
        else if (packet.channel.equals("shipInteract"))
        {
            in = ByteStreams.newDataInput(packet.data);
            id = in.readInt();
            entity = entityplayer.worldObj.getEntityByID(id);

            if (entity instanceof EntityShip)
            {
                entity.func_130002_c(entityplayer);
            }
        }
        else if (packet.channel.equals("reqShipSigns"))
        {
            in = ByteStreams.newDataInput(packet.data);
            id = in.readInt();
            entity = entityplayer.worldObj.getEntityByID(id);

            if (entity instanceof EntityShip)
            {
                if (!((EntityShip)entity).getCapabilities().hasSigns())
                {
                    return;
                }

                ArrayList var14 = new ArrayList();
                Iterator var15 = ((EntityShip)entity).getShipChunk().chunkTileEntityMap.values().iterator();

                while (var15.hasNext())
                {
                    TileEntity s = (TileEntity)var15.next();

                    if (s instanceof TileEntitySign)
                    {
                        var14.add((TileEntitySign)s);
                    }
                }

                if (var14.size() > 0)
                {
                    ByteArrayDataOutput var17 = ByteStreams.newDataOutput(6 + var14.size() * 70);
                    var17.writeInt(id);
                    var17.writeShort(var14.size());
                    Iterator var20 = var14.iterator();

                    while (var20.hasNext())
                    {
                        TileEntitySign te = (TileEntitySign)var20.next();
                        var17.writeShort(te.xCoord & 15 | (te.yCoord & 15) << 4 | (te.zCoord & 15) << 8);
                        var17.writeUTF(te.signText[0]);
                        var17.writeUTF(te.signText[1]);
                        var17.writeUTF(te.signText[2]);
                        var17.writeUTF(te.signText[3]);
                    }

                    try
                    {
                        Packet250CustomPayload var21 = new Packet250CustomPayload("shipSigns", var17.toByteArray());
                        PacketDispatcher.sendPacketToPlayer(var21, player);
                    }
                    catch (IllegalArgumentException var12)
                    {
                        ShipMod.modLogger.warning("Ship has too many signs to send");
                    }
                }
            }
        }
        else if (packet.channel.equals("shipSigns"))
        {
            in = ByteStreams.newDataInput(packet.data);
            id = in.readInt();
            entity = entityplayer.worldObj.getEntityByID(id);

            if (entity instanceof EntityShip)
            {
                short var13 = in.readShort();

                for (int var16 = 0; var16 < var13; ++var16)
                {
                    short var18 = in.readShort();
                    TileEntity var19 = ((EntityShip)entity).getShipChunk().getBlockTileEntity(var18 & 15, var18 >>> 4 & 15, var18 >>> 8 & 15);

                    if (var19 instanceof TileEntitySign)
                    {
                        ((TileEntitySign)var19).signText[0] = in.readUTF();
                        ((TileEntitySign)var19).signText[1] = in.readUTF();
                        ((TileEntitySign)var19).signText[2] = in.readUTF();
                        ((TileEntitySign)var19).signText[3] = in.readUTF();
                    }
                }
            }
        }
    }
}
