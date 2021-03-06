package cr0s.warpdrive.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.world.JumpgateGenerator;
import cr0s.warpdrive.world.WorldGenSmallShip;
import cr0s.warpdrive.world.WorldGenStation;

/*
 *   /generate <structure>
 *   Possible structures:
 *   moon, ship, asteroid, astfield, gascloud, star
 */

public class CommandGenerate extends CommandBase {
	@Override
	public String getCommandName() {
		return "generate";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender) {
		return "/" + getCommandName() + " <structure>\nPossible structures: moon, ship, asteroid, astfield, gascloud, star <class>, jumpgate <name>";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] params) {
		EntityPlayerMP player = (EntityPlayerMP) icommandsender;
		if (params.length > 0) {
			String struct = params[0];

			// Reject command, if player is not in space
			if (player.dimension != WarpDriveConfig.G_SPACE_DIMENSION_ID && (!"ship".equals(struct))) {
				WarpDrive.addChatMessage(player, "* generate: this structure is only allowed in space!");
				return;
			}

			int x = MathHelper.floor_double(player.posX);
			int y = MathHelper.floor_double(player.posY);
			int z = MathHelper.floor_double(player.posZ);

			if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
				if (struct.equals("moon")) {
					WarpDrive.logger.info("/generate: generating moon at " + x + ", " + (y - 16) + ", " + z);
					WarpDrive.instance.spaceWorldGenerator.generateMoon(player.worldObj, x, y - 16, z, null);
				} else if (struct.equals("ship")) {
					WarpDrive.logger.info("/generate: generating NPC ship at " + x + ", " + y + ", " + z);
					new WorldGenSmallShip(false).generate(player.worldObj, player.worldObj.rand, x, y, z);
				} else if (struct.equals("station")) {
					WarpDrive.logger.info("/generate: generating station at " + x + ", " + y + ", " + z);
					new WorldGenStation(false).generate(player.worldObj, player.worldObj.rand, x, y, z);
				} else if (struct.equals("asteroid")) {
					WarpDrive.logger.info("/generate: generating asteroid at " + x + ", " + (y - 10) + ", " + z);
					WarpDrive.instance.spaceWorldGenerator.generateRandomAsteroid(player.worldObj, x, y - 10, z);
				} else if (struct.equals("astfield")) {
					WarpDrive.logger.info("/generate: generating asteroid field at " + x + ", " + y + ", " + z);
					WarpDrive.instance.spaceWorldGenerator.generateAsteroidField(player.worldObj, x, y, z);
				} else if (struct.equals("gascloud")) {
					WarpDrive.logger.info("/generate: generating gas cloud at " + x + ", " + y + ", " + z);
					String type = (params.length > 1) ? params[1] : null;
					WarpDrive.instance.spaceWorldGenerator.generateGasCloudOfColor(player.worldObj, x, y, z, 15, 20, type);
				} else if (struct.equals("star")) {
					WarpDrive.logger.info("/generate: generating star at " + x + ", " + y + ", " + z);
					String type = (params.length > 1) ? params[1] : null;
					WarpDrive.instance.spaceWorldGenerator.generateStar(player.worldObj, x, y, z, type);
				} else if (struct.equals("jumpgate")) {
					if (params.length == 2) {
						WarpDrive.logger.info("/generate: creating jumpgate at " + x + ", " + y + ", " + z);

						if (WarpDrive.jumpgates.addGate(params[1], x, y, z)) {
							JumpgateGenerator.generate(player.worldObj, x, Math.min(y, 255 - JumpgateGenerator.GATE_SIZE_HALF - 1), z);
						} else {
							WarpDrive.logger.info("/generate: jumpgate '" + params[1] + "' already exists.");
						}
					}
				} else {
					WarpDrive.addChatMessage(player, getCommandUsage(icommandsender));
				}
			}
		} else {
			WarpDrive.addChatMessage(player, getCommandUsage(icommandsender));
		}
	}
}
