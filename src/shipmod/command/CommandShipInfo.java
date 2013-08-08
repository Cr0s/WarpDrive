package shipmod.command;

import java.util.Locale;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatMessageComponent;
import shipmod.entity.EntityShip;

public class CommandShipInfo extends CommandBase
{
    public String getCommandName()
    {
        return "sminfo";
    }

    public void processCommand(ICommandSender icommandsender, String[] astring)
    {
        if (icommandsender instanceof Entity && ((Entity)icommandsender).ridingEntity instanceof EntityShip)
        {
            EntityShip ship = (EntityShip)((Entity)icommandsender).ridingEntity;
            icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Ship information"));
            icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d(String.format(Locale.ENGLISH, "Position: %.2f, %.2f, %.2f", new Object[] {Double.valueOf(ship.posX), Double.valueOf(ship.posY), Double.valueOf(ship.posZ)})));
            icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d(String.format(Locale.ENGLISH, "Speed: %.2f km/h", new Object[] {Float.valueOf(ship.getHorizontalVelocity() * 20.0F * 3.6F)})));
            icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d(String.format(Locale.ENGLISH, "Ship health: %i / %i", new Object[] { Integer.valueOf(ship.health), Integer.valueOf(ship.getShipChunk().getBlockCount())})));
            
        }
        else
        {
            icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Not steering a ship"));
        }
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    /**
     * Returns true if the given command sender is allowed to use this command.
     */
    public boolean canCommandSenderUseCommand(ICommandSender icommandsender)
    {
        return icommandsender instanceof Entity;
    }

    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/".concat(this.getCommandName());
    }
}
