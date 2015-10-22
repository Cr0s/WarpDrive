package cr0s.warpdrive.block.passive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import cr0s.warpdrive.WarpDrive;

public class BlockHighlyAdvancedMachine extends Block {
	public BlockHighlyAdvancedMachine() {
		super(Material.rock);
		setHardness(3.0F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.passive.HighlyAdvancedMachine");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		blockIcon = par1IconRegister.registerIcon("warpdrive:passive/highlyAdvancedMachineSide");
	}
	
	@Override
	public Item getItemDropped(int var1, Random var2, int var3) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
}