package cr0s.WarpDrive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.lang.reflect.*;
import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.Configuration;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ic2.api.item.Items;

public class WarpDriveConfig
{
	public static WarpDriveConfig i;
	private Configuration config;
	public int coreID, controllerID, radarID, isolationID, airID, airgenID, gasID, laserID, miningLaserID, particleBoosterID, liftID, laserCamID, camID, monitorID, iridiumID;
//
	public boolean isGregLoaded = false, isAELoaded = false, isAdvSolPanelLoaded = false, isASLoaded = false, isICBMLoaded = false, isMFFSLoaded = false, isGraviSuiteLoaded = false;
//
	public int[] IC2_Air;
	public int CC_Computer = 0, CC_peripheral = 0, CCT_Turtle = 0, CCT_Upgraded = 0, CCT_Advanced = 0, GT_Ores = 0, GT_Granite = 0, GT_Machine = 0, ASP = 0, AS_Turbine = 0, ICBM_Machine = 0, ICBM_Missile = 0, MFFS_Field = 0;
	public Set<Integer> SpaceHelmets, Jetpacks, MinerOres;
	private Class<?> AEBlocks;
	private Class<?> AEMaterials;
	private Class<?> AEItems;
	public ArrayList<int[]> CommonWorldGenOres;

	private WarpDriveConfig() {}

	public ItemStack getIC2Item(String id)
	{
		return Items.getItem(id);
	}

	public ItemStack getAEBlock(String id)
	{
		try
		{
			Object ret = AEBlocks.getField(id).get(null);
			if (ret instanceof ItemStack)
				return (ItemStack)ret;
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Call getAEBlock failed for " + id);
		}
		return null;
	}

	public ItemStack getAEMaterial(String id)
	{
		try
		{
			Object ret = AEMaterials.getField(id).get(null);
			if (ret instanceof ItemStack)
				return (ItemStack)ret;
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Call getAEMaterial failed for " + id);
		}
		return null;
	}

	public ItemStack getAEItem(String id)
	{
		try
		{
			Object ret = AEItems.getField(id).get(null);
			if (ret instanceof ItemStack)
				return (ItemStack)ret;
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Call getAEItem failed for " + id);
		}
		return null;
	}

	public static void Init(Configuration config)
	{
		if (i == null)
			i = new WarpDriveConfig();
		i.config = config;
	}

	public void Init2()
	{
		CommonWorldGenOres = new ArrayList<int[]>();
		CommonWorldGenOres.add(new int[] {Block.oreIron.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreGold.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreCoal.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreEmerald.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreLapis.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreRedstoneGlowing.blockID, 0});
		CommonWorldGenOres.add(new int[] {Block.oreRedstone.blockID, 0});
//
		SpaceHelmets = new HashSet<Integer>();
		Jetpacks = new HashSet<Integer>();
		MinerOres = new HashSet<Integer>();
		config.load();
		coreID = config.getBlock("core", 500).getInt();
		controllerID = config.getBlock("controller", 501).getInt();
		radarID = config.getBlock("radar", 502).getInt();
		isolationID = config.getBlock("isolation", 503).getInt();
		airID = config.getBlock("air", 504).getInt();
		airgenID = config.getBlock("airgen", 505).getInt();
		gasID = config.getBlock("gas", 506).getInt();
		laserID = config.getBlock("laser", 507).getInt();
		miningLaserID = config.getBlock("mininglaser", 508).getInt();
		particleBoosterID = config.getBlock("particlebooster", 509).getInt();
		liftID = config.getBlock("lift", 510).getInt();
		laserCamID = config.getBlock("lasercam", 512).getInt();
		camID = config.getBlock("camera", 513).getInt();
		monitorID = config.getBlock("monitor", 514).getInt();
		iridiumID = config.getBlock("iridium", 515).getInt();
		config.save();
		LoadIC2();
		LoadCC();
		isGregLoaded = Loader.isModLoaded("gregtech_addon");
		if (isGregLoaded)
			LoadGT();
		isAELoaded = Loader.isModLoaded("AppliedEnergistics");
		if (isAELoaded)
			LoadAE();
		isAdvSolPanelLoaded = Loader.isModLoaded("AdvancedSolarPanel");
		if (isAdvSolPanelLoaded)
			LoadASP();
		isASLoaded = Loader.isModLoaded("AtomicScience");
		if (isASLoaded)
			LoadAS();
		isICBMLoaded = Loader.isModLoaded("ICBM|Explosion");
		if (isICBMLoaded)
			LoadICBM();
		isMFFSLoaded = Loader.isModLoaded("MFFS");
		if (isMFFSLoaded)
			LoadMFFS();
		isGraviSuiteLoaded = Loader.isModLoaded("GraviSuite");
		if (isGraviSuiteLoaded)
			LoadGS();
//
		MinerOres.add(iridiumID);
		MinerOres.add(Block.oreNetherQuartz.blockID);
		for (int[] t : CommonWorldGenOres)
			MinerOres.add(t[0]);
		MinerOres.add(Block.wood.blockID);
		MinerOres.add(Block.planks.blockID);
		MinerOres.add(Block.rail.blockID);
		MinerOres.add(Block.oreDiamond.blockID);
		MinerOres.add(Block.obsidian.blockID);
		MinerOres.add(Block.web.blockID);
		MinerOres.add(Block.fence.blockID);
		MinerOres.add(Block.torchWood.blockID);
	}

	private void LoadIC2()
	{
		ASP = Items.getItem("solarPanel").itemID;
		SpaceHelmets.add(Items.getItem("hazmatHelmet").itemID);
		SpaceHelmets.add(Items.getItem("quantumHelmet").itemID);
		Jetpacks.add(Items.getItem("jetpack").itemID);
		Jetpacks.add(Items.getItem("electricJetpack").itemID);
		IC2_Air = new int[] {Items.getItem("airCell").itemID, Items.getItem("airCell").getItemDamage()};
		CommonWorldGenOres.add(new int[] {Items.getItem("uraniumOre").itemID, Items.getItem("uraniumOre").getItemDamage()});
		CommonWorldGenOres.add(new int[] {Items.getItem("copperOre").itemID, Items.getItem("uraniumOre").getItemDamage()});
		CommonWorldGenOres.add(new int[] {Items.getItem("tinOre").itemID, Items.getItem("uraniumOre").getItemDamage()});
		MinerOres.add(Items.getItem("rubberWood").itemID);
	}

	private void LoadCC()
	{
		try
		{
			Class<?> z = Class.forName("dan200.ComputerCraft");
			CC_Computer = z.getField("computerBlockID").getInt(null);
			CC_peripheral = z.getField("peripheralBlockID").getInt(null);
			z = Class.forName("dan200.CCTurtle");
			CCT_Turtle = z.getField("turtleBlockID").getInt(null);
			CCT_Upgraded = z.getField("turtleUpgradedBlockID").getInt(null);
			CCT_Advanced = z.getField("turtleAdvancedBlockID").getInt(null);
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Error loading CC classes AWWW SHEEEEET NIGGA");
			e.printStackTrace();
		}
	}

	private void LoadGT()
	{
		try
		{
			Class<?> z = Class.forName("gregtechmod.GT_Mod");
			int[] t = (int[])z.getField("sBlockIDs").get(null);
			GT_Machine = t[1];
			GT_Ores = t[2]; // meta 1-15 = ores
			GT_Granite = t[5]; // 0 - black, 1 - black cobble, 8 - red, 9 - red cobble
			MinerOres.add(GT_Ores);
			MinerOres.add(GT_Granite);
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Error loading GT classes");
			e.printStackTrace();
			isGregLoaded = false;
		}
	}

	private void LoadAE()
	{
		try
		{
			AEBlocks = Class.forName("appeng.api.Blocks");
			AEMaterials = Class.forName("appeng.api.Materials");
			AEItems = Class.forName("appeng.api.Items");
			MinerOres.add(((ItemStack)AEBlocks.getField("blkQuartzOre").get(null)).itemID);
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Error loading AE classes");
			e.printStackTrace();
			isAELoaded = false;
		}
	}

	private void LoadASP()
	{
		try
		{
			Class<?> z = Class.forName("advsolar.common.AdvancedSolarPanel");
			ASP = z.getField("idAdv").getInt(null);
			SpaceHelmets.add(((Item)z.getField("advancedSolarHelmet").get(null)).itemID);
			SpaceHelmets.add(((Item)z.getField("hybridSolarHelmet").get(null)).itemID);
			SpaceHelmets.add(((Item)z.getField("ultimateSolarHelmet").get(null)).itemID);
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Error loading ASP classes");
			e.printStackTrace();
			isAdvSolPanelLoaded = false;
		}
	}

	private void LoadAS()
	{
		try
		{
			Class<?> z = Class.forName("atomicscience.ZhuYaoAS");
			CommonWorldGenOres.add(new int[] {((Block)z.getField("bHeOre").get(null)).blockID, 0});
			AS_Turbine = ((Block)z.getField("bWoLun").get(null)).blockID;
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Error loading AS classes");
			isASLoaded = false;
		}
	}

	private void LoadICBM()
	{
		try
		{
			Class<?> z = Class.forName("icbm.core.ICBMCore");
			CommonWorldGenOres.add(new int[] {((Block)z.getField("blockSulfurOre").get(null)).blockID, 0});
			z = Class.forName("icbm.explosion.ICBMExplosion");
			ICBM_Machine = ((Block)z.getField("blockMachine").get(null)).blockID;
			ICBM_Missile = ((Item)z.getField("itemMissile").get(null)).itemID;
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Error loading ICBM classes");
			e.printStackTrace();
			isICBMLoaded = false;
		}
	}

	private void LoadMFFS()
	{
		try
		{
			Class<?> z = Class.forName("mffs.ModularForceFieldSystem");
			MFFS_Field = ((Block)z.getField("blockForceField").get(null)).blockID;
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Error loading MFFS classes");
			e.printStackTrace();
			isICBMLoaded = false;
		}
	}

	private void LoadGS()
	{
		try
		{
			Class<?> z = Class.forName("gravisuite.GraviSuite");
			SpaceHelmets.add(((Item)z.getField("ultimateSolarHelmet").get(null)).itemID);
		}
		catch (Exception e)
		{
			System.out.println("WarpDriveConfig Error loading GS classes");
			e.printStackTrace();
			isGraviSuiteLoaded = false;
		}
	}

	public int[] getDefaultSurfaceBlock(Random random, boolean corrupted, boolean isMoon)
	{
		if (isMoon)
		{
			if (random.nextInt(100) == 1)
				if (random.nextBoolean())
					return new int[] {GT_Granite, (corrupted && random.nextBoolean())?1:0};
				else
					return new int[] {GT_Granite, (corrupted && random.nextBoolean())?9:8};
			else if (random.nextInt(666) == 1)
				return new int[] {Block.netherrack.blockID, 0};
			else if (random.nextInt(1000) == 1)
				return new int[] {Block.whiteStone.blockID, 0};
		}
		else
		{
			if (random.nextInt(25) == 1)
				if (random.nextBoolean())
					return new int[] {GT_Granite, (corrupted && random.nextBoolean())?1:0};
				else
					return new int[] {GT_Granite, (corrupted && random.nextBoolean())?9:8};
			else if (random.nextInt(50) == 1)
				return new int[] {Block.netherrack.blockID, 0};
			else if (random.nextInt(150) == 1)
				return new int[] {Block.whiteStone.blockID, 0};
		}
		if (corrupted && random.nextBoolean())
			return new int[] {Block.cobblestone.blockID, 0};
		return new int[] {Block.stone.blockID, 0};
	}

	public int[] getRandomSurfaceBlock(Random random, int blockID, int blockMeta, boolean bedrock)
	{
		if (bedrock && random.nextInt(1000) == 1)
			return new int[] {Block.bedrock.blockID, 0};
		if (blockID == GT_Granite)
			if (blockMeta == 0 || blockMeta == 1)
			{
				int[] t;
				t = getRandomOverworldBlock(random, blockID, blockMeta);
				if (t[0] == blockID)
					t = getRandomOverworldBlock(random, blockID, blockMeta);
				if (t[0] == blockID)
					t = getRandomEndBlock(random, blockID, blockMeta);
				return t;
			}
			else if (blockMeta == 8 || blockMeta == 9)
			{
				int[] t;
				t = getRandomOverworldBlock(random, blockID, blockMeta);
				if (t[0] == blockID)
					t = getRandomEndBlock(random, blockID, blockMeta);
				if (t[0] == blockID)
					t = getRandomOverworldBlock(random, blockID, blockMeta);
				return t;
			}
		else if (blockID == Block.whiteStone.blockID)
			return getRandomEndBlock(random, blockID, blockMeta);
		else if (blockID == Block.netherrack.blockID)
			return getRandomNetherBlock(random, blockID, blockMeta);
		return getRandomOverworldBlock(random, blockID, blockMeta);
	}

	public int[] getRandomOverworldBlock(Random random, int blockID, int blockMeta)
	{
		if (random.nextInt(25) == 5)
			return CommonWorldGenOres.get(random.nextInt(CommonWorldGenOres.size()));
		else if (isAELoaded && random.nextInt(750) == 1)
			return new int[] {getAEBlock("blkQuartzOre").itemID, getAEBlock("blkQuartzOre").getItemDamage()};
		else if (random.nextInt(250) == 1)
			return new int[] {Block.oreDiamond.blockID, 0};
		else if (random.nextInt(10000) == 42)
			return new int[] {iridiumID, 0};
		if (isGregLoaded)
		{
			if (random.nextInt(50) == 1)
				return new int[] {GT_Ores, 5}; //Bauxite S /* Stone/Iron/Diamod pick | +S = Silktouch recommended */
			else if (random.nextInt(50) == 1)
				return new int[] {GT_Ores, 1}; //Galena S
			else if (random.nextInt(100) == 1)
				return new int[] {GT_Ores, 8}; //Sphalerite S+S
			else if (random.nextInt(250) == 1)
				return new int[] {GT_Ores, 13}; //Tetrahedrite I
			else if (random.nextInt(250) == 1)
				return new int[] {GT_Ores, 14}; //Cassiterite I
			else if (random.nextInt(250) == 1)
				return new int[] {GT_Ores, 15}; //Nickel I
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 3}; //Ruby I+S
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 4}; //Sapphire I+S
			else if (random.nextInt(2000) == 1)
				return new int[] {GT_Ores, 2}; //Iridium D+S
		}
		return new int[] {blockID, blockMeta};
	}

	public int[] getRandomNetherBlock(Random random, int blockID, int blockMeta)
	{
		if (random.nextInt(10000) == 42)
			return new int[] {iridiumID, 0};
		else if (random.nextInt(25) == 1)
			return new int[] {Block.oreNetherQuartz.blockID, 0};
		else if (isGregLoaded)
		{
			if (random.nextInt(100) == 1)
				return new int[] {GT_Ores, 6}; //Pyrite S+S
			else if (random.nextInt(100) == 1)
				return new int[] {GT_Ores, 8}; //Sphalerite S+S
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 7}; //Cinnabar I+S
		}
		else if (random.nextInt(100) == 13)
			return CommonWorldGenOres.get(random.nextInt(CommonWorldGenOres.size()));
		return new int[] {blockID, blockMeta};
	}

	public int[] getRandomEndBlock(Random random, int blockID, int blockMeta)
	{
		if (random.nextInt(10000) == 42)
			return new int[] {iridiumID, 0};
		else if (isGregLoaded)
		{
			if (random.nextInt(250) == 1)
				return new int[] {GT_Ores, 9}; //Tungstate I
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 12}; //Sodalite I+S
			else if (random.nextInt(500) == 1)
				return new int[] {GT_Ores, 10}; //Cooperite=Sheldonite D
			else if (random.nextInt(1000) == 1)
				return new int[] {GT_Ores, 11}; //Olivine D+S
		}
		else if (random.nextInt(200) == 13)
			return CommonWorldGenOres.get(random.nextInt(CommonWorldGenOres.size()));
		return new int[] {blockID, blockMeta};
	}
}
