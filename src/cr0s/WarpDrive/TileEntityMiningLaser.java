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
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet62LevelSound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityMiningLaser extends TileEntity implements IPeripheral{
	private final int MAX_BOOSTERS_NUMBER = 1;
	
	private int dx, dz, dy;
	private boolean isMining = false;
	private boolean isQuarry = false;
	
    private String[] methodsArray = { 
                                        "startMining", "stop",       // 0, 1
                                        "isMining",                  // 2
                                        "startQuarry",				 // 3
                                        "getMinerState"              // 4
                                    };
    
   public static final ArrayList<Integer> valuableOres = new ArrayList<Integer>(Arrays.asList(
		Block.oreIron.blockID,
		Block.oreGold.blockID,
		Block.oreCoal.blockID,
		Block.oreEmerald.blockID,
		Block.oreLapis.blockID,
		Block.oreRedstoneGlowing.blockID,
		Block.oreRedstone.blockID,
		Block.oreNetherQuartz.blockID,
		247, // IC
		248, // IC
		249  // IC
	));
   
   public static final ArrayList<Integer> otherValuables = new ArrayList<Integer>(Arrays.asList(
		Block.wood.blockID,
		Block.planks.blockID,
		Items.getItem("rubberWood").itemID,
		Block.rail.blockID,
		902, // Quarz (AE),
		Block.oreDiamond.blockID,
		Block.obsidian.blockID,
		Block.web.blockID,
		Block.fence.blockID,
		Block.torchWood.blockID
	));   
   
   private final int SCAN_DELAY = 20 * 5;
   private int delayTicksScan = 0;
   
   private final int MINE_DELAY = 25;
   private int delayTicksMine = 0;
   
   private final int EU_PER_LAYER = 250;
      
   private int currentMode = 0; // 0 - scan next layer, 1 - collect valuables
   
   private int energy;
   private int currentLayer;
   
   private ArrayList<Vector3> valuablesInLayer = new ArrayList<Vector3>();
   private int valuableIndex = 0;
   
   Vector3 minerVector;
   private long uid = 0;

    @Override
    public void updateEntity() {
    	if (isMining) {
    		if (currentMode == 0) {
    			if (++delayTicksScan > SCAN_DELAY) {
    				delayTicksScan = 0;
    				currentMode = 1;
    				
    				if (currentLayer <= 0) {
    					isMining = false;
    					return;
    				}
    				
    				int blockID = worldObj.getBlockId(xCoord, currentLayer, zCoord);
    				int blockMeta = worldObj.getBlockMetadata(xCoord, currentLayer, zCoord);
    				
    				// That block is too hard
    				if (blockID != 0 && worldObj.getBlockMaterial(xCoord, currentLayer, zCoord) != Material.water && Block.blocksList[blockID].blockHardness > Block.obsidian.blockHardness) {
    					isMining = false;
    					return;
    				}
    				
    				sendLaserPacket(minerVector, new Vector3(xCoord, currentLayer, zCoord).add(0.5), 0, 0, 1, 20, 0, 50);
    				worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:hilaser", 4F, 1F);
    				
    				harvestBlock(new Vector3(xCoord, currentLayer, zCoord));
    				
    				energy -= EU_PER_LAYER;
    				
    				if (energy > 0) {
    					scanLayer();
    				} else {
    					System.out.println("[ML] Out of energy");
    					isMining = false;
    				}
    				
    				currentLayer--;
    			}
    		} else {
    			if (++delayTicksMine > MINE_DELAY) {
    				delayTicksMine = 0;
    				if (valuableIndex < valuablesInLayer.size()) {
    					//System.out.println("[ML] Mining: " + (valuableIndex + 1) + "/" + valuablesInLayer.size());
    					Vector3 valuable = valuablesInLayer.get(valuableIndex++);
    					
    					// Mine valuable ore
    					int blockID = worldObj.getBlockId(valuable.intX(), valuable.intY(), valuable.intZ());

    					// Skip if block is too hard or its empty block
    					if (worldObj.getBlockMaterial(xCoord, currentLayer, zCoord) != Material.water && (worldObj.isAirBlock(valuable.intX(), valuable.intY(), valuable.intZ()) || Block.blocksList[blockID].blockHardness > Block.obsidian.blockHardness)) {
    						return;
    					}
    					
    					sendLaserPacket(minerVector, new Vector3(valuable.intX(), valuable.intY(), valuable.intZ()).add(0.5), 1, 1, 0, 2 * MINE_DELAY, 0, 50);
    					
    					worldObj.playSoundEffect(xCoord + 0.5f, yCoord, zCoord + 0.5f, "warpdrive:lowlaser", 4F, 1F);
    					
    					harvestBlock(valuable);
    				} else {
    					currentMode = 0;
    				}
    			}
    		}
    	}
    }
    
    private void harvestBlock(Vector3 valuable) {
		int blockID = worldObj.getBlockId(valuable.intX(), valuable.intY(), valuable.intZ());
		int blockMeta = worldObj.getBlockMetadata(valuable.intX(), valuable.intY(), valuable.intZ());
		
		if (blockID != Block.waterMoving.blockID && blockID != Block.waterStill.blockID && blockID != Block.lavaMoving.blockID && blockID != Block.lavaStill.blockID) {
			List<ItemStack> stacks = getItemStackFromBlock(valuable.intX(), valuable.intY(), valuable.intZ(), blockID, blockMeta);
			
			if (stacks != null) {
				for (ItemStack stack : stacks) {
					putInChest(findChest(), stack);
				}
			}
			
			worldObj.playAuxSFXAtEntity(null, 2001, valuable.intX(), valuable.intY(), valuable.intZ(), blockID + (blockMeta << 12));
		// Evaporate water
		} else if (blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID) {
			worldObj.playSoundEffect((double)((float)valuable.intX() + 0.5F), (double)((float)valuable.intY() + 0.5F), (double)((float)valuable.intZ() + 0.5F), "random.fizz", 0.5F, 2.6F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.8F);			
		}

		worldObj.setBlockToAir(valuable.intX(), valuable.intY(), valuable.intZ());
    }
    
    private TileEntityChest findChest() {
    	TileEntity result = null;
        result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }

        result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }
        
        result = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
        if (result != null && result instanceof TileEntityChest) {
            return (TileEntityChest) result;
        }

        return null;
    }
    
	public List<ItemStack> getItemStackFromBlock(int i, int j, int k, int blockID, int blockMeta) {
		Block block = Block.blocksList[blockID];
		
		if (block == null)
		return null;
		
		return block.getBlockDropped(worldObj, i, j, k, blockMeta, 0);
	}
   
    public static int putInChest(TileEntityChest inventory, ItemStack itemStackSource)
    {
    	if (inventory == null || itemStackSource == null) {
    		return 0;
    	}
    	
        int transferred = 0;
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            if(!inventory.isItemValidForSlot(i, itemStackSource))
                continue;
            ItemStack itemStack = inventory.getStackInSlot(i);
            if(itemStack == null || !itemStack.isItemEqual(itemStackSource))
                continue;
            int transfer = Math.min(itemStackSource.stackSize - transferred, itemStack.getMaxStackSize() - itemStack.stackSize);
            itemStack.stackSize += transfer;
            transferred += transfer;
            if(transferred == itemStackSource.stackSize)
                return transferred;
        }

        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            if(!inventory.isItemValidForSlot(i, itemStackSource))
                continue;
            ItemStack itemStack = inventory.getStackInSlot(i);
            if(itemStack != null)
                continue;
            int transfer = Math.min(itemStackSource.stackSize - transferred, itemStackSource.getMaxStackSize());
            ItemStack dest = copyWithSize(itemStackSource, transfer);
            inventory.setInventorySlotContents(i, dest);
            transferred += transfer;
            if(transferred == itemStackSource.stackSize)
                return transferred;
        }

        return transferred;
    }	
	
    public static ItemStack copyWithSize(ItemStack itemStack, int newSize)
    {
        ItemStack ret = itemStack.copy();
        ret.stackSize = newSize;
        return ret;
    }    
    
    private void scanLayer() {
    	//System.out.println("Scanning layer");
    	valuablesInLayer.clear();
    	
        int xmax, zmax, x1, x2, z1, z2;
        int xmin, zmin;

        final int CUBE_SIDE = 8;
        
        x1 = xCoord + CUBE_SIDE / 2;
        x2 = xCoord - CUBE_SIDE / 2;
        
        if (x1 < x2) {
            xmin = x1;
            xmax = x2;
        } else
        {
            xmin = x2;
            xmax = x1;
        }

        z1 = zCoord + CUBE_SIDE / 2;
        z2 = zCoord - CUBE_SIDE / 2;
        
        if (z1 < z2) {
            zmin = z1;
            zmax = z2;
        } else
        {
            zmin = z2;
            zmax = z1;
        }
         
        //System.out.println("Layer: xmax: " + xmax + ", xmin: " + xmin);
        //System.out.println("Layer: zmax: " + zmax + ", zmin: " + zmin);
        
        // Search for valuable blocks
        for (int x = xmin; x <= xmax; x++) {
            for (int z = zmin; z <= zmax; z++) {
            	if (isQuarry) { // Quarry collects all blocks
             	   int blockID = worldObj.getBlockId(x, currentLayer, z);
             	   if (!worldObj.isAirBlock(x, currentLayer, z) && blockID != Block.lavaMoving.blockID && blockID != Block.lavaStill.blockID && (Block.blocksList[blockID].blockHardness <= Block.obsidian.blockHardness))
             		   valuablesInLayer.add(new Vector3(x, currentLayer, z));
            	} else // Not-quarry collect only valuables blocks
                if (valuableOres.contains((worldObj.getBlockId(x, currentLayer, z))) || otherValuables.contains((worldObj.getBlockId(x, currentLayer, z)))) {
                   	valuablesInLayer.add(new Vector3(x, currentLayer, z));
                }
            }
        }
        
        valuableIndex = 0;
        //System.out.println("[ML] Found " + valuablesInLayer.size() + " valuables");
    	
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

        return null;
    }
      
    public void sendLaserPacket(Vector3 source, Vector3 dest, float r, float g, float b, int age, int energy, int radius) {  
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.SERVER) {
        	if (source == null || dest == null || worldObj == null) {
        		return;
        	}
        	
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
            	outputStream.writeInt(0);           	
            } catch (Exception ex) {
                    ex.printStackTrace();
            }
            
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "WarpDriveBeam";
            packet.data = bos.toByteArray();
            packet.length = bos.size();
            
        	MinecraftServer.getServer().getConfigurationManager().sendToAllNear(source.intX(), source.intY(), source.intZ(), radius, worldObj.provider.dimensionId, packet);	
        	MinecraftServer.getServer().getConfigurationManager().sendToAllNear(dest.intX(), dest.intY(), dest.intZ(), radius, worldObj.provider.dimensionId, packet);	
        }	
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        
        isMining = tag.getBoolean("isMining");
        isQuarry = tag.getBoolean("isQuarry");
        currentLayer = tag.getInteger("currentLayer");
        energy = tag.getInteger("energy");
        
        minerVector = new Vector3(xCoord, yCoord - 1, zCoord).add(0.5);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        
        tag.setBoolean("isMining", isMining);
        tag.setBoolean("isQuarry", isQuarry);
        tag.setInteger("currentLayer", currentLayer);
        tag.setInteger("energy", energy);        
    }    
    
    // IPeripheral methods implementation
    @Override
    public String getType() {
        return "mininglaser";
    }

    @Override
    public String[] getMethodNames() {
        return methodsArray;
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
        switch (method) {
            case 0: // startMining()
            	isMining = true;
            	isQuarry = false;
            	energy = collectEnergyFromBoosters();
            	delayTicksScan = 0;
            	currentMode = 0;
            	
            	minerVector = new Vector3(xCoord, yCoord - 1, zCoord).add(0.5);
            	currentLayer = yCoord - 1;
                
                return new Object[] { 0 };
                                
            case 1: // stop()
            	isMining = false;
            	break;
            	
            case 2: // isMining() 
            	return new Boolean[] { isMining };
            	
            case 3: // startQuarry()
            	if (isMining) {
            		return new Object[] { -1 };   	
            	}
            	isMining = true;
            	isQuarry = true;
            	energy = collectEnergyFromBoosters();
            	delayTicksScan = 0;
            	currentMode = 0;
            	
            	minerVector = new Vector3(xCoord, yCoord - 1, zCoord).add(0.5);
            	currentLayer = yCoord - 1;
                
                return new Object[] { 0 };    
                
            // State is: state, energy, currentLayer, valuablesInLayer, valuablesMined = getMinerState()
            case 4: // getMinerState()
            	String state = "not mining";
            	Integer valuablesInLayer, valuablesMined;
            	
            	if (isMining) {
            		valuablesInLayer = this.valuablesInLayer.size();
            		valuablesMined = this.valuableIndex;
            		
            		state = "mining" + ((isQuarry) ? " (quarry mode)" : ""); 
            		return new Object[] {state, energy, currentLayer, valuablesMined, valuablesInLayer};
            	}
            	
            	return new Object[] {state, energy, currentLayer, 0, 0};
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
