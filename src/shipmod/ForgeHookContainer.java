package shipmod;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import shipmod.entity.EntityShip;

public class ForgeHookContainer
{
    @ForgeSubscribe
    public void onEntitySpawn(EntityJoinWorldEvent event)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient() && event.entity instanceof EntityShip)
        {
            if (!((EntityShip)event.entity).getCapabilities().hasSigns())
            {
                return;
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput(4);
            out.writeInt(event.entity.entityId);
            Packet250CustomPayload packet = new Packet250CustomPayload("reqShipSigns", out.toByteArray());
            PacketDispatcher.sendPacketToServer(packet);
        }
    }
}
