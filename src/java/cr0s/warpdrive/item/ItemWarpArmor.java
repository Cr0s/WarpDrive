package cr0s.warpdrive.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IAirCanister;
import cr0s.warpdrive.api.IBreathingHelmet;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemWarpArmor extends ItemArmor implements IBreathingHelmet {
	// private static Random ran = new Random();
	private int slot;
	
	Icon ic;
	
	public ItemWarpArmor(int id,int slot) {
		super(id, WarpDrive.armorMaterial, 0, slot);
		this.slot = slot;
		setUnlocalizedName("warpdrive.armor.Helmet");
		setCreativeTab(WarpDrive.warpdriveTab);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getArmorTexture(ItemStack is, Entity en, int parSlot, String type) {
		return "warpdrive:textures/armor/warpArmor_1.png";
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister ir) {
		if (slot == 0) {
			ic = ir.registerIcon("warpdrive:warpArmorHelmet");
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage) {
		return ic;
	}

	@Override
	public boolean canBreath(Entity player) {
		return true;
	}

	@Override
	public boolean removeAir(Entity player) {
		WarpDrive.debugPrint("Checking breathing!");
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP pl = (EntityPlayerMP) player;
			ItemStack[] plInv = pl.inventory.mainInventory;
			for(int i = 0; i < plInv.length; i++) {
				ItemStack is = plInv[i];
				if (is != null && is.getItem() instanceof IAirCanister) {
					IAirCanister airCanister = (IAirCanister)is.getItem();
					if (airCanister.containsAir(is)) {
						if (is.stackSize > 1) {// unstack
							is.stackSize--;
							ItemStack toAdd = is.copy();
							toAdd.stackSize = 1;
							toAdd.setItemDamage(is.getItemDamage() + 1);	// bypass unbreaking enchantment
							if (is.getItemDamage() >= is.getMaxDamage()) {
								toAdd = airCanister.emptyDrop(is);
							}
							if (!pl.inventory.addItemStackToInventory(toAdd)) {
								EntityItem ie = new EntityItem(pl.worldObj, pl.posX, pl.posY, pl.posZ, toAdd);
								pl.worldObj.spawnEntityInWorld(ie);
							}
						} else {
							is.setItemDamage(is.getItemDamage() + 1);	// bypass unbreaking enchantment
							if (is.getItemDamage() >= is.getMaxDamage()) {
								plInv[i] = airCanister.emptyDrop(is);
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	@Override
	public int ticksPerCanDamage()
	{
		return 40;
	}
}
