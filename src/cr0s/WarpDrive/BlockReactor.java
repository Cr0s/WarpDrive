package cr0s.WarpDrive;

import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockReactor extends BlockContainer {
    BlockReactor(int id, int texture, Material material) {
        super(id, texture, material);
    }
       
    @Override
    public String getTextureFile () {
            return CommonProxy.BLOCK_TEXTURE_OFFLINE;
    }

    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileEntityReactor(var1);
    }
    
    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 1;
    }

    /**
     * Returns the ID of the items to drop on destruction.
     */
    @Override
    public int idDropped(int par1, Random par2Random, int par3)
    {
        return this.blockID;
    }    
}