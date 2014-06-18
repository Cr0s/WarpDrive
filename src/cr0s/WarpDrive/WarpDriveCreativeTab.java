package cr0s.WarpDrive;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class WarpDriveCreativeTab extends CreativeTabs
{
	String topLabel;

	public WarpDriveCreativeTab(int par1, String par2Str)
	{
		super(par1, par2Str);
	}
	
	public WarpDriveCreativeTab(String par1Str,String topLabelIn)
	{
		super(par1Str);
		topLabel = topLabelIn;
	}
	
	public String getTranslatedTabLabel()
    {
        return topLabel;
    }
	
}
