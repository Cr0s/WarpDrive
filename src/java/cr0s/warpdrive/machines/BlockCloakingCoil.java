package cr0s.warpdrive.machines;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockCloakingCoil extends Block {
    private Icon[] iconBuffer;

    public BlockCloakingCoil(int id, int texture, Material material) {
        super(id, material);
        setHardness(0.5F);
		setStepSound(Block.soundMetalFootstep);
		setCreativeTab(WarpDrive.warpdriveTab);
		setUnlocalizedName("warpdrive.machines.CloakingCoil");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        iconBuffer = new Icon[3];
        iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:cloakCoilSide");
        iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:cloakCoilSideActive");
        iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:cloakCoilTop");
    }

    @Override
    public Icon getIcon(int side, int metadata) {
        if (side == 0) {
            return iconBuffer[2];
        } else if (side == 1) {
            return iconBuffer[2];
        }
    	if (metadata == 0) {
    		return iconBuffer[0];
    	} else if (metadata == 1) {
    		return iconBuffer[1];
    	} else {
    		return null;
    	}
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
