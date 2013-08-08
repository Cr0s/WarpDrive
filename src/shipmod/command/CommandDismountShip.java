package shipmod.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatMessageComponent;
import shipmod.entity.EntityShip;

public class CommandDismountShip extends CommandBase
{
    public String getCommandName()
    {
        return "dismountship";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    public void processCommand(ICommandSender icommandsender, String[] astring)
    {
        if (icommandsender instanceof Entity)
        {
            Entity player = (Entity)icommandsender;

            if (player.ridingEntity instanceof EntityShip)
            {
                boolean flag = false;

                if (astring != null && astring.length > 0 && (astring[0].equals("overwrite") || astring[0].equals("override")))
                {
                    icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Overwriting existing blocks with ship blocks"));
                    flag = true;
                }
                else
                {
                    icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Trying to add ship blocks to world"));
                }

                ((EntityShip)player.ridingEntity).decompileToBlocks(flag);
                player.mountEntity((Entity)null);
                return;
            }
        }

        icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Not steering a ship"));
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
        return "/" + this.getCommandName() + " [overwrite]";
    }
}
