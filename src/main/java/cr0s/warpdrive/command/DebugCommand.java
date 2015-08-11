package cr0s.warpdrive.command;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.WarpDriveConfig;

/*
 *   /wdebug <dimension> <coordinates> <blockId> <Metadata> <actions>
 */

public class DebugCommand extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "wdebug";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender)
	{
		return "/" + getCommandName() + " <dimension> <x> <y> <z> <blockId> <Metadata> <action><action>...\n"
				+ "dimension: 0/world, 2/space, 3/hyperspace\n"
				+ "coordinates: x,y,z\n"
				+ "action: I(nvalidate), V(alidate), A(set air), R(emoveEntity), P(setBlock), S(etEntity)";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] params)
	{
		EntityPlayerMP player = (EntityPlayerMP)icommandsender;
		if(params.length > 6 )
		{
			int dim, x, y, z, metadata;
			int block;
			String actions;
			try
			{
				String par = params[0].toLowerCase();
				if (par.equals("world") || par.equals("overworld") || par.equals("0"))
				{
					dim = 0;
				}
				else if (par.equals("nether") || par.equals("thenether") || par.equals("-1"))
				{
					dim = -1;
				}
				else if (par.equals("s") || par.equals("space"))
				{
					dim = WarpDriveConfig.G_SPACE_DIMENSION_ID;
				}
				else if (par.equals("h") || par.equals("hyper") || par.equals("hyperspace"))
				{
					dim = WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID;
				}
				else
				{
					dim = Integer.parseInt(par);
				}

				x = Integer.parseInt(params[1]);
				y = Integer.parseInt(params[2]);
				z = Integer.parseInt(params[3]);
				block = Integer.parseInt(params[4]);
				metadata = Integer.parseInt(params[5]);
				actions = params[6];
			}
			catch (Exception e)
			{
				e.printStackTrace();
				WarpDrive.addChatMessage(player, getCommandUsage(icommandsender));
				return;
			}

			WarpDrive.logger.info("/" + getCommandName() + " " + dim + " " + x + "," + y + "," + z + " " + block + ":" + metadata + " " + actions);
			World worldObj = DimensionManager.getWorld(dim);
			TileEntity te = worldObj.getTileEntity(x, y, z);
			WarpDrive.logger.info("[" + getCommandName() + "] In dimension " + worldObj.getProviderName() + " - " + worldObj.getWorldInfo().getWorldName() + ", Current block is "
					+ worldObj.getBlock(x, y, z) + ":" + worldObj.getBlockMetadata(x, y, z) + ", tile entity is " + ((te == null) ? "undefined" : "defined"));
			String side = FMLCommonHandler.instance().getEffectiveSide().isClient() ? "Client":"Server";

			// I(nvalidate), V(alidate), A(set air), R(emoveEntity), P(setBlock), S(etEntity)
			boolean bReturn = false;
			for (char ch: actions.toUpperCase().toCharArray()) {
				switch (ch) {
				case 'I':
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": invalidating");
					if (te != null) {
						te.invalidate();
					}
					break;
				case 'V':
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": validating");
					if (te != null) {
						te.validate();
					}
					break;
				case 'A':
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": setting to Air");
					bReturn = worldObj.setBlockToAir(x, y, z);
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": returned " + bReturn);
					break;
				case 'R':
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": remove entity");
					worldObj.removeTileEntity(x, y, z);
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": set block " + x + ", " + y + ", " + z + " to " + block + ":" + metadata);
					bReturn = worldObj.setBlock(x, y, z, Block.getBlockById(block), metadata, ch - '0');
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": returned " + bReturn);
					break;
				case 'P':
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": set block " + x + ", " + y + ", " + z + " to " + block + ":" + metadata);
					bReturn = worldObj.setBlock(x, y, z, Block.getBlockById(block), metadata, 2);
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": returned " + bReturn);
					break;
				case 'S':
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": set entity");
					worldObj.setTileEntity(x, y, z, te);
					break;
				case 'C':
					WarpDrive.logger.info("[" + getCommandName() + "] " + side + ": update containing block info");
					if (te != null) {
						te.updateContainingBlockInfo();
					}
					break;
				}
			}
		}
		else
		{
			WarpDrive.addChatMessage(player, getCommandUsage(icommandsender));
		}
	}

}