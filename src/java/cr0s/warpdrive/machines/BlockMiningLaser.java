package cr0s.warpdrive.machines;

import java.util.Random;

import javax.swing.Icon;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;

public class BlockMiningLaser extends BlockContainer {
    private Icon[] iconBuffer;
    private final static int ICON_TOP = 5;
    public final static int ICON_IDLE = 0;
    public final static int ICON_MININGLOWPOWER = 1;
    public final static int ICON_MININGPOWERED = 2;
    public final static int ICON_SCANNINGLOWPOWER = 3;
    public final static int ICON_SCANNINGPOWERED = 4;

    public BlockMiningLaser(int id, int texture, Material material) {
        super(id, material);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.machines.MiningLaser");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer = new Icon[16];
        // Solid textures
        iconBuffer[ICON_TOP             ] = par1IconRegister.registerIcon("warpdrive:particleBoosterTopBottom");
        iconBuffer[ICON_IDLE            ] = par1IconRegister.registerIcon("warpdrive:miningLaser_idle");
        iconBuffer[ICON_MININGLOWPOWER  ] = par1IconRegister.registerIcon("warpdrive:miningLaser_miningLowPower");
        iconBuffer[ICON_MININGPOWERED   ] = par1IconRegister.registerIcon("warpdrive:miningLaser_miningPowered");
        iconBuffer[ICON_SCANNINGLOWPOWER] = par1IconRegister.registerIcon("warpdrive:miningLaser_scanningLowPower");
        iconBuffer[ICON_SCANNINGPOWERED ] = par1IconRegister.registerIcon("warpdrive:miningLaser_scanningPowered");
    }

    @Override
    public Icon getIcon(int side, int metadata) {
        if (side == 0 || side == 1) {
            return iconBuffer[ICON_TOP];
        }
        if (metadata < iconBuffer.length) {
        	return iconBuffer[metadata];
        } else {
        	return null;
        }
    }
    
    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileEntityMiningLaser();
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

        TileEntityMiningLaser miningLaser = (TileEntityMiningLaser)par1World.getBlockTileEntity(par2, par3, par4);

        if (miningLaser != null && (par5EntityPlayer.getHeldItem() == null)) {
            par5EntityPlayer.addChatMessage(miningLaser.getStatus());
            return true;
        }

        return false;
    }
}