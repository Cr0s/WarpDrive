package cr0s.warpdrive.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IAirCanister;

public class ItemComponent extends Item implements IAirCanister {	
	private IIcon[] potentialIcons;
	private String[] potentialUnlocalized = new String[9];
	private ItemStack[] cachedIS;
	
	public ItemComponent() {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.crafting.Malformed");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		potentialUnlocalized[0] = "EmptyCore";
		potentialUnlocalized[1] = "TeleCore";
		potentialUnlocalized[2] = "WarpCore";
		potentialUnlocalized[3] = "LaserCore";
		potentialUnlocalized[4] = "ReactorCore";
		potentialUnlocalized[5] = "InterfaceComputer";
		potentialUnlocalized[6] = "InterfacePower";
		potentialUnlocalized[7] = "PowerCore";
		potentialUnlocalized[8] = "AirCanisterEmpty";
		
		potentialIcons = new IIcon[potentialUnlocalized.length];
		cachedIS = new ItemStack[potentialUnlocalized.length];
	}
	
	public ItemStack getItemStack(int damage) {
		if (damage >=0 && damage < potentialUnlocalized.length) {
			if (cachedIS[damage] == null) {
				cachedIS[damage] = new ItemStack(WarpDrive.itemComponent,1,damage);
			}
			return cachedIS[damage];
		}
		return null;
	}
	
	public ItemStack getISNoCache(int amount,int damage) {
		return new ItemStack(WarpDrive.itemComponent, amount, damage);
	}
	
	public void registerRecipes() {
		WarpDrive.debugPrint("Registering empty recipe");
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStack(0),false,"nrn","r r","nrn",
				'r', Items.redstone,
				'n', Items.gold_nugget));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStack(1),false,"g","e","c",
				'g', Blocks.glass,
				'e', Items.ender_pearl,
				'c', getItemStack(0)));
			
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStack(2),false," g ","ede"," c ",
				'g', Blocks.glass,
				'e', Items.ender_pearl,
				'd', Items.diamond,
				'c', getItemStack(0)));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStack(3),false," g ","rtr"," c ",
				'g', Blocks.glass,
				'r', "dyeBlue",
				't', Blocks.torch,
				'c', getItemStack(0)));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStack(4),false," l ","rcr"," l ",
				'l', "dyeWhite",
				'r', Items.coal,
				'c', getItemStack(0)));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStack(5),false,"g  ","gwr","rwr",
				'g', Items.gold_nugget,
				'r', Items.redstone,
				'w', "plankWood"));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStack(6),false,"gig","iri","gig",
				'g', Items.gold_nugget,
				'r', Items.redstone,
				'i', Items.iron_ingot));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStack(7),false,"glg","ldl","glg",
				'g', Items.gold_nugget,
				'l', "dyeBlue",
				'd', Items.diamond));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getItemStack(8),false,"gcg","g g","gcg",
				'g', Blocks.glass,
				'c', getItemStack(0)));
	}
	
	public boolean doesMatch(ItemStack is, String unlocalised) {
		if (is == null) {
			return false;
		}
		if (!(is.getItem() instanceof ItemComponent)) {
				return false;
		}
		String data = potentialUnlocalized[is.getItemDamage()];
		WarpDrive.debugPrint(data);
		return data.equals(unlocalised);
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
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
	public IIcon getIconFromDamage(int damage) {
		if (damage >= 0 && damage < potentialUnlocalized.length) {
			return potentialIcons[damage];
		}
		return potentialIcons[0];
	}
	
	@Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for(int i = 0; i < potentialUnlocalized.length; i++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
    }
	
	//For empty air cans
	@Override
	public ItemStack fullDrop(ItemStack is) {
		if (doesMatch(is, "AirCanisterEmpty")) {
			return WarpDrive.itemAirCanisterFull.fullDrop(is);
		}
		return null;
	}	
	
	@Override
	public ItemStack emptyDrop(ItemStack is) {
		if (doesMatch(is, "AirCanisterEmpty")) {
			return WarpDrive.itemAirCanisterFull.emptyDrop(is);
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