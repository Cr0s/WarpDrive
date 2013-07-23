package cr0s.serverMods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingFallEvent;

/**
 * Гашение урона при падении с джетпаком или квантовыми бутсами
 * @author Cr0s
 */
public class AntiFallDamage {
    private final int JETPACK_ID = 30210;
    private final int ELECTRIC_JETPACK_ID = 30209;
    private final int QUANTUM_BOOTS_ID = 30171;
    
    @ForgeSubscribe
    public void livingFall(LivingFallEvent event) {
        EntityLivingBase entity = event.entityLiving;
        float distance = event.distance;

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;

            int check = MathHelper.ceiling_float_int(distance - 3.0F);
            if (check > 0) { // Падение может нанести урон
                // Проверяем наличие защиты
                if ((player.getCurrentArmor(0) != null && player.getCurrentArmor(0).itemID == QUANTUM_BOOTS_ID) ||
                        (player.getCurrentArmor(2) != null && player.getCurrentArmor(2).itemID == JETPACK_ID) ||
                            (player.getCurrentArmor(2) != null && player.getCurrentArmor(2).itemID == ELECTRIC_JETPACK_ID)) {
                    event.setCanceled(true); // Блокируем падение, если защита есть
                }
            }
        }
    }
}
