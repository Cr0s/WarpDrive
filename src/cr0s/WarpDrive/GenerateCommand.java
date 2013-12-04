package cr0s.WarpDrive;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;

/*
*   /generate <structure>
*   Possible structures:
*   moon, ship, asteroid, astfield, gascloud, star
*/

public class GenerateCommand extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "generate";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "/" + getCommandName() + " <structure>\nPossible structures: moon, ship, asteroid, astfield, gascloud, star, jumpgate <name>";
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] params)
    {
        EntityPlayerMP player = (EntityPlayerMP)icommandsender;
        String struct = params[0];

        // Reject command, if player is not in space
        if (player.dimension != WarpDrive.instance.spaceDimID && (!"ship".equals(struct)))
        {
            player.addChatMessage("* generate: this structure generation allowed only in space!");
            return;
        }

        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ);

        if (struct.equals("moon"))
        {
            notifyAdmins(icommandsender, "/generate: generating moon at " + x + ", " + (y - 16) + ", " + z, new Object[0]);
            WarpDrive.instance.spaceWorldGenerator.generateMoon(player.worldObj, x, y - 16, z);
        }
        else if (struct.equals("ship"))
        {
            notifyAdmins(icommandsender, "/generate: generating NPC ship at " + x + ", " + y + ", " + z, new Object[0]);
            new WorldGenSmallShip(false).generate(player.worldObj, player.worldObj.rand, x, y, z);
        }
        else if (struct.equals("asteroid"))
        {
            notifyAdmins(icommandsender, "/generate: generating asteroid at " + x + ", " + (y - 10) + ", " + z, new Object[0]);
            WarpDrive.instance.spaceWorldGenerator.generateAsteroid(player.worldObj, x, y - 10, z, 6, 11);
        }
        else if (struct.equals("astfield"))
        {
            notifyAdmins(icommandsender, "/generate: generating asteroid field at " + x + ", " + y + ", " + z, new Object[0]);
            WarpDrive.instance.spaceWorldGenerator.generateAsteroidField(player.worldObj, x, y, z);
        }
        else if (struct.equals("gascloud"))
        {
            notifyAdmins(icommandsender, "/generate: generating gas cloud at " + x + ", " + y + ", " + z, new Object[0]);
            WarpDrive.instance.spaceWorldGenerator.generateGasCloudOfColor(player.worldObj, x, y, z, 15, 20, player.worldObj.rand.nextInt(12));
        }
        else if (struct.equals("star"))
        {
            notifyAdmins(icommandsender, "/generate: generating star at " + x + ", " + y + ", " + z, new Object[0]);
            WarpDrive.instance.spaceWorldGenerator.generateStar(player.worldObj, x, y, z);
        }
        else if (struct.equals("jumpgate"))
        {
            if (params.length == 2)
            {
                notifyAdmins(icommandsender, "/generate: creating jumpgate at " + x + ", " + y + ", " + z, new Object[0]);

                if (WarpDrive.instance.jumpGates.addGate(params[1], x, y, z))
                {
                    JumpGateGenerator.generate(player.worldObj, x, y, z);
                }
                else
                {
                    notifyAdmins(icommandsender, "/generate: jumpgate '" + params[1] + "' already exists.", new Object[0]);
                }
            }
        }
    }
}
