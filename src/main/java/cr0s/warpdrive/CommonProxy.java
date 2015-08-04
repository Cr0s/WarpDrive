package cr0s.warpdrive;

import cpw.mods.fml.common.registry.EntityRegistry;
import cr0s.warpdrive.render.EntityCamera;
import cr0s.warpdrive.world.EntitySphereGen;
import cr0s.warpdrive.world.EntityStarCore;

public class CommonProxy
{
    public void registerEntities()
    {
        EntityRegistry.registerModEntity(EntityJump.class, "EntityJump", 240, WarpDrive.instance, 80, 1, false);
        EntityRegistry.registerModEntity(EntitySphereGen.class, "EntitySphereGenerator", 241, WarpDrive.instance, 200, 1, false);
        EntityRegistry.registerModEntity(EntityStarCore.class, "EntityStarCore", 242, WarpDrive.instance, 300, 1, false);
        EntityRegistry.registerModEntity(EntityCamera.class, "EntityCamera", 243, WarpDrive.instance, 300, 1, false);
    }

    public void registerRenderers()
    {
    }
}