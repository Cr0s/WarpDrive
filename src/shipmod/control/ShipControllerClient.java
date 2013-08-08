package shipmod.control;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import shipmod.entity.EntityShip;

public class ShipControllerClient extends ShipController
{
    public void updateControl(EntityShip ship, EntityPlayer player, int i)
    {
        super.updateControl(ship, player, i);
        ByteArrayDataOutput out = ByteStreams.newDataOutput(5);
        out.writeInt(ship.entityId);
        out.writeByte(i);
        ((EntityClientPlayerMP)player).sendQueue.addToSendQueue(new Packet250CustomPayload("shipControl", out.toByteArray()));
    }
}
