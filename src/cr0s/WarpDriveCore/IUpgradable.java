package cr0s.WarpDriveCore;

import java.util.Map;

public interface IUpgradable
{
	public boolean takeUpgrade(int upgradeType,boolean simulate); //returns whether or not the upgrade should be installed
	public Map <Integer,Integer> installedUpgrades(); //returns a map in the form <UpgradeType,NumInstalled>
}
