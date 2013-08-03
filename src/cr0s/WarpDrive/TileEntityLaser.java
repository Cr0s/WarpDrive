package cr0s.WarpDrive;

import cr0s.WarpDrive.TileEntityReactor;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.WarpDrive;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.item.Items;

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
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityLaser extends TileEntity implements IPeripheral{
	private final int MAX_BOOSTERS_NUMBER = 10;
	private final int MAX_LASER_ENERGY = 2000000;
	
	private int dx, dz, dy;
	private float yaw, pitch; // laser direction
	
	private int frequency;    // beam frequency
	private float r, g, b;      // beam color (corresponds to frequency)
	
	private boolean isEmitting = false;
	
    private String[] methodsArray = { 
                                        "emitBeam",             // 0
                                        "getX", "getY", "getZ", // 1, 2, 3
                                        "setFrequency",         // 4
                                        "getFirstHit"           // 5
                                    };
    
   private int delayTicks = 0;
   private int energyFromOtherBeams = 0;
   
   private MovingObjectPosition firstHit;

    @Override
    public void updateEntity() {
    	if (isEmitting && ++delayTicks > 20 * 3) {
    		delayTicks = 0;
			isEmitting = false;
			emitBeam(Math.min(this.collectEnergyFromBoosters() + MathHelper.floor_double(energyFromOtherBeams * 0.60D), MAX_LASER_ENERGY));
			energyFromOtherBeams = 0;
    	}
    }
    
    public void addBeamEnergy(int amount) {
    	if (isEmitting) {
    		energyFromOtherBeams += amount; 
    		System.out.println("[EL] Added energy: " + amount);
    	} else {
    		System.out.println("[EL] Ignored energy: " + amount);
    	}
    }
    
    private int collectEnergyFromBoosters() {
    	int energyCollected = 0;
    	
    	if (findFirstBooster() != null) {
    		for (int shift = 1; shift <= MAX_BOOSTERS_NUMBER; shift++) {
    			int newX = xCoord + (dx * shift);
    			int newY = yCoord + (dy * shift);
    			int newZ = zCoord + (dz * shift);
    			
    			TileEntity te = worldObj.getBlockTileEntity(newX, newY, newZ);
    			
    			if (te != null && te instanceof TileEntityParticleBooster) {
    				energyCollected += ((TileEntityParticleBooster)te).collectAllEnergy();
    			} else {
    				break;
    			}
    		}
    	}
    	
    	return energyCollected;
    }
    
    // TODO refactor me
    private void emitBeam(int energy) {
    	// Beam power calculations
    	int beamLengthBlocks = energy / 5000;
    	
    	System.out.println("Energy: " + energy + " | beamLengthBlocks: " + beamLengthBlocks);
    	
    	if (energy == 0 || beamLengthBlocks < 1) {
    		return;
    	}
    	
    	Vector3 beamVector = new Vector3(this).add(0.5);
    	System.out.println("beamVector: " + beamVector);
		Vector3 lookVector = beamVector.clone().getDeltaPositionFromRotation(yaw, pitch);
		Vector3 reachPoint = beamVector.clone().translate(beamVector.clone(), beamVector.clone().scale(lookVector.clone(), beamLengthBlocks));
		System.out.println("Look vector: " + lookVector);
		System.out.println("reachPoint: " + reachPoint);
		
		Vector3.translate(beamVector, lookVector);
    	System.out.println("translatedBeamVector: " + beamVector);
    	Vector3 endPoint = reachPoint.clone();
    	
    	playSoundCorrespondsEnergy(energy);
    	
    	// This is scanning beam, do not deal damage to blocks
    	if (frequency == 1420) {
    		firstHit = worldObj.rayTraceBlocks_do_do(beamVector.toVec3(), reachPoint.toVec3(), false, false);
    		if (firstHit != null)
    			sendLaserPacket(beamVector, new Vector3(firstHit), r, g, b, 50, energy, 200); 
    		
    		return;
    	}
    	
    	for (int passedBlocks = 0; passedBlocks < beamLengthBlocks; ++passedBlocks) {
			// Get next block hit
    		MovingObjectPosition hit = worldObj.rayTraceBlocks_do_do(beamVector.toVec3(), reachPoint.toVec3(), true, false);
    		
    		// FIXME entity ray-tracing
			MovingObjectPosition entityHit = rayTraceEntities(beamVector.clone(), reachPoint.clone().toVec3(), true, beamLengthBlocks);
	    	if (entityHit != null && entityHit.entityHit instanceof EntityLivingBase) {
	    		if (hit != null && hit.hitVec.distanceTo(beamVector.clone().toVec3()) > entityHit.hitVec.distanceTo(beamVector.clone().toVec3())) {
	    			((EntityLivingBase)entityHit.entityHit).setFire(100);
	    			((EntityLivingBase)entityHit.entityHit).attackEntityFrom(DamageSource.inFire, 10);
	    			endPoint = new Vector3(entityHit);
	    			break;
	    		} else if (hit == null) {
	    			if (entityHit.hitVec.distanceTo(beamVector.clone().toVec3()) <= beamLengthBlocks) {
		    			((EntityLivingBase)entityHit.entityHit).setFire(100);
		    			((EntityLivingBase)entityHit.entityHit).attackEntityFrom(DamageSource.inFire, 10);
		    			endPoint = new Vector3(entityHit);
		    			break;
	    			}
	    		}
	    	}
	    	
			if (hit == null) {
				// Laser is missed
				endPoint = reachPoint;	
				break;
			} else {
				// We got a hit
				int distance = (int) new Vector3(hit.hitVec).distanceTo(beamVector);
				
				// Laser gone too far
				if (distance >= beamLengthBlocks) {
					break;
				}
				int blockID = worldObj.getBlockId(hit.blockX, hit.blockY, hit.blockZ);
				int blockMeta = worldObj.getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ);
				float resistance = Block.blocksList[blockID].blockResistance;
				
				if (blockID == Block.bedrock.blockID) {
					endPoint = new Vector3(hit.hitVec);	
					break;
				}
				
				// Hit is a laser head
				if (blockID == WarpDrive.instance.LASER_BLOCK_BLOCKID) {
					// Compare frequencies
					TileEntityLaser tel = (TileEntityLaser)worldObj.getBlockTileEntity(hit.blockX, hit.blockY, hit.blockZ);
					if (tel != null && tel.getFrequency() == frequency) {
						tel.addBeamEnergy(energy);
						endPoint = new Vector3(hit.hitVec);	
						break;
					}
				}
				
				if (Block.blocksList[blockID].blockMaterial == Material.glass) {
					worldObj.setBlockToAir(hit.blockX, hit.blockY, hit.blockZ);
					endPoint = new Vector3(hit.hitVec);	
				}
				
				energy -=  100000 + (resistance * 2000) + (distance * 100);
				endPoint = new Vector3(hit.hitVec);	
				
				if (energy <= 0) {
					break;
				}
				
				
				if (resistance >= Block.obsidian.blockResistance) {
					worldObj.newExplosion(null, hit.blockX, hit.blockY, hit.blockZ, 4F * (2 + (energy / 500000)), true, true);
					worldObj.setBlock(hit.blockX, hit.blockY, hit.blockZ, (worldObj.rand.nextBoolean()) ? Block.fire.blockID : 0);
				} else{
					worldObj.setBlockToAir(hit.blockX, hit.blockY, hit.blockZ);
				}
			}
    	}
    	
		sendLaserPacket(beamVector, endPoint, r, g, b, 50, energy, beamLengthBlocks);    	
    }
      
	public MovingObjectPosition rayTraceEntities(Vector3 beamVector, Vec3 reachPoint, boolean collisionFlag, double reachDistance)
	{
		MovingObjectPosition pickedEntity = null;
		Vec3 startingPosition = beamVector.toVec3();
		double playerBorder = 1.1 * reachDistance;
		AxisAlignedBB boxToScan = AxisAlignedBB.getAABBPool().getAABB(-playerBorder, -playerBorder, -playerBorder, playerBorder, playerBorder, playerBorder);

		@SuppressWarnings("unchecked")
		List<Entity> entitiesHit = worldObj.getEntitiesWithinAABBExcludingEntity(null, boxToScan);
		double closestEntity = reachDistance;

		if (entitiesHit == null || entitiesHit.isEmpty())
		{
			System.out.println("There is no Entitys hits.");
			return null;
		}
		for (Entity entityHit : entitiesHit)
		{
			if (entityHit != null && entityHit.canBeCollidedWith() && entityHit.boundingBox != null)
			{
				float border = entityHit.getCollisionBorderSize();
				AxisAlignedBB aabb = entityHit.boundingBox.expand(border, border, border);
				MovingObjectPosition hitMOP = aabb.calculateIntercept(startingPosition, reachPoint);

				if (hitMOP != null)
				{
					if (aabb.isVecInside(startingPosition))
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
						double distance = startingPosition.distanceTo(hitMOP.hitVec);

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
    
    public int getFrequency() {
    	return frequency;
    }
    
    private TileEntityParticleBooster findFirstBooster() {
        TileEntity result;

        result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
        if (result != null && result instanceof TileEntityParticleBooster) {
            dx = 1;
            dz = 0;
            dy = 0;
            return (TileEntityParticleBooster) result;
        }

        result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
        if (result != null && result instanceof TileEntityParticleBooster) {
            dx = -1;
            dz = 0;
            dy = 0;
            return (TileEntityParticleBooster) result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
        if (result != null && result instanceof TileEntityParticleBooster) {
        	dx = 0;
            dz = 1;
            dy = 0;
            return (TileEntityParticleBooster) result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
        if (result != null && result instanceof TileEntityParticleBooster) {
            dx = 0;
            dz = -1;
            dy = 0;
            return (TileEntityParticleBooster) result;
        }
        
        result = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
        if (result != null && result instanceof TileEntityParticleBooster) {
        	dx = 0;
            dz = 0;
            dy = 1;
            return (TileEntityParticleBooster) result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);
        if (result != null && result instanceof TileEntityParticleBooster) {
            dx = 0;
            dz = 0;
            dy = -1;
            return (TileEntityParticleBooster) result;
        }        

        return null;
    }
    
    public void sendLaserPacket(Vector3 source, Vector3 dest, float r, float g, float b, int age, int energy, int radius) {              
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.SERVER) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
            DataOutputStream outputStream = new DataOutputStream(bos);
            try {
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
            } catch (Exception ex) {
                    ex.printStackTrace();
            }
            
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "WarpDriveBeam";
            packet.data = bos.toByteArray();
            packet.length = bos.size();
        	MinecraftServer.getServer().getConfigurationManager().sendToAllNear(source.intX(), source.intY(), source.intZ(), radius, worldObj.provider.dimensionId, packet);	
        	
        	ByteArrayOutputStream bos2 = new ByteArrayOutputStream(8);
            DataOutputStream outputStream2 = new DataOutputStream(bos2);
            try {
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
            } catch (Exception ex) {
                    ex.printStackTrace();
            }
            
            Packet250CustomPayload packet2 = new Packet250CustomPayload();
            packet.channel = "WarpDriveBeam";
            packet.data = bos.toByteArray();
            packet.length = bos.size();
        	MinecraftServer.getServer().getConfigurationManager().sendToAllNear(dest.intX(), dest.intY(), dest.intZ(), radius, worldObj.provider.dimensionId, packet);	
        }	
    }
    
    private void playSoundCorrespondsEnergy(int energy) {
    	if (energy <= 500000) {
    		worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
    	} else 
    	if (energy > 500000 && energy <= 1000000) {
    		worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:midlaser", 4F, 1F);
    	} else if (energy > 1000000){
    		worldObj.playSoundEffect(xCoord + 0.5f, yCoord - 0.5f, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
    	}
    }
    
    private boolean parseFrequency(int freq) {
    	if (freq > 65000 || freq < 0) { // Invalid frequency
    		r = 1; g = 0; b = 0;
    		return false;
    	}
    	
    	if (freq > 0 && freq < 10000) {     // red
    		r = 1; g = 0; b = 0;
    	} else
    	if (freq > 10000 && freq <= 20000) { // orange
    		r = 1; g = 0; b = 0.5f;
    	} else
    	if (freq > 20000 && freq <= 30000) { // yellow
    		r = 1; g = 1; b = 0;
    	} else
    	if (freq > 30000 && freq <= 40000) { // green
    		r = 0; g = 1; b = 0;
    	} else
    	if (freq > 50000 && freq <= 60000) { // blue
    		r = 0; g = 0; b = 1;
    	} else
    	if (freq > 60000 && freq <= 65000) { // violet
    		r = 0.5f; g = 0; b = 0.5f;
    	} else { // impossible frequency
    		r = 1; g = 0; b = 0;
    	}
    	
    	return true;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        
        frequency = tag.getInteger("frequency");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        
        tag.setInteger("frequency", frequency);
    }    
    
    // IPeripheral methods implementation
    @Override
    public String getType() {
        return "laser";
    }

    @Override
    public String[] getMethodNames() {
        return methodsArray;
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
        switch (method) {
            case 0: // emitBeam(yaw, pitch)
            		// emitBeam(dx, dy, dz)

                if (arguments.length == 2) {
                	yaw = ((Double)arguments[0]).floatValue();
                	pitch = ((Double)arguments[1]).floatValue();
                	
                	isEmitting = true;
                	delayTicks = 0;
                } else
                if (arguments.length == 3) {
                	double dx = (Double)arguments[0];
                	double dy = (Double)arguments[1];
                	double dz = (Double)arguments[2];
                	
                	double targetX = xCoord + dx;
                	double targetY = yCoord + dy;
                	double targetZ = zCoord + dz;
                	
            		float xd = (float) (xCoord - targetX);
            		float yd = (float) (yCoord - targetY);
            		float zd = (float) (zCoord - targetZ);

            		double var7 = MathHelper.sqrt_double(xd * xd + zd * zd);

            		yaw = ((float) (Math.atan2(xd, zd) * 180.0D / 3.141592653589793D));
            		pitch = ((float) (Math.atan2(yd, var7) * 180.0D / 3.141592653589793D));
            		
                	isEmitting = true;
                	delayTicks = 0;            		
                }
                
                return new Object[] { 0 };
                                
            case 1: // getX
                return new Integer[] { this.xCoord };
                
            case 2: // getY
                return new Integer[] { this.yCoord };
                
            case 3: // getZ
                return new Integer[] { this.zCoord };
                
            case 4: // setFrequency(freq)
            	if (arguments.length == 1) {
            		int freq = ((Double)arguments[0]).intValue();
            		this.frequency = freq;
            		if (parseFrequency(freq)) {
            			return new Object[] { 0 };
            		} else {
            			return new Object[] { -1 };
            		}
            	}
            	
            case 5: // getFirstHit()
            	if (firstHit != null) {
            		int blockID = worldObj.getBlockId(firstHit.blockX, firstHit.blockY, firstHit.blockZ);
            		int blockMeta = worldObj.getBlockMetadata(firstHit.blockX, firstHit.blockY, firstHit.blockZ);
            		float blockResistance = Block.blocksList[blockID].blockResistance;
            		
            		Object[] info = { (Integer)firstHit.blockX, (Integer)firstHit.blockY, (Integer)firstHit.blockZ, blockID, blockMeta, (Float)blockResistance };
            		firstHit = null;
            		
            		return info;
            	} else {
            		return new Object[] { -1 };
            	}
                
        }
        
        return new Object[] { 0 };
    }

    @Override
    public boolean canAttachToSide(int side) {
        return true;
    }

    @Override
    public void attach(IComputerAccess computer) {

    }

    @Override
    public void detach(IComputerAccess computer) {

    }
}
