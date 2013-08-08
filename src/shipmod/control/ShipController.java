package shipmod.control;

import net.minecraft.entity.player.EntityPlayer;
import shipmod.entity.EntityShip;

public class ShipController
{
    private int shipControl = 0;

    public void updateControl(EntityShip ship, EntityPlayer player, int i)
    {
        this.shipControl = i;
    }

    public int getShipControl()
    {
        return this.shipControl;
    }
}
