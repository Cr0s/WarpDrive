package cr0s.WarpDrive.item;

import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.api.IAirCanister;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ItemWarpComponent extends Item implements IAirCanister {	
	private Icon[] potentialIcons;
	private String[] potentialUnlocalized = new String[9];
	private ItemStack[] cachedIS;
	
	public ItemWarpComponent(int par1) {
		super(par1);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.crafting.Malformed");
		setCreativeTab(WarpDrive.warpdriveTab);
		
		potentialUnlocalized[0] = "EmptyCore";
		potentialUnlocalized[1] = "TeleCore";
		potentialUnlocalized[2] = "WarpCore";
		potentialUnlocalized[3] = "LaserCore";
		potentialUnlocalized[4] = "ReactorCore";
		potentialUnlocalized[5] = "InterfaceComputer";
		potentialUnlocalized[6] = "InterfacePower";
		potentialUnlocalized[7] = "PowerCore";
		potentialUnlocalized[8] = "AirCanisterEmpty";
		
		potentialIcons = new Icon[potentialUnlocalized.length];
		cachedIS = new ItemStack[potentialUnlocalized.length];
	}
	
	public ItemStack getIS(int damage) {
		if (damage >=0 && damage < potentialUnlocalized.length) {
			if (cachedIS[damage] == null) {
				cachedIS[damage] = new ItemStack(WarpDrive.componentItem,1,damage);
			}
			return cachedIS[damage];
		}
		return null;
	}
	
	public ItemStack getISNoCache(int amount,int damage) {
		return new ItemStack(WarpDrive.componentItem, amount, damage);
	}
	
	public void registerRecipes() {
		WarpDrive.debugPrint("Registering empty recipe");
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(0),false,"nrn","r r","nrn",
				'r', Item.redstone,
				'n', Item.goldNugget));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(1),false,"g","e","c",
				'g', Block.glass,
				'e', Item.enderPearl,
				'c', getIS(0)));
			
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(2),false," g ","ede"," c ",
				'g', Block.glass,
				'e', Item.enderPearl,
				'd', Item.diamond,
				'c', getIS(0)));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(3),false," g ","rtr"," c ",
				'g', Block.glass,
				'r', "dyeBlue",
				't', Block.torchWood,
				'c', getIS(0)));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(4),false," l ","rcr"," l ",
				'l', "dyeWhite",
				'r', Item.coal,
				'c', getIS(0)));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(5),false,"g  ","gwr","rwr",
				'g', Item.goldNugget,
				'r', Item.redstone,
				'w', "plankWood"));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(6),false,"gig","iri","gig",
				'g', Item.goldNugget,
				'r', Item.redstone,
				'i', Item.ingotIron));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(7),false,"glg","ldl","glg",
				'g', Item.goldNugget,
				'l', "dyeBlue",
				'd', Item.diamond));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(8),false,"gcg","g g","gcg",
				'g', Block.glass,
				'c', getIS(0)));
	}
	
	public boolean doesMatch(ItemStack is, String unlocalised) {
		if (is == null) {
			return false;
		}
		if (!(is.getItem() instanceof ItemWarpComponent)) {
				return false;
		}
		String data = potentialUnlocalized[is.getItemDamage()];
		WarpDrive.debugPrint(data);
		return data.equals(unlocalised);
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		for(int i = 0; i < potentialUnlocalized.length; i++) {
			potentialIcons[i] = par1IconRegister.registerIcon("warpdrive:component" + potentialUnlocalized[i]);
		}
    }
	
	@Override
	public String getUnlocalizedName(ItemStack itemSt) {
		int damage = itemSt.getItemDamage();
		if (damage >= 0 && damage < potentialUnlocalized.length) {
			return "item.warpdrive.crafting." + potentialUnlocalized[damage];
		}
		return getUnlocalizedName();
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage) {
		if (damage >= 0 && damage < potentialUnlocalized.length) {
			return potentialIcons[damage];
		}
		return potentialIcons[0];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		for(int i = 0; i < potentialUnlocalized.length; i++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
    }
	
	//For empty air cans
	@Override
	public ItemStack fullDrop(ItemStack is) {
		if (doesMatch(is, "AirCanisterEmpty")) {
			return WarpDrive.airCanisterItem.fullDrop(is);
		}
		return null;
	}	
	
	@Override
	public ItemStack emptyDrop(ItemStack is) {
		if (doesMatch(is, "AirCanisterEmpty")) {
			return WarpDrive.airCanisterItem.emptyDrop(is);
		}
		return null;
	}
	
	@Override
	public boolean canContainAir(ItemStack is) {
		return doesMatch(is, "AirCanisterEmpty");
	}
	
	@Override
	public boolean containsAir(ItemStack is) {
		return false;
	}
}