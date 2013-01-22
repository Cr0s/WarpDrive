package cr0s.WarpDrive;

import cpw.mods.fml.common.registry.EntityRegistry;

public class CommonProxy {
    public static final String BLOCK_TEXTURE_OFFLINE = "/cr0s/WarpDrive/CORE_OFFLINE.png";
    public static final String BLOCK_TEXTURE_ONLINE = "/cr0s/WarpDrive/CORE_ONLINE.png";
    
    public void registerJumpEntity() {
        //EntityRegistry.registerModEntity(ThreadJump.class, "EntityJump", 1, WarpDrive.instance, 80, 3, false);
    }

    public void registerRenderers() {

    }
}