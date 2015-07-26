package cr0s.warpdrive.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import cr0s.warpdrive.WarpDrive;

public class InvisibleCommand extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public String getCommandName() {
		return "invisible";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		EntityPlayer player = (EntityPlayer) icommandsender;

		if (astring.length >= 1) {
			WarpDrive.logger.info("/invisible: setting invisible to " + astring[0]);
			
			// get an online player by name
			List<EntityPlayer> onlinePlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			for (EntityPlayer onlinePlayer : onlinePlayers) {
				if (onlinePlayer.getCommandSenderName().equalsIgnoreCase(astring[0])) {
					player = onlinePlayer;
				}
			}
		}

		if (player == null) {
			return;
		}

		// Toggle invisibility
		player.setInvisible(!player.isInvisible());
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/invisible [player]";
	}
}
