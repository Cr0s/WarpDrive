package cr0s.WarpDrive;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;


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
        EntityPlayerMP player = (EntityPlayerMP)icommandsender;
        MinecraftServer server = MinecraftServer.getServer();
        int targetDim = WarpDrive.instance.spaceDimID;
        if (astring.length >= 1) {
	        notifyAdmins(icommandsender, "/invisible: setting invisible to " + astring[0], new Object[0]);
	        player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(astring[0]);
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
