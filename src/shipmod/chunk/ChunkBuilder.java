package shipmod.chunk;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import cr0s.WarpDrive.WarpDrive;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import shipmod.ShipMod;
import shipmod.entity.EntityShip;

public class ChunkBuilder
{
    public static final int RESULT_OK = 0;
    public static final int RESULT_BLOCK_OVERFLOW = 1;
    public static final int RESULT_MISSING_MARKER = 2;
    public static final int RESULT_ERROR_OCCURED = 3;
    private World worldObj;
    private int startX;
    private int startY;
    private int startZ;
    private Map<ChunkPosition, LocatedBlock> filledBlocks;
    private LocatedBlock shipMarkingBlock;
    private int result;
    private final int maxBlocks;
    public int xOffset;
    public int yOffset;
    public int zOffset;
    
    private boolean isActiveCoreOnBoard = false;

    public ChunkBuilder(World world, int x, int y, int z)
    {
        this.worldObj = world;
        this.filledBlocks = new HashMap(256);
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.maxBlocks = ShipMod.instance.modConfig.maxShipChunkBlocks;
        this.result = -1;
    }

    public int getBlockCount()
    {
        return this.filledBlocks.size();
    }

    public int getResult()
    {
        return this.result;
    }

    public Collection<LocatedBlock> getFilledBlocks()
    {
        return this.filledBlocks.values();
    }

    public LocatedBlock getShipMarker()
    {
        return this.shipMarkingBlock;
    }

    public void doFilling()
    {
        this.xOffset = this.startX;
        this.yOffset = this.startY;
        this.zOffset = this.startZ;

        try
        {
            this.fill(new HashSet(), this.startX, this.startY, this.startZ);

            if (this.shipMarkingBlock == null)
            {
                this.result = 2;
            }
            else
            {
            	if (!isActiveCoreOnBoard)
            		this.result = 0;
            	else 
            		this.result = 4;
            }
        }
        catch (ShipSizeOverflowException var2)
        {
            this.result = 1;
        }
        catch (StackOverflowError var3)
        {
            System.err.println(var3.toString());
            this.result = 3;
        }
        catch (Exception var4)
        {
            var4.printStackTrace();
            this.result = 3;
        }
    }

    private void fill(HashSet<ChunkPosition> set, int x, int y, int z) throws ShipSizeOverflowException
    {
        if (this.getBlockCount() > this.maxBlocks)
        {
            throw new ShipSizeOverflowException();
        }
        else
        {
            ChunkPosition pos = new ChunkPosition(x, y, z);

            if (!set.contains(pos) && !this.filledBlocks.containsKey(pos))
            {
                set.add(pos);
                int id = this.worldObj.getBlockId(x, y, z);

                if (id != 0 && this.canUseBlockForVehicle(Block.blocksList[id], x, y, z))
                {
                    this.xOffset = Math.min(this.xOffset, x);
                    this.yOffset = Math.min(this.yOffset, y);
                    this.zOffset = Math.min(this.zOffset, z);

                    if (id == ShipMod.blockMarkShip.blockID && this.shipMarkingBlock == null)
                    {
                        this.shipMarkingBlock = new LocatedBlock(id, this.worldObj.getBlockMetadata(x, y, z), pos);
                        this.filledBlocks.put(pos, this.shipMarkingBlock);
                    }
                    else
                    {
                    	if (id == WarpDrive.WARP_CORE_BLOCKID) {
                    		isActiveCoreOnBoard = worldObj.getBlockMetadata(x, y, z) != 0;
                    	}
                        this.filledBlocks.put(pos, new LocatedBlock(id, this.worldObj.getBlockMetadata(x, y, z), pos));
                    }

                    this.fill(set, x - 1, y, z);
                    this.fill(set, x, y - 1, z);
                    this.fill(set, x, y, z - 1);
                    this.fill(set, x + 1, y, z);
                    this.fill(set, x, y + 1, z);
                    this.fill(set, x, y, z + 1);

                    if (ShipMod.instance.modConfig.connectDiagonalBlocks1)
                    {
                        this.fill(set, x - 1, y - 1, z);
                        this.fill(set, x + 1, y - 1, z);
                        this.fill(set, x + 1, y + 1, z);
                        this.fill(set, x - 1, y + 1, z);
                        this.fill(set, x - 1, y, z - 1);
                        this.fill(set, x + 1, y, z - 1);
                        this.fill(set, x + 1, y, z + 1);
                        this.fill(set, x - 1, y, z + 1);
                        this.fill(set, x, y - 1, z - 1);
                        this.fill(set, x, y + 1, z - 1);
                        this.fill(set, x, y + 1, z + 1);
                        this.fill(set, x, y - 1, z + 1);
                    }
                }
            }
        }
    }

    public boolean canUseBlockForVehicle(Block block, int x, int y, int z)
    {
        return block != null && !block.isAirBlock(this.worldObj, x, y, z) && !block.blockMaterial.isLiquid() && !ShipMod.instance.modConfig.forbiddenBlocks.contains(Integer.valueOf(block.blockID));
    }

    public EntityShip getEntity(World world)
    {
        if (this.result != 0)
        {
            return null;
        }
        else
        {
            EntityShip entity = new EntityShip(world);
            Iterator i$ = this.getFilledBlocks().iterator();
            LocatedBlock block;

            while (i$.hasNext())
            {
                block = (LocatedBlock)i$.next();
                int ix = block.coords.x - this.xOffset;
                int iy = block.coords.y - this.yOffset;
                int iz = block.coords.z - this.zOffset;
                TileEntity tileentity = world.getBlockTileEntity(block.coords.x, block.coords.y, block.coords.z);
                
                // cloning tile entity
                NBTTagCompound nbt = null;
                
                if (tileentity != null)
                {
                	nbt = new NBTTagCompound();
                    tileentity.writeToNBT(nbt);
                    world.removeBlockTileEntity(block.coords.x, block.coords.y, block.coords.z);
                }

                if (entity.getShipChunk().setBlockIDWithMetadata(ix, iy, iz, block.blockID, block.blockMeta))
                {
                    world.setBlock(block.coords.x, block.coords.y, block.coords.z, 0, 1, 2);
                    
                    if (nbt != null) {
                    	TileEntity newTE = TileEntity.createAndLoadEntity(nbt);
                    	entity.getShipChunk().setBlockTileEntity(ix, iy, iz, newTE);
                    }
                }
            }

            i$ = this.getFilledBlocks().iterator();

            while (i$.hasNext())
            {
                block = (LocatedBlock)i$.next();
                world.setBlockToAir(block.coords.x, block.coords.y, block.coords.z);
            }

            entity.setFrontDirection(this.shipMarkingBlock.blockMeta & 3);
            entity.seatX = this.shipMarkingBlock.coords.x - this.xOffset;
            entity.seatY = this.shipMarkingBlock.coords.y - this.yOffset;
            entity.seatZ = this.shipMarkingBlock.coords.z - this.zOffset;
            entity.getShipChunk().setCreationSpotBiomeGen(world.getBiomeGenForCoords(this.shipMarkingBlock.coords.x, this.shipMarkingBlock.coords.z));
            entity.getShipChunk().setChunkModified();
            entity.getShipChunk().onChunkLoad();
            entity.setLocationAndAngles((double)((float)this.xOffset + entity.getShipChunk().getCenterX()), (double)this.yOffset, (double)((float)this.zOffset + entity.getShipChunk().getCenterZ()), 0.0F, 0.0F);
            return entity;
        }
    }
}
