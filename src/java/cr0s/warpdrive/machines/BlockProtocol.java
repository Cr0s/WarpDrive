package cr0s.warpdrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockProtocol extends BlockContainer {
	private Icon[] iconBuffer;

	private final int ICON_INACTIVE_SIDE = 0, ICON_BOTTOM = 1, ICON_TOP = 2, ICON_SIDE_ACTIVATED = 3;

    public BlockProtocol(int id, int texture, Material material) {
        super(id, material);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.machines.WarpProtocol");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer = new Icon[10];
        // Solid textures
        iconBuffer[ICON_INACTIVE_SIDE] = par1IconRegister.registerIcon("warpdrive:contSideInactive");
        iconBuffer[ICON_BOTTOM] = par1IconRegister.registerIcon("warpdrive:contBottom");
        iconBuffer[ICON_TOP] = par1IconRegister.registerIcon("warpdrive:contTop");
        // Animated textures
        iconBuffer[ICON_SIDE_ACTIVATED    ] = par1IconRegister.registerIcon("warpdrive:contSideActive0");
        iconBuffer[ICON_SIDE_ACTIVATED + 1] = par1IconRegister.registerIcon("warpdrive:contSideActive1");
        iconBuffer[ICON_SIDE_ACTIVATED + 2] = par1IconRegister.registerIcon("warpdrive:contSideActive2");
        iconBuffer[ICON_SIDE_ACTIVATED + 3] = par1IconRegister.registerIcon("warpdrive:contSideActive3");
        iconBuffer[ICON_SIDE_ACTIVATED + 4] = par1IconRegister.registerIcon("warpdrive:contSideActive4");
        iconBuffer[ICON_SIDE_ACTIVATED + 5] = par1IconRegister.registerIcon("warpdrive:contSideActive5");
        iconBuffer[ICON_SIDE_ACTIVATED + 6] = par1IconRegister.registerIcon("warpdrive:contSideActive6");
    }

    @Override
    public Icon getIcon(int side, int metadata) {
        if (side == 0) {
            return iconBuffer[ICON_BOTTOM];
        } else if (side == 1) {
            return iconBuffer[ICON_TOP];
        }

        if (metadata == 0) { // Inactive state
            return iconBuffer[ICON_INACTIVE_SIDE];
        } else if (metadata > 0) { // Activated, in metadata stored mode number
			if (ICON_SIDE_ACTIVATED + metadata - 1 < iconBuffer.length) {
				return iconBuffer[ICON_SIDE_ACTIVATED + metadata];
			} else {
				return null;
			}
		}
        
		return null;
    }

    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileEntityProtocol();
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
    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return false;
        }

        if (par5EntityPlayer.getHeldItem() == null) {
        	TileEntityProtocol controller = (TileEntityProtocol)par1World.getBlockTileEntity(par2, par3, par4);
        	if (controller != null) {
	            controller.attachPlayer(par5EntityPlayer);
	            par5EntityPlayer.addChatMessage(controller.getBlockType().getLocalizedName() + " Attached players: " + controller.getAttachedPlayersList());
	            return true;
        	}
        }

        return false;
    }
}