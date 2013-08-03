package cr0s.WarpDrive;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public class GravityManager {
	private static double SPACE_GRAVITY = 0.0001D;
	private static double SPACE_GRAVITY_SNEAK = 0.01D;
	
    private static final int JETPACK_ID = 30210;
    private static final int ELECTRIC_JETPACK_ID = 30209;
	
	public static double getGravityForEntity(EntityLivingBase entity) {
		// Is entity in space or hyper-space?
		boolean inSpace = entity.worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID || entity.worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID;
		if (inSpace) {
			boolean insideGravField = isEntityInGraviField(entity);
		
			if (insideGravField) {
				return 0.08D;
			} else {
				if (entity instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer)entity;
					
					if (player.isSneaking()) {
						if (player.getCurrentArmor(2) != null && ((player.getCurrentArmor(2).itemID == JETPACK_ID) || player.getCurrentArmor(2).itemID == ELECTRIC_JETPACK_ID)) {
							return SPACE_GRAVITY_SNEAK;
						}
					}
				}
				return SPACE_GRAVITY;
			}
		}
		
		return 0.08D;
	}
	
    public static double getItemGravity(EntityItem entity)
    {
        if (entity.worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID || entity.worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID)
        {
        	if (isEntityInGraviField(entity)) {
        		return 0.03999999910593033D;
        	} else {
        		 return SPACE_GRAVITY;
        	}
        }
        else
        {
            return 0.03999999910593033D; // On Earth
        }
    }

    public static double getItemGravity2(EntityItem entity)
    {
        if (entity.worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID || entity.worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID)
        {
        	if (isEntityInGraviField(entity)) {
        		return 0.9800000190734863D;
        	} else {
        		 return SPACE_GRAVITY;
        	}
        }
        else
        {
            return 0.9800000190734863D;
        }
    }
	
    public static boolean isEntityInGraviField(Entity e) {
    	int y = MathHelper.floor_double(e.posY);
    	int x = MathHelper.floor_double(e.posX);
    	int z = MathHelper.floor_double(e.posZ);
    	
        final int CHECK_DISTANCE = 20;
             
        // Search non-air blocks under player
        for (int ny = y; ny > (y - CHECK_DISTANCE); ny--) {
            if (!e.worldObj.isAirBlock(x, ny, z)) {
                return true;
            }
        }
        
        return false;
    }
}
