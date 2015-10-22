package cr0s.warpdrive.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import cr0s.warpdrive.WarpDrive;

public class CommandJumpgates extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public String getCommandName() {
		return "jumpgates";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "Lists jumpgates";
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] astring) {
		EntityPlayerMP player = (EntityPlayerMP) commandSender;
		WarpDrive.addChatMessage(player, "Jumpgates: " + WarpDrive.jumpgates.commaList());
	}
}
