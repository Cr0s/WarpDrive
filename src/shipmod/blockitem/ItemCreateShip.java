package shipmod.blockitem;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import shipmod.chunk.ChunkBuilder;
import shipmod.entity.EntityShip;

public class ItemCreateShip extends Item
{
    public ItemCreateShip(int id)
    {
        super(id);
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return true;
        }
        else
        {
            ChunkBuilder filler = new ChunkBuilder(world, x, y, z);
            filler.doFilling();

            if (filler.getResult() == 0)
            {
                EntityShip entity = filler.getEntity(world);
                world.spawnEntityInWorld(entity);
                return true;
            }
            else
            {
                if (filler.getResult() == 1)
                {
                    ((EntityPlayerMP)entityplayer).addChatMessage("Cannot create vehicle with more than 200 blocks");
                }
                else if (filler.getResult() == 2)
                {
                    ((EntityPlayerMP)entityplayer).addChatMessage("Cannot create vehicle with no vehicle marker");
                }

                return false;
            }
        }
    }

    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (entity instanceof EntityShip)
        {
            ((EntityShip)entity).decompileToBlocks(true);
            return true;
        }
        else
        {
            return false;
        }
    }
}
