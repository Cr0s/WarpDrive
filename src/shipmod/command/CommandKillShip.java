package shipmod.command;

import java.util.Iterator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatMessageComponent;
import shipmod.entity.EntityShip;

public class CommandKillShip extends CommandBase
{
    public String getCommandName()
    {
        return "killship";
    }

    public void processCommand(ICommandSender icommandsender, String[] astring)
    {
        if (icommandsender instanceof Entity)
        {
            double range = 16.0D;

            if (astring != null && astring.length > 0)
            {
                try
                {
                    range = (double)Integer.parseInt(astring[0]);
                }
                catch (NumberFormatException var13)
                {
                    throw new NumberInvalidException();
                }
            }

            range *= range;
            Entity player = (Entity)icommandsender;
            EntityShip ne = null;

            if (player.ridingEntity instanceof EntityShip)
            {
                ne = (EntityShip)player.ridingEntity;
            }
            else
            {
                double nd = 0.0D;
                Iterator i$ = player.worldObj.getLoadedEntityList().iterator();

                while (i$.hasNext())
                {
                    Entity entity = (Entity)i$.next();

                    if (entity instanceof EntityShip)
                    {
                        double d = player.getDistanceSqToEntity(entity);

                        if (d < range && (ne == null || d < nd))
                        {
                            ne = (EntityShip)entity;
                            nd = d;
                        }
                    }
                }
            }

            if (ne == null)
            {
                icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("No ship in a " + (int)range + " blocks\' range"));
                return;
            }

            if (!ne.decompileToBlocks(false))
            {
                icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Failed to decompile ship; dropping to items"));
                ne.dropAsItems();
            }

            ne.setDead();
        }
    }

    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/" + this.getCommandName() + " [range]";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    /**
     * Returns true if the given command sender is allowed to use this command.
     */
    public boolean canCommandSenderUseCommand(ICommandSender icommandsender)
    {
        return icommandsender instanceof Entity;
    }
}
