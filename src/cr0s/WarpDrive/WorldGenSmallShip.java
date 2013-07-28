package cr0s.WarpDrive;

import cpw.mods.fml.common.Loader;
import ic2.api.item.Items;
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
        private boolean isICBMLoaded = false, isAELoaded = false, isAdvSolPanelLoaded = false;
        
	public WorldGenSmallShip (boolean corrupted) { 
            this.corrupted = corrupted; 
            
            isICBMLoaded = Configurator.isICBMLoaded;
            isAELoaded = Configurator.isAELoaded;
            isAdvSolPanelLoaded = Loader.isModLoaded("AdvancedSolarPanel");
        }

        @Override
	public boolean generate(World world, Random rand, int i, int j, int k) {
            ItemStack cableType = Items.getItem("copperCableBlock");
            switch (rand.nextInt(4)) {
                case 0:
                    cableType = Items.getItem("glassFiberCableBlock");
                    break;                    
                case 1:
                    cableType = Items.getItem("glassFiberCableBlock");
                    break;
                case 2:
                    cableType = Items.getItem("insulatedGoldCableBlock");
                    break;
                case 3:
                    cableType = Items.getItem("doubleInsulatedIronCableBlock");
                    break;
            }
            
            int ADV_SOLAR_BLOCKID = 194;
            int solarType = rand.nextInt(2);
            if (!isAdvSolPanelLoaded) {
                ADV_SOLAR_BLOCKID = Items.getItem("solarPanel").itemID;
                solarType = Items.getItem("solarPanel").getItemDamage();
            }
            
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
		world.setBlock(i + 7, j + 6, k + 6, cableType.itemID, cableType.getItemDamage(), 0);
		world.setBlock(i + 7, j + 6, k + 7, ADV_SOLAR_BLOCKID, solarType, 0);
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
		world.setBlock(i + 8, j + 6, k + 6, cableType.itemID, cableType.getItemDamage(), 0);
		world.setBlock(i + 8, j + 6, k + 7, ADV_SOLAR_BLOCKID, solarType, 0);
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
		world.setBlock(i + 9, j + 6, k + 6, cableType.itemID, cableType.getItemDamage(), 0);
		world.setBlock(i + 9, j + 6, k + 7, ADV_SOLAR_BLOCKID, solarType, 0);
                // Placing air generator
                world.setBlock(i + 9, j + 5, k + 7, WarpDrive.airgenBlock.blockID, 0, 0);
                
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
		world.setBlock(i + 10, j + 6, k + 6, cableType.itemID, cableType.getItemDamage(), 0);
		world.setBlock(i + 10, j + 6, k + 7, ADV_SOLAR_BLOCKID, solarType, 0);
                
                // Placing air generator
                world.setBlock(i + 10, j + 5, k + 7, WarpDrive.airgenBlock.blockID, 0, 0);
                
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
                if (rand.nextBoolean()) {
                    world.setBlock(i + 11, j + 3, k + 7, WarpDrive.protocolBlock.blockID);
                }
                
		world.setBlock(i + 11, j + 3, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 4, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 4, k + 6, WorldGenStructure.getGlassBlock(corrupted, rand));
                
                // Place computer
                if (rand.nextBoolean()) {
                    world.setBlock(i + 11, j + 4, k + 7, 1225, 16384, 0);    
                }
		
                
		world.setBlock(i + 11, j + 4, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 4, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 5, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 5, k + 7, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 5, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 11, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 11, j + 6, k + 6, cableType.itemID, cableType.getItemDamage(), 0);
		world.setBlock(i + 11, j + 6, k + 7, ADV_SOLAR_BLOCKID, solarType, 0);
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
                if (rand.nextBoolean()) {
                    world.setBlock(i + 12, j + 3, k + 7, WarpDrive.warpCore.blockID);
                }
		
                world.setBlock(i + 12, j + 3, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 3, k + 12, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 4, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 4, k + 6, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 4, k + 7, cableType.itemID, cableType.getItemDamage(), 0);
		world.setBlock(i + 12, j + 4, k + 8, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 4, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 5, k + 2, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 5, k + 7, cableType.itemID, cableType.getItemDamage(), 0);
		world.setBlock(i + 12, j + 5, k + 12, WorldGenStructure.getGlassBlock(corrupted, rand));
		world.setBlock(i + 12, j + 6, k + 3, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 6, k + 4, WorldGenStructure.getStoneBlock(corrupted, rand));
		world.setBlock(i + 12, j + 6, k + 6, cableType.itemID, cableType.getItemDamage(), 0);
		world.setBlock(i + 12, j + 6, k + 7, cableType.itemID, cableType.getItemDamage(), 0);
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
        
    public void spawnNPC(World world, int i, int j, int k) {
        int numMobs = 2 + world.rand.nextInt(10);
        
        if (world.rand.nextBoolean()) { // Villagers
            for (int idx = 0; idx < numMobs; idx++) {
                EntityVillager entityvillager = new EntityVillager(world, 0);
                entityvillager.setLocationAndAngles((double)i + 0.5D, (double)j, (double)k + 0.5D, 0.0F, 0.0F);
                world.spawnEntityInWorld(entityvillager);
            }
        } else // Zombies
        {
            for (int idx = 0; idx < numMobs; idx++) {
                EntityZombie entityzombie = new EntityZombie(world);
                entityzombie.setLocationAndAngles((double)i + 0.5D, (double)j, (double)k + 0.5D, 0.0F, 0.0F);
                world.spawnEntityInWorld(entityzombie);
            }            
        }
    }
        
    public void fillChestWithBonuses(World worldObj, Random rand, int x, int y, int z) {
       TileEntity te = worldObj.getBlockTileEntity(x, y, z);

       if (te != null) {
           TileEntityChest chest = (TileEntityChest)te;
           int numBonuses = rand.nextInt(28);

           for (int i = 0; i < chest.getSizeInventory(); i++)
           {
               if (rand.nextInt(15) == 0) {
                   numBonuses--;

                   chest.setInventorySlotContents(i, getRandomBonus(rand));
               }
           }
       }
   }
   
   private ItemStack getRandomBonus(Random rand) {
       ItemStack res = null;
       boolean isDone = false;
       while (!isDone) {
           switch (rand.nextInt(14)) {
               case 0:
                   res = Items.getItem("massFabricator");
                   res.stackSize = 1 + rand.nextInt(2);
                   isDone = true;
                   break;
               case 1:
                   res = Items.getItem("nuke");
                   res.stackSize = 1 + rand.nextInt(64);
                   isDone = true;
                   break;
               case 2: // Quantum armor bonuses
               case 3:
               case 4:            
               case 5:
                   isDone = true;
                   break;// skipped
               case 6:
                   res = Items.getItem("glassFiberCableItem");
                   res.stackSize = 2 + rand.nextInt(63);
                   isDone = true;
                   break;
               case 7:
                   res = Items.getItem("matter");
                   res.stackSize = 2 + rand.nextInt(63);
                   isDone = true;
                   break;
               case 8:
                   isDone = true;
                   break;// skipped
               // AE Quarz
               case 9:
                   if (isAELoaded) {
                       res = new ItemStack(4362, 2 + rand.nextInt(63), 6);
                       isDone = true;
                   }
                   break;
               // AE improved processor
               case 10:
                   if (isAELoaded) {
                       res = new ItemStack(4362, 2 + rand.nextInt(15), 19);               
                       isDone = true;
                   }
                   break;
               // Rocket launcher platform Tier3
               case 11:
                   if (isICBMLoaded) {
                       res = new ItemStack(3884, 2 + rand.nextInt(1), 2);
                       isDone = true;
                   }
                   break;
               // Missles from conventional to hypersonic
               case 12:
                   if (isICBMLoaded) {
                       res = new ItemStack(4159, 2 + rand.nextInt(1), rand.nextInt(21));    
                       isDone = true;
                   }
                   break;
               // Advanced solar panels
               case 13:
                   if (isAdvSolPanelLoaded) {
                       res = new ItemStack(194, 2 + rand.nextInt(1), rand.nextInt(3));
                       isDone = true;
                   }
                   break;
           }
       }
       return res;
   }        
}
