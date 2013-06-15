/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cr0s.WarpDrive;

import java.util.ArrayList;
import net.minecraft.util.MathHelper;

/**
 *
 * @author user
 */
public class WarpCoresRegistry {
    private ArrayList<TileEntityReactor> registry;
    public WarpCoresRegistry() {
        registry = new ArrayList<TileEntityReactor>();
    }
    
    public int searchCoreInRegistry(TileEntityReactor core) {
        int res = -1;
        
        for (int i = 0; i < registry.size(); i++) {
            TileEntityReactor c = registry.get(i);
            
            if (c.xCoord == core.xCoord && c.yCoord == core.yCoord && c.zCoord == core.zCoord) {
                return i;
            }
        }
        
        return res;
    }
    
    public boolean isCoreInRegistry(TileEntityReactor core) {
        return (searchCoreInRegistry(core) != -1);
    }
    
    public void addToRegistry(TileEntityReactor core) {
        if (!isCoreInRegistry(core)) {
            registry.add(core);
        }
    }
    
    public void removeFromRegistry(TileEntityReactor core) {
        int idx;
        
        if ((idx = searchCoreInRegistry(core)) != -1) {
            registry.remove(idx);
        }
    }
    
    public ArrayList<TileEntityReactor> searchWarpCoresInRadius(int x, int y, int z, int radius) {
        ArrayList<TileEntityReactor> res = new ArrayList<TileEntityReactor>();
        
        for (TileEntityReactor c : registry) {
            double d3 = c.xCoord - x;
            double d4 = c.yCoord - y;
            double d5 = c.zCoord - z;

            double distance = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
            
            if (distance <= radius)
            {
                System.out.println("Scan: " + MathHelper.floor_double(distance) + " <= " + radius);
                res.add(c);
            }
        }
        
        return res;
    }
       
    public void printRegistry() {
        System.out.println("WarpCores registry:");
        
        for (TileEntityReactor c : registry) {
            System.out.println(c.coreFrequency + " (" + c.xCoord + "; " + c.yCoord + "; " + c.zCoord + ")");
        }
    }
}
