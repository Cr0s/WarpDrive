package shipmod.chunk;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3Pool;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.ForgeDirection;
import shipmod.ShipMod;
import shipmod.entity.EntityShip;
import shipmod.entity.IShipTileEntity;
import shipmod.render.MobileChunkRenderer;

public class MobileChunk implements IBlockAccess
{
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_SIZE_EXP = 4;
    public static final int CHUNK_MEMORY_USING = 24576;
    private World worldObj;
    private EntityShip entityShip;
    private Map<ChunkPosition, ExtendedBlockStorage> blockStorageMap;
    public Map<ChunkPosition, TileEntity> chunkTileEntityMap;
    private boolean boundsInit;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private int blockCount;
    public boolean isChunkLoaded;
    public boolean isModified;
    public boolean chunkUpdated;
    private BiomeGenBase creationSpotBiome;
    @SideOnly(Side.CLIENT)
    public MobileChunkRenderer renderer;

    public MobileChunk(World world, EntityShip entityship)
    {
        this.worldObj = world;
        this.entityShip = entityship;
        this.blockStorageMap = new HashMap(1);
        this.chunkTileEntityMap = new HashMap(2);
        this.isChunkLoaded = false;
        this.isModified = false;
        this.chunkUpdated = false;
        this.boundsInit = false;
        this.minX = this.minY = this.minZ = this.maxX = this.maxY = this.maxZ = -1;
        this.blockCount = 0;

        if (FMLCommonHandler.instance().getSide().isClient())
        {
            this.renderer = new MobileChunkRenderer(this);
        }

        this.creationSpotBiome = BiomeGenBase.ocean;
    }

    public ExtendedBlockStorage getBlockStorage(int x, int y, int z)
    {
        ChunkPosition pos = new ChunkPosition(x >> 4, y >> 4, z >> 4);
        return (ExtendedBlockStorage)this.blockStorageMap.get(pos);
    }

    public ExtendedBlockStorage getBlockStorageOrCreate(int x, int y, int z)
    {
        ChunkPosition pos = new ChunkPosition(x >> 4, y >> 4, z >> 4);
        ExtendedBlockStorage storage = (ExtendedBlockStorage)this.blockStorageMap.get(pos);

        if (storage != null)
        {
            return storage;
        }
        else
        {
            storage = new ExtendedBlockStorage(pos.y, false);
            this.blockStorageMap.put(pos, storage);
            return storage;
        }
    }

    public int getBlockCount()
    {
        return this.blockCount;
    }

    public float getCenterX()
    {
        return (float)(this.minX + this.maxX) / 2.0F;
    }

    public float getCenterY()
    {
        return (float)(this.minY + this.maxY) / 2.0F;
    }

    public float getCenterZ()
    {
        return (float)(this.minZ + this.maxZ) / 2.0F;
    }

    public int minX()
    {
        return this.minX;
    }

    public int maxX()
    {
        return this.maxX;
    }

    public int minY()
    {
        return this.minY;
    }

    public int maxY()
    {
        return this.maxY;
    }

    public int minZ()
    {
        return this.minZ;
    }

    public int maxZ()
    {
        return this.maxZ;
    }

    public void setCreationSpotBiomeGen(BiomeGenBase biomegenbase)
    {
        this.creationSpotBiome = biomegenbase;
    }

    /**
     * Returns the block ID at coords x,y,z
     */
    public int getBlockId(int x, int y, int z)
    {
        ExtendedBlockStorage storage = this.getBlockStorage(x, y, z);
        return storage == null ? 0 : storage.getExtBlockID(x & 15, y & 15, z & 15);
    }

    /**
     * Returns the block metadata at coords x,y,z
     */
    public int getBlockMetadata(int x, int y, int z)
    {
        ExtendedBlockStorage storage = this.getBlockStorage(x, y, z);
        return storage == null ? 0 : storage.getExtBlockMetadata(x & 15, y & 15, z & 15);
    }

    public boolean setBlockIDWithMetadata(int x, int y, int z, int id, int meta)
    {
        ExtendedBlockStorage storage = this.getBlockStorageOrCreate(x, y, z);
        int i = x & 15;
        int j = y & 15;
        int k = z & 15;
        int currentid = storage.getExtBlockID(i, j, k);
        int currentmeta = storage.getExtBlockMetadata(i, j, k);

        if (currentid == 0 && (currentid != id || currentmeta != meta))
        {
            storage.setExtBlockID(i, j, k, id);
            storage.setExtBlockMetadata(i, j, k, meta);

            if (this.boundsInit)
            {
                this.minX = Math.min(this.minX, x);
                this.minY = Math.min(this.minY, y);
                this.minZ = Math.min(this.minZ, z);
                this.maxX = Math.max(this.maxX, x + 1);
                this.maxY = Math.max(this.maxY, y + 1);
                this.maxZ = Math.max(this.maxZ, z + 1);
            }
            else
            {
                this.boundsInit = true;
                this.minX = x;
                this.minY = y;
                this.minZ = z;
                this.maxX = x + 1;
                this.maxY = y + 1;
                this.maxZ = z + 1;
            }

            ++this.blockCount;
            this.entityShip.onChunkBlockAdded(id, meta);
            this.setChunkModified();

            if (Block.blocksList[id] != null && Block.blocksList[id].hasTileEntity(meta))
            {
                TileEntity tileentity = this.getChunkBlockTileEntity(x, y, z);

                /*if (tileentity == null)
                {
                    tileentity = Block.blocksList[id].createTileEntity(this.worldObj, meta);
                    this.setBlockTileEntity(x, y, z, tileentity);
                }*/

                if (tileentity != null)
                {
                    tileentity.updateContainingBlockInfo();
                    tileentity.blockType = Block.blocksList[id];
                    tileentity.blockMetadata = meta;
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean setBlockMetadata(int x, int y, int z, int meta)
    {
        ExtendedBlockStorage storage = this.getBlockStorage(x, y, z);

        if (storage == null)
        {
            return false;
        }
        else
        {
            int currentmeta = storage.getExtBlockMetadata(x, y & 15, z);

            if (currentmeta == meta)
            {
                return false;
            }
            else
            {
                this.setChunkModified();
                storage.setExtBlockMetadata(x & 15, y & 15, z & 15, meta);
                int id = storage.getExtBlockID(x & 15, y & 15, z & 15);

                if (id > 0 && Block.blocksList[id] != null && Block.blocksList[id].hasTileEntity(meta))
                {
                    TileEntity tileentity = this.getChunkBlockTileEntity(x, y, z);

                    if (tileentity != null)
                    {
                        tileentity.updateContainingBlockInfo();
                        tileentity.blockMetadata = meta;
                    }
                }

                return true;
            }
        }
    }

    public boolean setBlockAsFilledAir(int x, int y, int z)
    {
        ExtendedBlockStorage storage = this.getBlockStorage(x, y, z);

        if (storage == null)
        {
            return true;
        }
        else
        {
            int id = storage.getExtBlockID(x & 15, y & 15, z & 15);

            if (id != 0 && Block.blocksList[id] != null && !Block.blocksList[id].isAirBlock(this.worldObj, x, y, z))
            {
                return false;
            }
            else
            {
                storage.setExtBlockID(x & 15, y & 15, z & 15, 0);
                storage.setExtBlockMetadata(x & 15, y & 15, z & 15, 1);
                return true;
            }
        }
    }

    /**
     * Returns the TileEntity associated with a given block in X,Y,Z coordinates, or null if no TileEntity exists
     */
    public TileEntity getBlockTileEntity(int i, int j, int k)
    {
        return this.getChunkBlockTileEntity(i, j, k);
    }

    private TileEntity getChunkBlockTileEntity(int x, int y, int z)
    {
        ChunkPosition chunkposition = new ChunkPosition(x, y, z);
        TileEntity tileentity = (TileEntity)this.chunkTileEntityMap.get(chunkposition);

        if (tileentity != null && tileentity.isInvalid())
        {
            this.chunkTileEntityMap.remove(chunkposition);
            tileentity = null;
        }

        /*if (tileentity == null)
        {
            int l = this.getBlockId(x, y, z);
            int meta = this.getBlockMetadata(x, y, z);

            if (l <= 0 || !Block.blocksList[l].hasTileEntity(meta))
            {
                return null;
            }

            // GET OUT!
            //tileentity = Block.blocksList[l].createTileEntity(this.worldObj, meta);
            this.setBlockTileEntity(x, y, z, tileentity);
            tileentity = (TileEntity)this.chunkTileEntityMap.get(chunkposition);
        }*/

        return tileentity;
    }

    public void setBlockTileEntity(int x, int y, int z, TileEntity tileentity)
    {
        if (tileentity != null && !tileentity.isInvalid())
        {
            this.setChunkBlockTileEntity(x, y, z, tileentity);
        }
    }

    private void setChunkBlockTileEntity(int x, int y, int z, TileEntity tileentity)
    {
        ChunkPosition chunkposition = new ChunkPosition(x, y, z);
        tileentity.setWorldObj(this.worldObj);
        tileentity.xCoord = x;
        tileentity.yCoord = y;
        tileentity.zCoord = z;
        Block block = Block.blocksList[this.getBlockId(x, y, z)];

        if (block != null && block.hasTileEntity(this.getBlockMetadata(x, y, z)))
        {
            if (this.chunkTileEntityMap.containsKey(chunkposition))
            {
                ((TileEntity)this.chunkTileEntityMap.get(chunkposition)).invalidate();
            }

            tileentity.blockMetadata = this.getBlockMetadata(x, y, z);
            tileentity.validate();
            this.chunkTileEntityMap.put(chunkposition, tileentity);

            if (tileentity instanceof IShipTileEntity)
            {
                ((IShipTileEntity)tileentity).setVehicle(this.entityShip);
            }

            //if (this.isChunkLoaded)
            //{
            //    this.worldObj.addTileEntity(tileentity);
            //}
        }
    }

    public void addTileEntity(TileEntity tileentity)
    {
        int i = tileentity.xCoord;
        int j = tileentity.yCoord;
        int k = tileentity.zCoord;
        if (!worldObj.isAirBlock(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord) && worldObj.getBlockTileEntity(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord) != null) {
        	worldObj.removeBlockTileEntity(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord);
        	tileentity.validate();
        }
        this.setChunkBlockTileEntity(i, j, k, tileentity);

        //if (this.isChunkLoaded)
        //{
        //    this.worldObj.addTileEntity(tileentity);
        //}
    }

    public void removeChunkBlockTileEntity(int x, int y, int z)
    {
        ChunkPosition chunkposition = new ChunkPosition(x, y, z);

        if (this.isChunkLoaded)
        {
            TileEntity tileentity = (TileEntity)this.chunkTileEntityMap.remove(chunkposition);

            if (tileentity != null)
            {
                tileentity.invalidate();
            }
        }
    }

    public void onChunkLoad()
    {
        this.isChunkLoaded = true;
        // do not load mobile chunk entities
        //this.worldObj.addTileEntity(this.chunkTileEntityMap.values());
    }

    public void onChunkUnload()
    {
        this.isChunkLoaded = false;

        if (FMLCommonHandler.instance().getSide().isClient())
        {
            this.renderer.markRemoved();
        }
    }

    public void setChunkModified()
    {
        this.isModified = true;
        this.chunkUpdated = true;

        if (FMLCommonHandler.instance().getSide().isClient())
        {
            this.renderer.markDirty();
        }
    }

    /**
     * Any Light rendered on a 1.8 Block goes through here
     */
    public int getLightBrightnessForSkyBlocks(int i, int j, int k, int l)
    {
        int i1 = EnumSkyBlock.Sky.defaultLightValue;
        return i1 << 20 | l << 4;
    }

    public float getBrightness(int i, int j, int k, int l)
    {
        return 1.0F;
    }

    /**
     * Returns how bright the block is shown as which is the block's light value looked up in a lookup table (light
     * values aren't linear for brightness). Args: x, y, z
     */
    public float getLightBrightness(int i, int j, int k)
    {
        return 1.0F;
    }

    /**
     * Returns the block's material.
     */
    public Material getBlockMaterial(int i, int j, int k)
    {
        int l = this.getBlockId(i, j, k);
        return l == 0 ? Material.air : Block.blocksList[l].blockMaterial;
    }

    /**
     * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
     */
    public boolean isBlockOpaqueCube(int par1, int par2, int par3)
    {
        Block block = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return block != null && block.isOpaqueCube();
    }

    /**
     * Indicate if a material is a normal solid opaque cube.
     */
    public boolean isBlockNormalCube(int par1, int par2, int par3)
    {
        Block block = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return block != null && block.isBlockNormalCube(this.worldObj, par1, par2, par3);
    }

    /**
     * Returns true if the block at the specified coordinates is empty
     */
    public boolean isAirBlock(int x, int y, int z)
    {
        int id = this.getBlockId(x, y, z);
        return id == 0 || Block.blocksList[id] == null || Block.blocksList[id].isAirBlock(this.worldObj, x, y, z);
    }

    public boolean isBlockTakingWaterVolume(int x, int y, int z)
    {
        int id = this.getBlockId(x, y, z);

        if (id == 0)
        {
            if (this.getBlockMetadata(x, y, z) == 1)
            {
                return false;
            }
        }
        else if (Block.blocksList[id] == null || Block.blocksList[id].isAirBlock(this.worldObj, x, y, z))
        {
            return false;
        }

        return true;
    }

    /**
     * Gets the biome for a given set of x/z coordinates
     */
    public BiomeGenBase getBiomeGenForCoords(int i, int j)
    {
        return this.creationSpotBiome;
    }

    /**
     * Returns current world height.
     */
    public int getHeight()
    {
        return 16;
    }

    /**
     * set by !chunk.getAreLevelsEmpty
     */
    public boolean extendedLevelsInChunkCache()
    {
        return false;
    }

    public boolean isBlockSolidOnSide(int x, int y, int z, ForgeDirection side, boolean _default)
    {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000)
        {
            Block block = Block.blocksList[this.getBlockId(x, y, z)];
            return block == null ? false : block.isBlockSolidOnSide(this.worldObj, x, y, z, side);
        }
        else
        {
            return _default;
        }
    }

    /**
     * Returns true if the block at the given coordinate has a solid (buildable) top surface.
     */
    public boolean doesBlockHaveSolidTopSurface(int i, int j, int k)
    {
        return this.isBlockSolidOnSide(i, j, k, ForgeDirection.UP, false);
    }

    /**
     * Return the Vec3Pool object for this world.
     */
    public Vec3Pool getWorldVec3Pool()
    {
        return this.worldObj.getWorldVec3Pool();
    }

    /**
     * Is this block powering in the specified direction Args: x, y, z, direction
     */
    public int isBlockProvidingPowerTo(int i, int j, int k, int l)
    {
        return 0;
    }

    public void writeChunkData(DataOutput out) throws IOException
    {
        int count = 0;
        int i;
        int j;
        int k;
        int id;

        for (i = this.minX; i < this.maxX; ++i)
        {
            for (j = this.minY; j < this.maxY; ++j)
            {
                for (k = this.minZ; k < this.maxZ; ++k)
                {
                    id = this.getBlockId(i, j, k);

                    if (id != 0 && Block.blocksList[id] != null)
                    {
                        ++count;
                    }
                }
            }
        }

        ShipMod.modLogger.finest("Writing mobile chunk data: " + count);
        out.writeShort(count);

        for (i = this.minX; i < this.maxX; ++i)
        {
            for (j = this.minY; j < this.maxY; ++j)
            {
                for (k = this.minZ; k < this.maxZ; ++k)
                {
                    id = this.getBlockId(i, j, k);

                    if (id != 0 && Block.blocksList[id] != null)
                    {
                        out.writeByte(i);
                        out.writeByte(j);
                        out.writeByte(k);
                        out.writeShort(id);
                        out.writeInt(this.getBlockMetadata(i, j, k));
                    }
                }
            }
        }
    }

    public void readChunkData(DataInput in) throws IOException
    {
        short count = in.readShort();
        ShipMod.modLogger.finest("Reading mobile chunk data: " + count);

        for (int i = 0; i < count; ++i)
        {
            byte x = in.readByte();
            byte y = in.readByte();
            byte z = in.readByte();
            short id = in.readShort();
            int meta = in.readInt();
            this.setBlockIDWithMetadata(x, y, z, id, meta);
        }
    }

    public int getMemoryUsage()
    {
        return 2 + this.blockCount * 9;
    }
}
