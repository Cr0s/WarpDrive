package cr0s.warpdrive.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IAirCanister;

public class ItemAirCanisterFull extends Item implements IAirCanister {
	
	private IIcon icon;
	
	public ItemAirCanisterFull() {
		super();
		setMaxDamage(20);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.armor.AirCanisterFull");
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("warpdrive:componentAirCanisterFull");
	}
	
	@Override
	public IIcon getIconFromDamage(int damage) {
		return icon;
	}

	@Override
	public ItemStack emptyDrop(ItemStack is) {
 		return WarpDrive.itemComponent.getISNoCache(1, 8);
 	}
 
	@Override
	public ItemStack fullDrop(ItemStack can) {
		return new ItemStack(WarpDrive.itemAirCanisterFull,1);
	}
	
	@Override
	public boolean canContainAir(ItemStack can) {
		if (can != null && can.getItem() instanceof ItemAirCanisterFull) {
			return can.getItemDamage() > 0;
		}
		return false;
	}

	@Override
	public boolean containsAir(ItemStack can) {
		return true;
	}
}
