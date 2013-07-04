package cr0s.serverMods;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.minecraftforge.common.MinecraftForge;
import cr0s.WarpDrive.SpaceEventHandler;

@Mod(modid="ServerMods", name="ServerMods", version="0.0.1")
@NetworkMod(clientSideRequired = false, serverSideRequired = true, connectionHandler = LoginHookClass.class)

/**
 * @author Cr0s
 */
public class ServerMods {

        // The instance of your mod that Forge uses.
        @Instance("ServerMods")
        public static ServerMods instance;
       
        @PreInit
        public void preInit(FMLPreInitializationEvent event) {
                // Stub Method
        }
       
        @Init
        public void load(FMLInitializationEvent event) {
            // Включить авторизацию (включается автоматически)
            //proxy.setupLoginHook();
            
            // Снять урон от падения с джетпаком и крузис-тапками
            MinecraftForge.EVENT_BUS.register(new AntiFallDamage());
        }
       
        @PostInit
        public void postInit(FMLPostInitializationEvent event) {
                // Stub Method
        }
}