package cr0s.warpdrive.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.WarpDriveConfig;
import cr0s.warpdrive.world.SpaceTeleporter;

public class CommandSpace extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandName() {
		return "space";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		EntityPlayerMP player = null;
		MinecraftServer server = MinecraftServer.getServer();
		int targetDim = WarpDriveConfig.G_SPACE_DIMENSION_ID;

		if (icommandsender != null && icommandsender instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) icommandsender;
		}
		if (astring.length >= 1) {
			if ("hyper".equals(astring[0])) {
				targetDim = WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID;
			} else if ("overworld".equals(astring[0])) {
				targetDim = 0;
			} else {
				// get an online player by name
				List<EntityPlayer> onlinePlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
				for (EntityPlayer onlinePlayer : onlinePlayers) {
					if (onlinePlayer.getCommandSenderName().equalsIgnoreCase(astring[0]) && onlinePlayer instanceof EntityPlayerMP) {
						player = (EntityPlayerMP) onlinePlayer;
					}
				}
			}
		}

		if (player == null) {
			WarpDrive.logger.info("/space: undefined player");
			return;
		}

		WorldServer targetWorld = server.worldServerForDimension(targetDim);
		WarpDrive.logger.info("/space: teleporting player " + player.getCommandSenderName() + " to " + targetDim + ":" + targetWorld.getWorldInfo().getWorldName());
		SpaceTeleporter teleporter = new SpaceTeleporter(targetWorld, 0, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
		server.getConfigurationManager().transferPlayerToDimension(player, targetDim, teleporter);
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/space [hyper|overworld|<player>]";
	}
}
