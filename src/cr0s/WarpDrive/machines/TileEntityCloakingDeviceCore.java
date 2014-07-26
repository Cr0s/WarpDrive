package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.WarpDrive.CloakManager.CloakedArea;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.ForgeDirection;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.entity.player.EntityPlayerMP;
import cr0s.WarpDrive.*;

public class TileEntityCloakingDeviceCore extends WarpEnergyTE implements IPeripheral {
	private final int MAX_ENERGY_VALUE = 500000000; // 500kk EU
	
	private String[] methodsArray = { "setFieldTier", // 0 setFieldTier(1 or 2)
			"isAssemblyValid", // 1 - returns true or false
			"getEnergyLevel", // 2 
			"enableCloakingField", // 3 enables field if assembled right
			"disableCloakingField", // 4 disables cloaking field
			"setFieldFrequency" // 5 setFieldFrequency(int)
	};
	
	public boolean isEnabled = false;
	public byte tier = 1; // cloaking field tier, 1 or 2
//	public int frequency = 0;
	
	// Spatial cloaking field parameters
	public int front, back, up, down, left, right;
	public int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
	
	public boolean isValid = false;
	public boolean isCloaking = false;
	public int volume = 0;
	private int updateTicks = 0;
	private int laserDrawingTicks = 0;
	
	private boolean soundPlayed = false;
	private int soundTicks = 0;
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			return;
		}

		// Reset sound timer
		soundTicks++;
		if (soundTicks >= 40) {
			soundTicks = 0;
			soundPlayed = false;
		}
		
		updateTicks--;
		if (updateTicks <= 0) {
			//System.out.println("" + this + " Updating cloaking state...");
			updateTicks = ((tier == 1) ? 20 : (tier == 2) ? 10 : 20) * WarpDriveConfig.CD_FIELD_REFRESH_INTERVAL_SECONDS; // resetting timer
			
			isValid = validateAssembly();
			isCloaking = WarpDrive.instance.cloaks.isAreaExists(worldObj, xCoord, yCoord, zCoord); 
			if (!isEnabled) {// disabled
				if (isCloaking) {// disabled, cloaking => stop cloaking
					WarpDrive.debugPrint("" + this + " Disabled, cloak field going down...");
					disableCloakingField();
				} else {// disabled, no cloaking
					// IDLE
				}
			} else {// isEnabled
				boolean hasEnoughPower = countBlocksAndConsumeEnergy();
				if (!isCloaking) {// enabled, not cloaking
					if (hasEnoughPower && isValid) {// enabled, can cloak and able to
						setCoilsState(true);
						
						// Register cloak
						WarpDrive.instance.cloaks.addCloakedAreaWorld(worldObj, minX, minY, minZ, maxX, maxY, maxZ, xCoord, yCoord, zCoord, tier);
						if (!soundPlayed) {
							soundPlayed = true;
							worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:cloak", 4F, 1F);
						}
						
						// Refresh the field
						CloakedArea area = WarpDrive.instance.cloaks.getCloakedArea(worldObj, xCoord, yCoord, zCoord);
						if (area != null) {
							area.sendCloakPacketToPlayersEx(false); // recloak field
						}
					} else {// enabled, not cloaking but not able to
						// IDLE
					}
				} else {// enabled & cloaked
					if (!isValid) {// enabled, cloaking but invalid
						WarpDrive.debugPrint("" + this + " Coil(s) lost, cloak field is collapsing...");
						consumeAllEnergy();
						disableCloakingField();				
					} else {// enabled, cloaking and valid
						if (hasEnoughPower) {// enabled, cloaking and able to
							// IDLE
							// Refresh the field	!!! LemTest 2014-07-12
							CloakedArea area = WarpDrive.instance.cloaks.getCloakedArea(worldObj, xCoord, yCoord, zCoord);
							if (area != null) {
								area.sendCloakPacketToPlayersEx(false); // recloak field
							}
						} else {// loosing power
							WarpDrive.debugPrint("" + this + " Low power, cloak field is collapsing...");
							disableCloakingField();
						}
					}
				}
			}
		}
		
		if (laserDrawingTicks++ > 100) {
			laserDrawingTicks = 0;
			
			if (isEnabled && isValid) {
				drawLasers();
			}
		}
	}
	
	public void setCoilsState(boolean enabled) {
		final int START_LENGTH = 2; // Step length from core block to main coils
		
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (enabled) ? 1 : 0, 2);
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		byte[] dx = { 1, -1,  0,  0,  0,  0 };
		byte[] dy = { 0,  0, -1,  1,  0,  0 };
		byte[] dz = { 0,  0,  0,  0, -1,  1 };
		
		for (int i = 0; i < 6; i++) {
			searchCoilInDirectionAndSetState(dx[i], dy[i], dz[i], enabled);
		}
	}
	
	public void searchCoilInDirectionAndSetState(byte dx, byte dy, byte dz, boolean enabled) {
		int coilCount = 0;
		for (int i = 0; i < WarpDriveConfig.CD_MAX_CLOAKING_FIELD_SIDE; i++) {
			if (worldObj.getBlockId(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz) == WarpDriveConfig.cloakCoilID) {
				coilCount++;
				if (coilCount > 2) {
					return;
				}
				worldObj.setBlockMetadataWithNotify(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz, (enabled) ? 1 : 0, 2);
			}
		}
	}	
	
	public void searchCoilInDirectionAndDrawLaser(byte dx, byte dy, byte dz) {
		final int START_LENGTH = 2;
		float r = 0.0f, g = 1.0f, b = 0;
		if (tier == 1) {
			r = 0.0f;
			g = 1.0f; 
		} else if (tier == 2) {
			r = 1.0f;
			g = 0.0f;
		}
		
		for (int i = START_LENGTH + 1; i < WarpDriveConfig.CD_MAX_CLOAKING_FIELD_SIDE; i++) {
			if (worldObj.getBlockId(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz) == WarpDriveConfig.cloakCoilID) {
				sendLaserPacket(new Vector3(this).add(0.5), new Vector3(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz).add(0.5), r, g, b, 110, 0, 100);
			}
		}
	}	
	
	public void drawLasers() {
		final int START_LENGTH = 2;
		float r = 0.0f, g = 1.0f, b = 0;
		if (this.tier == 1) {
			r = 0.0f; g = 1.0f; 
		} else if (this.tier == 2) {
			r = 1.0f; g = 0.0f;
		}
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		byte[] dx = { 1, -1,  0,  0,  0,  0 };
		byte[] dy = { 0,  0, -1,  1,  0,  0 };
		byte[] dz = { 0,  0,  0,  0, -1,  1 };
		
		for (int k = 0; k < 6; k++) {
			searchCoilInDirectionAndDrawLaser(dx[k], dy[k], dz[k]);
		}
		
		for (int i = 0; i < 6; i++) {		
			for (int j = 0; j < 6; j++) {
				switch (worldObj.rand.nextInt(6)) {
					case 0:
						r = 1.0f;
						g = b = 0;
						break;
					case 1:
						r = b = 0;
						g = 1.0f;
						break;
					case 2:
						r = g = 0;
						b = 1.0f;
						break;
					case 3:
						r = b = 0.5f;
						g = 0;
						break;
					case 4:
						r = g = 1.0f;
						b = 0;
						break;
					case 5:
						r = 1.0f; 
						b = 0.5f;
						g = 0f;
				}
				
				sendLaserPacket(new Vector3(xCoord + START_LENGTH * dx[i], yCoord + START_LENGTH * dy[i], zCoord + START_LENGTH * dz[i]).add(0.5), new Vector3(xCoord + START_LENGTH * dx[j], yCoord + START_LENGTH * dy[j], zCoord + START_LENGTH * dz[j]).add(0.5), r, g, b, 110, 0, 100);
			}
		}
	}

	public void disableCloakingField() {
		setCoilsState(false);
		if (WarpDrive.instance.cloaks.isAreaExists(worldObj, xCoord, yCoord, zCoord)) {
			WarpDrive.instance.cloaks.removeCloakedArea(worldObj, xCoord, yCoord, zCoord);
			
			if (!soundPlayed) {
				soundPlayed = true;
				worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:decloak", 4F, 1F);
			}
		}
	}
	public boolean countBlocksAndConsumeEnergy() {
		int x, y, z, energyToConsume = 0;
		volume = 0;
		if (tier == 1) {// tier1 = gaz and air blocks don't count
			for (y = minY; y <= maxY; y++) {
				for (x = minX; x <= maxX; x++) {
					for(z = minZ; z <= maxZ; z++) {
						if (!worldObj.isAirBlock(x, y, z)) {
							volume++;
						} 
					}
				}
			}
			energyToConsume = volume * WarpDriveConfig.CD_ENERGY_PER_BLOCK_TIER1;
		} else {// tier2 = everything counts
			for (y = minY; y <= maxY; y++) {
				for (x = minX; x <= maxX; x++) {
					for(z = minZ; z <= maxZ; z++) {
						if (worldObj.getBlockId(x, y, z) != 0) {
							volume++;
						} 
					}
				}
			}
			energyToConsume = volume * WarpDriveConfig.CD_ENERGY_PER_BLOCK_TIER2;
		}
		
		//System.out.println("" + this + " Consuming " + energyToConsume + " eU for " + blocksCount + " blocks");
		return consumeEnergy(energyToConsume, false);
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
			
			// Send packet to all players within cloaked area
			List<Entity> list = worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
			for (Entity e : list) {
				if (e != null && e instanceof EntityPlayer) {
					((EntityPlayerMP)e).playerNetServerHandler.sendPacketToPlayer(packet);
				}
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.tier = tag.getByte("tier");
//		this.frequency = tag.getInteger("frequency");
		this.isEnabled = tag.getBoolean("enabled");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("tier", tier);
//		tag.setInteger("frequency", frequency);
		tag.setBoolean("enabled", isEnabled);
	}

	public int searchCoilInDirection(byte dx, byte dy, byte dz) {
		for (int i = 3; i < WarpDriveConfig.CD_MAX_CLOAKING_FIELD_SIDE; i++) {
			if (worldObj.getBlockId(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz) == WarpDriveConfig.cloakCoilID) {
				return i;
			}
		}
		
		return 0;
	}
	public boolean validateAssembly() {
		final int START_LENGTH = 2; // Step length from core block to main coils
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		byte[] dx = { 1, -1,  0,  0,  0,  0 };
		byte[] dy = { 0,  0, -1,  1,  0,  0 };
		byte[] dz = { 0,  0,  0,  0, -1,  1 };
		
		for (int i = 0; i < 6; i++) {
			if (worldObj.getBlockId(xCoord + START_LENGTH * dx[i], yCoord + START_LENGTH * dy[i], zCoord + START_LENGTH * dz[i]) != WarpDriveConfig.cloakCoilID) {
				return false;
			}
		}
		
		// Check cloaking field parameters defining coils		
		this.left = searchCoilInDirection((byte)1, (byte)0, (byte)0)   + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.left == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS) return false;
		this.right = searchCoilInDirection((byte)-1, (byte)0, (byte)0) + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.right == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS) return false;		
		
		this.up = searchCoilInDirection((byte)0, (byte)1, (byte)0)     + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.up == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS) return false; 
		this.down = searchCoilInDirection((byte)0, (byte)-1, (byte)0)  + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.down == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS) return false;
				
		this.front = searchCoilInDirection((byte)0, (byte)0, (byte)1)  + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.front == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS) return false;
		this.back = searchCoilInDirection((byte)0, (byte)0, (byte)-1)  + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.back == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS) return false;
		
        int x1 = 0, x2 = 0, z1 = 0, z2 = 0;


        z1 = zCoord - this.back;
        z2 = zCoord + this.front;
        x1 = xCoord - this.right;
        x2 = xCoord + this.left;

        if (x1 < x2) {
        	this.minX = x1; this.maxX = x2;
        }
        else {
        	this.minX = x2; this.maxX = x1;
        }

        if (z1 < z2) {
        	this.minZ = z1; this.maxZ = z2;
        }
        else {
        	this.minZ = z2; this.maxZ = z1;
        }		
		
        this.minY = yCoord - this.down;
        this.maxY = yCoord + this.up;
        
		return true;
	}

	// CC
	// IPeripheral methods implementation
	@Override
	public String getType() {
		return "cloakingdevicecore";
	}

	@Override
	public String[] getMethodNames() {
		return methodsArray;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
		switch (method) {
		case 0: // setFieldTier(1 or 2)
			if (arguments.length == 1) {
				if (((Double)arguments[0]).byteValue() != 1 && ((Double)arguments[0]).byteValue() != 2) {
					this.tier = 1;
				} else {
					this.tier = ((Double)arguments[0]).byteValue();
				}
			}
			break;
			
		case 1: // isAssemblyValid()
			return new Object[] { (boolean)validateAssembly() };

		case 2: // getEnergyLevel()
			return new Object[] { getEnergyStored() };
			
		case 3: // enableCloakingField()
			this.isEnabled = true;
			break;
			
		case 4: // disableCloakingField()
			this.isEnabled = false;
			break;
			
		case 5: // setFieldFrequency(int)
			if (arguments.length == 1) {
				disableCloakingField();
				
				if (WarpDrive.instance.cloaks.isAreaExists(worldObj, xCoord, yCoord, zCoord)) { // ((Double)arguments[0]).intValue())) {
					return new Object[] { (Boolean)false };
				}
				
//				this.frequency = ((Double)arguments[0]).intValue();
				return new Object[] { (Boolean)true };
			}		
			break;
		}
		
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {
	    computer.mount("/cloakingdevicecore", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/cloakingdevicecore"));
        computer.mount("/uncloak", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/cloakingdevicecore/uncloak"));
        computer.mount("/cloak1", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/cloakingdevicecore/cloak1"));
        computer.mount("/cloak2", ComputerCraftAPI.createResourceMount(WarpDrive.class, "warpdrive", "lua/cloakingdevicecore/cloak2"));
	}

	@Override
	public void detach(IComputerAccess computer) {
	}
	
	@Override
	public int getMaxEnergyStored() {
		return MAX_ENERGY_VALUE;
	}
	
	@Override
	public int getMaxSafeInput() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean equals(IPeripheral other) {
		// TODO Auto-generated method stub
		return false;
	}
}
