package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockReactor extends BlockContainer {    
    BlockReactor(int id, int texture, Material material) {
        super(id, material);
    }
       
  //  @Override
  //  public String getTextureFile () {
  //          return CommonProxy.BLOCK_TEXTURE;
  //  }
    
    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileEntityReactor();
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
    
    /**
     * Called upon block activation (right click on the block.)
     */
    @SideOnly(Side.SERVER)
    @Override
    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        return false;
    }  
    
     /**
     * Called when the block is clicked by a player. Args: x, y, z, entityPlayer
     */
    //@SideOnly(Side.SERVER)
    @Override
    public void onBlockClicked(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {        
        TileEntityReactor reactor = (TileEntityReactor)par1World.getBlockTileEntity(par2, par3, par4);
        
        if (reactor != null){ 
            par5EntityPlayer.sendChatToPlayer(reactor.getCoreState());
        }
    }   
}