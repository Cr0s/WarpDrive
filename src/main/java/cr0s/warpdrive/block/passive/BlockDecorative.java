package cr0s.warpdrive.block.passive;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import cr0s.warpdrive.WarpDrive;

public class BlockDecorative extends Block {
	public static enum decorativeTypes {
		Plain, Energized, Network
	};

	private ItemStack[] isCache = new ItemStack[decorativeTypes.values().length];
	private IIcon[] iconBuffer = new IIcon[decorativeTypes.values().length];

	public BlockDecorative() {
		super(Material.iron);
		setHardness(0.5f);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.warpdriveTab);
		setBlockName("warpdrive.passive.Plain");
	}

	private boolean isValidDamage(final int damage) {
		return damage >= 0 && damage < decorativeTypes.values().length;
	}

	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for (decorativeTypes val : decorativeTypes.values())
			par3List.add(new ItemStack(par1, 1, val.ordinal()));
	}

	@Override
	public void registerBlockIcons(IIconRegister ir) {
		for (decorativeTypes val : decorativeTypes.values())
			iconBuffer[val.ordinal()] = ir.registerIcon("warpdrive:passive/decorative" + val.toString());
	}

	@Override
	public IIcon getIcon(int side, int damage) {
		if (isValidDamage(damage))
			return iconBuffer[damage];
		return iconBuffer[0];
	}

	@Override
	public int damageDropped(int damage) {
		return damage;
	}

	public ItemStack getItemStack(int damage) {
		if (!isValidDamage(damage))
			return null;

		if (isCache[damage] == null)
			isCache[damage] = getItemStackNoCache(damage);
		return isCache[damage];
	}

	public ItemStack getItemStackNoCache(int damage, int amount) {
		if (!isValidDamage(damage))
			return null;

		return new ItemStack(WarpDrive.decorativeBlock, amount, damage);
	}

	public ItemStack getItemStackNoCache(int damage) {
		return getItemStackNoCache(damage, 1);
	}

	public void initRecipes() {
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStackNoCache(0, 8), false, "sss", "scs", "sss", 's', Blocks.stone, 'c', WarpDrive.componentItem.getItemStack(0)));

		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStackNoCache(2, 8), false, "sss", "scs", "sss", 's', getItemStack(0), 'c', WarpDrive.componentItem.getItemStack(5)));
	}

}
