package cr0s.warpdrive.command;

import cr0s.warpdrive.WarpDriveConfig;
import cr0s.warpdrive.world.SpaceTeleporter;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;

public class SpaceTpCommand extends CommandBase {
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
        	player = (EntityPlayerMP)icommandsender; 
        }
        if (astring.length >= 1) {
            if ("hyper".equals(astring[0])) {
                targetDim = WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID;
            } else if ("overworld".equals(astring[0])) {
                targetDim = 0;
            } else {
                player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(astring[0]);
            }
        }

        if (player == null) {
           notifyAdmins(icommandsender, "/space: undefined player");
           return;
        }

        WorldServer targetWorld = server.worldServerForDimension(targetDim);
        notifyAdmins(icommandsender, "/space: teleporting player " + player.getDisplayName() + " to " + targetDim + ":" + targetWorld.getWorldInfo().getWorldName(), new Object[0]);
        SpaceTeleporter teleporter = new SpaceTeleporter(targetWorld, 0, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
        server.getConfigurationManager().transferPlayerToDimension(player, targetDim, teleporter);
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/space [hyper|overworld|<player>]";
    }
}
