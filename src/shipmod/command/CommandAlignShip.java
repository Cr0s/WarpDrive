package shipmod.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatMessageComponent;
import shipmod.entity.EntityShip;

public class CommandAlignShip extends CommandBase
{
    public String getCommandName()
    {
        return "alignship";
    }

    public void processCommand(ICommandSender icommandsender, String[] astring)
    {
        if (icommandsender instanceof Entity && ((Entity)icommandsender).ridingEntity instanceof EntityShip)
        {
            EntityShip ship = (EntityShip)((Entity)icommandsender).ridingEntity;
            ship.alignToGrid();
            icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Ship aligned to world grid"));
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
        return "/" + this.getCommandName();
    }
}
