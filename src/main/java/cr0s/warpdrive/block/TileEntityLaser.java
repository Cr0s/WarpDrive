package cr0s.warpdrive.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityLaser extends TileEntityAbstractInterfaced {
	private final int BEAM_FREQUENCY_SCANNING = 1420;
	private final int BEAM_FREQUENCY_MAX = 65000;
	
	private int dx, dz, dy;
	private float yaw, pitch; // laser direction
	
	private int beamFrequency = -1;
	private int cameraFrequency = -1;
	private float r, g, b; // beam color (corresponds to frequency)
	
	public boolean isEmitting = false;
	
	private int delayTicks = 0;
	private int energyFromOtherBeams = 0;
	
	private MovingObjectPosition firstHit_position = null;
	private Block firstHit_block;
	private int firstHit_blockMeta = 0;
	private float firstHit_blockResistance = -2;
	
	private final static int REGISTRY_UPDATE_INTERVAL_TICKS = 15 * 20;
	private final static int PACKET_SEND_INTERVAL_TICKS = 60 * 20;
	
	private int registryUpdateTicks = 20;
	private int packetSendTicks = 20;
	
	public TileEntityLaser() {
		super();
		peripheralName = "warpdriveLaser";
		methodsArray = new String[] {
			"emitBeam", // 0
			"pos", // 1
			"freq", // 2
			"getFirstHit", // 3
			"getBoosterDXDZ", // 4
			"camFreq" // 5
		};
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (isWithCamera()) {
			// Update frequency on clients (recovery mechanism, no need to go too fast)
			if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
				packetSendTicks--;
				if (packetSendTicks <= 0) {
					packetSendTicks = PACKET_SEND_INTERVAL_TICKS;
					PacketHandler.sendFreqPacket(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, cameraFrequency);
				}
			} else {
				registryUpdateTicks--;
				if (registryUpdateTicks <= 0) {
					registryUpdateTicks = REGISTRY_UPDATE_INTERVAL_TICKS;
					WarpDrive.instance.cameras.updateInRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord), cameraFrequency, 1);
				}
			}
		}
		
		// Frequency is not set
		if (beamFrequency <= 0 || beamFrequency > 65000) {
			return;
		}
		
		delayTicks++;
		if ( isEmitting
		  && ( (beamFrequency != BEAM_FREQUENCY_SCANNING && delayTicks > WarpDriveConfig.LASER_CANNON_EMIT_FIRE_DELAY_TICKS)
		    || (beamFrequency == BEAM_FREQUENCY_SCANNING && delayTicks > WarpDriveConfig.LASER_CANNON_EMIT_SCAN_DELAY_TICKS))) {
			delayTicks = 0;
			isEmitting = false;
			int beamEnergy = Math.min(
					this.consumeEnergyFromBoosters() + MathHelper.floor_double(energyFromOtherBeams * WarpDriveConfig.LASER_CANNON_BOOSTER_BEAM_ENERGY_EFFICIENCY),
					WarpDriveConfig.LASER_CANNON_MAX_LASER_ENERGY);
			emitBeam(beamEnergy);
			energyFromOtherBeams = 0;
			sendEvent("laserSend", new Object[] { beamFrequency, beamEnergy });
		}
	}
	
	public void initiateBeamEmission(float parYaw, float parPitch) {
		yaw = parYaw;
		pitch = parPitch;
		delayTicks = 0;
		isEmitting = true;
	}
	
	public void addBeamEnergy(int amount) {
		if (isEmitting) {
			energyFromOtherBeams += amount;
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info(this + " Added energy " + amount);
			}
		} else {
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info(this + " Ignored energy " + amount);
			}
		}
	}
	
	private int consumeEnergyFromBoosters() {
		int energyCollected = 0;
		
		if (findFirstBooster() != null) {
			int newX, newY, newZ;
			TileEntity te;
			for (int shift = 1; shift <= WarpDriveConfig.LASER_CANNON_MAX_MEDIUMS_COUNT; shift++) {
				newX = xCoord + (dx * shift);
				newY = yCoord + (dy * shift);
				newZ = zCoord + (dz * shift);
				te = worldObj.getTileEntity(newX, newY, newZ);
				if (te != null && te instanceof TileEntityLaserMedium) {
					energyCollected += ((TileEntityLaserMedium) te).consumeAllEnergy();
				} else {
					break;
				}
			}
		}
		
		return energyCollected;
	}
	
	private void emitBeam(int beamEnergy) {
		int energy = beamEnergy; // FIXME Beam power calculations
		int beamLengthBlocks = clamp(0, WarpDriveConfig.LASER_CANNON_RANGE_MAX,
				energy / WarpDriveConfig.LASER_CANNON_RANGE_ENERGY_PER_BLOCK);
		
		if (energy == 0 || beamLengthBlocks < 1 || beamFrequency > 65000 || beamFrequency <= 0) {
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info(this + " Beam canceled (energy " + energy + " over " + beamLengthBlocks + " blocks, beamFrequency " + beamFrequency + ")");
			}
			return;
		}
		
		float yawz = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float yawx = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float pitchhorizontal = -MathHelper.cos(-pitch * 0.017453292F);
		float pitchvertical = MathHelper.sin(-pitch * 0.017453292F);
		float directionx = yawx * pitchhorizontal;
		float directionz = yawz * pitchhorizontal;
		Vector3 vDirection = new Vector3(directionx, pitchvertical, directionz);
		Vector3 vSource = new Vector3(this).translate(0.5D).translate(vDirection);
		Vector3 vReachPoint = Vector3.translate(vSource.clone(), Vector3.scale(vDirection.clone(), beamLengthBlocks));
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info(this + " Energy " + energy + " over " + beamLengthBlocks + " blocks"
					+ ", Orientation " + yaw + " " + pitch
					+ ", Direction " + vDirection
					+ ", From " + vSource + " to " + vReachPoint);
		}
		
		playSoundCorrespondsEnergy(energy);
		
		// This is a scanning beam, do not deal damage to block nor entity
		if (beamFrequency == BEAM_FREQUENCY_SCANNING) {
			firstHit_position = worldObj.rayTraceBlocks(vSource.toVec3(), vReachPoint.toVec3());
			
			if (firstHit_position != null) {
				firstHit_block = worldObj.getBlock(firstHit_position.blockX, firstHit_position.blockY, firstHit_position.blockZ);
				firstHit_blockMeta = worldObj.getBlockMetadata(firstHit_position.blockX, firstHit_position.blockY, firstHit_position.blockZ);
				firstHit_blockResistance = -2;
				if (firstHit_block != null) {
					firstHit_blockResistance = firstHit_block.getExplosionResistance(null); // TODO: what entity should be used?
				}
				PacketHandler.sendBeamPacket(worldObj, vSource, new Vector3(firstHit_position.hitVec), r, g, b, 50, energy, 200);
			} else {
				firstHit_block = null;
				firstHit_blockMeta = 0;
				firstHit_blockResistance = -2;
				PacketHandler.sendBeamPacket(worldObj, vSource, vReachPoint, r, g, b, 50, energy, 200);
			}
			
			return;
		}
		
		// get colliding entities
		TreeMap<Double, MovingObjectPosition> entityHits = raytraceEntities(vSource.clone(), vDirection.clone(), true, beamLengthBlocks);
		
		if (WarpDriveConfig.LOGGING_WEAPON) {
			WarpDrive.logger.info("Entity hits are " + entityHits);
		}
		
		Vector3 vHitPoint = vReachPoint.clone();
		double distanceTravelled = 0.0D; // distance traveled from beam sender to previous hit if there were any
		for (int passedBlocks = 0; passedBlocks < beamLengthBlocks; passedBlocks++) {
			// Get next block hit
			MovingObjectPosition blockHit = worldObj.rayTraceBlocks(vSource.toVec3(), vReachPoint.toVec3());
			double blockHitDistance = beamLengthBlocks + 0.1D;
			if (blockHit != null) {
				blockHitDistance = blockHit.hitVec.distanceTo(vSource.toVec3());
			}
			
			// Apply effect to entities
			if (entityHits != null) {
				for (Entry<Double, MovingObjectPosition> entityHitEntry : entityHits.entrySet()) {
					double entityHitDistance = entityHitEntry.getKey();
					// ignore entities behind walls
					if (entityHitDistance >= blockHitDistance) {
						break;
					}
					
					// only hits entities with health
					MovingObjectPosition mopEntity = entityHitEntry.getValue();
					if (mopEntity != null && mopEntity.entityHit instanceof EntityLivingBase) {
						EntityLivingBase entity = (EntityLivingBase) mopEntity.entityHit;
						
						// Consume energy
						energy -= WarpDriveConfig.LASER_CANNON_ENTITY_HIT_ENERGY
								+ ((blockHitDistance - distanceTravelled) * WarpDriveConfig.LASER_CANNON_ENERGY_LOSS_PER_BLOCK);
						distanceTravelled = blockHitDistance;
						vHitPoint = new Vector3(mopEntity.hitVec);
						if (energy <= 0) {
							break;
						}
						
						// apply effects
						entity.setFire(WarpDriveConfig.LASER_CANNON_ENTITY_HIT_SET_ON_FIRE_SECONDS);
						float damage = (float)clamp(0.0D, WarpDriveConfig.LASER_CANNON_ENTITY_HIT_MAX_DAMAGE,
								WarpDriveConfig.LASER_CANNON_ENTITY_HIT_BASE_DAMAGE + energy / WarpDriveConfig.LASER_CANNON_ENTITY_HIT_ENERGY_PER_DAMAGE);
						entity.attackEntityFrom(DamageSource.inFire, damage);
						
						if (energy > WarpDriveConfig.LASER_CANNON_ENTITY_HIT_ENERGY_THRESHOLD_FOR_EXPLOSION) {
							float strength = (float)clamp(0.0D, WarpDriveConfig.LASER_CANNON_ENTITY_HIT_EXPLOSION_MAX_STRENGTH,
								  WarpDriveConfig.LASER_CANNON_ENTITY_HIT_EXPLOSION_BASE_STRENGTH + energy / WarpDriveConfig.LASER_CANNON_ENTITY_HIT_EXPLOSION_ENERGY_PER_STRENGTH); 
							worldObj.newExplosion(null, entity.posX, entity.posY, entity.posZ, strength, true, true);
						}
						
						// remove entity from hit list
						entityHits.put(entityHitDistance, null);
					}
				}
				if (energy <= 0) {
					break;
				}
			}
			
			// Laser went too far or no block hit
			if (blockHitDistance >= beamLengthBlocks || blockHit == null) {
				vHitPoint = vReachPoint;
				break;
			}
			
			Block block = worldObj.getBlock(blockHit.blockX, blockHit.blockY, blockHit.blockZ);
			// int blockMeta = worldObj.getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ);
			float resistance = block.getExplosionResistance(null); // TODO: choose entity
			
			if (block.isAssociatedBlock(Blocks.bedrock)) {
				vHitPoint = new Vector3(blockHit.hitVec);
				break;
			}
			
			// Boost a laser if it uses same beam frequency
			if (block.isAssociatedBlock(WarpDrive.blockLaser) || block.isAssociatedBlock(WarpDrive.blockLaserCamera)) {
				TileEntityLaser tileEntityLaser = (TileEntityLaser) worldObj.getTileEntity(blockHit.blockX, blockHit.blockY, blockHit.blockZ);
				if (tileEntityLaser != null && tileEntityLaser.getBeamFrequency() == beamFrequency) {
					tileEntityLaser.addBeamEnergy(energy);
					vHitPoint = new Vector3(blockHit.hitVec);
					break;
				}
			}
			
			if (block.getMaterial() == Material.glass) {
				worldObj.setBlockToAir(blockHit.blockX, blockHit.blockY, blockHit.blockZ);
				vHitPoint = new Vector3(blockHit.hitVec);
			}
			
			// Consume energy
			energy -= WarpDriveConfig.LASER_CANNON_BLOCK_HIT_ENERGY
					+ (resistance * WarpDriveConfig.LASER_CANNON_BLOCK_HIT_ENERGY_PER_BLOCK_RESISTANCE)
					+ ((blockHitDistance - distanceTravelled) * WarpDriveConfig.LASER_CANNON_ENERGY_LOSS_PER_BLOCK);
			distanceTravelled = blockHitDistance;
			vHitPoint = new Vector3(blockHit.hitVec);
			if (energy <= 0) {
				break;
			}
			
			if (resistance >= WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_RESISTANCE_THRESHOLD) {
				float strength = (float)clamp(0.0D, WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_MAX_STRENGTH,
						WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_BASE_STRENGTH + energy / WarpDriveConfig.LASER_CANNON_BLOCK_HIT_EXPLOSION_ENERGY_PER_STRENGTH); 
				worldObj.newExplosion(null, blockHit.blockX, blockHit.blockY, blockHit.blockZ, strength, true, true);
				worldObj.setBlock(blockHit.blockX, blockHit.blockY, blockHit.blockZ, (worldObj.rand.nextBoolean()) ? Blocks.fire : Blocks.air);
			} else {
				worldObj.setBlockToAir(blockHit.blockX, blockHit.blockY, blockHit.blockZ);
			}
		}
		
		PacketHandler.sendBeamPacket(worldObj, new Vector3(this).translate(0.5D).translate(vDirection.scale(0.5D)), vHitPoint, r, g, b, 50, energy,
				beamLengthBlocks);
	}
	
	public TreeMap<Double, MovingObjectPosition> raytraceEntities(Vector3 vSource, Vector3 vDirection, boolean collisionFlag, double reachDistance) {
		final double raytraceTolerance = 2.0D;
		
		// Pre-computation
		Vec3 vec3Source = vSource.toVec3();
		Vec3 vec3Target = Vec3.createVectorHelper(
				vec3Source.xCoord + vDirection.x * reachDistance,
				vec3Source.yCoord + vDirection.y * reachDistance,
				vec3Source.zCoord + vDirection.z * reachDistance);
		
		// Get all possible entities
		AxisAlignedBB boxToScan = AxisAlignedBB.getBoundingBox(
				Math.min(xCoord - raytraceTolerance, vec3Target.xCoord - raytraceTolerance),
				Math.min(yCoord - raytraceTolerance, vec3Target.yCoord - raytraceTolerance),
				Math.min(zCoord - raytraceTolerance, vec3Target.zCoord - raytraceTolerance),
				Math.max(xCoord + raytraceTolerance, vec3Target.xCoord + raytraceTolerance),
				Math.max(yCoord + raytraceTolerance, vec3Target.yCoord + raytraceTolerance),
				Math.max(zCoord + raytraceTolerance, vec3Target.zCoord + raytraceTolerance));
		List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(null, boxToScan);
		
		if (entities == null || entities.isEmpty()) {
			if (WarpDriveConfig.LOGGING_WEAPON) {
				WarpDrive.logger.info("No entity on trajectory (box)");
			}
			return null;
		}
		
		// Pick the closest one on trajectory
		HashMap<Double, MovingObjectPosition> entityHits = new HashMap(entities.size());
		for (Entity entity : entities) {
			if (entity != null && entity.canBeCollidedWith() && entity.boundingBox != null) {
				double border = entity.getCollisionBorderSize();
				AxisAlignedBB aabbEntity = entity.boundingBox.expand(border, border, border);
				MovingObjectPosition hitMOP = aabbEntity.calculateIntercept(vec3Source, vec3Target);
				if (WarpDriveConfig.LOGGING_WEAPON) {
					WarpDrive.logger.info("Checking " + entity + " boundingBox " + entity.boundingBox + " border " + border + " aabbEntity " + aabbEntity + " hitMOP " + hitMOP);
				}
				if (hitMOP != null) {
					MovingObjectPosition mopEntity = new MovingObjectPosition(entity);
					mopEntity.hitVec = hitMOP.hitVec;
					double distance = vec3Source.distanceTo(hitMOP.hitVec);
					if (entityHits.containsKey(distance)) {
						distance += worldObj.rand.nextDouble() / 10.0D;
					}
					entityHits.put(distance, mopEntity);
				}
			}
		}
		
		if (entityHits.isEmpty()) {
			return null;
		}
		
		return new TreeMap(entityHits);
	}
	
	public boolean isWithCamera() {
		return (getBlockType().isAssociatedBlock(WarpDrive.blockLaserCamera));
	}
	
	public int getBeamFrequency() {
		return beamFrequency;
	}
	
	public void setBeamFrequency(int parBeamFrequency) {
		if (beamFrequency != parBeamFrequency && (parBeamFrequency <= BEAM_FREQUENCY_MAX) && (parBeamFrequency > 0)) {
			if (WarpDriveConfig.LOGGING_FREQUENCY) {
				WarpDrive.logger.info(this + " Beam frequency set from " + beamFrequency + " to " + parBeamFrequency);
			}
			beamFrequency = parBeamFrequency;
		}
		updateColor();
	}
	
	public int getCameraFrequency() {
		return cameraFrequency;
	}
	
	public void setCameraFrequency(int parCameraFrequency) {
		if (cameraFrequency != parCameraFrequency) {
			if (WarpDriveConfig.LOGGING_FREQUENCY) {
				WarpDrive.logger.info(this + " Camera frequency set from " + cameraFrequency + " to " + parCameraFrequency);
			}
			cameraFrequency = parCameraFrequency;
			// force update through main thread since CC runs on server as
			// 'client'
			packetSendTicks = 0;
			registryUpdateTicks = 0;
		}
	}
	
	private TileEntityLaserMedium findFirstBooster() {
		TileEntity result;
		result = worldObj.getTileEntity(xCoord + 1, yCoord, zCoord);
		if (result != null && result instanceof TileEntityLaserMedium) {
			dx = 1;
			dy = 0;
			dz = 0;
			return (TileEntityLaserMedium) result;
		}
		
		result = worldObj.getTileEntity(xCoord - 1, yCoord, zCoord);
		if (result != null && result instanceof TileEntityLaserMedium) {
			dx = -1;
			dy = 0;
			dz = 0;
			return (TileEntityLaserMedium) result;
		}
		
		result = worldObj.getTileEntity(xCoord, yCoord, zCoord + 1);
		if (result != null && result instanceof TileEntityLaserMedium) {
			dx = 0;
			dy = 0;
			dz = 1;
			return (TileEntityLaserMedium) result;
		}
		
		result = worldObj.getTileEntity(xCoord, yCoord, zCoord - 1);
		if (result != null && result instanceof TileEntityLaserMedium) {
			dx = 0;
			dy = 0;
			dz = -1;
			return (TileEntityLaserMedium) result;
		}
		
		result = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
		if (result != null && result instanceof TileEntityLaserMedium) {
			dx = 0;
			dy = 1;
			dz = 0;
			return (TileEntityLaserMedium) result;
		}
		
		result = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
		if (result != null && result instanceof TileEntityLaserMedium) {
			dx = 0;
			dy = -1;
			dz = 0;
			return (TileEntityLaserMedium) result;
		}
		
		return null;
	}
	
	private void playSoundCorrespondsEnergy(int energy) {
		if (energy <= 500000) {
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
		} else if (energy > 500000 && energy <= 1000000) {
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:midlaser", 4F, 1F);
		} else if (energy > 1000000) {
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
		}
	}
	
	private void updateColor() {
		if (beamFrequency <= 0) { // invalid frequency
			r = 1.0F;
			g = 0.0F;
			b = 0.0F;
		} else if (beamFrequency <= 10000) { // red
			r = 1.0F;
			g = 0.0F;
			b = 0.0F + 0.5f * beamFrequency / 10000F;
		} else if (beamFrequency <= 20000) { // orange
			r = 1.0F;
			g = 0.0F + 1.0F * (beamFrequency - 10000F) / 10000F;
			b = 0.5F - 0.5F * (beamFrequency - 10000F) / 10000F;
		} else if (beamFrequency <= 30000) { // yellow
			r = 1.0F - 1.0F * (beamFrequency - 20000F) / 10000F;
			g = 1.0F;
			b = 0.0F;
		} else if (beamFrequency <= 40000) { // green
			r = 0.0F;
			g = 1.0F - 1.0F * (beamFrequency - 30000F) / 10000F;
			b = 0.0F + 1.0F * (beamFrequency - 30000F) / 10000F;
		} else if (beamFrequency <= 50000) { // blue
			r = 0.0F + 0.5F * (beamFrequency - 40000F) / 10000F;
			g = 0.0F;
			b = 1.0F - 0.5F * (beamFrequency - 40000F) / 10000F;
		} else if (beamFrequency <= 60000) { // violet
			r = 0.5F + 0.5F * (beamFrequency - 50000F) / 10000F;
			g = 0.0F;
			b = 0.5F - 0.5F * (beamFrequency - 50000F) / 10000F;
		} else if (beamFrequency <= BEAM_FREQUENCY_MAX) { // rainbow
			int component = Math.round(4096F * (beamFrequency - 60000F) / (BEAM_FREQUENCY_MAX - 60000F));
			r = 1.0F - 0.5F * (component & 0xF);
			g = 0.5F + 0.5F * (component >> 4 & 0xF);
			b = 0.5F + 0.5F * (component >> 8 & 0xF);
		} else { // invalid frequency
			r = 1.0F;
			g = 0.0F;
			b = 0.0F;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		setBeamFrequency(tag.getInteger("beamFrequency"));
		setCameraFrequency(tag.getInteger("cameraFrequency"));
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("beamFrequency", beamFrequency);
		tag.setInteger("cameraFrequency", cameraFrequency);
	}
	
	@Override
	public void invalidate() {
		WarpDrive.instance.cameras.removeFromRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord));
		super.invalidate();
	}
	
	@Override
	public void onChunkUnload() {
		WarpDrive.instance.cameras.removeFromRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord));
		super.onChunkUnload();
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] emitBeam(Context context, Arguments arguments) {
		return emitBeam(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] pos(Context context, Arguments arguments) {
		return new Integer[] { xCoord, yCoord, zCoord };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] freq(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			setBeamFrequency(arguments.checkInteger(0));
		}
		return new Integer[] { beamFrequency };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getFirstHit(Context context, Arguments arguments) {
		return getFirstHit();
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getBoosterDXDZ(Context context, Arguments arguments) {
		findFirstBooster();
		return new Integer[] { dx, dz };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] CamFreq(Context context, Arguments arguments) {
		if (isWithCamera()) {
			if (arguments.count() == 1) {
				setCameraFrequency(arguments.checkInteger(0));
			}
			return new Integer[] { cameraFrequency };
		}
		return null;
	}
	
	private Object[] emitBeam(Object[] arguments) {
		try {
			float newYaw, newPitch;
			if (arguments.length == 2) {
				newYaw = toFloat(arguments[0]);
				newPitch = toFloat(arguments[1]);
				initiateBeamEmission(newYaw, newPitch);
			} else if (arguments.length == 3) {
				float deltaX = -toFloat(arguments[0]);
				float deltaY = -toFloat(arguments[1]);
				float deltaZ = toFloat(arguments[2]);
				double horizontalDistance = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
				newYaw = (float) (Math.atan2(deltaX, deltaZ) * 180.0D / Math.PI);
				newPitch = (float) (Math.atan2(deltaY, horizontalDistance) * 180.0D / Math.PI);
				initiateBeamEmission(newYaw, newPitch);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Object[] { false };
		}
		return new Object[] { true };
	}
	
	private Object[] getFirstHit() {
		if (firstHit_position != null) {
			try {
				Object[] info = { firstHit_position.blockX, firstHit_position.blockY, firstHit_position.blockZ, firstHit_block, firstHit_blockMeta,
						firstHit_blockResistance };
				firstHit_position = null;
				firstHit_block = null;
				firstHit_blockMeta = 0;
				firstHit_blockResistance = -2;
				return info;
			} catch (Exception e) {
				e.printStackTrace();
				return new Integer[] { 0, 0, 0, 0, 0, -3 };
			}
		} else {
			return new Integer[] { 0, 0, 0, 0, 0, -1 };
		}
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = methodsArray[method];
		if (methodName.equals("emitBeam")) { // emitBeam(yaw, pitch) or
			// emitBeam(deltaX, deltaY,
			// deltaZ)
			return emitBeam(arguments);
		} else if (methodName.equals("pos")) {
			return new Integer[] { xCoord, yCoord, zCoord };
		} else if (methodName.equals("freq")) {
			if (arguments.length == 1) {
				setBeamFrequency(toInt(arguments[0]));
			}
			return new Integer[] { beamFrequency };
		} else if (methodName.equals("getFirstHit")) {
			return getFirstHit();
		} else if (methodName.equals("getBoosterDXDZ")) {
			findFirstBooster();
			return new Integer[] { dx, dz };
		} else if (methodName.equals("camFreq")) { // camFreq (only for lasers
			// with cam)
			if (isWithCamera()) {
				if (arguments.length == 1) {
					setCameraFrequency(toInt(arguments[0]));
				}
				return new Integer[] { cameraFrequency };
			}
			return null;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return String.format("%s/%d Beam \'%d\' Camera \'%d\' @ \'%s\' %d, %d, %d", new Object[] { getClass().getSimpleName(), Integer.valueOf(hashCode()),
				beamFrequency, cameraFrequency, worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord });
	}
}