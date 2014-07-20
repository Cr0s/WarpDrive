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

public class TileEntityShipScanner extends TileEntity implements IEnergySink,
		IPeripheral {
	public boolean addedToEnergyNet = false;

	private final int MAX_ENERGY_VALUE = 500000000; // 500kk eU
	private int currentEnergyValue = 0;

	private int state = 0; // 0 - inactive, 1 - active
	private int firstUncoveredY;

	private boolean isEnabled = false;
	private TileEntityReactor core = null;

	int laserTicks = 0;
	int scanTicks = 0;
	int deployDelayTicks = 0;
	
	int warpCoreSearchTicks = 0;

	// Config //TODO add to WarpDriveConfig
	private final String SCHEMATICS_DIR = "schematics";
	private final int EU_PER_BLOCK_SCAN = 100; // eU per block of ship volume (including air)
	private final int EU_PER_BLOCK_DEPLOY = 5000;
	private final int BLOCK_TO_DEPLOY_PER_TICK = 1000;
	private final int ALLOWED_DEPLOY_RADIUS = 50; // blocks
	
	private String[] methodsArray = { "scan", // 0
			"fileName", // 1
			"energy", // 2
			"deploy" // 3 deploy(file, offsetX, offsetY, offsetZ)
	};

	private String schematicFileName;
	
	private JumpBlock[] blocksToDeploy; // JumpBlock class stores a basic information about block
	private int currentDeployIndex;
	private int blocksToDeployCount;
	private boolean isDeploying = false;
	
	private int newX, newY, newZ;
	
	
	@Override
	public void updateEntity() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;

		if (!addedToEnergyNet && !this.tileEntityInvalid) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			addedToEnergyNet = true;
		}

		if (++warpCoreSearchTicks > 20) {
			core = searchWarpCore();
			warpCoreSearchTicks = 0;
		}

		// Warp core is not found
		if (!isDeploying && core == null) {
			switchState(0); // disable scanner
			return;
		}

		if (state == 0) { // inactive
			if (++laserTicks > 20) {
				sendLaserPacket(new Vector3(this).add(0.5), new Vector3(
						core.xCoord, core.yCoord, core.zCoord).add(0.5), 0f,
						1f, 0f, 40, 0, 100);
				laserTicks = 0;
			}
		} else if (state == 1 && !isDeploying) { // active: scanning
			if (++laserTicks > 5) {
				laserTicks = 0;
				
				for (int i = 0; i < core.maxX - core.minX; i++) {
					int x = core.minX + i;
					int randomZ = core.minZ + worldObj.rand.nextInt(core.maxZ - core.minZ);
					
					worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
					float r = 0.0f, g = 0.0f, b = 0.0f;
					
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
					
					sendLaserPacket(new Vector3(this).add(0.5), new Vector3(x, core.maxY, randomZ).add(0.5), r, g, b, 15, 0, 100);
				}
			}
			
			if (++scanTicks > 20 * (1 + core.shipVolume / 10)) {
				switchState(0);
				scanTicks = 0;
			}
		} if (state == 1 && isDeploying) { // active: deploying
			if (++deployDelayTicks < 20)
				return;
			
			deployDelayTicks = 0;
			
			int blocks = Math.min(BLOCK_TO_DEPLOY_PER_TICK, blocksToDeployCount - currentDeployIndex);

			if (blocks == 0) {
				isDeploying = false;
				switchState(0);
				return;
			}
			
			for (int index = 0; index < blocks; index++)
			{
				if (currentDeployIndex >= blocksToDeployCount)
				{
					isDeploying = false;
					switchState(0);
					break;
				}
				
				// Deploy single block
				JumpBlock block = blocksToDeploy[currentDeployIndex];
				
				if (block != null &&
					block.blockID != Block.bedrock.blockID &&
					!WarpDriveConfig.i.scannerIgnoreBlocks.contains(block.blockID) &&
					worldObj.isAirBlock(newX + block.x, newY + block.y, newZ + block.z))
				{
					moveBlockSimple(block);
					
					if (worldObj.rand.nextInt(100) <= 10) {
						worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
						
						sendLaserPacket(new Vector3(this).add(0.5), new Vector3(
								newX + block.x, newY + block.y, newZ + block.z).add(0.5), 0f,
								1f, 0f, 15, 0, 100);
					}
				}
				
				currentDeployIndex++;
			} 
		}
	}

	private void switchState(int newState) {
		this.state = newState;
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newState, 2);
	}

	private TileEntityReactor searchWarpCore() {
		TileEntityReactor result = null;

		// Search for warp cores above
		for (int newY = yCoord + 1; newY <= 255; newY++) {
			if (worldObj.getBlockId(xCoord, newY, zCoord) == WarpDriveConfig.i.coreID) { // found
																							// warp
																							// core
																							// above
				result = (TileEntityReactor) worldObj.getBlockTileEntity(
						xCoord, newY, zCoord);

				if (result != null) {
					if (!result.prepareToJump()) { // If we can't refresh ship's
													// spatial parameters
						result = null;
					}
				}

				break;
			}
		}

		return result;
	}

	public void sendLaserPacket(Vector3 source, Vector3 dest, float r, float g,
			float b, int age, int energy, int radius) {
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
			MinecraftServer
					.getServer()
					.getConfigurationManager()
					.sendToAllNear(source.intX(), source.intY(), source.intZ(),
							radius, worldObj.provider.dimensionId, packet);
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
			MinecraftServer
					.getServer()
					.getConfigurationManager()
					.sendToAllNear(dest.intX(), dest.intY(), dest.intZ(),
							radius, worldObj.provider.dimensionId, packet);
		}
	}

	// Checks energy level
	private boolean isEnoughEnergyForScan() {
		if (core != null) {
			return core.shipVolume * EU_PER_BLOCK_SCAN <= currentEnergyValue;
		}

		return false;
	}

	private void saveShipToSchematic(String fileName) {
		NBTTagCompound schematic = new NBTTagCompound("Schematic");

		short width = (short) Math.abs(core.maxX - core.minX);
		short length = (short) Math.abs(core.maxZ - core.minZ);
		short height = (short) (core.maxY - core.minY);
		
		width++;
		height++;
		length++;
		
		schematic.setShort("Width", width);
		schematic.setShort("Length", length);
		schematic.setShort("Height", height);
	
		
		int size = width * length * height;
		
		// Consume energy
		currentEnergyValue = Math.abs(currentEnergyValue - size * EU_PER_BLOCK_SCAN);
		
		
		byte localBlocks[] = new byte[size];
		byte localMetadata[] = new byte[size];
		byte extraBlocks[] = new byte[size];
		byte extraBlocksNibble[] = new byte[(int) Math.ceil(size / 2.0)];
		boolean extra = false;

		NBTTagList tileEntitiesList = new NBTTagList();		
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					int blockID = worldObj.getBlockId(core.minX + x, core.minY + y, core.minZ + z);
					
					// Do not scan air and bedrock
					if ( worldObj.isAirBlock(core.minX + x, core.minY + y, core.minZ + z) || blockID == Block.bedrock.blockID )
						blockID = 0;
					
					int blockMetadata = (byte) worldObj.getBlockMetadata(core.minX + x, core.minY + y, core.minZ + z);
					localBlocks[x + (y * length + z) * width] = (byte) blockID;
					localMetadata[x + (y * length + z) * width] = (byte) blockMetadata;
					if ((extraBlocks[x + (y * length + z) * width] = (byte) (blockID >> 8)) > 0) {
						extra = true;
					}
					
					if (blockID != 0) {
						TileEntity te = worldObj.getBlockTileEntity(core.minX + x, core.minY + y, core.minZ + z);
						if (te != null && !(te instanceof IInventory))
						{
							try {
								NBTTagCompound tileTag = new NBTTagCompound();
								te.writeToNBT(tileTag);
								
								// Remove energy from energy storages
								if (te instanceof IEnergyTile) {
									if (tileTag.hasKey("energy"))
										tileTag.setInteger("energy", 0);
								}
								
								// Transform TE's coordinates from local axis to .schematic offset-axis
								tileTag.setInteger("x", te.xCoord - core.minX);
								tileTag.setInteger("y", te.yCoord - core.minY);
								tileTag.setInteger("z", te.zCoord - core.minZ);
								
								tileEntitiesList.appendTag(tileTag);
							} catch (Exception e) {}
						}
					}
				}
			}
		}

		for (int i = 0; i < extraBlocksNibble.length; i++) {
			if (i * 2 + 1 < extraBlocks.length) {
				extraBlocksNibble[i] = (byte) ((extraBlocks[i * 2 + 0] << 4) | extraBlocks[i * 2 + 1]);
			} else {
				extraBlocksNibble[i] = (byte) (extraBlocks[i * 2 + 0] << 4);
			}
		}

		schematic.setString("Materials", "Alpha");
		schematic.setByteArray("Blocks", localBlocks);
		schematic.setByteArray("Data", localMetadata);
		
		if (extra)
			schematic.setByteArray("AddBlocks", extraBlocksNibble);
		
		schematic.setTag("Entities", new NBTTagList()); // don't save entities
		schematic.setTag("TileEntities", tileEntitiesList);		
		
		writeNBTToFile(fileName, schematic);
	}

	private void writeNBTToFile(String fileName, NBTTagCompound nbttagcompound) {
		
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fileoutputstream = new FileOutputStream(file);

			CompressedStreamTools.writeCompressed(nbttagcompound,
					fileoutputstream);

			fileoutputstream.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	// Begins ship scan
	private void scanShip() {
		// Enable scanner
		switchState(1);
		File f = new File(SCHEMATICS_DIR);
		if (!f.exists() || !f.isDirectory())
			f.mkdirs();

		// Generate unique file name
		do {
			schematicFileName = (new StringBuilder().append(core.coreFrequency)
					.append(System.currentTimeMillis()).append(".schematic"))
					.toString();
		} while (new File(SCHEMATICS_DIR + "/" + schematicFileName).exists());
		saveShipToSchematic(SCHEMATICS_DIR + "/" + schematicFileName);
	}

	private NBTTagCompound readNBTFromFile(String fileName) {
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				return null;
			}
			
			FileInputStream fileinputstream = new FileInputStream(file);
			NBTTagCompound nbttagcompound = CompressedStreamTools
					.readCompressed(fileinputstream);

			fileinputstream.close();
			
			return nbttagcompound;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	
		return null;
	}
	
	private boolean isEnoughEnergyForDeploy(int size) {
		return size * EU_PER_BLOCK_DEPLOY <= currentEnergyValue;
	}
	
	// Returns result array for CC interface: [ code, "message" ]
	private Object[] deployShip(String fileName, int offsetX, int offsetY, int offsetZ) {
		NBTTagCompound schematic = readNBTFromFile(SCHEMATICS_DIR + "/" + fileName);
		
		if (schematic == null) {
			return new Object[] { -1, "Unknow error. Schematic NBT is null" };
		}
		
		short width = schematic.getShort("Width");
		short height = schematic.getShort("Height");
		short length = schematic.getShort("Length");
		
		int targetX = xCoord + offsetX;
		int targetY = yCoord + offsetY;
		int targetZ = zCoord + offsetZ;
		
		double d3 = xCoord - targetX;
		double d4 = yCoord - targetY;
		double d5 = zCoord - targetZ;
		double distance = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
		
		if (distance > ALLOWED_DEPLOY_RADIUS)
			return new Object[] { 5, "Cannot deploy ship so far away from scanner." };
		
		int size = width* height * length;
		
		
		// Check energy level
		if (!isEnoughEnergyForDeploy(size)) {
			return new Object[] { 1, "Not enough energy! Need at least " + (Math.abs(size * EU_PER_BLOCK_DEPLOY - currentEnergyValue)) + " Eu" };
		}
		
		// Check specified area for occupation by blocks
		// If specified area occupied, break deploying with error message
		int occupiedBlockCount = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					if (!worldObj.isAirBlock(targetX + x, targetY + y, targetZ + z))
						occupiedBlockCount++;
				}
			}
		}

		if (occupiedBlockCount > 0) {
			return new Object[] { 2, "Deploying area occupied with " + occupiedBlockCount + " blocks. Can't deploy ship." };
		}
		
		// Consume energy
		currentEnergyValue = Math.abs(currentEnergyValue - size * EU_PER_BLOCK_DEPLOY);
		
		// Set deployment vars
		this.blocksToDeploy = new JumpBlock[size];
		this.isDeploying = true;
		this.currentDeployIndex = 0;
		this.blocksToDeployCount = size;
		
		this.newX = targetX;
		this.newY = targetY;
		this.newZ = targetZ;
		
		
		// Read blocks and TileEntities from NBT to internal storage array
		
		byte localBlocks[] = schematic.getByteArray("Blocks");
		byte localMetadata[] = schematic.getByteArray("Data");

		boolean extra = schematic.hasKey("Add") || schematic.hasKey("AddBlocks");
		byte extraBlocks[] = null;
		byte extraBlocksNibble[] = null;
		if (schematic.hasKey("AddBlocks")) {
			extraBlocksNibble = schematic.getByteArray("AddBlocks");
			extraBlocks = new byte[extraBlocksNibble.length * 2];
			for (int i = 0; i < extraBlocksNibble.length; i++) {
				extraBlocks[i * 2 + 0] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
				extraBlocks[i * 2 + 1] = (byte) (extraBlocksNibble[i] & 0xF);
			}
		} else if (schematic.hasKey("Add")) {
			extraBlocks = schematic.getByteArray("Add");
		}
		
		// Load Tile Entities
		NBTTagCompound[] tileEntities = new NBTTagCompound[size];
		NBTTagList tileEntitiesList = schematic.getTagList("TileEntities");

		for (int i = 0; i < tileEntitiesList.tagCount(); i++) {
			NBTTagCompound teTag = (NBTTagCompound) tileEntitiesList.tagAt(i);
			int teX = teTag.getInteger("x");
			int teY = teTag.getInteger("y");
			int teZ = teTag.getInteger("z");
			
			tileEntities[teX + (teY * length + teZ) * width] = teTag;
		}			
		
		// Create list of blocks to deploy
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					JumpBlock jb = new JumpBlock();
					
					jb.blockID = (localBlocks[x + (y * length + z) * width]) & 0xFF;
					if (extra)
						jb.blockID |= ((extraBlocks[x + (y * length + z) * width]) & 0xFF) << 8;
					
					jb.blockMeta = (localMetadata[x + (y * length + z) * width]) & 0xFF;
					jb.blockNBT = tileEntities[x + (y * length + z) * width];
							
					jb.x = x;
					jb.y = y;
					jb.z = z;
					blocksToDeploy[x + (y * length + z) * width] = jb;
				}
			}
		}
		
		switchState(1);
		return new Object[] { 3, "Ship deployed." };
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.currentEnergyValue = tag.getInteger("energy");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("energy", this.getCurrentEnergyValue());
	}

	// CC
	// IPeripheral methods implementation
	@Override
	public String getType() {
		return "shipscanner";
	}

	@Override
	public String[] getMethodNames() {
		return methodsArray;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
		switch (method) {
		case 0: // scanShip()
			// Already scanning?
			if (this.state == 1)
				return new Object[] { false, 0, "Already scanning" };

			if (core != null && isEnoughEnergyForScan()) {
				scanShip();
			} else if (core == null) {
				return new Object[] { false, 1, "Warp-Core not found" };
			} else {
				return new Object[] { false, 2, "Not enough energy!" };
			}

			break;
		case 1: // getSchematicFileName()
			if (state != 0 && !schematicFileName.isEmpty())
				return new Object[] { "Scanning in process. Please wait." };
			
			return new Object[] { schematicFileName };

		case 2: // getEnergyLevel()
			return new Object[] { currentEnergyValue };
			
		case 3: // deployShipFromSchematic(schematicFileName, offsetX, offsetY, offsetZ)
			if (arguments.length == 4) {
				String fileName = (String)arguments[0];
				int x = ((Double)arguments[1]).intValue();
				int y = ((Double)arguments[2]).intValue();
				int z = ((Double)arguments[3]).intValue();
				
				if (!new File(SCHEMATICS_DIR + "/" + fileName).exists()) 
					return new Object[] { 0, "Specified .schematic file not found!" };
				else
				{
					return deployShip(fileName, x, y, z);
				}
			} else
				return new Object[] { 4, ".schematic file name not specified or invalid arguments count!" };
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
	
	public boolean moveBlockSimple(JumpBlock shipBlock)
	{
		try
		{
			if (shipBlock == null)
			{
				return false;
			}

			int oldX = shipBlock.x;
			int oldY = shipBlock.y;
			int oldZ = shipBlock.z;

			int blockID = shipBlock.blockID;
			int blockMeta = shipBlock.blockMeta;
			mySetBlock(worldObj, oldX + newX, oldY + newY, oldZ + newZ, blockID, blockMeta, 2);

			NBTTagCompound oldnbt = new NBTTagCompound();
			oldnbt = shipBlock.blockNBT;
			if (oldnbt != null) {
				TileEntity newTileEntity;
				newTileEntity = TileEntity.createAndLoadEntity(oldnbt);
				newTileEntity.worldObj = worldObj;
				newTileEntity.validate();
				worldObj.setBlockTileEntity(oldX + newX, oldY + newY, oldZ + newZ, newTileEntity);
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			return false;
		}

		return true;
	}	
	
	// Own implementation of setting blocks without light recalculation in optimization purposes
	public boolean mySetBlock(World w, int x, int y, int z, int blockId, int blockMeta, int par6)
	{
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) //FIXME magic numbers
		{
			if (y < 0)
			{
				return false;
			}
			else if (y >= 256)
			{
				return false;
			}
			else
			{
				w.markBlockForUpdate(x, y, z);
				Chunk chunk = w.getChunkFromChunkCoords(x >> 4, z >> 4);
				return myChunkSBIDWMT(chunk, x & 15, y, z & 15, blockId, blockMeta);
			}
		}
		else
		{
			return false;
		}
	}

	public boolean myChunkSBIDWMT(Chunk c, int x, int y, int z, int blockId, int blockMeta)
	{
		int j1 = z << 4 | x;

		if (y >= c.precipitationHeightMap[j1] - 1)
		{
			c.precipitationHeightMap[j1] = -999;
		}

		//int k1 = c.heightMap[j1];
		int l1 = c.getBlockID(x, y, z);
		int i2 = c.getBlockMetadata(x, y, z);

		if (l1 == blockId && i2 == blockMeta)
		{
			return false;
		}
		else
		{
			ExtendedBlockStorage[] storageArrays = c.getBlockStorageArray();
			ExtendedBlockStorage extendedblockstorage = storageArrays[y >> 4];

			if (extendedblockstorage == null)
			{
				if (blockId == 0)
				{
					return false;
				}

				extendedblockstorage = storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !c.worldObj.provider.hasNoSky);
			}

			int j2 = c.xPosition * 16 + x;
			int k2 = c.zPosition * 16 + z;
			extendedblockstorage.setExtBlockID(x, y & 15, z, blockId);

			if (l1 != 0)
			{
				if (!c.worldObj.isRemote)
				{
					Block.blocksList[l1].breakBlock(c.worldObj, j2, y, k2, l1, i2);
				}
				else if (Block.blocksList[l1] != null && Block.blocksList[l1].hasTileEntity(i2))
				{
					TileEntity te = worldObj.getBlockTileEntity(j2, y, k2);

					if (te != null && te.shouldRefresh(l1, blockId, i2, blockMeta, worldObj, j2, y, k2))
					{
						c.worldObj.removeBlockTileEntity(j2, y, k2);
					}
				}
			}

			if (extendedblockstorage.getExtBlockID(x, y & 15, z) != blockId)
			{
				return false;
			}
			else
			{
				extendedblockstorage.setExtBlockMetadata(x, y & 15, z, blockMeta);
				// Removed light recalculations
				/*if (flag)
				{
					c.generateSkylightMap();
				}
				else
				{
					if (c.getBlockLightOpacity(par1, par2, par3) > 0)
					{
						if (par2 >= k1)
						{
							c.relightBlock(par1, par2 + 1, par3);
						}
					}
					else if (par2 == k1 - 1)
					{
						c.relightBlock(par1, par2, par3);
					}

					c.propagateSkylightOcclusion(par1, par3);
				}*/
				TileEntity tileentity;

				if (blockId != 0)
				{
					if (Block.blocksList[blockId] != null && Block.blocksList[blockId].hasTileEntity(blockMeta))
					{
						tileentity = c.getChunkBlockTileEntity(x, y, z);

						if (tileentity == null)
						{
							tileentity = Block.blocksList[blockId].createTileEntity(c.worldObj, blockMeta);
							c.worldObj.setBlockTileEntity(j2, y, k2, tileentity);
						}

						if (tileentity != null)
						{
							tileentity.updateContainingBlockInfo();
							tileentity.blockMetadata = blockMeta;
						}
					}
				}

				c.isModified = true;
				return true;
			}
		}
	}	
}
