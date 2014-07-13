package cr0s.WarpDrive.machines;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.WarpDrive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockPowerReactor extends BlockContainer
{
	Icon[] iconBuffer = new Icon[2];
	public BlockPowerReactor(int id)
    {
        super(id, Material.iron);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.power.Reactor");
    }

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityPowerReactor();
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:powerReactorTopBottom");
        iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:powerReactorSides");
    }

}
