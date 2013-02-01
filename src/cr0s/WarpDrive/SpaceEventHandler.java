/*
 * Невесомость и отключение текучести жидкостей
 */
package cr0s.WarpDrive;

import keepcalm.mods.events.events.LiquidFlowEvent;
import keepcalm.mods.events.events.PlayerMoveEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

/**
 * Обработчик событий в мире Space
 * @author Cr0s
 */
public class SpaceEventHandler {    
    @ForgeSubscribe
    public void onBlockFlow(LiquidFlowEvent lfe) {
        // В космосе жидкости не текут, так что событие отменяется
        System.out.println("onLiquidFlow: liquid is flowing");
        if (lfe.world.provider.dimensionId == WarpDrive.instance.spaceDimID) {
            System.out.println("onLiquidFlow: [blocking flow]");
            lfe.setCanceled(true);
        }
    }
    
    @ForgeSubscribe
    public void onPlayerMove(PlayerMoveEvent pme) {
        //System.out.println("onPlayerMove(): event called.");
        
        // Движение происходит в космическом пространстве
        if (pme.entity.worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID) {
            if (pme.entity instanceof EntityPlayer) {
                // Отправить назад на Землю
                if (pme.newY < -50.0D) {
                    ((EntityPlayerMP)pme.entityPlayer).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) pme.entityPlayer), 0, new SpaceTeleporter(DimensionManager.getWorld(WarpDrive.instance.spaceDimID), 0, MathHelper.floor_double(pme.newX), 255, MathHelper.floor_double(pme.newZ)));
                    ((EntityPlayerMP)pme.entityPlayer).setFire(30);
                    ((EntityPlayerMP)pme.entityPlayer).setPositionAndUpdate(pme.newX, 256D, pme.newZ);
                    return;
                }
            }
            /*
            // Если это игрок в режиме Creative, то игнорируем
            if (pme.entity instanceof EntityPlayer && ((EntityPlayer)pme.entity).capabilities.isCreativeMode) {
                return;
            }
            
            //System.out.println("onPlayerMove(): oldY: " + pme.oldY + " newY: " + pme.newY);
            // Происходит падение
            if (pme.oldY > pme.newY && pme.flying) {
                //System.out.println("onPlayerMove(): [blocking falling]");
                if (pme.entity instanceof EntityPlayer) { 
                    pme.entityPlayer.setPositionAndUpdate(pme.oldX, pme.oldY, pme.oldZ);
                } else {
                    pme.entity.setPosition(pme.oldX, pme.oldY, pme.oldZ);
                }
                
                pme.setCanceled(true); // Предотвращаем падение
            }*/
        }
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
    }
}
