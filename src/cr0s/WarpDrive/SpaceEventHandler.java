/*
 * Невесомость и отключение текучести жидкостей
 */
package cr0s.WarpDrive;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

/**
 * Обработчик событий в мире Space
 * @author Cr0s
 */
public class SpaceEventHandler {
    public static int HELMET_ID_SKUBA;
    public static int HELMET_ID_QUANTUM;
    public static int HELMET_ID_HAZMAT;
    public static int HELMET_ID_NANO;
    @ForgeSubscribe
    public void livingUpdate(LivingUpdateEvent event) {
	EntityLivingBase entity = event.entityLiving;

        if (Math.abs(MathHelper.floor_double(entity.posX)) > WarpDrive.WORLD_LIMIT_BLOCKS || Math.abs(MathHelper.floor_double(entity.posZ)) > WarpDrive.WORLD_LIMIT_BLOCKS) {
            entity.attackEntityFrom(DamageSource.outOfWorld, 9000);
            return;
        }        
        

        
        // Обновление происходит в космическом или гипер пространстве
        if (entity.worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID || entity.worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID) {
            boolean inVacuum = isEntityInVacuum(entity);
            
            // Damage entity if in vacuum without protection
            if (inVacuum) {
                if (entity instanceof EntityPlayerMP) {   
                    if (!(((EntityPlayerMP)entity).getCurrentArmor(3) != null && (((EntityPlayerMP)entity).getCurrentArmor(3).itemID == HELMET_ID_SKUBA || ((EntityPlayerMP)entity).getCurrentArmor(3).itemID == HELMET_ID_QUANTUM || ((EntityPlayerMP)entity).getCurrentArmor(3).itemID == HELMET_ID_NANO || ((EntityPlayerMP)entity).getCurrentArmor(3).itemID == HELMET_ID_HAZMAT))) {
                        entity.attackEntityFrom(DamageSource.drown, 1);    
                    }
                    // Отправить назад на Землю
                    if (entity.posY < -10.0D) {
                        ((EntityPlayerMP)entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), 0, new SpaceTeleporter(DimensionManager.getWorld(WarpDrive.instance.spaceDimID), 0, MathHelper.floor_double(entity.posX), 250, MathHelper.floor_double(entity.posZ)));
                        ((EntityPlayerMP)entity).setFire(30);
                        ((EntityPlayerMP)entity).setPositionAndUpdate(entity.posX, 250D, entity.posZ);
                    }
                } else {
                    entity.attackEntityFrom(DamageSource.drown, 1);
                }                
            }
        }
    }
    

    /**
     * Проверка, находится ли Entity в открытом космосе
     * @param e
     * @return 
     */
    private boolean isEntityInVacuum(Entity e) {
        
        int x = MathHelper.floor_double(e.posX);
        int y = MathHelper.floor_double(e.posY);
        int z = MathHelper.floor_double(e.posZ);
        
        if ((e.worldObj.getBlockId(x, y, z) == 0 || e.worldObj.getBlockId(x, y, z) == WarpDrive.gasBlock.blockID) && (e.worldObj.getBlockId(x, y + 1, z) == 0 || e.worldObj.getBlockId(x, y + 1, z) == WarpDrive.gasBlock.blockID)) {
            return true;
        }
        
        return false;
    }
}
