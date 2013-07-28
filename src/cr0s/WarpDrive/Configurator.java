package cr0s.WarpDrive;
import java.io.File;
import java.util.ArrayList;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Configurator {
    protected static File configFolder;
    protected static File configFile;



    public static boolean isICBMLoaded;
    public static boolean isAELoaded;

    // Side ores ID's
    public static ArrayList<Integer> oresForGeneration;
    public static int oreCertusQuartzID;

    // Side Items

    public static Property helmetNano;
    public static Property helmetScuba;
    public static Property helmetQuantum;
    public static Property helmetHazmat;

    // Blocks
    public static Property blockWarpCore;
    public static Property blockWarpController;
    public static Property blockWRadar;
    public static Property blockWarpFieldIsolation;
    public static Property blockAir;
    public static Property blockAirGenerator;
    public static Property blockGas;

    // Items


    // Settings

    public static Property blocksPerTickJump;


    public static void initConfig(FMLPreInitializationEvent event) {
        configFolder = event.getModConfigurationDirectory();
        configFile = new File(configFolder.getAbsolutePath() + "/WarpDrive.cfg");
        loadPropertiesFromFile(configFile);
    }

    public static void loadPropertiesFromFile(File file) {
        Configuration localConfig = new Configuration(file);
        localConfig.load();

        blockWarpCore = localConfig.getBlock("blockWarpCore", 500);
        blockWarpController = localConfig.getBlock("blockWarpController", 501);
        blockWRadar = localConfig.getBlock("blockWRadar", 502);
        blockWarpFieldIsolation = localConfig.getBlock("blockWarpFieldIsolation", 503);
        blockAir = localConfig.getBlock("blockAir", 504);
        blockAirGenerator = localConfig.getBlock("blockAirGenerator", 505);
        blockGas = localConfig.getBlock("blockAirGenerator", 506);


        blocksPerTickJump = localConfig.get("settings", "blocksPerTickJump", 3500, "How many blocks will \"jump\" per 1 tick");



        localConfig.save();
    }

    //dunno how to name this correctly
    public static void determineOresForGeneration(){
        Configuration localConfig;
        oresForGeneration = new ArrayList<Integer>();
        oresForGeneration.add(Block.oreIron.blockID);
        oresForGeneration.add(Block.oreGold.blockID);
        oresForGeneration.add(Block.oreCoal.blockID);
        oresForGeneration.add(Block.oreEmerald.blockID);
        oresForGeneration.add(Block.oreLapis.blockID);
        oresForGeneration.add(Block.oreRedstoneGlowing.blockID);
        //IC2 ores
        localConfig = new Configuration(new File(configFolder.getAbsolutePath() + "/IC2.cfg"));
        oresForGeneration.add((localConfig.getBlock("blockOreCopper", 249).getInt()));
        oresForGeneration.add((localConfig.getBlock("blockOreCopper", 248).getInt()));
        oresForGeneration.add((localConfig.getBlock("blockOreUran", 247).getInt()));


        //ICBM ores, if exists
        if(isICBMLoaded){
            localConfig = new Configuration(new File(configFolder.getAbsolutePath() + "/ICBM.cfg"));
            oresForGeneration.add((localConfig.getBlock("oreSulfur", 3880)).getInt());
            //???
            oresForGeneration.add(3970);
            oresForGeneration.add(39701);
        }

        if(isAELoaded){
            //Like in ICBM (UE, BasicComponents) config ore ID's aren't listed directly (except sulfur ore), maybe it
            //would be better to add "Space generation" section in WarpDrive.cfg?
            oreCertusQuartzID = 902;
        }

    }

    public static void loadSideItems(){
        Configuration localConfig = new Configuration(new File(configFolder.getAbsolutePath()+"IC2.cfg"));

        localConfig.getItem("itemArmorHazmatHelmet",29826);
        localConfig.getItem("itemArmorNanoHelmet",29922);
        localConfig.getItem("itemArmorQuantumHelmet",29918);

        helmetHazmat.set(14023);

    }

    public static void checkForModsLoaded(){

        isICBMLoaded =  Loader.isModLoaded("ICBM|Explosion");
        isAELoaded =  Loader.isModLoaded("AppliedEnergistics");
    }
}