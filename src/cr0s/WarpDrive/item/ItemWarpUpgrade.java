package cr0s.WarpDrive.item;

import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.data.EnumUpgradeTypes;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ItemWarpUpgrade extends Item
{
	private ItemStack[] isCache = new ItemStack[EnumUpgradeTypes.values().length];
	private Icon[] iconBuffer = new Icon[EnumUpgradeTypes.values().length];
	
	public ItemWarpUpgrade(int par1) 
	{
		super(par1);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.upgrade.Malformed");
		setCreativeTab(WarpDrive.warpdriveTab);
	}
	
	private boolean isValidDamage(int damage)
	{
		return damage >= 0 && damage < EnumUpgradeTypes.values().length;
	}
	
	public ItemStack getIS(int damage)
	{
		if(!isValidDamage(damage))
			return null;
		
		if(isCache[damage] == null)
			isCache[damage] = getISNoCache(damage);
		return isCache[damage];
	}
	
	public ItemStack getISNoCache(int damage)
	{
		if(!isValidDamage(damage))
			return null;
		
		return new ItemStack(WarpDrive.upgradeItem,1,damage);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		if(is == null)
			return  null;
		
		int damage = is.getItemDamage();
		if(isValidDamage(damage))
			return "item.warpdrive.upgrade." + EnumUpgradeTypes.values()[damage].toString();
		
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
		for(int i=0;i<EnumUpgradeTypes.values().length;i++)
			par3List.add(getIS(i));
    }
	
	@Override
	public void addInformation(ItemStack is, EntityPlayer pl, List list, boolean par4)
	{
		if(is == null)
			return;
		
		int damage = is.getItemDamage();
		if(damage == EnumUpgradeTypes.Energy.ordinal())
			list.add("Increases the max energy of the machine");
		else if(damage == EnumUpgradeTypes.Power.ordinal())
			list.add( "Decreases the power usage of the machine");
		else if(damage == EnumUpgradeTypes.Speed.ordinal())
			list.add( "Increases the speed of the machine");
		else if(damage == EnumUpgradeTypes.Range.ordinal())
			list.add( "Increases the range of the machine");
	}
	
	public void initRecipes()
	{
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(EnumUpgradeTypes.Energy.ordinal()),false,"c","e","r",
				'c', WarpDrive.componentItem.getIS(0),
				'e', WarpDrive.componentItem.getIS(7),
				'r', Item.redstone));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(EnumUpgradeTypes.Power.ordinal()),false,"c","e","r",
				'c', WarpDrive.componentItem.getIS(0),
				'e', WarpDrive.componentItem.getIS(6),
				'r', Item.redstone));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(EnumUpgradeTypes.Speed.ordinal()),false,"c","e","r",
				'c', WarpDrive.componentItem.getIS(0),
				'e', Item.sugar,
				'r', Item.redstone));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(getIS(EnumUpgradeTypes.Range.ordinal()),false,"c","e","r",
				'c', WarpDrive.componentItem.getIS(0),
				'e', WarpDrive.transportBeaconBlock,
				'r', Item.redstone));
		
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister ir)
	{
		for(EnumUpgradeTypes val : EnumUpgradeTypes.values())
		{
			iconBuffer[val.ordinal()] = ir.registerIcon("warpdrive:upgrade" + val.toString());
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage)
	{
		if(damage >= 0 && damage < EnumUpgradeTypes.values().length)
			return iconBuffer[damage];
		return iconBuffer[0];
	}

}
