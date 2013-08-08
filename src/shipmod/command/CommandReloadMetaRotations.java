package shipmod.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;
import shipmod.ShipMod;

public class CommandReloadMetaRotations extends CommandBase
{
    public String getCommandName()
    {
        return "reloadmeta";
    }

    public void processCommand(ICommandSender icommandsender, String[] astring)
    {
        if (this.canCommandSenderUseCommand(icommandsender))
        {
            ShipMod.instance.metaRotations.readMetaRotationFiles();
            icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Reloading MetaRotations"));
        }
    }

    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/" + this.getCommandName();
    }
}
