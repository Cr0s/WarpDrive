package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import net.minecraftforge.common.ForgeDirection;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;

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

public class TileEntityCloakingDeviceCore extends TileEntity implements IEnergySink,
		IPeripheral {
	public boolean addedToEnergyNet = false;

	private final int MAX_ENERGY_VALUE = 500000000; // 500kk eU
	private int currentEnergyValue = 0;
	
	private String[] methodsArray = { "setFieldTier", // 0 setFieldTier(1 or 2)
			"isAssemblyValid", // 1 - returns true or false
			"getEnergyLevel", // 2 
			"enableCloakingField", // 3 enables field if assembled right
			"disableCloakingField", // 4 disables cloaking field
			"setFieldFrequency" // 5 setFieldFrequency(int)
	};
	
	public boolean isEnabled = false;
	public byte tier = 1; // cloaking field tier, 1 or 2
	public int frequency = 0;
	
	// Spatial cloaking field parameters
	public int front, back, up, down, left, right;
	public int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
	
	private int updateTicks = 0;
	private int laserDrawingTicks = 0;
	
	@Override
	public void updateEntity() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}

		if (!addedToEnergyNet && !this.tileEntityInvalid) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			addedToEnergyNet = true;
		}

		if (--this.updateTicks <= 0) {
			this.updateTicks = ((this.tier == 1) ? 20 : (tier == 2) ? 10 : 20) * WarpDriveConfig.i.CD_FIELD_REFRESH_INTERVAL_SECONDS; // resetting timer
			
			if (validateAssembly() && isEnabled) {
				// Consume power for sustaining cloaking field
				countBlocksAndConsumeEnergy();
				
				if (currentEnergyValue >= 0) {
					if (!WarpDrive.instance.cloaks.isAreaExists(this.frequency)) {
						WarpDrive.instance.cloaks.addCloakedAreaWorld(worldObj, minX, minY, minZ, maxX, maxY, maxZ, frequency, tier);
						worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 2);
						worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:cloak", 4F, 1F);
						
						// Enable coils
						setCoilsState(true);
					} else {
						// Refresh the field
						CloakedArea area = WarpDrive.instance.cloaks.getCloakedArea(frequency);
						if (area != null)
							area.sendCloakPacketToPlayersEx(false); // recloak field
					}
				} else {
					currentEnergyValue = 0;
					setCoilsState(false);
					disableCloakingField();
				}
			} else if (!validateAssembly() && isEnabled) {
				currentEnergyValue = 0;
				setCoilsState(false);
				disableCloakingField();				
			}
		}
		
		if (laserDrawingTicks++ > 100) {
			laserDrawingTicks = 0;
			
			if (isEnabled)
				drawLasers();
		}
	}
	
	public void setCoilsState(boolean enabled) {
		final int START_LENGTH = 2; // Step length from core block to main coils
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		byte[] dx = { 1, -1,  0,  0,  0,  0 };
		byte[] dy = { 0,  0, -1,  1,  0,  0 };
		byte[] dz = { 0,  0,  0,  0, -1,  1 };
		
		for (int i = 0; i < 6; i++)
			searchCoilInDirectionAndSetState(dx[i], dy[i], dz[i], enabled);
	}
	
	public void searchCoilInDirectionAndSetState(byte dx, byte dy, byte dz, boolean state) {
		for (int i = 0; i < WarpDriveConfig.i.CD_MAX_CLOAKING_FIELD_SIDE; i++) {
			if (worldObj.getBlockId(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz) == WarpDriveConfig.i.cloakCoilID)
				worldObj.setBlockMetadataWithNotify(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz, (state) ? 1 : 0, 2);
		}
	}	
	
	public void searchCoilInDirectionAndDrawLaser(byte dx, byte dy, byte dz) {
		final int START_LENGTH = 2;
		float r = 0.0f, g = 1.0f, b = 0;
		if (this.tier == 1) {
			r = 0.0f; g = 1.0f; 
		} else if (this.tier == 2) {
			r = 1.0f; g = 0.0f;
		}
		
		for (int i = START_LENGTH + 1; i < WarpDriveConfig.i.CD_MAX_CLOAKING_FIELD_SIDE; i++) {
			if (worldObj.getBlockId(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz) == WarpDriveConfig.i.cloakCoilID)
				sendLaserPacket(new Vector3(this).add(0.5), new Vector3(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz).add(0.5), r, g, b, 110, 0, 100);	
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
		
		for (int k = 0; k < 6; k++)
			searchCoilInDirectionAndDrawLaser(dx[k], dy[k], dz[k]);
		
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
		this.isEnabled = false;
		
		if (WarpDrive.instance.cloaks.isAreaExists(this.frequency))
			WarpDrive.instance.cloaks.removeCloakedArea(this.frequency);
		
		worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:decloak", 4F, 1F);
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);
	}
	public void countBlocksAndConsumeEnergy() {
		int blocksCount = 0;
		for (int y = minY; y <= maxY; y++)
			for (int x = minX; x <= maxX; x++)
				for(int z = minZ; z <= maxZ; z++)
					if (worldObj.getBlockId(x, y, z) != 0)
						blocksCount++;
		int energyToConsume = blocksCount * ((this.tier == 1) ? WarpDriveConfig.i.CD_ENERGY_PER_BLOCK_TIER1 : WarpDriveConfig.i.CD_ENERGY_PER_BLOCK_TIER2);
		
		this.currentEnergyValue -= energyToConsume;
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
		this.currentEnergyValue = tag.getInteger("energy");
		this.tier = tag.getByte("tier");
		this.frequency = tag.getInteger("frequency");
		this.isEnabled = tag.getBoolean("enabled");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("energy", this.getCurrentEnergyValue());
		tag.setByte("tier", this.tier);
		tag.setInteger("frequency", this.frequency);
		tag.setBoolean("enabled", this.isEnabled);
	}

	public int searchCoilInDirection(byte dx, byte dy, byte dz) {
		for (int i = 3; i < WarpDriveConfig.i.CD_MAX_CLOAKING_FIELD_SIDE; i++) {
			if (worldObj.getBlockId(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz) == WarpDriveConfig.i.cloakCoilID)
				return i;
		}
		
		return 0;
	}
	public boolean validateAssembly() {
		final int START_LENGTH = 2; // Step length from core block to main coils
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		byte[] dx = { 1, -1,  0,  0,  0,  0 };
		byte[] dy = { 0,  0, -1,  1,  0,  0 };
		byte[] dz = { 0,  0,  0,  0, -1,  1 };
		
		for (int i = 0; i < 6; i++)
			if (worldObj.getBlockId(xCoord + START_LENGTH * dx[i], yCoord + START_LENGTH * dy[i], zCoord + START_LENGTH * dz[i]) != WarpDriveConfig.i.cloakCoilID)
				return false;
		
		// Check cloaking field parameters defining coils		
		this.left = searchCoilInDirection((byte)1, (byte)0, (byte)0)   + WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS;
		if (this.left == WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS) return false;
		this.right = searchCoilInDirection((byte)-1, (byte)0, (byte)0) + WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS;
		if (this.right == WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS) return false;		
		
		this.up = searchCoilInDirection((byte)0, (byte)1, (byte)0)	 + WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS;
		if (this.up == WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS) return false; 
		this.down = searchCoilInDirection((byte)0, (byte)-1, (byte)0)  + WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS;
		if (this.down == WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS) return false;
				
		this.front = searchCoilInDirection((byte)0, (byte)0, (byte)1)  + WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS;
		if (this.front == WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS) return false;
		this.back = searchCoilInDirection((byte)0, (byte)0, (byte)-1)  + WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS;
		if (this.back == WarpDriveConfig.i.CD_COIL_CAPTURE_BLOCKS) return false;
		
		int x1 = 0, x2 = 0, z1 = 0, z2 = 0;


		z1 = zCoord - this.back;
		z2 = zCoord + this.front;
		x1 = xCoord - this.right;
		x2 = xCoord + this.left;

		if (x1 < x2) {
			this.minX = x1;this. maxX = x2;
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
				this.tier = ((Double)arguments[0]).byteValue();
			}

			break;
		case 1: // isAssemblyValid()
			return new Object[] { (boolean)validateAssembly() };

		case 2: // getEnergyLevel()
			return new Object[] { currentEnergyValue };
			
		case 3: // enableCloakingField()
			this.isEnabled = true;
			break;
			
		case 4: // disableCloakingField()
			disableCloakingField();
			setCoilsState(false);
			break;
			
		case 5: // setFieldFrequency(int)
			if (arguments.length == 1) {
				if (isEnabled)
					disableCloakingField();
				
				if (WarpDrive.instance.cloaks.isAreaExists(((Double)arguments[0]).intValue()))
					return new Object[] { (Boolean)false };
				
				this.frequency = ((Double)arguments[0]).intValue();
				return new Object[] { (Boolean)true };
			}		
			break;
		}
		
		return null;
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

	// IEnergySink methods implementation
	@Override
	public double demandedEnergyUnits() {
		return (MAX_ENERGY_VALUE - currentEnergyValue);
	}

	@Override
	public double injectEnergyUnits(ForgeDirection directionFrom, double amount) {
		double leftover = 0;
		currentEnergyValue += Math.round(amount);

		if (getCurrentEnergyValue() > MAX_ENERGY_VALUE) {
			leftover = (getCurrentEnergyValue() - MAX_ENERGY_VALUE);
			currentEnergyValue = MAX_ENERGY_VALUE;
		}

		return leftover;
	}

	@Override
	public int getMaxSafeInput() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter,
			ForgeDirection direction) {
		return true;
	}

	/**
	 * @return the currentEnergyValue
	 */
	public int getCurrentEnergyValue() {
		return currentEnergyValue;
	}

	public int collectAllEnergy() {
		int energy = currentEnergyValue;
		currentEnergyValue = 0;
		return energy;
	}

	@Override
	public void onChunkUnload() {
		if (addedToEnergyNet) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			addedToEnergyNet = false;
		}
	}

	@Override
	public void invalidate() {
		if (addedToEnergyNet) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			addedToEnergyNet = false;
		}

		super.invalidate();
	}
	}
