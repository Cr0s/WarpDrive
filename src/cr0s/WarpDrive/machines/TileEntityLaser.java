package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import cr0s.WarpDrive.*;
import cr0s.WarpDrive.data.Vector3;

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = WarpDriveConfig.modid_OpenComputers),
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = WarpDriveConfig.modid_ComputerCraft)
})
public class TileEntityLaser extends WarpTE implements IPeripheral, Environment {
	//magic constants
	private final int SCANNING_BEAM_LENGTH = 400;	//FIXME merge re-adding a non-used definition?
	private final int SCANNING_BEAM_FREQ = 1420;

	private int dx, dz, dy;
	public float yaw, pitch; // laser direction

	private int beamFrequency = -1;
	private int cameraFrequency = -1;
	private float r, g, b;	  // beam color (corresponds to frequency)

	public boolean isEmitting = false;

	private String[] methodsArray =
	{
		"emitBeam",		// 0
		"pos",			// 1
		"freq",			// 2
		"getFirstHit",		// 3
		"getBoosterDXDZ",	// 4
		"camFreq"		// 5
	};
	private HashMap<Integer,IComputerAccess> connectedComputers = new HashMap<Integer,IComputerAccess>();

	public int delayTicks = 0;
	private int energyFromOtherBeams = 0;
	
	private MovingObjectPosition firstHit = null;

	private final static int REGISTRY_UPDATE_INTERVAL_TICKS = 15 * 20;
	private final static int PACKET_SEND_INTERVAL_TICKS = 60 * 20;

	private int registryUpdateTicks = 20;
	private int packetSendTicks = 20;

	private String peripheralName = "laser";

    protected Node node;
    protected boolean addedToNetwork = false;

	public TileEntityLaser() {
		if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
			initOC();
	}

	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	private void initOC() {
		node = Network.newNode(this, Visibility.Network).withComponent(peripheralName).create();
	}
    
    @Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
    private void addToNetwork() {
		if (!addedToNetwork) {
			addedToNetwork = true;
			Network.joinOrCreateNetwork(this);
		}
    }

	@Override
	public void updateEntity() {
		if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
			addToNetwork();
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
					WarpDrive.instance.cams.updateInRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord), cameraFrequency, 1);
				}
			}
		}

		// Frequency is not set		
		if (beamFrequency <= 0 || beamFrequency > 65000) {
			return;
		}

		delayTicks++;
		if (isEmitting && ((beamFrequency != 1420 && delayTicks > WarpDriveConfig.LE_EMIT_DELAY_TICKS) || ((beamFrequency == 1420) && delayTicks > WarpDriveConfig.LE_EMIT_SCAN_DELAY_TICKS))) {
			delayTicks = 0;
			isEmitting = false;
			emitBeam(Math.min(this.consumeEnergyFromBoosters() + MathHelper.floor_double(energyFromOtherBeams * WarpDriveConfig.LE_COLLECT_ENERGY_MULTIPLIER), WarpDriveConfig.LE_MAX_LASER_ENERGY));
			energyFromOtherBeams = 0;
			if (Loader.isModLoaded(WarpDriveConfig.modid_ComputerCraft))
				sendEventCC("laserSend", null);
		}
	}

	public void addBeamEnergy(int amount) {
		if (isEmitting) {
			energyFromOtherBeams += amount;
			WarpDrive.debugPrint(this + " Added energy " + amount);
		} else {
			WarpDrive.debugPrint(this + " Ignored energy " + amount);
		}
	}

	private int consumeEnergyFromBoosters() {
		int energyCollected = 0;

		if (findFirstBooster() != null) {
			int newX, newY, newZ;
			TileEntity te;
			for (int shift = 1; shift <= WarpDriveConfig.LE_MAX_BOOSTERS_NUMBER; shift++) {
				newX = xCoord + (dx * shift);
				newY = yCoord + (dy * shift);
				newZ = zCoord + (dz * shift);
				te = worldObj.getBlockTileEntity(newX, newY, newZ);
				if (te != null && te instanceof TileEntityParticleBooster) {
					energyCollected += ((TileEntityParticleBooster)te).consumeAllEnergy();
				} else {
					break;
				}
			}
		}

		return energyCollected;
	}

	// TODO refactor me
	private void emitBeam(int parEnergy) {
		int energy = parEnergy; 	// FIXME
		// Beam power calculations
		int beamLengthBlocks = energy / WarpDriveConfig.LE_BEAM_LENGTH_PER_ENERGY_DIVIDER;

		if (energy == 0 || beamLengthBlocks < 1 || beamFrequency > 65000 || beamFrequency <= 0) {
			return;
		}

		Vector3 beamVector = new Vector3(this).translate(0.5D);
		WarpDrive.debugPrint("" + this + " Energy " + energy + " over " + beamLengthBlocks + " blocks, Initial beam " + beamVector);
		float yawz = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float yawx = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float pitchhorizontal = -MathHelper.cos(-pitch * 0.017453292F);
		float pitchvertical = MathHelper.sin(-pitch * 0.017453292F);
		float directionx = yawx * pitchhorizontal;
		float directionz = yawz * pitchhorizontal;
		Vector3 lookVector = new Vector3(directionx, pitchvertical, directionz);
		Vector3.translate(beamVector, lookVector);
		Vector3 reachPoint = Vector3.translate(beamVector.clone(), Vector3.scale(lookVector.clone(), beamLengthBlocks));
		WarpDrive.debugPrint(this + " Beam " + beamVector + " Look " + lookVector + " Reach " + reachPoint + " TranslatedBeam " + beamVector);
		Vector3 endPoint = reachPoint.clone();
		playSoundCorrespondsEnergy(energy);
		int distanceTravelled = 0; //distance travelled from beam emitter to previous hit if there were any
		
		// This is scanning beam, do not deal damage to blocks
		if (beamFrequency == SCANNING_BEAM_FREQ) {
			firstHit = worldObj.rayTraceBlocks_do_do(beamVector.toVec3(), reachPoint.toVec3(), false, false);

 			if (firstHit != null) {
				WarpDrive.sendLaserPacket(worldObj, beamVector, new Vector3(firstHit.hitVec), r, g, b, 50, energy, 200);
			} else {
				WarpDrive.sendLaserPacket(worldObj, beamVector, reachPoint, r, g, b, 50, energy, 200);
  			}
 			
			return;
		}

		for (int passedBlocks = 0; passedBlocks < beamLengthBlocks; passedBlocks++) {
			// Get next block hit
			MovingObjectPosition hit = worldObj.rayTraceBlocks_do_do(beamVector.toVec3(), reachPoint.toVec3(), true, false);
			// FIXME entity ray-tracing
			MovingObjectPosition entityHit = raytraceEntities(beamVector.clone(), lookVector.clone(), true, beamLengthBlocks);

			WarpDrive.debugPrint("Entity hit is " + entityHit);

			if (entityHit != null && entityHit.entityHit instanceof EntityLivingBase) {
				EntityLivingBase e = (EntityLivingBase)entityHit.entityHit;
				double distanceToEntity = entityHit.hitVec.distanceTo(beamVector.clone().toVec3());

				if (hit == null || (hit != null && hit.hitVec.distanceTo(beamVector.clone().toVec3()) > distanceToEntity)) {
					if (distanceToEntity <= beamLengthBlocks) {
						e.setFire(WarpDriveConfig.LE_ENTITY_HIT_SET_ON_FIRE_TIME);
						e.attackEntityFrom(DamageSource.inFire, energy / WarpDriveConfig.LE_ENTITY_HIT_DAMAGE_PER_ENERGY_DIVIDER);

						if (energy > WarpDriveConfig.LE_ENTITY_HIT_EXPLOSION_LASER_ENERGY) {
							worldObj.newExplosion(null, e.posX, e.posY, e.posZ, 4F, true, true);
						}

						// consume energy
						energy -= WarpDriveConfig.LE_ENTITY_HIT_DAMAGE_PER_ENERGY_DIVIDER + (10 * distanceToEntity);
						endPoint = new Vector3(entityHit.hitVec);
						break;
					}
				}
			}

			// Laser is missed
			if (hit == null && entityHit == null) {
				endPoint = reachPoint;
				break;
			} else if (hit != null) {
				// We got a hit block
				int distance = (int) new Vector3(hit.hitVec).distanceTo(beamVector);

				// Laser gone too far
				if (distance >= beamLengthBlocks) {
					endPoint = reachPoint;
					break;
				}

				int blockID = worldObj.getBlockId(hit.blockX, hit.blockY, hit.blockZ);
				// int blockMeta = worldObj.getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ);
				float resistance = Block.blocksList[blockID].blockResistance;

				if (blockID == Block.bedrock.blockID) {
					endPoint = new Vector3(hit.hitVec);
					break;
				}

				// Hit is a laser head
				if (blockID == WarpDriveConfig.laserID || blockID == WarpDriveConfig.laserCamID) {
					// Compare frequencies
					TileEntityLaser tel = (TileEntityLaser)worldObj.getBlockTileEntity(hit.blockX, hit.blockY, hit.blockZ);

					if (tel != null && tel.getBeamFrequency() == beamFrequency) {
						tel.addBeamEnergy(energy);
						endPoint = new Vector3(hit.hitVec);
						break;
					}
				}

				if (Block.blocksList[blockID].blockMaterial == Material.glass) {
					worldObj.destroyBlock(hit.blockX, hit.blockY, hit.blockZ, (worldObj.rand.nextInt(20) == 0));
					endPoint = new Vector3(hit.hitVec);
				}

				energy -=  WarpDriveConfig.LE_BLOCK_HIT_CONSUME_ENERGY + (resistance * WarpDriveConfig.LE_BLOCK_HIT_CONSUME_ENERGY_PER_BLOCK_RESISTANCE) + ( (distance - distanceTravelled) * WarpDriveConfig.LE_BLOCK_HIT_CONSUME_ENERGY_PER_DISTANCE);
				distanceTravelled = distance;
				endPoint = new Vector3(hit.hitVec);

				if (energy <= 0) {
					break;
				}

				if (resistance >= Block.obsidian.blockResistance) {
					worldObj.newExplosion(null, hit.blockX, hit.blockY, hit.blockZ, 4F * (2 + (energy / 500000)), true, true);
					worldObj.setBlock(hit.blockX, hit.blockY, hit.blockZ, (worldObj.rand.nextBoolean()) ? Block.fire.blockID : 0);
				} else {
					worldObj.destroyBlock(hit.blockX, hit.blockY, hit.blockZ, (worldObj.rand.nextInt(20) == 0));
				}
			}
		}

		WarpDrive.instance.sendLaserPacket(worldObj, beamVector, endPoint, r, g, b, 50, energy, beamLengthBlocks);
	}

	public MovingObjectPosition raytraceEntities(Vector3 beamVec, Vector3 lookVec, boolean collisionFlag, double reachDistance) {
		MovingObjectPosition pickedEntity = null;
		Vec3 playerPosition = beamVec.toVec3();
		Vec3 playerLook = lookVec.toVec3();
		Vec3 playerViewOffset = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord * reachDistance, playerPosition.yCoord
								+ playerLook.yCoord * reachDistance, playerPosition.zCoord + playerLook.zCoord * reachDistance);
		double playerBorder = 1.1 * reachDistance;
		AxisAlignedBB boxToScan = WarpDrive.laserBlock.getCollisionBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord).expand(playerBorder, playerBorder, playerBorder);
		List entitiesHit = worldObj.getEntitiesWithinAABBExcludingEntity(null, boxToScan);
		double closestEntity = reachDistance;

		if (entitiesHit == null || entitiesHit.isEmpty()) {
			return null;
		}

		for (Entity entityHit : (Iterable<Entity>) entitiesHit) {
			if (entityHit != null && entityHit.canBeCollidedWith() && entityHit.boundingBox != null) {
				double border = entityHit.getCollisionBorderSize();
				AxisAlignedBB aabb = entityHit.boundingBox.expand(border, border, border);
				MovingObjectPosition hitMOP = aabb.calculateIntercept(playerPosition, playerViewOffset);

				if (hitMOP != null) {
					if (aabb.isVecInside(playerPosition)) {
						if (0.0D < closestEntity || closestEntity == 0.0D) {
							pickedEntity = new MovingObjectPosition(entityHit);

							if (pickedEntity != null) {
								pickedEntity.hitVec = hitMOP.hitVec;
								closestEntity = 0.0D;
							}
						}
					} else {
						double distance = playerPosition.distanceTo(hitMOP.hitVec);

						if (distance < closestEntity || closestEntity == 0.0D) {
							pickedEntity = new MovingObjectPosition(entityHit);
							pickedEntity.hitVec = hitMOP.hitVec;
							closestEntity = distance;
						}
					}
				}
			}
		}

		return pickedEntity;
	}

	public boolean isWithCamera() {
		return (getBlockType().blockID == WarpDriveConfig.laserCamID);
	}

	public int getBeamFrequency() {
		return beamFrequency;
	}
	
	public void setBeamFrequency(int parBeamFrequency) {
		if (beamFrequency != parBeamFrequency) {
			WarpDrive.debugPrint(this + " Beam frequency set from " + beamFrequency + " to " + parBeamFrequency);
			beamFrequency = parBeamFrequency;
		}
		updateColor();
	}

	public int getCameraFrequency() {
		return cameraFrequency;
	}

	public void setCameraFrequency(int parCameraFrequency) {
		if (cameraFrequency != parCameraFrequency) {
			WarpDrive.debugPrint(this + " Camera frequency set from " + cameraFrequency + " to " + parCameraFrequency);
			cameraFrequency = parCameraFrequency;
	        // force update through main thread since CC runs on server as 'client'
	        packetSendTicks = 0;
	        registryUpdateTicks = 0;
		}
	}

	private TileEntityParticleBooster findFirstBooster() {
		TileEntity result;
		result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
		if (result != null && result instanceof TileEntityParticleBooster) {
			dx = 1;
			dy = 0;
			dz = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
		if (result != null && result instanceof TileEntityParticleBooster) {
			dx = -1;
			dy = 0;
			dz = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
		if (result != null && result instanceof TileEntityParticleBooster) {
			dx = 0;
			dy = 0;
			dz = 1;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
		if (result != null && result instanceof TileEntityParticleBooster) {
			dx = 0;
			dy = 0;
			dz = -1;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
		if (result != null && result instanceof TileEntityParticleBooster) {
			dx = 0;
			dy = 1;
			dz = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);
		if (result != null && result instanceof TileEntityParticleBooster) {
			dx = 0;
			dy = -1;
			dz = 0;
			return (TileEntityParticleBooster) result;
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
		if (beamFrequency > 65000 || beamFrequency <= 0) { // Invalid frequency
			r = 1;
			g = 0;
			b = 0;
		}

		if (beamFrequency > 0 && beamFrequency <= 10000) { // red
			r = 1;
			g = 0;
			b = 0;
		} else if (beamFrequency > 10000 && beamFrequency <= 20000) { // orange
			r = 1;
			g = 0;
			b = 0.5f;
		} else if (beamFrequency > 20000 && beamFrequency <= 30000) { // yellow
			r = 1;
			g = 1;
			b = 0;
		} else if (beamFrequency > 30000 && beamFrequency <= 40000) { // green
			r = 0;
			g = 1;
			b = 0;
		} else if (beamFrequency > 50000 && beamFrequency <= 60000) { // blue
			r = 0;
			g = 0;
			b = 1;
		} else if (beamFrequency > 60000 && beamFrequency <= 65000) { // violet
			r = 0.5f;
			g = 0;
			b = 0.5f;
		} else { // impossible frequency
			r = 1;
			g = 0;
			b = 0;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		setBeamFrequency(tag.getInteger("beamFrequency"));
		setCameraFrequency(tag.getInteger("cameraFrequency"));
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	readFromNBT_OC(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("beamFrequency", beamFrequency);
		tag.setInteger("cameraFrequency", cameraFrequency);
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	writeToNBT_OC(tag);
	}
	
	@Override
	public void invalidate() {
        WarpDrive.instance.cams.removeFromRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord));
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	invalidate_OC();
		super.invalidate();
	}

    @Override
    public void onChunkUnload() {
        WarpDrive.instance.cams.removeFromRegistry(worldObj, new ChunkPosition(xCoord, yCoord, zCoord));
        if (Loader.isModLoaded(WarpDriveConfig.modid_OpenComputers))
        	onChunkUnload_OC();
        super.onChunkUnload();
    }

	// IPeripheral methods implementation
	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public String getType() {
		return peripheralName;
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public String[] getMethodNames() {
		return methodsArray;
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
		switch (method)
		{
			case 0: // emitBeam(yaw, pitch)
				// emitBeam(dx, dy, dz)
				if (arguments.length == 2) {
					yaw = ((Double)arguments[0]).floatValue();
					pitch = ((Double)arguments[1]).floatValue();
					isEmitting = true;
					delayTicks = 0;
				} else if (arguments.length == 3) {
					double dx = (Double)arguments[0];
					double dy = (Double)arguments[1];
					double dz = -(Double)arguments[2];	//FIXME kostyl
					double targetX = xCoord + dx;
					double targetY = yCoord + dy;
					double targetZ = zCoord + dz;
					float xd = (float)(xCoord - targetX);
					float yd = (float)(yCoord - targetY);
					float zd = (float)(zCoord - targetZ);
					double var7 = MathHelper.sqrt_double(xd * xd + zd * zd);
					yaw = ((float)(Math.atan2(xd, zd) * 180.0D / Math.PI));
					pitch = ((float)(Math.atan2(yd, var7) * 180.0D / Math.PI));
					isEmitting = true;
					delayTicks = 0;
				}
				break;
				
			case 1: // getX
				return new Integer[] { xCoord, yCoord, zCoord };
				
			case 2: // Freq
				if (arguments.length == 1) {
					int parFrequency = ((Double)arguments[0]).intValue();
					if ((parFrequency <= 65000) && (parFrequency > 0)) {
						setBeamFrequency(parFrequency);
					}
				}
				return new Integer[] { beamFrequency };
				
			case 3: // getFirstHit()
				if (firstHit != null) {
					try {
						int blockID = worldObj.getBlockId(firstHit.blockX, firstHit.blockY, firstHit.blockZ);
						int blockMeta = worldObj.getBlockMetadata(firstHit.blockX, firstHit.blockY, firstHit.blockZ);
						float blockResistance = -2;
						if (Block.blocksList[blockID] != null) {
							blockResistance = Block.blocksList[blockID].blockResistance;
						}
						Object[] info = { firstHit.blockX, firstHit.blockY, firstHit.blockZ, blockID, blockMeta, blockResistance };
						firstHit = null;
						return info;
					} catch (Exception e) {
						e.printStackTrace();
						return new Integer[] { 0, 0, 0, 0, 0, -3 };
					}
				} else {
					return new Integer[] { 0, 0, 0, 0, 0, -1 };
				}
				
			case 4: // getBoosterDXDZ
				findFirstBooster();
				return new Integer[] { dx, dz };

			case 5: // CamFreq (only for lasers with cam)
				if (isWithCamera()) {
					if (arguments.length == 1) {
						setCameraFrequency(((Double)arguments[0]).intValue());
					}
				}
				return new Integer[] { cameraFrequency };
		}
		return null;
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public void attach(IComputerAccess computer) {
		int id = computer.getID();
		connectedComputers.put(id, computer);
	}
	
	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public void detach(IComputerAccess computer) {
		int id = computer.getID();
		if (connectedComputers.containsKey(id)) {
			connectedComputers.remove(id);
		}
	}

	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	private void sendEventCC(String eventName, Object[] arguments) {
		// WarpDrive.debugPrint("" + this + " Sending event '" + eventName + "'");
		Set<Integer> keys = connectedComputers.keySet();
		for(Integer key:keys) {
			IComputerAccess comp = connectedComputers.get(key);
			comp.queueEvent(eventName, arguments);
		}
	}
	
	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_ComputerCraft)
	public boolean equals(IPeripheral other) {
		return other == this;
	}
	
	@Override
	public String toString() {
        return String.format("%s/%d Beam \'%d\' Camera \'%d\' @ \'%s\' %d, %d, %d", new Object[] {
       		getClass().getSimpleName(),
       		Integer.valueOf(hashCode()),
       		beamFrequency,
       		cameraFrequency,
       		worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
       		xCoord, yCoord, zCoord});
	}

	// OpenComputers.

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Node node() {
		return node;
	}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onConnect(Node node) {}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onDisconnect(Node node) {}

	@Override
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onMessage(Message message) {}
	
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void onChunkUnload_OC() {
		if (node != null) node.remove();
	}

	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void invalidate_OC() {
		if (node != null) node.remove();
	}
	
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void readFromNBT_OC(final NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (node != null && node.host() == this) {
			node.load(nbt.getCompoundTag("oc:node"));
		}
	}

	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public void writeToNBT_OC(final NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if (node != null && node.host() == this) {
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			node.save(nodeNbt);
			nbt.setTag("oc:node", nodeNbt);
		}
	}

	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] emitBeam(Context context, Arguments args) {
		// emitBeam(yaw, pitch)
		// emitBeam(dx, dy, dz)
		if (args.count() == 2) {
			yaw = (float)args.checkDouble(0);
			pitch = (float)args.checkDouble(1);
			isEmitting = true;
			delayTicks = 0;
		} else if (args.count() == 3) {
			double dx = args.checkDouble(0);
			double dy = args.checkDouble(1);
			double dz = -args.checkDouble(1);	//FIXME kostyl
			double targetX = xCoord + dx;
			double targetY = yCoord + dy;
			double targetZ = zCoord + dz;
			float xd = (float)(xCoord - targetX);
			float yd = (float)(yCoord - targetY);
			float zd = (float)(zCoord - targetZ);
			double var7 = MathHelper.sqrt_double(xd * xd + zd * zd);
			yaw = ((float)(Math.atan2(xd, zd) * 180.0D / Math.PI));
			pitch = ((float)(Math.atan2(yd, var7) * 180.0D / Math.PI));
			isEmitting = true;
			delayTicks = 0;
		}
		return null;
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] pos(Context context, Arguments args) {
		return new Integer[] { xCoord, yCoord, zCoord };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] freq(Context context, Arguments args) {
		if (args.count() == 1) {
			int parFrequency = args.checkInteger(0);
			if ((parFrequency <= 65000) && (parFrequency > 0)) {
				setBeamFrequency(parFrequency);
			}
		}
		return new Integer[] { beamFrequency };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] getFirstHit(Context context, Arguments args) {
		if (firstHit != null) {
			try {
				int blockID = worldObj.getBlockId(firstHit.blockX, firstHit.blockY, firstHit.blockZ);
				int blockMeta = worldObj.getBlockMetadata(firstHit.blockX, firstHit.blockY, firstHit.blockZ);
				float blockResistance = -2;
				if (Block.blocksList[blockID] != null) {
					blockResistance = Block.blocksList[blockID].blockResistance;
				}
				Object[] info = { firstHit.blockX, firstHit.blockY, firstHit.blockZ, blockID, blockMeta, blockResistance };
				firstHit = null;
				return info;
			} catch (Exception e) {
				e.printStackTrace();
				return new Integer[] { 0, 0, 0, 0, 0, -3 };
			}
		} else {
			return new Integer[] { 0, 0, 0, 0, 0, -1 };
		}
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] getBoosterDXDZ(Context context, Arguments args) {
		findFirstBooster();
		return new Integer[] { dx, dz };
	}
	
	@Callback
	@Optional.Method(modid = WarpDriveConfig.modid_OpenComputers)
	public Object[] camFreq(Context context, Arguments args) {
		if (isWithCamera()) {
			if (args.count() == 1) {
				setCameraFrequency(args.checkInteger(0));
			}
		}
		return new Integer[] { cameraFrequency };
	}
}