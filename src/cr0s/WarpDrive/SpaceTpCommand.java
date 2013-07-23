package cr0s.WarpDrive;

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
        EntityPlayerMP player = (EntityPlayerMP)icommandsender;
        MinecraftServer server = MinecraftServer.getServer();
        int targetDim = WarpDrive.instance.spaceDimID;
        if (astring.length >= 1) {
            if ("hyper".equals(astring[0])) {
                targetDim = WarpDrive.instance.hyperSpaceDimID;
            }
        }
        
        WorldServer to = server.worldServerForDimension(targetDim);        
        SpaceTeleporter teleporter = new SpaceTeleporter(to, 0, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));

        server.getConfigurationManager().transferPlayerToDimension(player, targetDim, teleporter);
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/space [hyper]";
    }
    
}
