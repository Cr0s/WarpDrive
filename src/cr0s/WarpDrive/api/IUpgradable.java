package cr0s.WarpDrive.api;

import java.util.HashMap;
import java.util.Map;

import cr0s.WarpDrive.data.EnumUpgradeTypes;
import net.minecraft.item.ItemStack;

public interface IUpgradable
{
	public boolean takeUpgrade(EnumUpgradeTypes upgradeType,boolean simulate);
	public Map<EnumUpgradeTypes,Integer> getInstalledUpgrades();
}
