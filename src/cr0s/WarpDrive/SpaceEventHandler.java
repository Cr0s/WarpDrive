package cr0s.WarpDrive;

import java.util.HashMap;

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
    private HashMap<String, Integer> vacuumPlayers;
    
    public SpaceEventHandler() {
        vacuumPlayers = new HashMap<String, Integer>();
    }
    
    @ForgeSubscribe
    public void livingUpdate(LivingUpdateEvent event) {
	EntityLivingBase entity = event.entityLiving;
        
        if (Math.abs(MathHelper.floor_double(entity.posX)) > WarpDrive.WORLD_LIMIT_BLOCKS || Math.abs(MathHelper.floor_double(entity.posZ)) > WarpDrive.WORLD_LIMIT_BLOCKS) {
            if (entity instanceof EntityPlayerMP) {
                if (((EntityPlayerMP)entity).capabilities.isCreativeMode) {
                    return;
                }
            }
            entity.attackEntityFrom(DamageSource.outOfWorld, 9000);
            return;
        }        
        
        final int HELMET_ID_SKUBA = 30082;
        final int HELMET_ID_HAZMAT = 14023;
        final int HELMET_ID_QUANTUM = 30174;
        final int AIR_CELL_ID = 30079;
        
        // Обновление происходит в космическом или гипер пространстве
        if (entity.worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID || entity.worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID) {
            boolean inVacuum = isEntityInVacuum(entity);
            
            // Damage entity if in vacuum without protection
            if (inVacuum) {
                if (entity instanceof EntityPlayerMP) {   
                    if (((EntityPlayerMP)entity).getCurrentArmor(3) != null && ((((EntityPlayerMP)entity).getCurrentArmor(3).itemID == HELMET_ID_SKUBA || ((EntityPlayerMP)entity).getCurrentArmor(3).itemID == HELMET_ID_HAZMAT) || ((EntityPlayerMP)entity).getCurrentArmor(3).itemID == HELMET_ID_QUANTUM)) {
                        Integer airValue = vacuumPlayers.get(((EntityPlayerMP)entity).username);
                        if (airValue == null) {
                            vacuumPlayers.put(((EntityPlayerMP)entity).username, 300);
                            airValue = 300;
                        }
                        
                        if (airValue <= 0) {
                            if (((EntityPlayerMP)entity).inventory.consumeInventoryItem(AIR_CELL_ID)) {
                                setPlayerAirValue(entity, 300);
                            } else {
                                setPlayerAirValue(entity, 0);                               
                                entity.attackEntityFrom(DamageSource.drown, 1);
                            }
                        } else {
                            setPlayerAirValue(entity, airValue - 1);
                        }
                    } else {
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
    
    private void setPlayerAirValue(EntityLivingBase entity, Integer air) {
                        vacuumPlayers.remove(((EntityPlayerMP)entity).username);
                        vacuumPlayers.put(((EntityPlayerMP)entity).username, air);        
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
        
        int id1 = e.worldObj.getBlockId(x, y, z);
        int id2 = e.worldObj.getBlockId(x, y + 1, z);
        
        if (id1 == WarpDrive.instance.config.airID || id2 == WarpDrive.instance.config.airID) {
            return false;
        }
        
        return true;
    }
}
