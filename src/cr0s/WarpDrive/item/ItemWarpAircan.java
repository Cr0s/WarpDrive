package cr0s.WarpDrive.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.api.IAirCanister;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemWarpAircan extends Item implements IAirCanister {
	Icon icon;
	public ItemWarpAircan(int id) {
		super(id);
		setMaxDamage(20);
		setCreativeTab(WarpDrive.warpdriveTab);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.useful.AirCanFull");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister ir) {
		icon = ir.registerIcon("warpdrive:aircanFull");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage) {
		return icon;
	}

	@Override
	public ItemStack emptyDrop(ItemStack is) {
 		return WarpDrive.componentItem.getISNoCache(1, 8);
 	}
 
	@Override
	public ItemStack fullDrop(ItemStack can) {
		return new ItemStack(WarpDrive.airCanItem,1);
	}
	
	@Override
	public boolean canContainAir(ItemStack can) {
		if (can != null && can.getItem() instanceof ItemWarpAircan) {
			return can.getItemDamage() > 0;
		}
		return false;
	}

	@Override
	public boolean containsAir(ItemStack can) {
		return true;
	}
}
