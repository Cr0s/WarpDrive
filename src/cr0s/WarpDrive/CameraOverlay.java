package cr0s.WarpDrive;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.ForgeSubscribe;

public class CameraOverlay
{
    private Minecraft mc;

    public CameraOverlay(Minecraft mc)
    {
        this.mc = mc;
    }

    protected void renderOverlay(int par1, int par2)
    {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        if (WarpDrive.instance.overlayType == 0)
        {
            this.mc.getTextureManager().bindTexture(new ResourceLocation("warpdrive", "textures/blocks/camOverlay.png"));
        }
        else
        {
            this.mc.getTextureManager().bindTexture(new ResourceLocation("warpdrive", "textures/blocks/lasercamOverlay.png"));
        }

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0.0D, (double)par2, -90.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV((double)par1, (double)par2, -90.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV((double)par1, 0.0D, -90.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
        tessellator.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @ForgeSubscribe
    public void onRender(RenderGameOverlayEvent.Pre event)
    {
        if (WarpDrive.instance.isOverlayEnabled)
        {
            if (event.type == ElementType.HELMET)
            {
                renderOverlay(event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
            }
            else if (event.type == ElementType.EXPERIENCE || event.type == ElementType.HOTBAR || event.type == ElementType.ARMOR || event.type == ElementType.HEALTH || event.type == ElementType.HEALTHMOUNT || event.type == ElementType.FOOD || event.type == ElementType.BOSSHEALTH || event.type == ElementType.TEXT)
            {
                // Don't render other GUI parts
                event.setCanceled(true);
            }
        }
    }
}