package cr0s.WarpDrive;

import cpw.mods.fml.common.Loader;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenSmallShip extends WorldGenerator
{
	private boolean corrupted;
	private int solarType;

	public WorldGenSmallShip(boolean corrupted)
	{
		this.corrupted = corrupted;
	}

	@Override
	public boolean generate(World world, Random rand, int i, int j, int k)
	{

		world.setBlock(i + 0, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 0, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 1, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 1, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 1, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 1, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 1, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 1, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 3, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 3, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 3, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 2, j + 4, k + 6, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 2, j + 4, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 2, j + 4, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 2, j + 5, k + 6, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 2, j + 5, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 2, j + 5, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 3, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 2, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 2, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 2, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 2, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 2, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 3, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 3, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 4, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 4, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 5, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 5, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 6, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 6, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 3, j + 6, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 2, k + 5, Block.glowStone.blockID);
		world.setBlock(i + 4, j + 2, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 2, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 2, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 2, k + 9, Block.glowStone.blockID);
		world.setBlock(i + 4, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 3, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 3, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 4, k + 4, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 4, j + 4, k + 10, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 4, j + 5, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 5, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 5, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 5, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 6, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 6, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 4, j + 6, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 2, k + 4, Block.glowStone.blockID);
		world.setBlock(i + 5, j + 2, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 2, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 2, k + 7, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 5, j + 2, k + 8, Block.cloth.blockID, 8, 0);
		world.setBlock(i + 5, j + 2, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 2, k + 10, Block.glowStone.blockID);
		world.setBlock(i + 5, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 3, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 3, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 4, k + 3, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 5, j + 4, k + 11, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 5, j + 5, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 5, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 5, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 5, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 6, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 6, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 6, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 6, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 6, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 7, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 7, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 5, j + 7, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 2, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 2, k + 6, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 6, j + 2, k + 7, Block.cloth.blockID, 8, 0);
		world.setBlock(i + 6, j + 2, k + 8, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 6, j + 2, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 3, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 3, k + 3, Block.chest.blockID, 3, 0);
		fillChestWithBonuses(world, rand, i + 6, j + 3, k + 3);
		world.setBlock(i + 6, j + 3, k + 11, Block.chest.blockID, 2, 0);
		fillChestWithBonuses(world, rand, i + 6, j + 3, k + 11);
		world.setBlock(i + 6, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 4, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 4, k + 3, Block.chest.blockID, 3, 0);
		fillChestWithBonuses(world, rand, i + 6, j + 4, k + 3);
		world.setBlock(i + 6, j + 4, k + 11, Block.chest.blockID, 2, 0);
		fillChestWithBonuses(world, rand, i + 6, j + 4, k + 11);
		world.setBlock(i + 6, j + 4, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 5, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 5, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 6, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 6, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 6, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 6, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 6, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 6, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 7, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 7, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 7, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 7, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 6, j + 7, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 2, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 2, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 2, k + 7, Block.cloth.blockID, 8, 0);
		world.setBlock(i + 7, j + 2, k + 8, Block.cloth.blockID, 8, 0);
		world.setBlock(i + 7, j + 2, k + 9, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 7, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 3, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 4, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 7, j + 4, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 7, j + 5, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 7, j + 5, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 7, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 6, k + 7, WarpDriveConfig.ASP, solarType, 0);
		world.setBlock(i + 7, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 6, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 7, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 7, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 7, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 7, j + 7, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 7, j + 7, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 2, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 2, k + 6, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 8, j + 2, k + 7, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 8, j + 2, k + 8, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 8, j + 2, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 3, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 4, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 8, j + 4, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 8, j + 5, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 8, j + 5, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 8, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 6, k + 7, WarpDriveConfig.ASP, solarType, 0);
		world.setBlock(i + 8, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 6, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 7, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 7, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 7, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 7, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 8, j + 7, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 7, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 8, j + 7, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 2, k + 5, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 9, j + 2, k + 6, Block.cloth.blockID, 8, 0);
		world.setBlock(i + 9, j + 2, k + 7, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 9, j + 2, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 2, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 2, k + 10, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 9, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 3, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 4, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 9, j + 4, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 9, j + 5, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 9, j + 5, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 9, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 6, k + 7, WarpDriveConfig.ASP, solarType, 0);
		// Placing air generator
		world.setBlock(i + 9, j + 5, k + 7, WarpDriveConfig.airgenID, 0, 0);
		world.setBlock(i + 9, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 6, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 7, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 7, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 7, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 7, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 9, j + 7, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 7, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 7, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 8, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 9, j + 8, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 2, k + 5, Block.cloth.blockID, 8, 0);
		world.setBlock(i + 10, j + 2, k + 6, Block.cloth.blockID, 8, 0);
		world.setBlock(i + 10, j + 2, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 2, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 2, k + 9, Block.cloth.blockID, 8, 0);
		world.setBlock(i + 10, j + 2, k + 10, Block.cloth.blockID, 14, 0);
		world.setBlock(i + 10, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 3, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 3, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 10, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 4, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 10, j + 4, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 10, j + 5, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 10, j + 5, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 10, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 6, k + 7, WarpDriveConfig.ASP, solarType, 0);
		// Placing air generator
		world.setBlock(i + 10, j + 5, k + 7, WarpDriveConfig.airgenID, 0, 0);
		world.setBlock(i + 10, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 6, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 7, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 7, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 7, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 7, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 10, j + 7, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 7, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 7, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 8, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 10, j + 8, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 2, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 2, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 2, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 2, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 2, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 3, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 3, k + 6, WorldGenStructure.getGlassBlock(corrupted, rand));

		// Place warp-controller
		if (rand.nextBoolean())
		{
			world.setBlock(i + 11, j + 3, k + 7, WarpDriveConfig.controllerID);
		}

		world.setBlock(i + 11, j + 3, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 4, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 4, k + 6, WorldGenStructure.getGlassBlock(corrupted, rand));

		// Place computer
		if (rand.nextBoolean())
		{
			world.setBlock(i + 11, j + 4, k + 7, 1225, 16384, 0);
		}

		world.setBlock(i + 11, j + 4, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 4, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 5, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 5, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 5, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 6, k + 7, WarpDriveConfig.ASP, solarType, 0);
		world.setBlock(i + 11, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 6, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 7, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 7, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 7, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 7, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 7, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 7, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 7, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 8, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 8, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 9, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 9, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 2, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 2, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 2, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 2, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 3, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 3, k + 6, WorldGenStructure.getGlassBlock(corrupted, rand));

		// Place warp-core
		if (rand.nextBoolean())
		{
			world.setBlock(i + 12, j + 3, k + 7, WarpDriveConfig.coreID);
		}

		world.setBlock(i + 12, j + 3, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 4, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 4, k + 6, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 4, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 4, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 5, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 5, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 6, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 7, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 7, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 7, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 7, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 7, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 1, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 1, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 1, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 1, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 1, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 1, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 1, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 2, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 2, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 2, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 2, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 2, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 3, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 11, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 13, j + 4, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 5, k + 2, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 5, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 5, k + 4, Block.glowStone.blockID);
		world.setBlock(i + 13, j + 5, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 5, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 5, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 5, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 5, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 5, k + 10, Block.glowStone.blockID);
		world.setBlock(i + 13, j + 5, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 5, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 6, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 6, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 6, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 6, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 6, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 6, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 7, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 7, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 13, j + 7, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 3, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 3, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 3, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 3, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 3, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 3, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 3, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 4, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 4, k + 5, Block.blockRedstone.blockID);
		world.setBlock(i + 14, j + 4, k + 6, Block.blockRedstone.blockID);
		world.setBlock(i + 14, j + 4, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 4, k + 8, Block.blockRedstone.blockID);
		world.setBlock(i + 14, j + 4, k + 9, Block.blockRedstone.blockID);
		world.setBlock(i + 14, j + 4, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 5, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 5, k + 5, Block.blockRedstone.blockID);
		world.setBlock(i + 14, j + 5, k + 6, Block.blockRedstone.blockID);
		world.setBlock(i + 14, j + 5, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 5, k + 8, Block.blockRedstone.blockID);
		world.setBlock(i + 14, j + 5, k + 9, Block.blockRedstone.blockID);
		world.setBlock(i + 14, j + 5, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 6, k + 5, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 6, k + 6, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 6, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 6, k + 8, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 6, k + 9, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 14, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 2, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 2, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 2, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 2, k + 11, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 3, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 3, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 4, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 5, k + 7, Block.blockRedstone.blockID);
		world.setBlock(i + 15, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 6, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 15, j + 6, k + 10, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 16, j + 4, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 16, j + 5, k + 7, Block.blockRedstone.blockID);
		world.setBlock(i + 16, j + 6, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 17, j + 5, k + 7, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 2, k + 9, Block.trapdoor.blockID, 10, 0);
		spawnNPC(world, i + 9, j + 3, k + 5);
		return true;
	}

	public void spawnNPC(World world, int i, int j, int k)
	{
		int numMobs = 2 + world.rand.nextInt(10);

		if (world.rand.nextBoolean())   // Villagers
		{
			for (int idx = 0; idx < numMobs; idx++)
			{
				EntityVillager entityvillager = new EntityVillager(world, 0);
				entityvillager.setLocationAndAngles((double)i + 0.5D, (double)j, (double)k + 0.5D, 0.0F, 0.0F);
				world.spawnEntityInWorld(entityvillager);
			}
		}
		else   // Zombies
		{
			for (int idx = 0; idx < numMobs; idx++)
			{
				EntityZombie entityzombie = new EntityZombie(world);
				entityzombie.setLocationAndAngles((double)i + 0.5D, (double)j, (double)k + 0.5D, 0.0F, 0.0F);
				world.spawnEntityInWorld(entityzombie);
			}
		}
	}

	public void fillChestWithBonuses(World worldObj, Random rand, int x, int y, int z)
	{
		TileEntity te = worldObj.getBlockTileEntity(x, y, z);

		if (te != null)
		{
			TileEntityChest chest = (TileEntityChest)te;
			int size = chest.getSizeInventory();
			int numBonuses = rand.nextInt(size) / 2;

			for (int i = 0; i < size; i++)
			{
				if (rand.nextInt(size) <= numBonuses)
				{
					numBonuses--;
					chest.setInventorySlotContents(i, getRandomBonus(rand));
				}
			}
		}
	}

	private ItemStack getRandomBonus(Random rand)
	{
		ItemStack res = null;
		boolean isDone = false;

		while (!isDone)
		{
			switch (rand.nextInt(14))
			{
				default:
					res = null;
			}
		}

		return res;
	}
}
