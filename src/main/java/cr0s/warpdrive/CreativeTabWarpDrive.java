package cr0s.warpdrive;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreativeTabWarpDrive extends CreativeTabs {
	String topLabel;

	public CreativeTabWarpDrive(int par1, String par2Str) {
		super(par1, par2Str);
	}
	
	public CreativeTabWarpDrive(String par1Str,String topLabelIn) {
		super(par1Str);
		topLabel = topLabelIn;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {
		return WarpDrive.itemComponent;
        // return WarpDrive.reactorLaserFocusItem;
    }
	
	@Override
	public String getTranslatedTabLabel() {
        return topLabel;
    }
}
