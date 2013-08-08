package shipmod.render;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import shipmod.entity.EntityShip;

public class RenderShip extends Render
{
    public RenderShip()
    {
        this.shadowSize = 1.0F;
    }

    public void renderVehicle(EntityShip entity, double x, double y, double z, float yaw, float pitch)
    {
        GL11.glPushAttrib(8256);
        RenderHelper.disableStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
        float fx = entity.getShipChunk().getCenterX();
        float fz = entity.getShipChunk().getCenterZ();
        GL11.glTranslatef(-fx, (float)(-entity.getShipChunk().minY()), -fz);
        float f4 = 0.75F;
        this.func_110777_b(entity);
        entity.getShipChunk().renderer.render(0.0F);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity entity, double x, double y, double z, float yaw, float pitch)
    {
        this.renderVehicle((EntityShip)entity, x, y, z, yaw, pitch);
    }

    protected ResourceLocation func_110775_a(Entity entity)
    {
        return TextureMap.field_110575_b;
    }
}
