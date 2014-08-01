package cr0s.WarpDrive;

import java.util.HashMap;
import java.util.List;

import cr0s.WarpDrive.CloakManager.CloakedArea;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import cr0s.WarpDrive.api.IBreathingHelmet;

/**
 * 
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
					boolean hasHelmet = false;
					if (((EntityPlayerMP)entity).getCurrentArmor(3) != null)
					{
						ItemStack helmetStack = ((EntityPlayerMP)entity).getCurrentArmor(3);
						Item helmet = helmetStack.getItem();
						if(helmet instanceof IBreathingHelmet)
						{
							IBreathingHelmet breathHelmet = (IBreathingHelmet)helmet;
							Integer airValue = vacuumPlayers.get(((EntityPlayerMP)entity).username);
							if(breathHelmet.canBreath(entity))
							{
								hasHelmet = true;
								if (airValue == null)
								{
									vacuumPlayers.put(((EntityPlayerMP)entity).username, 300);
									airValue = 300;
								}
		
								if (airValue <= 0)
								{
									if (breathHelmet.removeAir(entity))
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
						}
					}
					else
					{
						entity.attackEntityFrom(DamageSource.drown, 1);
					}
					
					if(!hasHelmet)
					{
						if(vacuumPlayers.containsKey(((EntityPlayerMP)entity).username))
							vacuumPlayers.remove(((EntityPlayerMP)entity).username);
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
					//WarpDrive.debugPrint("[Cloak] Player inside " + cloaks.size() + " cloaked areas");
					for (CloakedArea area : cloaks) {
						//WarpDrive.debugPrint("[Cloak] Frequency: " + area.frequency + ". In: " + area.isPlayerInArea(p) + ", W: " + area.isPlayerWithinArea(p));
						if (!area.isPlayerInArea(p) && area.isPlayerWithinArea(p)) {
							WarpDrive.instance.cloaks.playerEnteringCloakedArea(area, p);
						}
					}
				} else {
					//WarpDrive.debugPrint("[Cloak] Player is not inside any cloak fields. Check, which field player may left...");
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
	 * 
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

		if (id1 == WarpDriveConfig.airID || id2 == WarpDriveConfig.airID)
			return false;
		return true;
	}
}
