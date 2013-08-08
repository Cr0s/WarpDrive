package shipmod.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatMessageComponent;

public class CommandHelp extends CommandBase
{
    public static List<CommandBase> asCommands = new ArrayList();

    public String getCommandName()
    {
        return "smhelp";
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

    public List<String> getCommandAliases()
    {
        return Arrays.asList(new String[] {"sm"});
    }

    public void processCommand(ICommandSender icommandsender, String[] astring)
    {
        icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("ShipMod commands:"));
        Iterator i$ = asCommands.iterator();

        while (i$.hasNext())
        {
            CommandBase cb = (CommandBase)i$.next();
            icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d(cb.getCommandUsage(icommandsender)));
        }
    }

    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/sm or /smhelp";
    }
}
