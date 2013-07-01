/*
 * Невесомость и отключение текучести жидкостей
 */
package cr0s.WarpDrive;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.common.ForgeHooks;

/**
 * Обработчик событий в мире Space
 * @author Cr0s
 */
public class SpaceEventHandler {    
/*
    @ForgeSubscribe
    public void onBlockFlow(LiquidFlowEvent lfe) {
        // В космосе жидкости не текут, так что событие отменяется
        System.out.println("onLiquidFlow: liquid is flowing");
        if (lfe.world.provider.dimensionId == WarpDrive.instance.spaceDimID) {
            System.out.println("onLiquidFlow: [blocking flow]");
            lfe.setCanceled(true);
        }
    }
*/
    @ForgeSubscribe
    public void livingUpdate(LivingUpdateEvent event) {
	EntityLiving entity = event.entityLiving;

        final int HELMET_ID_SKUBA = 30082;
        final int HELMET_ID_QUANTUM = 30174;
        final int HELMET_ID_ADV_SOLAR = 30832;
        final int HELMET_ID_HYB_SOLAR = 30833;
        final int HELMET_ID_ULT_SOLAR = 30834;
        final int HELMET_HEAD = 397;
        
        // Движение происходит в космическом пространстве
        if (entity.worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID) {
            if (entity instanceof EntityPlayerMP) {
                
                if (isEntityInVacuum(entity)) {
                    if (!(entity.getCurrentArmor(3) != null && (entity.getCurrentArmor(3).itemID == HELMET_ID_SKUBA || entity.getCurrentArmor(3).itemID == HELMET_ID_QUANTUM || entity.getCurrentArmor(3).itemID == HELMET_HEAD
 || entity.getCurrentArmor(3).itemID == HELMET_ID_ADV_SOLAR || entity.getCurrentArmor(3).itemID == HELMET_ID_HYB_SOLAR || entity.getCurrentArmor(3).itemID == HELMET_ID_ULT_SOLAR))) {
                        entity.attackEntityFrom(DamageSource.drown, 3);    
                    }
                }
                
                // Отправить назад на Землю
                if (entity.posY < -50.0D) {
                    ((EntityPlayerMP)entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), 0, new SpaceTeleporter(DimensionManager.getWorld(WarpDrive.instance.spaceDimID), 0, MathHelper.floor_double(entity.posX), 250, MathHelper.floor_double(entity.posZ)));
                    ((EntityPlayerMP)entity).setFire(30);
                    ((EntityPlayerMP)entity).setPositionAndUpdate(entity.posX, 250D, entity.posZ);
                    return;
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
        
        final int CHECK_DISTANCE = 10;
        
        if (e.onGround) { return false; }
                
        for (int ny = y; ny > (y - CHECK_DISTANCE); ny--) {
            if (!e.worldObj.isAirBlock(x, ny, z)) {
                return false;
            }
        }
        
        if (!e.worldObj.canBlockSeeTheSky(x, y, z) || !e.worldObj.canBlockSeeTheSky(x, y - 1, z) ) {         
            return false; 
        }        
        
        return true;
    }
    
    @ForgeSubscribe
    public void onEntityJoinedWorld(EntityJoinWorldEvent ejwe) {
        if (!(ejwe.entity instanceof EntityPlayer)) {
            return;
        }
        
        if (ejwe.world.provider.dimensionId == WarpDrive.instance.spaceDimID) {
            ((EntityPlayer)ejwe.entity).capabilities.allowFlying = true;
        } else
        {
            ((EntityPlayer)ejwe.entity).capabilities.allowFlying = false;
        }
        
        if (((EntityPlayer)ejwe.entity).username.contains(".")) {
            ((EntityPlayer)ejwe.entity).username = ((EntityPlayer)ejwe.entity).username.split("\\.")[0];
        }
        
        ((EntityPlayer)ejwe.entity).skinUrl = "http://koprokubach.servegame.com/getskin.php?user=" + ((EntityPlayer)ejwe.entity).username;
    }
}
