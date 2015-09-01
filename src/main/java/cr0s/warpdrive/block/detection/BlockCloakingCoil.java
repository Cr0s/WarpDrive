package cr0s.warpdrive.block.detection;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import cr0s.warpdrive.WarpDrive;

public class BlockCloakingCoil extends Block {
	private IIcon[] iconBuffer;
	
	public BlockCloakingCoil(int texture, Material material) {
		super(material);
		setHardness(0.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		this.setBlockName("warpdrive.detection.CloakingCoil");
	}
	
	static final boolean oldTextures = true;
	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[4];
		if (oldTextures) {
			iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:detection/cloakingCoilSide");
			iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:detection/cloakingCoilSideActive");
			iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:detection/cloakingCoilTop");
		} else {
			iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:detection/cloakingCoilInPassive");
			iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:detection/cloakingCoilOutPassive");
			iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:detection/cloakingCoilInActive");
			iconBuffer[3] = par1IconRegister.registerIcon("warpdrive:detection/cloakingCoilOutActive");
		}
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		// Metadata values
		// 0 = not linked
		// 1 = inner coil passive
		// 2-7 = outer coil passive
		// 8 = (not used)
		// 9 = inner coil active
		// 10-15 = outer coil active
		if (oldTextures) {
			if (side == 0) {
				return iconBuffer[2];
			} else if (side == 1) {
				return iconBuffer[2];
			}
			if (metadata < 8) {
				return iconBuffer[0];
			} else {
				return iconBuffer[1];
			}
		} else {
			// not linked or in inventory
			if (metadata == 0) {
				if (side == 2) {
					return iconBuffer[0];
				} else {
					return iconBuffer[1];
				}
			}
			
			// inner coils
			if (metadata == 1) {
				return iconBuffer[0];
			} else if (metadata == 9) {
				return iconBuffer[2];
			}
			
			// outer coils
			int direction = (metadata & 7) - 2;
			int activeOffset = (metadata < 8) ? 0 : 2; 
			if (ForgeDirection.OPPOSITES[direction] == side) {
				return iconBuffer[0 + activeOffset];
			} else {
				return iconBuffer[1 + activeOffset];
			}
		}
	}
	
	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
	
	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return Item.getItemFromBlock(this);
	}
}
