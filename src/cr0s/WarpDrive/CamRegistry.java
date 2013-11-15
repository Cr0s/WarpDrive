package cr0s.WarpDrive;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class CamRegistry
{
    private ArrayList<CamRegistryItem> registry;

    public CamRegistry()
    {
        registry = new ArrayList<CamRegistryItem>();
    }

    public CamRegistryItem getCamByFreq(int frequency, World worldObj)
    {
        for (CamRegistryItem i : registry)
        {
            if (i.freq == frequency && i.worldObj == worldObj)
            {
                return i;
            }
        }

        return null;
    }

    public boolean isCamAlive(CamRegistryItem i)
    {
        if (i.worldObj != null)
        {
            if (i.worldObj.getBlockId(i.camPos.x, i.camPos.y, i.camPos.z) != WarpDrive.instance.config.camID && i.worldObj.getBlockId(i.camPos.x, i.camPos.y, i.camPos.z) != WarpDrive.instance.config.laserCamID)
            {
                return false;
            }

            return true;
        }

        return false;
    }

    public void removeDeadCams()
    {
        for (CamRegistryItem i : registry)
        {
            if (!isCamAlive(i))
            {
                registry.remove(i);
                return;
            }
        }
    }

    public void updateInRegistry(CamRegistryItem i)
    {
        removeDeadCams();

        if (isCamAlive(i))
        {
            CamRegistryItem existingCam = this.getCamByFreq(i.freq, i.worldObj);

            if (existingCam == null)
            {
                registry.add(i);
            }
        }
    }
}
