package shipmod.render;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import shipmod.ShipMod;
import shipmod.chunk.MobileChunk;

public class MobileChunkRenderer
{
    private MobileChunk chunk;
    private RenderBlocks blockRenderer;
    private int glRenderList = -1;
    public boolean isInFrustum = false;
    public boolean[] skipRenderPass = new boolean[2];
    public boolean needsUpdate;
    public boolean isRemoved;
    public AxisAlignedBB rendererBoundingBox;
    private boolean isInitialized = false;
    private List<TileEntity> tileEntityRenderers = new ArrayList();
    public List<TileEntity> tileEntities;
    private int bytesDrawn;

    public MobileChunkRenderer(MobileChunk vehiclechunk)
    {
        this.chunk = vehiclechunk;
        this.needsUpdate = true;
        this.tileEntities = new ArrayList();
    }

    public void render(float partialticks)
    {
        if (this.isRemoved)
        {
            if (this.glRenderList != -1)
            {
                GLAllocation.deleteDisplayLists(this.glRenderList);
                this.glRenderList = -1;
            }
        }
        else
        {
            if (this.needsUpdate)
            {
                try
                {
                    this.updateRender();
                }
                catch (Exception var7)
                {
                    ShipMod.modLogger.log(Level.SEVERE, "A mobile chunk render error has occured", var7);
                }
            }

            if (this.glRenderList != -1)
            {
                for (int pass = 0; pass < 2; ++pass)
                {
                    GL11.glCallList(this.glRenderList + pass);
                    RenderHelper.enableStandardItemLighting();
                    Iterator it = this.tileEntityRenderers.iterator();

                    while (it.hasNext())
                    {
                        TileEntity tile = (TileEntity)it.next();

                        try
                        {
                            if (tile.shouldRenderInPass(pass))
                            {
                                this.renderTileEntity(tile, partialticks);
                            }
                        }
                        catch (Exception var6)
                        {
                            it.remove();
                            ShipMod.modLogger.log(Level.SEVERE, "A tile entity render error has occured", var6);
                        }
                    }
                }
            }
        }
    }

    public void renderTileEntity(TileEntity par1TileEntity, float partialticks)
    {
        try {
            int i = this.chunk.getLightBrightnessForSkyBlocks(par1TileEntity.xCoord, par1TileEntity.yCoord, par1TileEntity.zCoord, 0);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            
        	TileEntityRenderer.instance.renderTileEntityAt(par1TileEntity, (double)par1TileEntity.xCoord, (double)par1TileEntity.yCoord, (double)par1TileEntity.zCoord, partialticks);
        } catch (Exception e) {
        	if (Tessellator.instance.isDrawing) {
        		Tessellator.instance.draw();
        	}
        }
     }

    private void updateRender()
    {
        if (this.glRenderList == -1)
        {
            this.glRenderList = GLAllocation.generateDisplayLists(2);
        }

        this.needsUpdate = false;

        for (int hashset0 = 0; hashset0 < 2; ++hashset0)
        {
            this.skipRenderPass[hashset0] = true;
        }

        Chunk.isLit = false;
        HashSet var14 = new HashSet();
        var14.addAll(this.tileEntityRenderers);
        this.tileEntityRenderers.clear();
        boolean b0 = true;
        RenderBlocks renderblocks = new RenderBlocks(this.chunk);
        this.bytesDrawn = 0;

        for (int hashset1 = 0; hashset1 < 2; ++hashset1)
        {
            boolean flag = false;
            boolean flag1 = false;
            boolean isDrawStarted = false;

            for (int y = this.chunk.minY(); y < this.chunk.maxY(); ++y)
            {
                for (int z = this.chunk.minZ(); z < this.chunk.maxZ(); ++z)
                {
                    for (int x = this.chunk.minX(); x < this.chunk.maxX(); ++x)
                    {
                        int l2 = this.chunk.getBlockId(x, y, z);

                        if (l2 > 0)
                        {
                            if (!isDrawStarted)
                            {
                            	isDrawStarted = true;
                                GL11.glNewList(this.glRenderList + hashset1, GL11.GL_COMPILE);
                                GL11.glPushMatrix();
                                float block = 1.000001F;
                                GL11.glTranslatef(-8.0F, -8.0F, -8.0F);
                                GL11.glScalef(block, block, block);
                                GL11.glTranslatef(8.0F, 8.0F, 8.0F);
                                Tessellator.instance.startDrawingQuads();
                            }

                            Block var16 = Block.blocksList[l2];

                            if (var16 != null)
                            {
                                if (hashset1 == 0 && var16.hasTileEntity(this.chunk.getBlockMetadata(x, y, z)))
                                {
                                    TileEntity blockpass = this.chunk.getBlockTileEntity(x, y, z);

                                    if (TileEntityRenderer.instance.hasSpecialRenderer(blockpass))
                                    {
                                        this.tileEntityRenderers.add(blockpass);
                                    }
                                }

                            	int var17 = var16.getRenderBlockPass();
                                if (var17 > hashset1)
                                {
                                    flag = true;
                                }

                                try {
	                                if (var16.canRenderInPass(hashset1))
	                                {
	                                    flag1 |= renderblocks.renderBlockByRenderType(var16, x, y, z);
	                                }
                                } catch (Exception e) {
                                	flag1 = false;
                                }
                            }
                        }
                    }
                }
            }

            if (isDrawStarted)
            {
                this.bytesDrawn += Tessellator.instance.draw();
                GL11.glPopMatrix();
                GL11.glEndList();
                Tessellator.instance.setTranslation(0.0D, 0.0D, 0.0D);
            }
            else
            {
                flag1 = false;
            }

            if (flag1)
            {
                this.skipRenderPass[hashset1] = false;
            }

            if (!flag)
            {
                break;
            }
        }

        HashSet var15 = new HashSet();
        var15.addAll(this.tileEntityRenderers);
        var15.removeAll(var14);
        this.tileEntities.addAll(var15);
        var14.removeAll(this.tileEntityRenderers);
        this.tileEntities.removeAll(var14);
        this.isInitialized = true;
    }

    public void markDirty()
    {
        this.needsUpdate = true;
    }

    public void markRemoved()
    {
        this.isRemoved = true;

        try
        {
            if (this.glRenderList != -1)
            {
                ShipMod.modLogger.finest("Deleting mobile chunk display list " + this.glRenderList);
                GLAllocation.deleteDisplayLists(this.glRenderList);
                this.glRenderList = -1;
            }
        }
        catch (Exception var2)
        {
            ShipMod.modLogger.log(Level.SEVERE, "Failed to destroy mobile chunk display list", var2);
        }
    }
}
