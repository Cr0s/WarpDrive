package cr0s.WarpDrive.machines;

import java.util.Random;

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

public class BlockTransporter extends BlockContainer {

	private Icon[] iconBuffer;
	
	public BlockTransporter(int par1, Material par2Material) {
		super(par1, par2Material);
		setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("Transporter");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityTransporter();
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer = new Icon[3];
        // Solid textures
        iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:transporterBottom");
        iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:transporterTop");
        iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:transporterSide");
    }

    @Override
    public Icon getIcon(int side, int metadata) {
        if (side == 0 || side == 1) {
            return iconBuffer[side];
        }

        return iconBuffer[2];
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random) {
        return 1;
    }

    /**
     * Returns the ID of the items to drop on destruction.
     */
    @Override
    public int idDropped(int par1, Random par2Random, int par3) {
        return this.blockID;
    }
}