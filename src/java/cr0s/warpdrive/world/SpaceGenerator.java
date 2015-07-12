/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cr0s.warpdrive.world;

import java.util.List;
import java.util.Random;

import cr0s.warpdrive.WarpDrive;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;

public class SpaceGenerator extends ChunkProviderGenerate implements IChunkProvider
{
    private Random rand;
    private BiomeGenBase[] biomesForGeneration = new BiomeGenBase[1];

    /**
     * Reference to the World object.
     */
    private World worldObj;

    public SpaceGenerator(World par1World, long par2)
    {
        super(par1World, par2, false);
        rand = new Random();
        this.worldObj = par1World;
    }

    @Override
    public Chunk provideChunk(int par1, int par2)
    {
        this.rand.setSeed(par1 * 341873128712L + par2 * 132897987541L);
        byte[] var3 = new byte[32768];
        generateTerrain(par1, par2, var3);
        //this.caveGenerator.generate(this, this.worldObj, par1, par2, var3);
        this.biomesForGeneration[0] = WarpDrive.spaceBiome;
        Chunk var4 = new Chunk(this.worldObj, var3, par1, par2);
        var4.generateSkylightMap();
        return var4;
    }

    @Override
    public Chunk loadChunk(int var1, int var2)
    {
        // TODO Auto-generated method stub
        return this.provideChunk(var1, var2);
    }

    @Override
    public void populate(IChunkProvider var1, int var2, int var3)
    {
        //super.populate(var1, var2, var3);
        // Generate chunk population
        // GameRegistry.generateWorld(var2, var3, worldObj, var1, var1);
    }

    @Override
    public boolean saveChunks(boolean var1, IProgressUpdate var2)
    {
        return super.saveChunks(var1, var2);
    }

    @Override
    public void generateTerrain(int par1, int par2, byte[] par3ArrayOfByte)
    {
        this.biomesForGeneration[0] = WarpDrive.spaceBiome;
        // if (!"Space".equals(worldObj.provider.getDimensionName())) {
        // }
        /*byte var4 = 4;
        byte var5 = 16;
        byte var6 = 16;
        int var7 = var4 + 1;
        byte var8 = 17;
        int var9 = var4 + 1;
        this.biomesForGeneration[0] = WarpDrive.spaceBiome;
        this.noiseArray = this.initializeNoiseField(this.noiseArray, par1 * var4, 0, par2 * var4, var7, var8, var9);

        for (int var10 = 0; var10 < var4; ++var10) {
            for (int var11 = 0; var11 < var4; ++var11) {
                for (int var12 = 0; var12 < var5; ++var12) {
                    double var13 = 0.125D;
                    double var15 = this.noiseArray[((var10 + 0) * var9 + var11 + 0) * var8 + var12 + 0];
                    double var17 = this.noiseArray[((var10 + 0) * var9 + var11 + 1) * var8 + var12 + 0];
                    double var19 = this.noiseArray[((var10 + 1) * var9 + var11 + 0) * var8 + var12 + 0];
                    double var21 = this.noiseArray[((var10 + 1) * var9 + var11 + 1) * var8 + var12 + 0];
                    double var23 = (this.noiseArray[((var10 + 0) * var9 + var11 + 0) * var8 + var12 + 1] - var15) * var13;
                    double var25 = (this.noiseArray[((var10 + 0) * var9 + var11 + 1) * var8 + var12 + 1] - var17) * var13;
                    double var27 = (this.noiseArray[((var10 + 1) * var9 + var11 + 0) * var8 + var12 + 1] - var19) * var13;
                    double var29 = (this.noiseArray[((var10 + 1) * var9 + var11 + 1) * var8 + var12 + 1] - var21) * var13;

                    for (int var31 = 0; var31 < 8; ++var31) {
                        double var32 = 0.25D;
                        double var34 = var15;
                        double var36 = var17;
                        double var38 = (var19 - var15) * var32;
                        double var40 = (var21 - var17) * var32;

                        for (int var42 = 0; var42 < 4; ++var42) {
                            int var43 = var42 + var10 * 4 << 11 | 0 + var11 * 4 << 7 | var12 * 8 + var31;
                            short var44 = 128;
                            var43 -= var44;
                            double var45 = 0.25D;
                            double var49 = (var36 - var34) * var45;
                            double var47 = var34 - var49;

                            for (int var51 = 0; var51 < 4; ++var51) {
                                if ((var47 += var49) > 0.0D) {
                                    par3ArrayOfByte[var43 += var44] = (byte) Block.stone.blockID;
                                } else if (var12 * 8 + var31 < var6) {
                                    par3ArrayOfByte[var43 += var44] = (byte) Block.sandStone.blockID;
                                } else {
                                    par3ArrayOfByte[var43 += var44] = 0;
                                }
                            }

                            var34 += var38;
                            var36 += var40;
                        }

                        var15 += var23;
                        var17 += var25;
                        var19 += var27;
                        var21 += var29;
                    }
                }
            }
        }*/
    }

    @Override
    public void replaceBlocksForBiome(int par1, int par2, byte[] par3ArrayOfByte, BiomeGenBase[] par4ArrayOfBiomeGenBase)
    {
        /*ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(this, par1, par2, par3ArrayOfByte, par4ArrayOfBiomeGenBase);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) return;    */
    }

    @Override
    public boolean canSave()
    {
        // TODO Auto-generated method stub
        return super.canSave();
    }

    @Override
    public String makeString()
    {
        // TODO Auto-generated method stub
        return super.makeString();
    }

    @Override
    public List getPossibleCreatures(EnumCreatureType var1, int var2, int var3, int var4)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChunkPosition findClosestStructure(World var1, String var2,
            int var3, int var4, int var5)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLoadedChunkCount()
    {
        // TODO Auto-generated method stub
        return super.getLoadedChunkCount();
    }

    @Override
    public void recreateStructures(int var1, int var2)
    {
        // TODO Auto-generated method stub
    }
}