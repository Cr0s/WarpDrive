package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet62LevelSound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityLaser extends TileEntity implements IPeripheral
{
	private int dx, dz, dy;
	public float yaw, pitch; // laser direction

	private int frequency = -1;	// beam frequency
	public int camFreq = -1;	   // camera frequency
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

	public int delayTicks = 0;
	private int energyFromOtherBeams = 0;

	private MovingObjectPosition firstHit;

	private int camUpdateTicks = 20;
	private int registryUpdateTicks = 20 * 10;

	@Override
	public void updateEntity()
	{
		// Frequency is not set
		if (frequency <= 0)
		{
			return;
		}

		if (isWithCamera())
		{
			if (registryUpdateTicks-- == 0 && FMLCommonHandler.instance().getEffectiveSide().isClient())
			{
				registryUpdateTicks = 20 * 10;
				WarpDrive.instance.cams.updateInRegistry(new CamRegistryItem(this.camFreq, new ChunkPosition(xCoord, yCoord, zCoord), worldObj).setType(1));
			}

			if (camUpdateTicks-- == 0)
			{
				camUpdateTicks = 20 * 5; // 5 seconds
				sendFreqPacket();  // send own cam frequency to clients
			}
		}

		if (isEmitting && (frequency != 1420 && ++delayTicks > WarpDriveConfig.i.LE_EMIT_DELAY_TICKS) || ((frequency == 1420) && ++delayTicks > WarpDriveConfig.i.LE_EMIT_SCAN_DELAY_TICKS))
		{
			delayTicks = 0;
			isEmitting = false;
			emitBeam(Math.min(this.collectEnergyFromBoosters() + MathHelper.floor_double(energyFromOtherBeams * WarpDriveConfig.i.LE_COLLECT_ENERGY_MULTIPLIER), WarpDriveConfig.i.LE_MAX_LASER_ENERGY));
			energyFromOtherBeams = 0;
		}
	}

	public void addBeamEnergy(int amount)
	{
		if (isEmitting)
		{
			energyFromOtherBeams += amount;
			System.out.println("[LE] Added energy: " + amount);
		}
		else
		{
			System.out.println("[LE] Ignored energy: " + amount);
		}
	}

	private int collectEnergyFromBoosters()
	{
		int energyCollected = 0;

		if (findFirstBooster() != null)
		{
			for (int shift = 1; shift <= WarpDriveConfig.i.LE_MAX_BOOSTERS_NUMBER; shift++)
			{
				int newX = xCoord + (dx * shift);
				int newY = yCoord + (dy * shift);
				int newZ = zCoord + (dz * shift);
				TileEntity te = worldObj.getBlockTileEntity(newX, newY, newZ);

				if (te != null && te instanceof TileEntityParticleBooster)
				{
					energyCollected += ((TileEntityParticleBooster)te).collectAllEnergy();
				}
				else
				{
					break;
				}
			}
		}

		return energyCollected;
	}

	// TODO refactor me
	private void emitBeam(int energy)
	{
		// Beam power calculations
		int beamLengthBlocks = energy / WarpDriveConfig.i.LE_BEAM_LENGTH_PER_ENERGY_DIVIDER;
		System.out.println("Energy: " + energy + " | beamLengthBlocks: " + beamLengthBlocks);

		if (energy == 0 || beamLengthBlocks < 1 || frequency > 65000 || frequency <= 0)
		{
			return;
		}

		Vector3 beamVector = new Vector3(this).add(0.5);
		System.out.println("beamVector: " + beamVector);
		float yawz = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float yawx = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float pitchhorizontal = -MathHelper.cos(-pitch * 0.017453292F);
		float pitchvertical = MathHelper.sin(-pitch * 0.017453292F);
		float directionx = yawx * pitchhorizontal;
		float directionz = yawz * pitchhorizontal;
		Vector3 lookVector = new Vector3((double) directionx, (double) pitchvertical, (double) directionz);
		Vector3.translate(beamVector, lookVector);
		Vector3 reachPoint = beamVector.clone().translate(beamVector.clone(), beamVector.clone().scale(lookVector.clone(), beamLengthBlocks));
		System.out.println("Look vector: " + lookVector);
		System.out.println("reachPoint: " + reachPoint);
		System.out.println("translatedBeamVector: " + beamVector);
		Vector3 endPoint = reachPoint.clone();
		playSoundCorrespondsEnergy(energy);

		int distanceTravelled = 0; //distance travelled from beam emitter to previous hit if there were any

		// This is scanning beam, do not deal damage to blocks
		if (frequency == 1420)
		{
			firstHit = worldObj.rayTraceBlocks_do_do(beamVector.toVec3(), reachPoint.toVec3(), false, false);

			if (firstHit != null)
			{
				sendLaserPacket(beamVector, new Vector3(firstHit), r, g, b, 50, energy, 200);
			}

			return;
		}

		for (int passedBlocks = 0; passedBlocks < beamLengthBlocks; ++passedBlocks)
		{
			// Get next block hit
			MovingObjectPosition hit = worldObj.rayTraceBlocks_do_do(beamVector.toVec3(), reachPoint.toVec3(), true, false);
			// FIXME entity ray-tracing
			MovingObjectPosition entityHit = raytraceEntities(beamVector.clone(), lookVector.clone(), true, beamLengthBlocks);

			if (entityHit == null)
			{
				System.out.println("Entity hit is null.");
			}
			else
			{
				System.out.println("Entity hit: " + entityHit);
			}

			if (entityHit != null && entityHit.entityHit instanceof EntityLivingBase)
			{
				EntityLivingBase e = (EntityLivingBase)entityHit.entityHit;
				double distanceToEntity = entityHit.hitVec.distanceTo(beamVector.clone().toVec3());

				if (hit == null || (hit != null && hit.hitVec.distanceTo(beamVector.clone().toVec3()) > distanceToEntity))
				{
					if (distanceToEntity <= beamLengthBlocks)
					{
						((EntityLivingBase)e).setFire(WarpDriveConfig.i.LE_ENTITY_HIT_SET_ON_FIRE_TIME);
						((EntityLivingBase)e).attackEntityFrom(DamageSource.inFire, energy / WarpDriveConfig.i.LE_ENTITY_HIT_DAMAGE_PER_ENERGY_DIVIDER);

						if (energy > WarpDriveConfig.i.LE_ENTITY_HIT_EXPLOSION_LASER_ENERGY)
						{
							worldObj.newExplosion(null, e.posX, e.posY, e.posZ, 4F, true, true);
						}

						// consume energy
						energy -= WarpDriveConfig.i.LE_ENTITY_HIT_DAMAGE_PER_ENERGY_DIVIDER + (10 * distanceToEntity);
						endPoint = new Vector3(entityHit.hitVec);
						break;
					}
				}
			}

			// Laser is missed
			if (hit == null && entityHit == null)
			{
				endPoint = reachPoint;
				break;
			}
			else if (hit != null)
			{
				// We got a hit block
				int distance = (int) new Vector3(hit.hitVec).distanceTo(beamVector);

				// Laser gone too far
				if (distance >= beamLengthBlocks)
				{
					endPoint = reachPoint;
					break;
				}

				int blockID = worldObj.getBlockId(hit.blockX, hit.blockY, hit.blockZ);
				int blockMeta = worldObj.getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ);
				float resistance = Block.blocksList[blockID].blockResistance;

				if (blockID == Block.bedrock.blockID)
				{
					endPoint = new Vector3(hit.hitVec);
					break;
				}

				// Hit is a laser head
				if (blockID == WarpDriveConfig.i.laserID || blockID == WarpDriveConfig.i.laserCamID)
				{
					// Compare frequencies
					TileEntityLaser tel = (TileEntityLaser)worldObj.getBlockTileEntity(hit.blockX, hit.blockY, hit.blockZ);

					if (tel != null && tel.getFrequency() == frequency)
					{
						tel.addBeamEnergy(energy);
						endPoint = new Vector3(hit.hitVec);
						break;
					}
				}

				if (Block.blocksList[blockID].blockMaterial == Material.glass)
				{
					worldObj.destroyBlock(hit.blockX, hit.blockY, hit.blockZ, (worldObj.rand.nextInt(20) == 0));
					endPoint = new Vector3(hit.hitVec);
				}

				energy -=  WarpDriveConfig.i.LE_BLOCK_HIT_CONSUME_ENERGY + (resistance * WarpDriveConfig.i.LE_BLOCK_HIT_CONSUME_ENERGY_PER_BLOCK_RESISTANCE) + ( (distance - distanceTravelled) * WarpDriveConfig.i.LE_BLOCK_HIT_CONSUME_ENERGY_PER_DISTANCE);
				distanceTravelled = distance;
				endPoint = new Vector3(hit.hitVec);

				if (energy <= 0)
				{
					break;
				}

				if (resistance >= Block.obsidian.blockResistance)
				{
					worldObj.newExplosion(null, hit.blockX, hit.blockY, hit.blockZ, 4F * (2 + (energy / 500000)), true, true);
					worldObj.setBlock(hit.blockX, hit.blockY, hit.blockZ, (worldObj.rand.nextBoolean()) ? Block.fire.blockID : 0);
				}
				else
				{
					worldObj.destroyBlock(hit.blockX, hit.blockY, hit.blockZ, (worldObj.rand.nextInt(20) == 0));
				}
			}
		}

		sendLaserPacket(beamVector, endPoint, r, g, b, 50, energy, beamLengthBlocks);
	}

	public MovingObjectPosition raytraceEntities(Vector3 beamVec, Vector3 lookVec, boolean collisionFlag, double reachDistance)
	{
		MovingObjectPosition pickedEntity = null;
		Vec3 playerPosition = beamVec.toVec3();
		Vec3 playerLook = lookVec.toVec3();
		Vec3 playerViewOffset = Vec3.createVectorHelper(playerPosition.xCoord + playerLook.xCoord * reachDistance, playerPosition.yCoord
								+ playerLook.yCoord * reachDistance, playerPosition.zCoord + playerLook.zCoord * reachDistance);
		double playerBorder = 1.1 * reachDistance;
		AxisAlignedBB boxToScan = WarpDrive.laserBlock.getCollisionBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord).expand(playerBorder, playerBorder, playerBorder);
		List entitiesHit = worldObj.getEntitiesWithinAABBExcludingEntity(null, boxToScan);
		double closestEntity = reachDistance;

		if (entitiesHit == null || entitiesHit.isEmpty())
		{
			return null;
		}

		for (Entity entityHit : (Iterable<Entity>) entitiesHit)
		{
			if (entityHit != null && entityHit.canBeCollidedWith() && entityHit.boundingBox != null)
			{
				float border = entityHit.getCollisionBorderSize();
				AxisAlignedBB aabb = entityHit.boundingBox.expand((double) border, (double) border, (double) border);
				MovingObjectPosition hitMOP = aabb.calculateIntercept(playerPosition, playerViewOffset);

				if (hitMOP != null)
				{
					if (aabb.isVecInside(playerPosition))
					{
						if (0.0D < closestEntity || closestEntity == 0.0D)
						{
							pickedEntity = new MovingObjectPosition(entityHit);

							if (pickedEntity != null)
							{
								pickedEntity.hitVec = hitMOP.hitVec;
								closestEntity = 0.0D;
							}
						}
					}
					else
					{
						double distance = playerPosition.distanceTo(hitMOP.hitVec);

						if (distance < closestEntity || closestEntity == 0.0D)
						{
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

	public boolean isWithCamera()
	{
		return (worldObj.getBlockId(xCoord, yCoord, zCoord) == WarpDriveConfig.i.laserCamID);
	}

	public int getFrequency()
	{
		return frequency;
	}

	private TileEntityParticleBooster findFirstBooster()
	{
		TileEntity result;
		result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = 1;
			dz = 0;
			dy = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = -1;
			dz = 0;
			dy = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = 0;
			dz = 1;
			dy = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = 0;
			dz = -1;
			dy = 0;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = 0;
			dz = 0;
			dy = 1;
			return (TileEntityParticleBooster) result;
		}

		result = worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);

		if (result != null && result instanceof TileEntityParticleBooster)
		{
			dx = 0;
			dz = 0;
			dy = -1;
			return (TileEntityParticleBooster) result;
		}

		return null;
	}

	public void sendLaserPacket(Vector3 source, Vector3 dest, float r, float g, float b, int age, int energy, int radius)
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();

		if (side == Side.SERVER)
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
			DataOutputStream outputStream = new DataOutputStream(bos);

			try
			{
				// Write source vector
				outputStream.writeDouble(source.x);
				outputStream.writeDouble(source.y);
				outputStream.writeDouble(source.z);
				// Write target vector
				outputStream.writeDouble(dest.x);
				outputStream.writeDouble(dest.y);
				outputStream.writeDouble(dest.z);
				// Write r, g, b of laser
				outputStream.writeFloat(r);
				outputStream.writeFloat(g);
				outputStream.writeFloat(b);
				// Write age
				outputStream.writeByte(age);
				// Write energy value
				outputStream.writeInt(energy);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = "WarpDriveBeam";
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(source.intX(), source.intY(), source.intZ(), radius, worldObj.provider.dimensionId, packet);
			ByteArrayOutputStream bos2 = new ByteArrayOutputStream(8);
			DataOutputStream outputStream2 = new DataOutputStream(bos2);

			try
			{
				// Write source vector
				outputStream2.writeDouble(source.x);
				outputStream2.writeDouble(source.y);
				outputStream2.writeDouble(source.z);
				// Write target vector
				outputStream2.writeDouble(dest.x);
				outputStream2.writeDouble(dest.y);
				outputStream2.writeDouble(dest.z);
				// Write r, g, b of laser
				outputStream2.writeFloat(r);
				outputStream2.writeFloat(g);
				outputStream2.writeFloat(b);
				// Write age
				outputStream2.writeByte(age);
				// Write energy value
				outputStream2.writeInt(energy);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			Packet250CustomPayload packet2 = new Packet250CustomPayload();
			packet.channel = "WarpDriveBeam";
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(dest.intX(), dest.intY(), dest.intZ(), radius, worldObj.provider.dimensionId, packet);
		}
	}

	private void playSoundCorrespondsEnergy(int energy)
	{
		if (energy <= 500000)
		{
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
		}
		else if (energy > 500000 && energy <= 1000000)
		{
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:midlaser", 4F, 1F);
		}
		else if (energy > 1000000)
		{
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
		}
	}

	private boolean parseFrequency(int freq)
	{
		if (freq > 65000 || freq <= 0)   // Invalid frequency
		{
			r = 1;
			g = 0;
			b = 0;
			return false;
		}

		if (freq > 0 && freq <= 10000)	   // red
		{
			r = 1;
			g = 0;
			b = 0;
		}
		else if (freq > 10000 && freq <= 20000)	// orange
		{
			r = 1;
			g = 0;
			b = 0.5f;
		}
		else if (freq > 20000 && freq <= 30000)	// yellow
		{
			r = 1;
			g = 1;
			b = 0;
		}
		else if (freq > 30000 && freq <= 40000)	// green
		{
			r = 0;
			g = 1;
			b = 0;
		}
		else if (freq > 50000 && freq <= 60000)	// blue
		{
			r = 0;
			g = 0;
			b = 1;
		}
		else if (freq > 60000 && freq <= 65000)	// violet
		{
			r = 0.5f;
			g = 0;
			b = 0.5f;
		}
		else	 // impossible frequency
		{
			r = 1;
			g = 0;
			b = 0;
		}

		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		frequency = tag.getInteger("frequency");
		camFreq = tag.getInteger("camfreq");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setInteger("frequency", frequency);
		tag.setInteger("camFreq", camFreq);
	}

	// IPeripheral methods implementation
	@Override
	public String getType()
	{
		return "laser";
	}

	@Override
	public String[] getMethodNames()
	{
		return methodsArray;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		switch (method)
		{
			case 0: // emitBeam(yaw, pitch)
				// emitBeam(dx, dy, dz)
				if (arguments.length == 2)
				{
					yaw = ((Double)arguments[0]).floatValue();
					pitch = ((Double)arguments[1]).floatValue();
					isEmitting = true;
					delayTicks = 0;
				}
				else if (arguments.length == 3)
				{
					double dx = (Double)arguments[0];
					double dy = (Double)arguments[1];
					double dz = (Double)arguments[2];
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
				if (arguments.length == 1)
				{
					if (parseFrequency(((Double)arguments[0]).intValue()))
						frequency = ((Double)arguments[0]).intValue();
					else
						return new Integer[] { -1 };
				}
				return new Integer[] { frequency };
			case 3: // getFirstHit()
				if (firstHit != null)
				{
					int blockID = worldObj.getBlockId(firstHit.blockX, firstHit.blockY, firstHit.blockZ);
					int blockMeta = worldObj.getBlockMetadata(firstHit.blockX, firstHit.blockY, firstHit.blockZ);
					float blockResistance = Block.blocksList[blockID].blockResistance;
					Object[] info = { firstHit.blockX, firstHit.blockY, firstHit.blockZ, blockID, blockMeta, (Float)blockResistance };
					firstHit = null;
					return info;
				}
				else
					return new Integer[] { 0, 0, 0, 0, 0, -1 };
			case 4: // getBoosterDXDZ
				findFirstBooster();
				return new Integer[] { dx, dz };

			case 5: // CamFreq (only for lasers with cam)
				if (isWithCamera())
				{
					if (arguments.length == 1)
						camFreq = ((Double)arguments[0]).intValue();
					return new Integer[] { camFreq };
				}
				break;
		}
		return null;
	}

	// Camera frequency refresh to clients packet
	public void sendFreqPacket()
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();

		if (side == Side.SERVER)
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
			DataOutputStream outputStream = new DataOutputStream(bos);

			try
			{
				// Write source vector
				outputStream.writeInt(xCoord);
				outputStream.writeInt(yCoord);
				outputStream.writeInt(zCoord);
				outputStream.writeInt(this.camFreq);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = "WarpDriveFreq";
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			MinecraftServer.getServer().getConfigurationManager().sendToAllNear(xCoord, yCoord, zCoord, 100, worldObj.provider.dimensionId, packet);
		}
	}

	@Override
	public boolean canAttachToSide(int side)
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
	}

	@Override
	public void detach(IComputerAccess computer)
	{
	}
}
