package cr0s.WarpDrive;

import ic2.api.item.Items;

import java.util.HashMap;
import java.util.List;

import cr0s.WarpDrive.CloakManager.CloakedArea;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

/**
 * Обработчик событий в мире Space
 * @author Cr0s
 */
public class SpaceEventHandler
{
	private HashMap<String, Integer> vacuumPlayers;
	private HashMap<String, Integer> cloakPlayersTimers;
	private long lastTimer = 0;
	
	private final int CLOAK_CHECK_TIMEOUT_SEC = 5;
	
	public SpaceEventHandler()
	{
		vacuumPlayers = new HashMap<String, Integer>();
		cloakPlayersTimers = new HashMap<String, Integer>();
		this.lastTimer = 0;
	}

	@ForgeSubscribe
	public void livingUpdate(LivingUpdateEvent event)
	{
		EntityLivingBase entity = event.entityLiving;
		
		// Instant kill if entity exceeds world's limit
		if (Math.abs(MathHelper.floor_double(entity.posX)) > WarpDrive.WORLD_LIMIT_BLOCKS || Math.abs(MathHelper.floor_double(entity.posZ)) > WarpDrive.WORLD_LIMIT_BLOCKS)
		{
			if (entity instanceof EntityPlayerMP)
			{
				if (((EntityPlayerMP)entity).capabilities.isCreativeMode)
				{
					return;
				}
			}

			entity.attackEntityFrom(DamageSource.outOfWorld, 9000);
			return;
		}
		
		if (entity instanceof EntityPlayerMP) { 
			updatePlayerCloakState(entity);
		}

		// If player in vaccum, check and start consuming air cells
		if (entity.worldObj.provider.dimensionId == WarpDrive.instance.spaceDimID || entity.worldObj.provider.dimensionId == WarpDrive.instance.hyperSpaceDimID)
		{
			boolean inVacuum = isEntityInVacuum(entity);

			// Damage entity if in vacuum without protection
			if (inVacuum)
			{
				if (entity instanceof EntityPlayerMP)
				{

					if (((EntityPlayerMP)entity).getCurrentArmor(3) != null && WarpDriveConfig.i.SpaceHelmets.contains(((EntityPlayerMP)entity).getCurrentArmor(3).itemID))
					{
						Integer airValue = vacuumPlayers.get(((EntityPlayerMP)entity).username);

						if (airValue == null)
						{
							vacuumPlayers.put(((EntityPlayerMP)entity).username, 300);
							airValue = 300;
						}

						if (airValue <= 0)
						{
							if (consumeO2(((EntityPlayerMP)entity).inventory.mainInventory))
							{
								setPlayerAirValue(entity, 300);
							}
							else
							{
								setPlayerAirValue(entity, 0);
								entity.attackEntityFrom(DamageSource.drown, 1);
							}
						}
						else
						{
							setPlayerAirValue(entity, airValue - 1);
						}
					}
					else
					{
						entity.attackEntityFrom(DamageSource.drown, 1);
					}

					// If player falling down, teleport on earth
					if (entity.posY < -10.0D)
					{
						((EntityPlayerMP)entity).mcServer.getConfigurationManager().transferPlayerToDimension(((EntityPlayerMP) entity), 0, new SpaceTeleporter(DimensionManager.getWorld(WarpDrive.instance.spaceDimID), 0, MathHelper.floor_double(entity.posX), 250, MathHelper.floor_double(entity.posZ)));
						((EntityPlayerMP)entity).setFire(30);
						((EntityPlayerMP)entity).setPositionAndUpdate(entity.posX, 250D, entity.posZ);
					}
				}
				else
				{
					entity.attackEntityFrom(DamageSource.drown, 1);
				}
			}
		}
	}

	private void updatePlayerCloakState(EntityLivingBase entity) {
		// Make sure for elapsed time is second after last update
		if (System.currentTimeMillis() - this.lastTimer > 1000)
			lastTimer = System.currentTimeMillis();
		else 
			return;
		
		try {
			EntityPlayerMP p = (EntityPlayerMP)entity;
			Integer cloakTicks = this.cloakPlayersTimers.get(p.username);
			
			if (cloakTicks == null) {
				this.cloakPlayersTimers.remove(p.username);
				this.cloakPlayersTimers.put(p.username, 0);
				
				return;
			}
			
			if (cloakTicks >= CLOAK_CHECK_TIMEOUT_SEC) {
				this.cloakPlayersTimers.remove(p.username);
				this.cloakPlayersTimers.put(p.username, 0);
				
				List<CloakedArea> cloaks = WarpDrive.instance.cloaks.getCloaksForPoint(p.worldObj.provider.dimensionId, MathHelper.floor_double(p.posX), MathHelper.floor_double(p.posY), MathHelper.floor_double(p.posZ), false);
				if (cloaks.size() != 0) {
					//System.out.println("[Cloak] Player inside " + cloaks.size() + " cloaked areas");
					for (CloakedArea area : cloaks) {
						//System.out.println("[Cloak] Frequency: " + area.frequency + ". In: " + area.isPlayerInArea(p) + ", W: " + area.isPlayerWithinArea(p));
						if (!area.isPlayerInArea(p) && area.isPlayerWithinArea(p)) {
							WarpDrive.instance.cloaks.playerEnteringCloakedArea(area, p);
						}
					}
				} else {
					//System.out.println("[Cloak] Player is not inside any cloak fields. Check, which field player may left...");
					WarpDrive.instance.cloaks.checkPlayerLeavedArea(p);
				}
			} else {
				this.cloakPlayersTimers.remove(p.username);
				this.cloakPlayersTimers.put(p.username, cloakTicks + 1);			
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	private void setPlayerAirValue(EntityLivingBase entity, Integer air)
	{
		vacuumPlayers.remove(((EntityPlayerMP)entity).username);
		vacuumPlayers.put(((EntityPlayerMP)entity).username, air);
	}

	/**
	 * Проверка, находится ли Entity в открытом космосе
	 * @param e
	 * @return
	 */
	private boolean isEntityInVacuum(Entity e)
	{
		int x = MathHelper.floor_double(e.posX);
		int y = MathHelper.floor_double(e.posY);
		int z = MathHelper.floor_double(e.posZ);
		int id1 = e.worldObj.getBlockId(x, y, z);
		int id2 = e.worldObj.getBlockId(x, y + 1, z);

		if (id1 == WarpDriveConfig.i.airID || id2 == WarpDriveConfig.i.airID)
			return false;
		return true;
	}

	private boolean consumeO2(ItemStack[] i)
	{
		for (int j = 0; j < i.length; ++j)
			if (i[j] != null && i[j].itemID == WarpDriveConfig.i.IC2_Air[0] && i[j].getItemDamage() == WarpDriveConfig.i.IC2_Air[1])
			{
				if (--i[j].stackSize <= 0)
					i[j] = null;
				return true;
			}
		return false;
	}
	
    @ForgeSubscribe
    public void livingFall(LivingFallEvent event)
    {
        EntityLivingBase entity = event.entityLiving;
        float distance = event.distance;

        if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            int check = MathHelper.ceiling_float_int(distance - 3.0F);

            if (check > 0)
            {
                if ((player.getCurrentArmor(0) != null && player.getCurrentArmor(0).itemID == Items.getItem("quantumBoots").itemID) ||
                        (player.getCurrentArmor(2) != null && WarpDriveConfig.i.Jetpacks.contains(player.getCurrentArmor(2).itemID)))
                {
                    event.setCanceled(true); // Don't damage player
                }
            }
        }
    }	
}
