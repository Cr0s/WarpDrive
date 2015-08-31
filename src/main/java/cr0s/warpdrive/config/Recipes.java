package cr0s.warpdrive.config;

import cpw.mods.fml.common.registry.GameRegistry;
import cr0s.warpdrive.WarpDrive;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Hold the different recipe sets
 *
 * @author LemADEC
 *
 */
public class Recipes {

	public static void initVanilla() {
		WarpDrive.itemComponent.registerRecipes();
		WarpDrive.blockDecorative.initRecipes();
		WarpDrive.itemUpgrade.initRecipes();
		
		// WarpCore
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockShipCore), false, "ipi", "ici", "idi",
				'i', Items.iron_ingot,
				'p', WarpDrive.itemComponent.getItemStack(6),
				'c', WarpDrive.itemComponent.getItemStack(2),
				'd', Items.diamond));
		
		// Controller
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockShipController), false, "ici", "idi", "iii",
				'i', Items.iron_ingot,
				'c', WarpDrive.itemComponent.getItemStack(5),
				'd', Items.diamond));
		
		// Radar
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockRadar), false, "ggg", "pdc", "iii",
				'i', Items.iron_ingot,
				'c', WarpDrive.itemComponent.getItemStack(5),
				'p', WarpDrive.itemComponent.getItemStack(6),
				'g', Blocks.glass,
				'd', Items.diamond));
		
		// Isolation Block
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockWarpIsolation), false, "igi", "geg", "igi",
				'i', Items.iron_ingot,
				'g', Blocks.glass,
				'e', Items.ender_pearl));
		
		// Air generator
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAirGenerator), false, "ibi", "i i", "ipi",
				'i', Items.iron_ingot,
				'b', Blocks.iron_bars,
				'p', WarpDrive.itemComponent.getItemStack(6)));
		
		// Laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaser), false, "ili", "iri", "ici",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				'c', WarpDrive.itemComponent.getItemStack(5),
				'l', WarpDrive.itemComponent.getItemStack(3),
				'p', WarpDrive.itemComponent.getItemStack(6)));
		
		// Mining laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockMiningLaser), false, "ici", "iti", "ili",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				't', WarpDrive.itemComponent.getItemStack(1),
				'c', WarpDrive.itemComponent.getItemStack(5),
				'l', WarpDrive.itemComponent.getItemStack(3)));
		
		// Tree farm laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserTreeFarm), false, "ili", "sts", "ici",
				'i', Items.iron_ingot,
				's', "treeSapling",
				't', WarpDrive.itemComponent.getItemStack(1), 
				'c', WarpDrive.itemComponent.getItemStack(5),
				'l', WarpDrive.itemComponent.getItemStack(3)));
		
		// Laser Lift
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLift), false, "ipi", "rtr", "ili",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				't', WarpDrive.itemComponent.getItemStack(1),
				'l', WarpDrive.itemComponent.getItemStack(3),
				'p', WarpDrive.itemComponent.getItemStack(6)));
		
		// Transporter
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransporter), false, "iii", "ptc", "iii",
				'i', Items.iron_ingot,
				't', WarpDrive.itemComponent.getItemStack(1),
				'c', WarpDrive.itemComponent.getItemStack(5),
				'p', WarpDrive.itemComponent.getItemStack(6)));
		
		// Particle Booster
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserMedium), false, "ipi", "rgr", "iii", 
				'i', Items.iron_ingot,
				'r', Items.redstone,
				'g', Blocks.glass,
				'p', WarpDrive.itemComponent.getItemStack(6)));
		
		// Camera
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockCamera), false, "ngn", "i i", "ici",
				'i', Items.iron_ingot,
				'n', Items.gold_nugget,
				'g', Blocks.glass,
				'c', WarpDrive.itemComponent.getItemStack(5)));
		
		// LaserCamera
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(WarpDrive.blockLaserCamera), WarpDrive.blockCamera, WarpDrive.blockLaser));
		
		// Monitor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockMonitor), false, "ggg", "iti", "ici",
				'i', Items.iron_ingot,
				't', Blocks.torch,
				'g', Blocks.glass,
				'c', WarpDrive.itemComponent.getItemStack(5)));
		
		// Cloaking device
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockCloakingCore), false, "ipi", "lrl", "ici",
				'i', Items.iron_ingot,
				'r', Items.redstone,
				'l', WarpDrive.itemComponent.getItemStack(3),
				'c', WarpDrive.itemComponent.getItemStack(5),
				'p', WarpDrive.itemComponent.getItemStack(6)));
		
		// Cloaking coil
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockCloakingCoil), false, "ini", "rdr", "ini",
				'i', Items.iron_ingot,
				'd', Items.diamond,
				'r', Items.redstone,
				'n', Items.gold_nugget));
		
		// Power Laser
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnanReactorLaser), false, "iii", "ilg", "ici",
				'i', Items.iron_ingot,
				'g', Blocks.glass,
				'c', WarpDrive.itemComponent.getItemStack(5),
				'l', WarpDrive.itemComponent.getItemStack(3)));
		
		// Power Reactor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnanReactorCore), false, "ipi", "gog", "ici",
				'i', Items.iron_ingot,
				'g', Blocks.glass,
				'o', WarpDrive.itemComponent.getItemStack(4),
				'c', WarpDrive.itemComponent.getItemStack(5),
				'p', WarpDrive.itemComponent.getItemStack(6)));
		
		// Power Store
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockEnergyBank), false, "ipi", "isi", "ici",
				'i', Items.iron_ingot,
				's', WarpDrive.itemComponent.getItemStack(7),
				'c', WarpDrive.itemComponent.getItemStack(5),
				'p', WarpDrive.itemComponent.getItemStack(6)));
		
		// Transport Beacon
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransportBeacon), false, " e ", "ldl", " s ",
				'e', Items.ender_pearl,
				'l', "dyeBlue",
				'd', Items.diamond,
				's', Items.stick));
		
		// Chunk Loader
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockChunkLoader), false, "ipi", "ici", "ifi",
				'i', Items.iron_ingot,
				'p', WarpDrive.itemComponent .getItemStack(6),
				'c', WarpDrive.itemComponent.getItemStack(0),
				'f', WarpDrive.itemComponent.getItemStack(5)));
		
		// Helmet
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemHelmet), false, "iii", "iwi", "gcg",
				'i', Items.iron_ingot,
				'w', Blocks.wool,
				'g', Blocks.glass,
				'c', WarpDrive.itemComponent.getItemStack(8)));
	}

	public static void initIC2() {
		ItemStack advancedAlloy = WarpDriveConfig.getModItemStack("IC2", "itemPartAlloy", -1);
		ItemStack iridiumAlloy = WarpDriveConfig.getModItemStack("IC2", "itemPartIridium", -1);
		ItemStack advancedMachine = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 12);
		ItemStack miner = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 7);
		ItemStack magnetizer = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 9);
		ItemStack fiberGlassCable = WarpDriveConfig.getModItemStack("IC2", "itemCable", 9);
		ItemStack circuit = WarpDriveConfig.getModItemStack("IC2", "itemPartCircuit", -1);
		ItemStack advancedCircuit = WarpDriveConfig.getModItemStack("IC2", "itemPartCircuitAdv", -1);
		ItemStack ironPlate = WarpDriveConfig.getModItemStack("IC2", "itemPlates", 4);
		ItemStack mfe = WarpDriveConfig.getModItemStack("IC2", "blockElectric", 1);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockShipCore), "ici", "cmc", "ici",
				'i', iridiumAlloy,
				'm', advancedMachine,
				'c', advancedCircuit);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockShipController), "iic", "imi", "cii",
				'i', iridiumAlloy,
				'm', advancedMachine,
				'c', advancedCircuit);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockRadar), "ifi", "imi", "imi",
				'i', iridiumAlloy,
				'm', advancedMachine,
				'f', WarpDriveConfig.getModItemStack("IC2", "itemFreq", -1));
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockWarpIsolation), "iii", "idi", "iii",
				'i', WarpDriveConfig.getModItemStack("IC2", "itemPartIridium", -1),
				'm', advancedMachine,
				'd', Blocks.diamond_block);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockAirGenerator), "lcl", "lml", "lll",
				'l', Blocks.leaves,
				'm', advancedMachine,
				'c', advancedCircuit);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockLaser), "sss", "ama", "aaa",
				'm', advancedMachine,
				'a', advancedAlloy,
				's', advancedCircuit);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockMiningLaser), "aaa", "ama", "ccc",
				'c', advancedCircuit,
				'a', advancedAlloy,
				'm', miner);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockLaserMedium), "afc", "ama", "cfa",
				'c', advancedCircuit,
				'a', advancedAlloy,
				'f', fiberGlassCable,
				'm', mfe);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockLift), "aca", "ama", "a#a",
				'c', advancedCircuit,
				'a', WarpDriveConfig.getModItemStack("IC2", "itemPartAlloy", -1),
				'm', magnetizer);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockIridium), "iii", "iii", "iii",
				'i', iridiumAlloy);
		
		GameRegistry.addShapelessRecipe(new ItemStack(iridiumAlloy.getItem(), 9), new ItemStack(WarpDrive.blockIridium));
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockLaserCamera), "imi", "cec", "#k#",
				'i', iridiumAlloy,
				'm', advancedMachine,
				'c', advancedCircuit,
				'e', WarpDrive.blockLaser,
				'k', WarpDrive.blockCamera);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockCamera), "cgc", "gmg", "cgc",
				'm', advancedMachine,
				'c', advancedCircuit,
				'g', Blocks.glass);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockMonitor), "gcg", "gmg", "ggg",
				'm', advancedMachine,
				'c', advancedCircuit,
				'g', Blocks.glass);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockShipScanner), "sgs", "mma", "amm",
				'm', advancedMachine,
				'a', advancedAlloy,
				's', advancedCircuit,
				'g', Blocks.glass);
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserTreeFarm), false, new Object[] { "cwc", "wmw", "cwc",
				'c', circuit,
				'w', "logWood",
				'm', WarpDrive.blockMiningLaser }));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransporter), false, new Object[] { "ece", "imi", "iei",
				'e', Items.ender_pearl,
				'c', circuit,
				'i', ironPlate,
				'm', advancedMachine }));
		
		if (WarpDriveConfig.isIndustrialCraft2loaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemIC2reactorLaserFocus), false, new Object[] { " p ", "pdp", " p ",
					'p', ironPlate,
					'd', "gemDiamond" }));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockIC2reactorLaserMonitor), false, new Object[] { "pdp", "dmd", "pdp",
					'p', ironPlate,
					'd', "gemDiamond",
					'm', mfe }));
		}
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockCloakingCore), "imi", "mcm", "imi",
				'i', WarpDrive.blockIridium,
				'c', WarpDrive.blockCloakingCoil,
				'm', advancedMachine);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockCloakingCoil), "iai", "aca", "iai",
				'i', iridiumAlloy,
				'c', advancedCircuit,
				'a', advancedAlloy);
	}
	
	public static void initHardIC2() {
		ItemStack advancedAlloy = WarpDriveConfig.getModItemStack("IC2", "itemPartAlloy", -1);
		ItemStack iridiumAlloy = WarpDriveConfig.getModItemStack("IC2", "itemPartIridium", -1);
		ItemStack advancedMachine = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 12);
		ItemStack magnetizer = WarpDriveConfig.getModItemStack("IC2", "blockMachine", 9);
		ItemStack fiberGlassCable = WarpDriveConfig.getModItemStack("IC2", "itemCable", 9);
		ItemStack mfe = WarpDriveConfig.getModItemStack("IC2", "blockElectric", 1);
		ItemStack mfsu = WarpDriveConfig.getModItemStack("IC2", "blockElectric", 2);
		ItemStack energiumDust = WarpDriveConfig.getModItemStack("IC2", "itemDust2", 2);
		ItemStack crystalmemory = WarpDriveConfig.getModItemStack("IC2", "itemcrystalmemory", -1);
		ItemStack itemHAMachine = new ItemStack(WarpDrive.blockHighlyAdvancedMachine);
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockShipCore),"uau", "tmt", "uau",
				'a', advancedAlloy,
				't', WarpDriveConfig.getModItemStack("IC2", "blockMachine2", 0), // Teleporter
				'm', itemHAMachine,
				'u', mfsu);
		
		if (WarpDriveConfig.isOpenComputersLoaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockShipController), false, new Object[] { "aha", "cmc", "apa", // With OC Adapter
					'a', advancedAlloy,
					'm', itemHAMachine,
					'c', "circuitAdvanced",
					'h', crystalmemory,
					'p', WarpDriveConfig.getModItemStack("OpenComputers", "adapter", -1)}));
		} else if (WarpDriveConfig.isComputerCraftLoaded) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockShipController), false, new Object[] { "aha", "cmc", "apa", // With CC Modem
					'a', advancedAlloy,
					'm', itemHAMachine,
					'c', "circuitAdvanced",
					'h', crystalmemory,
					'p', WarpDriveConfig.getModItemStack("ComputerCraft", "CC-Cable", 1)}));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockShipController), false, new Object[] { "aha", "cmc", "aca",
				'a', advancedAlloy,
				'm', itemHAMachine,
				'c', "circuitAdvanced",
				'h', crystalmemory}));
		}
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockRadar), false, new Object[] { "afa", "cmc", "aca",
				'a', advancedAlloy,
				'm', itemHAMachine,
				'c', "circuitAdvanced",
				'f', WarpDriveConfig.getModItemStack("IC2", "itemFreq", -1)}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockWarpIsolation), false, new Object[] { "sls", "lml", "sls",
				's', "plateDenseSteel",
				'l', "plateDenseLead",
				'm', itemHAMachine}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockAirGenerator), false, new Object[] { "lel", "vmv", "lcl",
				'l', Blocks.leaves, 
				'm', WarpDriveConfig.getModItemStack("IC2", "blockMachine", 0),
				'c', "circuitBasic",
				'e', WarpDriveConfig.getModItemStack("IC2", "blockMachine", 5), // Compressor
				'v', WarpDriveConfig.getModItemStack("IC2", "reactorVent", -1)}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaser), false, new Object[] { "aca", "cmc", "ala",
				'm', advancedMachine,
				'a', advancedAlloy,
				'c', "circuitAdvanced",
				'l', WarpDriveConfig.getModItemStack("IC2", "itemToolMiningLaser", -1)}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockMiningLaser), false, new Object[] { "pcp", "pap", "plp",
				'c', "circuitAdvanced",
				'p', advancedAlloy,
				'a', WarpDriveConfig.getModItemStack("IC2", "blockMachine2", 11), // Advanced Miner
				'l', WarpDrive.blockLaser}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserMedium), false, new Object[] { "efe", "aca", "ama",
				'c', "circuitAdvanced",
				'a', advancedAlloy,
				'f', fiberGlassCable,
				'e', energiumDust,
				'm', mfe}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLift), false, new Object[] { "aca", "ama", "aea",
				'c', "circuitAdvanced",
				'a', advancedAlloy,
				'm', magnetizer,
				'e', energiumDust}));
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockIridium), "iii", "iii", "iii",
				'i', iridiumAlloy);
		
		GameRegistry.addShapelessRecipe(new ItemStack(iridiumAlloy.getItem(), 9), new ItemStack(WarpDrive.blockIridium));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserCamera), false, new Object[] { "ala", "sss", "aca",
				'a', advancedAlloy,
				's', "circuitAdvanced",
				'l', WarpDrive.blockLaser,
				'c', WarpDrive.blockCamera}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockCamera), false, new Object[] { "aed", "cma", "aga",
				'a', advancedAlloy,
				'e', WarpDriveConfig.getModItemStack("IC2", "itemRecipePart", 1), // Electric Motor
				'd', "gemDiamond",
				'c', crystalmemory,
				'm', advancedMachine,
				'g', WarpDriveConfig.getModItemStack("IC2", "itemCable", 2)}));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockMonitor), false, new Object[] { "ala", "aca", "aga",
				'a', advancedAlloy,
				'l', Blocks.redstone_lamp,
				'c', "circuitAdvanced",
				'g', "paneGlassColorless" }));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockShipScanner), false, new Object[] { "ici", "isi", "mcm",
				'm', mfsu,
				'i', iridiumAlloy,
				'c', "circuitAdvanced",
				's', WarpDriveConfig.getModItemStack("IC2", "blockMachine2", 7) })); // Scanner
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockLaserTreeFarm), false, new Object[] { "awa", "cmc", "asa",
				'a', advancedAlloy,
				'c', "circuitAdvanced",
				'w', "logWood",
				'm', WarpDrive.blockMiningLaser,
				's', WarpDriveConfig.getModItemStack("IC2", "itemToolChainsaw", -1) }));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockTransporter), false, new Object[] { "aea", "ctc", "ama",
				'a', advancedAlloy,
				'e', Items.ender_pearl,
				'c', "circuitAdvanced",
				'm', advancedMachine,
				't', WarpDriveConfig.getModItemStack("IC2", "blockMachine2", 0) })); // Teleporter
		
		// IC2 is loaded for this recipe set
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.itemIC2reactorLaserFocus), false, new Object[] { "a a", " d ", "a a",
				'a', advancedAlloy,
				'd', "gemDiamond" }));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockIC2reactorLaserMonitor), false, new Object[] { "pdp", "dmd", "pdp",
				'p', advancedAlloy,
				'd', "gemDiamond",
				'm', mfe }));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockCloakingCore), false, new Object[] { "ici", "cmc", "igi",
				'i', WarpDrive.blockIridium,
				'c', WarpDrive.blockCloakingCoil,
				'm', WarpDrive.blockHighlyAdvancedMachine,
				'g', "circuitAdvanced" }));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(WarpDrive.blockCloakingCoil), false, new Object[] { "iai", "ccc", "iai",
				'i', iridiumAlloy,
				'c', WarpDriveConfig.getModItemStack("IC2", "itemRecipePart", 0), // Coil
				'a', advancedAlloy })); 
		
		GameRegistry.addRecipe(new ItemStack(WarpDrive.blockHighlyAdvancedMachine), "iii", "imi", "iii",
				'i', iridiumAlloy,
				'm', advancedMachine);
	}
}
