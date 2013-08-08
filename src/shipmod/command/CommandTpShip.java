package shipmod.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandTpShip extends CommandBase
{
    public String getCommandName()
    {
        return "tpship";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "commands.tp.usage";
    }

    public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        if (par2ArrayOfStr.length < 1)
        {
            throw new WrongUsageException("Invalid command usage", new Object[0]);
        }
        else
        {
            EntityPlayerMP entityplayermp;

            if (par2ArrayOfStr.length != 2 && par2ArrayOfStr.length != 4)
            {
                entityplayermp = getCommandSenderAsPlayer(par1ICommandSender);
            }
            else
            {
                entityplayermp = func_82359_c(par1ICommandSender, par2ArrayOfStr[0]);

                if (entityplayermp == null)
                {
                    throw new PlayerNotFoundException();
                }
            }

            if (par2ArrayOfStr.length != 3 && par2ArrayOfStr.length != 4)
            {
                if (par2ArrayOfStr.length == 1 || par2ArrayOfStr.length == 2)
                {
                    EntityPlayerMP entityplayermp1 = func_82359_c(par1ICommandSender, par2ArrayOfStr[par2ArrayOfStr.length - 1]);

                    if (entityplayermp1 == null)
                    {
                        throw new PlayerNotFoundException();
                    }

                    if (entityplayermp1.worldObj != entityplayermp.worldObj)
                    {
                        notifyAdmins(par1ICommandSender, "commands.tp.notSameDimension", new Object[0]);
                        return;
                    }

                   // Entity ship = entityplayermp.ridingEntity;
                    
                    //entityplayermp.mountEntity((Entity)null);
                    entityplayermp.ridingEntity.setPositionAndRotation(entityplayermp1.posX, Math.min(entityplayermp1.posY + 128, 255), entityplayermp1.posZ, entityplayermp.ridingEntity.rotationYaw, entityplayermp.ridingEntity.rotationPitch);
                    entityplayermp.playerNetServerHandler.setPlayerLocation(entityplayermp1.posX, Math.min(entityplayermp1.posY + 128, 255), entityplayermp1.posZ, entityplayermp1.rotationYaw, entityplayermp1.rotationPitch);
                    //entityplayermp.mountEntity(ship);
                    
                    notifyAdmins(par1ICommandSender, "commands.tp.success", new Object[] {entityplayermp.getEntityName(), entityplayermp1.getEntityName()});
                }
            }
            else if (entityplayermp.worldObj != null)
            {
                int i = par2ArrayOfStr.length - 3;
                double d0 = func_110666_a(par1ICommandSender, entityplayermp.posX, par2ArrayOfStr[i++]);
                double d1 = func_110665_a(par1ICommandSender, entityplayermp.posY, par2ArrayOfStr[i++], 0, 0);
                double d2 = func_110666_a(par1ICommandSender, entityplayermp.posZ, par2ArrayOfStr[i++]);
                //Entity ship = entityplayermp.ridingEntity;
                //entityplayermp.mountEntity((Entity)null);
                entityplayermp.ridingEntity.setPositionAndRotation(d0, d1, d2, entityplayermp.ridingEntity.rotationYaw, entityplayermp.ridingEntity.rotationPitch);
                entityplayermp.setPositionAndUpdate(d0, d1, d2);
                
                notifyAdmins(par1ICommandSender, "commands.tp.success.coordinates", new Object[] {entityplayermp.getEntityName(), Double.valueOf(d0), Double.valueOf(d1), Double.valueOf(d2)});
            }
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        return par2ArrayOfStr.length != 1 && par2ArrayOfStr.length != 2 ? null : getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames());
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] par1ArrayOfStr, int par2)
    {
        return par2 == 0;
    }
}
