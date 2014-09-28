package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.*;
import cr0s.WarpDrive.data.JumpBlock;
import cr0s.WarpDrive.data.Vector3;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IPeripheral;
import ic2.api.energy.tile.IEnergyTile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityShipScanner extends WarpEnergyTE implements IPeripheral {
	private int state = 0; // 0 - inactive, 1 - active
	private int firstUncoveredY;

	private boolean isEnabled = false;
	private TileEntityReactor core = null;

	int laserTicks = 0;
	int scanTicks = 0;
	int deployDelayTicks = 0;
	
	int warpCoreSearchTicks = 0;

	private String[] methodsArray = {
		"scan",					// 0
		"fileName",		// 1
		"energy",			// 2
		"deploy"	// 3 deployShipFromSchematic(file, offsetX, offsetY, offsetZ)
	};

	private String schematicFileName;
	
	private JumpBlock[] blocksToDeploy; // JumpBlock class stores a basic information about block
	private int currentDeployIndex;
	private int blocksToDeployCount;
	private boolean isDeploying = false;
	
	private int targetX, targetY, targetZ;
	
	
	@Override
	public void updateEntity() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;

		super.updateEntity();

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
				PacketHandler.sendBeamPacket(worldObj,
						new Vector3(this).translate(0.5D), new Vector3(core.xCoord, core.yCoord, core.zCoord).translate(0.5D),
						0f, 1f, 0f, 40, 0, 100);
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
					
					PacketHandler.sendBeamPacket(worldObj, new Vector3(this).translate(0.5D), new Vector3(x, core.maxY, randomZ).translate(0.5D), r, g, b, 15, 0, 100);
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
			
			int blocks = Math.min(WarpDriveConfig.G_BLOCKS_PER_TICK, blocksToDeployCount - currentDeployIndex);

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
				JumpBlock jb = blocksToDeploy[currentDeployIndex];
				
				if (jb != null &&
					jb.blockID != Block.bedrock.blockID &&
					!WarpDriveConfig.scannerIgnoreBlocks.contains(jb.blockID) &&
					worldObj.isAirBlock(targetX + jb.x, targetY + jb.y, targetZ + jb.z)) {
					jb.deploy(worldObj, targetX, targetY, targetZ);
					
					if (worldObj.rand.nextInt(100) <= 10) {
						worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
						
						PacketHandler.sendBeamPacket(worldObj,
								new Vector3(this).translate(0.5D),
								new Vector3(targetX + jb.x, targetY + jb.y, targetZ + jb.z).translate(0.5D),
								0f, 1f, 0f, 15, 0, 100);
					}
				}
				
				currentDeployIndex++;
			} 
		}
	}

	private void switchState(int newState) {
		this.state = newState;
		if (getBlockMetadata() != newState) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newState, 2);
		}
	}

	private TileEntityReactor searchWarpCore() {
		StringBuilder reason = new StringBuilder();
		TileEntityReactor result = null;

		// Search for warp cores above
		for (int newY = yCoord + 1; newY <= 255; newY++) {
			if (worldObj.getBlockId(xCoord, newY, zCoord) == WarpDriveConfig.coreID) { // found warp core above
				result = (TileEntityReactor) worldObj.getBlockTileEntity(
						xCoord, newY, zCoord);

				if (result != null) {
					if (!result.validateShipSpatialParameters(reason)) { // If we can't refresh ship's spatial parameters
						result = null;
					}
				}

				break;
			}
		}

		return result;
	}
	
	private int getScanningEnergyCost(int size) {
		if (WarpDriveConfig.SS_EU_PER_BLOCK_SCAN > 0) {
			return size * WarpDriveConfig.SS_EU_PER_BLOCK_SCAN;
		} else {
			return WarpDriveConfig.SS_MAX_ENERGY_VALUE;
		}
	}
	
	private int getDeploymentEnergyCost(int size) {
		if (WarpDriveConfig.SS_EU_PER_BLOCK_DEPLOY > 0) {
			return size * WarpDriveConfig.SS_EU_PER_BLOCK_DEPLOY;
		} else {
			return WarpDriveConfig.SS_MAX_ENERGY_VALUE;
		}
	}
	
	private boolean saveShipToSchematic(String fileName, StringBuilder reason) {
		NBTTagCompound schematic = new NBTTagCompound("Schematic");

		short width  = (short) (core.maxX - core.minX + 1);
		short length = (short) (core.maxZ - core.minZ + 1);
		short height = (short) (core.maxY - core.minY + 1);
		
		if (width <= 0 || length <= 0 || height <= 0) {
			reason.append("Invalid ship dimensions, nothing to scan");
			return false;
		}
		
		schematic.setShort("Width", width);
		schematic.setShort("Length", length);
		schematic.setShort("Height", height);
		
		int size = width * length * height;
		
		// Consume energy
		if (!consumeEnergy(getScanningEnergyCost(size), false)) {
			reason.append("Insufficient energy (" + getScanningEnergyCost(size) + " required)");
			return false;
		}
		
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
					
					// Do not scan air, bedrock and specified forbidden blocks (like ore or Warp-Cores)
					if (worldObj.isAirBlock(core.minX + x, core.minY + y, core.minZ + z) || blockID == Block.bedrock.blockID /*|| WarpDriveConfig.scannerIgnoreBlocks.contains(blockID)/**/) {
						blockID = 0;
					}
					
					int blockMetadata = (byte) worldObj.getBlockMetadata(core.minX + x, core.minY + y, core.minZ + z);
					localBlocks[x + (y * length + z) * width] = (byte) blockID;
					localMetadata[x + (y * length + z) * width] = (byte) blockMetadata;
					if ((extraBlocks[x + (y * length + z) * width] = (byte) (blockID >> 8)) > 0) {
						extra = true;
					}
					
					if (blockID != 0) {
						TileEntity te = worldObj.getBlockTileEntity(core.minX + x, core.minY + y, core.minZ + z);
						if (te != null) {
							try {
								NBTTagCompound tileTag = new NBTTagCompound();
								te.writeToNBT(tileTag);
								
								// Clear inventory.
								if (te instanceof IInventory) {
									TileEntity tmp_te = TileEntity.createAndLoadEntity(tileTag);
									if (tmp_te instanceof IInventory) {
										for (int i = 0; i < ((IInventory)tmp_te).getSizeInventory(); i++) {
											((IInventory)tmp_te).setInventorySlotContents(i, null);
										}
									}
									tmp_te.writeToNBT(tileTag);
								}

								// Remove energy from energy storages
								if (te instanceof IEnergyTile) {
									// IC2
									if (tileTag.hasKey("energy")) {
										tileTag.setInteger("energy", 0);
									}
									// Gregtech
									if (tileTag.hasKey("mStoredEnergy")) {
										tileTag.setInteger("mStoredEnergy", 0);
									}
								}
								
								// Transform TE's coordinates from local axis to .schematic offset-axis
								tileTag.setInteger("x", te.xCoord - core.minX);
								tileTag.setInteger("y", te.yCoord - core.minY);
								tileTag.setInteger("z", te.zCoord - core.minZ);
								
								tileEntitiesList.appendTag(tileTag);
							} catch (Exception exception) {
								exception.printStackTrace();
							}
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
		
		return true;
	}

	private static void writeNBTToFile(String fileName, NBTTagCompound nbttagcompound) {
		System.out.println("[ShipScanner] Filename: " + fileName);
		
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fileoutputstream = new FileOutputStream(file);

			CompressedStreamTools.writeCompressed(nbttagcompound, fileoutputstream);

			fileoutputstream.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	// Begins ship scan
	private boolean scanShip(StringBuilder reason) {
		// Enable scanner
		switchState(1);
		File f = new File(WarpDriveConfig.G_SCHEMALOCATION);
		if (!f.exists() || !f.isDirectory()) {
			f.mkdirs();
		}

		// Generate unique file name
		do {
			schematicFileName = (new StringBuilder().append(core.coreFrequency)
					.append(System.currentTimeMillis()).append(".schematic"))
					.toString();
		} while (new File(WarpDriveConfig.G_SCHEMALOCATION + "/" + schematicFileName).exists());

		if (!saveShipToSchematic(WarpDriveConfig.G_SCHEMALOCATION + "/" + schematicFileName, reason)) {
			return false;
		}
		reason.append(schematicFileName);
		return true;
	}

	private static NBTTagCompound readNBTFromFile(String fileName) {
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				return null;
			}
			
			FileInputStream fileinputstream = new FileInputStream(file);
			NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);

			fileinputstream.close();
			
			return nbttagcompound;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	
		return null;
	}
	
	// Returns error code and reason string
	private int deployShip(String fileName, int offsetX, int offsetY, int offsetZ, StringBuilder reason) {
		// Load schematic
		NBTTagCompound schematic = readNBTFromFile(WarpDriveConfig.G_SCHEMALOCATION + "/" + fileName);
		if (schematic == null) {
			reason.append("Schematic not found or unknow error reading it.");
			return -1;
		}
		
		// Compute geometry
		short width = schematic.getShort("Width");
		short height = schematic.getShort("Height");
		short length = schematic.getShort("Length");
		
		targetX = xCoord + offsetX;
		targetY = yCoord + offsetY;
		targetZ = zCoord + offsetZ;
		blocksToDeployCount = width * height * length;
		
		// Validate context
		{
			// Check distance
			double dX = xCoord - targetX;
			double dY = yCoord - targetY;
			double dZ = zCoord - targetZ;
			double distance = MathHelper.sqrt_double(dX * dX + dY * dY + dZ * dZ);
			
			if (distance > WarpDriveConfig.SS_MAX_DEPLOY_RADIUS_BLOCKS) {
				reason.append("Cannot deploy ship so far away from scanner.");
				return 5;
			}
			
			// Consume energy
			if (!consumeEnergy(getDeploymentEnergyCost(blocksToDeployCount), false)) {
				reason.append("Insufficient energy (" + getDeploymentEnergyCost(blocksToDeployCount) + " required)");
				return 1;
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
				reason.append("Deploying area occupied with " + occupiedBlockCount + " blocks. Can't deploy ship.");
				return 2;
			}
		}
		
		// Set deployment variables
		blocksToDeploy = new JumpBlock[blocksToDeployCount];
		isDeploying = true;
		currentDeployIndex = 0;
		
		// Read blocks and TileEntities from NBT to internal storage array
		byte localBlocks[] = schematic.getByteArray("Blocks");
		byte localMetadata[] = schematic.getByteArray("Data");

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
		NBTTagCompound[] tileEntities = new NBTTagCompound[blocksToDeployCount];
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
					int index = x + (y * length + z) * width;
					JumpBlock jb = new JumpBlock();
					
					jb.x = x;
					jb.y = y;
					jb.z = z;
					jb.blockID = (localBlocks[index]) & 0xFF;
					if (extraBlocks != null) {
						jb.blockID |= ((extraBlocks[index]) & 0xFF) << 8;
					}
					jb.blockMeta = (localMetadata[index]) & 0xFF;
					jb.blockNBT = tileEntities[index];
					
					if (jb.blockID != 0 && Block.blocksList[jb.blockID] != null) {
						if (tileEntities[index] == null) {
							WarpDrive.debugPrint("[ShipScanner] Adding block to deploy: " + Block.blocksList[jb.blockID].getUnlocalizedName() + " (no tile entity)");
						} else {
							WarpDrive.debugPrint("[ShipScanner] Adding block to deploy: " + Block.blocksList[jb.blockID].getUnlocalizedName() + " with tile entity " + tileEntities[index].getString("id"));
						}
						
						blocksToDeploy[index] = jb;
					} else {
						jb = null;
						
						blocksToDeploy[index] = jb;
					}
				}
			}
		}
		
		switchState(1);
		reason.append("Ship deploying...");
		return 3;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
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
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
		switch (method) {
		case 0: // scanShip()
			// Already scanning?
			if (this.state == 1) {
				return new Object[] { false, 0, "Already scanning" };
			}
			
			if (core == null) {
				return new Object[] { false, 1, "Warp-Core not found" };
			} else if (consumeEnergy(core.shipVolume * WarpDriveConfig.SS_EU_PER_BLOCK_SCAN, true)) {
				StringBuilder reason = new StringBuilder();
				boolean success = scanShip(reason);
				return new Object[] { success, 3, reason.toString() };
			} else {
				return new Object[] { false, 2, "Not enough energy!" };
			}
			// break;
			
		case 1: // getSchematicFileName()
			if (state != 0 && !schematicFileName.isEmpty()) {
				return new Object[] { "Scanning in process. Please wait..." };
			}
			
			return new Object[] { schematicFileName };
			
		case 2: // getEnergyLevel()
			return new Object[] { getEnergyStored() };
			
		case 3: // deployShipFromSchematic(schematicFileName, offsetX, offsetY, offsetZ)
			if (arguments.length == 4) {
				String fileName = (String)arguments[0];
				int x = ((Double)arguments[1]).intValue();
				int y = ((Double)arguments[2]).intValue();
				int z = ((Double)arguments[3]).intValue();
				
				if (!new File(WarpDriveConfig.G_SCHEMALOCATION + "/" + fileName).exists()) {
					return new Object[] { 0, "Specified .schematic file was not found!" };
				} else {
					StringBuilder reason = new StringBuilder();
					int result = deployShip(fileName, x, y, z, reason);
					return new Object[] { result, reason.toString() };
				}
			} else {
				return new Object[] { 4, "Invalid arguments count, you need .schematic file name, offsetX, offsetY and offsetZ!" };
			}
		}
		
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {
	}

	@Override
	public void detach(IComputerAccess computer) {
	}

	// IEnergySink methods implementation
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.SS_MAX_ENERGY_VALUE;
	}
	
	@Override
	public int getMaxSafeInput() {
		return Integer.MAX_VALUE;
	}
    
    @Override
    public boolean canInputEnergy(ForgeDirection from) {
    	return true;
    }
	
	@Override
	public boolean equals(IPeripheral other) {
		return other == this;
	}	
}
