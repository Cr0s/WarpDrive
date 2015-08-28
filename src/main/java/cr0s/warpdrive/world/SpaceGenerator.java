package cr0s.warpdrive.world;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import cr0s.warpdrive.WarpDrive;

public class SpaceGenerator extends ChunkProviderGenerate {
	private World worldObj;
	private Random rand;
	private BiomeGenBase[] biomesForGeneration = new BiomeGenBase[1];
	
	public SpaceGenerator(World worldObj, long par2) {
		super(worldObj, par2, false);
		rand = new Random(par2);
		this.worldObj = worldObj;
	}
	
	@Override
	public Chunk provideChunk(int par1, int par2) {
		this.rand.setSeed(par1 * 341873128712L + par2 * 132897987541L);
		Block[] var3 = new Block[32768];
		// this.biomesForGeneration[0] = WarpDrive.spaceBiome;
		// this.caveGenerator.generate(this, this.worldObj, par1, par2, var3);
		this.biomesForGeneration[0] = WarpDrive.spaceBiome;
		Chunk var4 = new Chunk(worldObj, var3, par1, par2);
		var4.generateSkylightMap();
		return var4;
	}
	
	@Override
	public void populate(IChunkProvider var1, int var2, int var3) {
		// super.populate(var1, var2, var3);
		// Generate chunk population
		// GameRegistry.generateWorld(var2, var3, worldObj, var1, var1);
	}
	
	@Override
	public List getPossibleCreatures(EnumCreatureType var1, int var2, int var3, int var4) {
		return null;
	}
	
	@Override
	public ChunkPosition func_147416_a(World var1, String var2, int var3, int var4, int var5) {
		// no structure generation
		return null;
	}

	@Override
	public void recreateStructures(int var1, int var2) {
		// no structure generation
	}
}