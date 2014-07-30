package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;
import cr0s.WarpDrive.CloakManager.CloakedArea;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.ForgeDirection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityCloakingDeviceCore extends TileEntityAbstractLaser implements IPeripheral
{
	public boolean addedToEnergyNet = false;

	private final int MAX_ENERGY_VALUE = 500000000; // 500kk RF
	
	private String[] methodsArray = { "setFieldTier", // 0 setFieldTier(1 or 2)
			"isAssemblyValid", // 1 - returns true or false
			"getEnergyLevel", // 2 
			"enableCloakingField", // 3 enables field if assembled right
			"disableCloakingField", // 4 disables cloaking field
			"fieldFrequency" };
	
	public boolean isEnabled = false;
	public byte tier = 1; // cloaking field tier, 1 or 2
	public int frequency = 0;
	
	// Spatial cloaking field parameters
	public int front, back, up, down, left, right;
	public int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
	
	private int updateTicks = 0;
	private int laserDrawingTicks = 0;
	
	private boolean soundPlayed = false;
	private int soundTicks = 0;
	
	@Override
	public int getMaxEnergyStored()
	{
		return MAX_ENERGY_VALUE;
	}
	
	@Override
	public void updateEntity() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			return;
		}

		// Reset sound timer
		if (soundTicks++ >= 40)
		{
			this.soundTicks = 0;
			this.soundPlayed = false;
		}
		
		if (--this.updateTicks <= 0)
		{
			//WarpDrive.debugPrint("[CloakDev] Updating cloaking state...");
			this.updateTicks = ((this.tier == 1) ? 20 : (tier == 2) ? 10 : 20) * WarpDriveConfig.CD_FIELD_REFRESH_INTERVAL_SECONDS; // resetting timer
			
			if (validateAssembly() && isEnabled)
			{
				// Consume power for sustaining cloaking field
				countBlocksAndConsumeEnergy();
				
				if (getEnergyStored() >= 0)
				{
					if (!WarpDrive.instance.cloaks.isAreaExists(this.frequency))
					{
						WarpDrive.instance.cloaks.addCloakedAreaWorld(worldObj, minX, minY, minZ, maxX, maxY, maxZ, frequency, tier);
						worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 2);
						if (!soundPlayed)
						{
							soundPlayed = true;
							worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:cl", 4F, 1F);
						}
						
						// Enable coils
						setCoilsState(true);
					}
					else
					{
						// Refresh the field
						CloakedArea area = WarpDrive.instance.cloaks.getCloakedArea(frequency);
						if (area != null)
							area.sendCloakPacketToPlayersEx(false); // recloak field
					}
				}
				else
				{
					WarpDrive.debugPrint("[CloakDev] Low power, cloak field collapsing...");
					removeEnergy(getEnergyStored(),false);
					setCoilsState(false);
					disableCloakingField();
				}
			}
			else if (!validateAssembly() && isEnabled)
			{
				WarpDrive.debugPrint("[CloakDev] Device lost coils, field collapsing");
				removeEnergy(getEnergyStored(),false);
				setCoilsState(false);
				disableCloakingField();				
			}
		}
		
		if (laserDrawingTicks++ > 100)
		{
			laserDrawingTicks = 0;
			
			if (isEnabled)
				drawLasers();
		}
	}
	
	public void setCoilsState(boolean enabled)
	{
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		byte[] dx = { 1, -1,  0,  0,  0,  0 };
		byte[] dy = { 0,  0, -1,  1,  0,  0 };
		byte[] dz = { 0,  0,  0,  0, -1,  1 };
		
		for (int i = 0; i < 6; i++)
			searchCoilInDirectionAndSetState(dx[i], dy[i], dz[i], enabled);
	}
	
	public void searchCoilInDirectionAndSetState(byte dx, byte dy, byte dz, boolean state)
	{
		for (int i = 0; i < WarpDriveConfig.CD_MAX_CLOAKING_FIELD_SIDE; i++)
		{
			if (worldObj.getBlockId(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz) == WarpDriveConfig.cloakCoilID)
				worldObj.setBlockMetadataWithNotify(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz, (state) ? 1 : 0, 2);
		}
	}	
	
	public void searchCoilInDirectionAndDrawLaser(byte dx, byte dy, byte dz)
	{
		final int START_LENGTH = 2;
		float r = 0.0f, g = 1.0f, b = 0;
		if (this.tier == 1)
		{
			r = 0.0f; g = 1.0f; 
		}
		else if (this.tier == 2) 
		{
			r = 1.0f; g = 0.0f;
		}
		
		for (int i = START_LENGTH + 1; i < WarpDriveConfig.CD_MAX_CLOAKING_FIELD_SIDE; i++)
		{
			if (worldObj.getBlockId(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz) == WarpDriveConfig.cloakCoilID)
				sendLaserPacket(new Vector3(this).translate(0.5), new Vector3(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz).translate(0.5), r, g, b, 110, 0, 100);	
		}
	}	
	
	public void drawLasers()
	{
		final int START_LENGTH = 2;
		float r = 0.0f, g = 1.0f, b = 0;
		if (this.tier == 1)
		{
			r = 0.0f; g = 1.0f; 
		}
		else if (this.tier == 2)
		{
			r = 1.0f; g = 0.0f;
		}
				
		// Directions to check (all six directions: left, right, up, down, front, back)
		byte[] dx = { 1, -1,  0,  0,  0,  0 };
		byte[] dy = { 0,  0, -1,  1,  0,  0 };
		byte[] dz = { 0,  0,  0,  0, -1,  1 };
		
		for (int k = 0; k < 6; k++)
			searchCoilInDirectionAndDrawLaser(dx[k], dy[k], dz[k]);
		
		for (int i = 0; i < 6; i++)
		{		
			for (int j = 0; j < 6; j++)
			{
				switch (worldObj.rand.nextInt(6))
				{
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
				
				sendLaserPacket(new Vector3(xCoord + START_LENGTH * dx[i], yCoord + START_LENGTH * dy[i], zCoord + START_LENGTH * dz[i]).translate(0.5), new Vector3(xCoord + START_LENGTH * dx[j], yCoord + START_LENGTH * dy[j], zCoord + START_LENGTH * dz[j]).translate(0.5), r, g, b, 110, 0, 100);
			}
		}
	}

	public void disableCloakingField()
	{
		this.isEnabled = false;
		
		if (WarpDrive.instance.cloaks.isAreaExists(this.frequency))
			WarpDrive.instance.cloaks.removeCloakedArea(this.frequency);
		
		if (!soundPlayed)
		{
			soundPlayed = true;
			worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:dcl", 4F, 1F);
		}
		
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);
	}
	public void countBlocksAndConsumeEnergy()
	{
		int blocksCount = 0;
		for (int y = minY; y <= maxY; y++)
			for (int x = minX; x <= maxX; x++)
				for(int z = minZ; z <= maxZ; z++)
					if (worldObj.getBlockId(x, y, z) != 0)
						blocksCount++;
		int energyToConsume = blocksCount * ((this.tier == 1) ? WarpDriveConfig.CD_ENERGY_PER_BLOCK_TIER1 : WarpDriveConfig.CD_ENERGY_PER_BLOCK_TIER2);
		
		//WarpDrive.debugPrint("[CloakDev] Consuming " + energyToConsume + " eU for " + blocksCount + " blocks");
		removeEnergy(energyToConsume,false);
	}
	
	@Override
	public boolean shouldChunkLoad()
	{
		return false;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		this.tier = tag.getByte("tier");
		this.frequency = tag.getInteger("frequency");
		this.isEnabled = tag.getBoolean("enabled");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setByte("tier", this.tier);
		tag.setInteger("frequency", this.frequency);
		tag.setBoolean("enabled", this.isEnabled);
	}

	public int searchCoilInDirection(byte dx, byte dy, byte dz)
	{
		for (int i = 3; i < WarpDriveConfig.CD_MAX_CLOAKING_FIELD_SIDE; i++)
		{
			if (worldObj.getBlockId(xCoord + i * dx, yCoord + i * dy, zCoord + i * dz) == WarpDriveConfig.cloakCoilID)
				return i;
		}
		
		return 0;
	}
	public boolean validateAssembly()
	{
		final int START_LENGTH = 2; // Step length from core block to main coils
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		byte[] dx = { 1, -1,  0,  0,  0,  0 };
		byte[] dy = { 0,  0, -1,  1,  0,  0 };
		byte[] dz = { 0,  0,  0,  0, -1,  1 };
		
		for (int i = 0; i < 6; i++)
			if (worldObj.getBlockId(xCoord + START_LENGTH * dx[i], yCoord + START_LENGTH * dy[i], zCoord + START_LENGTH * dz[i]) != WarpDriveConfig.cloakCoilID)
				return false;
		
		// Check cloaking field parameters defining coils		
		this.left = searchCoilInDirection((byte)1, (byte)0, (byte)0)   + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.left == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS)
			return false;
		this.right = searchCoilInDirection((byte)-1, (byte)0, (byte)0) + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.right == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS)
			return false;		
		
		this.up = searchCoilInDirection((byte)0, (byte)1, (byte)0)     + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.up == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS)
			return false; 
		this.down = searchCoilInDirection((byte)0, (byte)-1, (byte)0)  + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.down == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS)
			return false;
				
		this.front = searchCoilInDirection((byte)0, (byte)0, (byte)1)  + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.front == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS)
			return false;
		this.back = searchCoilInDirection((byte)0, (byte)0, (byte)-1)  + WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS;
		if (this.back == WarpDriveConfig.CD_COIL_CAPTURE_BLOCKS)
			return false;
		
        int x1 = 0, x2 = 0, z1 = 0, z2 = 0;


        z1 = zCoord - this.back;
        z2 = zCoord + this.front;
        x1 = xCoord - this.right;
        x2 = xCoord + this.left;

        if (x1 < x2)
        {
        	this.minX = x1;this. maxX = x2;
        }
        else
        {
        	this.minX = x2; this.maxX = x1;
        }

        if (z1 < z2)
        {
        	this.minZ = z1; this.maxZ = z2;
        }
        else
        {
        	this.minZ = z2; this.maxZ = z1;
        }		
		
        this.minY = yCoord - this.down;
        this.maxY = yCoord + this.up;
        
		return true;
	}
	
	// CC
	// IPeripheral methods implementation
	@Override
	public String getType()
	{
		return "cloakingdevicecore";
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
			case 0: // setFieldTier(1 or 2)
				try
				{
					if (arguments.length >= 1)
					{
						tier = (byte) clamp(toInt(arguments[0]),1,2);
					}
				}
				catch(NumberFormatException e)
				{
					tier = 1;
				}
				return new Object[] { tier };
			case 1: // isAssemblyValid()
				return new Object[] { (boolean)validateAssembly() };
	
			case 2: // getEnergyLevel()
				return new Object[] { getEnergyStored() };
				
			case 3: // enableCloakingField()
				this.isEnabled = true;
				return new Object[] { true };
				
			case 4: // disableCloakingField()
				disableCloakingField();
				setCoilsState(false);
				return new Object[] { false };
				
			case 5: // setFieldFrequency(int)
				if (arguments.length == 1)
				{
					try
					{
						if (isEnabled)
							disableCloakingField();
						
						if (WarpDrive.instance.cloaks.isAreaExists(toInt(arguments[0])))
							return new Object[] { this.frequency };
						
						this.frequency = toInt(arguments[0]);
						return new Object[] { this.frequency };
					}
					catch(NumberFormatException e)
					{
						return new Object[] { false };
					}
				}		
				return new Object[] { this.frequency };
			}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	public int collectAllEnergy()
	{
		int energy = getEnergyStored();
		removeEnergy(energy,false);
		return energy;
	}

	@Override
	public boolean equals(IPeripheral other) {
		// TODO Auto-generated method stub
		return false;
	}
}
