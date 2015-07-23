package cr0s.WarpDrive.machines;

import cr0s.WarpDrive.machines.WarpBlockContainer;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockChunkLoader extends WarpBlockContainer
{
	Icon iconBuffer;

	public BlockChunkLoader(int par1)
	{
		super(par1);
		setUnlocalizedName("warpdrive.machines.ChunkLoader");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityChunkLoader();
	}
	
	@Override
	public void registerIcons(IconRegister ir)
	{
		iconBuffer = ir.registerIcon("warpdrive:chunkLoader");
	}
	
	@Override
	public Icon getIcon(int side, int damage)
	{
		return iconBuffer;
	}

}
