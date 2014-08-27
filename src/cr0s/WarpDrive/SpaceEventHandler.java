package cr0s.WarpDrive;

import ic2.api.item.Items;

import java.util.HashMap;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.data.CloakedArea;
import cr0s.WarpDrive.world.SpaceTeleporter;
import cr0s.WarpDrive.api.IBreathingHelmet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumGameType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

/**
 * 
 * @author Cr0s
 */
public class SpaceEventHandler {
	private HashMap<Integer, Integer> entity_airBlock;
	private HashMap<String, Integer> player_airTank;
	private HashMap<String, Integer> player_cloakTicks;
	
	private final int CLOAK_CHECK_TIMEOUT_TICKS = 100;
	private final int AIR_BLOCK_TICKS = 20;
	private final int AIR_TANK_TICKS = 300;
	private final int AIR_DROWN_TICKS = 20;
	
	public SpaceEventHandler() 	{
		entity_airBlock = new HashMap<Integer, Integer>();
		player_airTank = new HashMap<String, Integer>();
		player_cloakTicks = new HashMap<String, Integer>();
	}

	@ForgeSubscribe
	public void livingUpdate(LivingUpdateEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

		EntityLivingBase entity = event.entityLiving;
		int x = MathHelper.floor_double(entity.posX);
		int y = MathHelper.floor_double(entity.posY);
		int z = MathHelper.floor_double(entity.posZ);
		
		// Instant kill if entity exceeds world's limit
		if (WarpDriveConfig.G_SPACE_WORLDBORDER_BLOCKS > 0 && (Math.abs(x) > WarpDriveConfig.G_SPACE_WORLDBORDER_BLOCKS || Math.abs(z) > WarpDriveConfig.G_SPACE_WORLDBORDER_BLOCKS)) {
			if (entity instanceof EntityPlayerMP) {
				if (((EntityPlayerMP)entity).capabilities.isCreativeMode) {
					return;
				}
			}

			entity.attackEntityFrom(DamageSource.outOfWorld, 9000);
			return;
		}
		if (entity instanceof EntityPlayerMP) {
			updatePlayerCloakState(entity);
			
			// skip players in creative
			if (((EntityPlayerMP)entity).theItemInWorldManager.getGameType() == EnumGameType.CREATIVE) {
				return;
			}
		}
		
		// skip dead or invulnerable entities
		if (entity.isDead || entity.isEntityInvulnerable()) {
			return;
		}
		
		// If entity is in vacuum, check and start consuming air cells
		if (entity.worldObj.provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID || entity.worldObj.provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) {
			int id1 = entity.worldObj.getBlockId(x, y    , z);
			int id2 = entity.worldObj.getBlockId(x, y + 1, z);
			boolean inVacuum = (id1 != WarpDriveConfig.airID && id2 != WarpDriveConfig.airID);
			Integer air;
			if (!inVacuum) {// In space with air blocks
				air = entity_airBlock.get(entity.entityId);
				if (air == null) {
					entity_airBlock.put(entity.entityId, AIR_BLOCK_TICKS);
				} else if (air <= 1) {// time elapsed => consume air block
					entity_airBlock.put(entity.entityId, AIR_BLOCK_TICKS);
					
					int metadata;
					if (id1 == WarpDriveConfig.airID) {
						metadata = entity.worldObj.getBlockMetadata(x, y, z);
						if (metadata > 0 && metadata < 15) {
							entity.worldObj.setBlockMetadataWithNotify(x, y, z, metadata - 1, 2);
						}
					} else {
						metadata = entity.worldObj.getBlockMetadata(x, y + 1, z);
						if (metadata > 0 && metadata < 15) {
							entity.worldObj.setBlockMetadataWithNotify(x, y + 1, z, metadata - 1, 2);
						}
					}
				} else {
					entity_airBlock.put(entity.entityId, air - 1);
				}
			} else {// In space without air blocks
				// Damage entity if in vacuum without protection
				if (entity instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP)entity;
					air = player_airTank.get(player.username);

					boolean hasHelmet = false;
					ItemStack helmetStack = player.getCurrentArmor(3);
					if (helmetStack != null) {
						Item helmet = helmetStack.getItem();
						if (helmet instanceof IBreathingHelmet) {
							IBreathingHelmet breathHelmet = (IBreathingHelmet)helmet;
							int airTicks = breathHelmet.ticksPerCanDamage();
							if (breathHelmet.canBreath(player)) {
								hasHelmet = true;
								if (air == null) {// new player in space => grace period
									player_airTank.put(player.username, airTicks);
								} else if (air <= 1) {
									if (breathHelmet.removeAir(player)) {
										player_airTank.put(player.username, airTicks);
									} else {
										player_airTank.put(player.username, AIR_DROWN_TICKS);
										player.attackEntityFrom(DamageSource.drown, 2.0F);
									}
								} else {
									player_airTank.put(player.username, air - 1);
								}
							}
						} else if (WarpDriveConfig.SpaceHelmets.contains(helmetStack.itemID)) {
							hasHelmet = true;
							if (air == null) {// new player in space => grace period
								player_airTank.put(player.username, AIR_TANK_TICKS);
							} else if (air <= 1) {
								if (consumeO2(player.inventory.mainInventory, player)) {
									player_airTank.put(player.username, AIR_TANK_TICKS);
								} else {
									player_airTank.put(player.username, AIR_DROWN_TICKS);
									entity.attackEntityFrom(DamageSource.drown, 2.0F);
								}
							} else {
								player_airTank.put(player.username, air - 1);
							}
						}
					}
					
					if (!hasHelmet) {
						if (air == null) {// new player in space => grace period
							player_airTank.put(player.username, AIR_TANK_TICKS);
						} else if (air <= 1) {
							player_airTank.put(player.username, AIR_DROWN_TICKS);
							entity.attackEntityFrom(DamageSource.drown, 2.0F);
						} else {
							player_airTank.put(player.username, air - 1);
						}
					}

					// If player falling down, teleport on earth
					if (entity.posY < -10.0D) {
						player.mcServer.getConfigurationManager().transferPlayerToDimension(player, 0, new SpaceTeleporter(DimensionManager.getWorld(WarpDriveConfig.G_SPACE_DIMENSION_ID), 0, x, 250, z));
						player.setFire(30);
						player.setPositionAndUpdate(entity.posX, 250.0D, entity.posZ);
					}
				} else {// (in space, no air block and not a player)
					entity_airBlock.put(entity.entityId, 0);
					entity.attackEntityFrom(DamageSource.drown, 2.0F);
				}
			}
		}
	}

	private void updatePlayerCloakState(EntityLivingBase entity) {
		try {
			EntityPlayerMP player = (EntityPlayerMP)entity;
			Integer cloakTicks = player_cloakTicks.get(player.username);
			
			if (cloakTicks == null) {
				player_cloakTicks.put(player.username, 0);
				return;
			}
			
			if (cloakTicks >= CLOAK_CHECK_TIMEOUT_TICKS) {
				player_cloakTicks.put(player.username, 0);
				
				List<CloakedArea> cloaks = WarpDrive.cloaks.getCloaksForPoint(player.worldObj.provider.dimensionId, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ), false);
				if (cloaks.size() != 0) {
					//WarpDrive.debugPrint("[Cloak] Player inside " + cloaks.size() + " cloaked areas");
					for (CloakedArea area : cloaks) {
						//WarpDrive.debugPrint("[Cloak] Frequency: " + area.frequency + ". In: " + area.isPlayerInArea(p) + ", W: " + area.isPlayerWithinArea(p));
						if (!area.isPlayerInArea(player.username) && area.isEntityWithinArea(player)) {
							area.playerEnteringCloakedArea(player);
						}
					}
				} else {
					//WarpDrive.debugPrint("[Cloak] Player is not inside any cloak fields. Check, which field player may left...");
					WarpDrive.cloaks.checkPlayerLeftArea(player);
				}
			} else {
				player_cloakTicks.put(player.username, cloakTicks + 1);			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean consumeO2(ItemStack[] inventory, EntityPlayerMP entityPlayer) {
		for (int j = 0; j < inventory.length; ++j) {
			if (inventory[j] != null && inventory[j].itemID == WarpDriveConfig.IC2_Air[0] && inventory[j].getItemDamage() == WarpDriveConfig.IC2_Air[1]) {
				inventory[j].stackSize--;
				if (inventory[j].stackSize <= 0) {
					inventory[j] = null;
				}

				if (WarpDriveConfig.IC2_Empty.length != 0) {
//					WarpDrive.debugPrint("giveEmptyCell");
					ItemStack emptyCell = new ItemStack(WarpDriveConfig.IC2_Empty[0], 1, WarpDriveConfig.IC2_Empty[1]);
					if (!entityPlayer.inventory.addItemStackToInventory(emptyCell)) {
						World world = entityPlayer.worldObj;
						EntityItem itemEnt = new EntityItem(world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, emptyCell);
						entityPlayer.worldObj.spawnEntityInWorld(itemEnt);
					}
				}
				return true;
			}
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
                        (player.getCurrentArmor(2) != null && WarpDriveConfig.Jetpacks.contains(player.getCurrentArmor(2).itemID)))
                {
                    event.setCanceled(true); // Don't damage player
                }
            }
        }
    }	
}
