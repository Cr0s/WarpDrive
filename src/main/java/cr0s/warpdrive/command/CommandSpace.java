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
import cr0s.warpdrive.config.WarpDriveConfig;
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
	public void processCommand(ICommandSender sender, String[] args) {
		MinecraftServer server = MinecraftServer.getServer();
		
		// set defaults
		int targetDimensionId = Integer.MAX_VALUE;
		
		EntityPlayerMP player = null;
		if (sender != null && sender instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) sender;
		}
		
		// parse arguments
		if (args.length == 0) {
			// nop
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
				WarpDrive.addChatMessage(sender, getCommandUsage(sender));
				return;
			}
			
			EntityPlayerMP playerFound = getOnlinePlayerByName(args[0]);
			if (playerFound != null) {
				player = playerFound;
			} else {
				targetDimensionId = getDimensionId(args[0]);
			}
			
		} else if (args.length == 2) {
			player = getOnlinePlayerByName(args[0]);
			targetDimensionId = getDimensionId(args[1]);
			
		} else {
			WarpDrive.addChatMessage(sender, "/space: too many arguments " + args.length);
			return;
		}
		
		// check player
		if (player == null) {
			WarpDrive.addChatMessage(sender, "/space: undefined player");
			return;
		}
		
		// toggle between overworld and space if no dimension was providen
		if (targetDimensionId == Integer.MAX_VALUE) {
			if (player.worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID) {
				targetDimensionId = 0;
			} else {
				targetDimensionId = WarpDriveConfig.G_SPACE_DIMENSION_ID;
			}
		}
		
		// get target world
		WorldServer targetWorld = server.worldServerForDimension(targetDimensionId);
		if (targetWorld == null) {
			WarpDrive.addChatMessage(sender, "/space: undefined dimension " + targetDimensionId);
			return;
		}
		
		// inform player
		String message = "Teleporting player " + player.getCommandSenderName() + " to dimension " + targetDimensionId + "..."; // + ":" + targetWorld.getWorldInfo().getWorldName();
		WarpDrive.addChatMessage(sender, message);
		WarpDrive.logger.info(message);
		if (sender != player) {
			WarpDrive.addChatMessage(player, sender + " is teleporting you to dimension " + targetDimensionId); // + ":" + targetWorld.getWorldInfo().getWorldName());
		}
		
		// find a good spot
		int newX = MathHelper.floor_double(player.posX);
		int newY = Math.min(255, Math.max(0, MathHelper.floor_double(player.posY)));
		int newZ = MathHelper.floor_double(player.posZ);
		
		if ( (targetWorld.isAirBlock(newX, newY - 1, newZ) && !player.capabilities.allowFlying)
		  || !targetWorld.isAirBlock(newX, newY, newZ)
		  || !targetWorld.isAirBlock(newX, newY + 1, newZ)) {// non solid ground and can't fly, or inside blocks
			newY = targetWorld.getTopSolidOrLiquidBlock(newX, newZ) + 1;
			if (newY == 0) {
				newY = 128;
			}
		}
		
		SpaceTeleporter teleporter = new SpaceTeleporter(targetWorld, 0, newX, newY, newZ);
		server.getConfigurationManager().transferPlayerToDimension(player, targetDimensionId, teleporter);
		player.setPositionAndUpdate(newX + 0.5D, newY + 0.05D, newZ + 0.5D);
		player.sendPlayerAbilities();
	}
	
	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/space (<playerName>) ([overworld|nether|end|theend|space|hyper|hyperspace|<dimensionId>])";
	}
	
	private EntityPlayerMP getOnlinePlayerByName(final String playerName) {
		List<EntityPlayer> onlinePlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayer onlinePlayer : onlinePlayers) {
			if (onlinePlayer.getCommandSenderName().equalsIgnoreCase(playerName) && onlinePlayer instanceof EntityPlayerMP) {
				return (EntityPlayerMP) onlinePlayer;
			}
		}
		return null;
	}
	
	private int getDimensionId(String stringDimension) {
		if (stringDimension.equalsIgnoreCase("overworld")) {
			return 0;
		} else if (stringDimension.equalsIgnoreCase("nether")) {
			return -1;
		} else if (stringDimension.equalsIgnoreCase("end") || stringDimension.equalsIgnoreCase("theend")) {
			return 1;
		} else if (stringDimension.equalsIgnoreCase("space")) {
			return WarpDriveConfig.G_SPACE_DIMENSION_ID;
		} else if (stringDimension.equalsIgnoreCase("hyper") || stringDimension.equalsIgnoreCase("hyperspace")) {
			return WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID;
		}
		try {
			return Integer.parseInt(stringDimension);
		} catch(Exception exception) {
			// exception.printStackTrace();
			WarpDrive.logger.info("/space: invalid dimension '" + stringDimension + "', expecting integer or overworld/nether/end/theend/space/hyper/hyperspace");
		}
		return 0;
	}
}
