package cr0s.warpdrive;

import cpw.mods.fml.common.registry.EntityRegistry;
import cr0s.warpdrive.conf.WarpDriveConfig;
import cr0s.warpdrive.render.EntityCamera;
import cr0s.warpdrive.world.EntitySphereGen;
import cr0s.warpdrive.world.EntityStarCore;

public class CommonProxy
{
    public void registerEntities() {
    	EntityRegistry.registerModEntity(EntityJump.class     , "EntityJump"           , WarpDriveConfig.G_ENTITY_JUMP_ID            , WarpDrive.instance,  80, 1, false);
        EntityRegistry.registerModEntity(EntitySphereGen.class, "EntitySphereGenerator", WarpDriveConfig.G_ENTITY_SPHERE_GENERATOR_ID, WarpDrive.instance, 200, 1, false);
        EntityRegistry.registerModEntity(EntityStarCore.class , "EntityStarCore"       , WarpDriveConfig.G_ENTITY_STAR_CORE_ID       , WarpDrive.instance, 300, 1, false);
        EntityRegistry.registerModEntity(EntityCamera.class   , "EntityCamera"         , WarpDriveConfig.G_ENTITY_CAMERA_ID          , WarpDrive.instance, 300, 1, false);
    }

    public void registerRenderers() {
    }
}