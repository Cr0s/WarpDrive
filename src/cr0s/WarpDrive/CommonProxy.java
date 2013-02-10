package cr0s.WarpDrive;

import cpw.mods.fml.common.registry.EntityRegistry;

public class CommonProxy {
    public static final String BLOCK_TEXTURE = "WarpDrive.png";
    
    public void registerJumpEntity() {
        EntityRegistry.registerModEntity(EntityJump.class, "EntityJump", 1, WarpDrive.instance, 80, 1, false);
    }

    public void registerRenderers() {

    }
}