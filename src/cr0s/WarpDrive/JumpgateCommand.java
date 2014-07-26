package cr0s.WarpDrive;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class JumpgateCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getCommandName()
    {
        return "jumpgates";
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return "Lists jumpgates";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring)
	{
		EntityPlayerMP player = (EntityPlayerMP)icommandsender;
		player.addChatMessage(WarpDrive.instance.jumpGates.commaList());
	}
}
