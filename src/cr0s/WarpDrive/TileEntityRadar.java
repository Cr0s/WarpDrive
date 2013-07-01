package cr0s.WarpDrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;
import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import java.util.ArrayList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityRadar extends TileEntity implements IPeripheral, IEnergySink {
    
    public boolean addedToEnergyNet;
    
    private final int MAX_ENERGY_VALUE = 100 * (1000 * 1000); // 100 000 000 Eu
    private int currentEnergyValue = 0;
    
    private String[] methodsArray = { 
                                        "scanRay",                             // 0
                                        "scanRadiusW",                         // 1
                                        "getResultsCountW",                    // 2
                                        "getResultW",                          // 3
                                        "getEnergyLevel",                      // 4
            
                                        "getRadarX", "getRadarY", "getRadarZ", // 5, 6, 7
                                    };
    
    private ArrayList<TileEntityReactor> results;
    
    private int scanRadius = 0;
    private int cooldownTime = 0;
    
    private boolean isEnergyEnoughForScanRadiusW(int radius) {
        int needEnergy = (radius * radius);
        
        return ((getCurrentEnergyValue() - needEnergy) > 0);
    }
    
    @Override
    public void updateEntity() {
        if (!addedToEnergyNet && !worldObj.isRemote) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            return;
        try {
            if (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 2) {
                if (cooldownTime++ > (20 * ((scanRadius / 1000) + 1))) {
                    //System.out.println("Scanning...");
                    WarpDrive.instance.registry.removeDeadCores();
                    results = WarpDrive.instance.registry.searchWarpCoresInRadius(xCoord, yCoord, zCoord, scanRadius);
                    worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);

                    cooldownTime = 0;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        
        this.currentEnergyValue = tag.getInteger("energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        
        tag.setInteger("energy", this.getCurrentEnergyValue());
    }    
    
    // IPeripheral methods implementation
    @Override
    public String getType() {
        return "radar";
    }

    @Override
    public String[] getMethodNames() {
        return methodsArray;
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception {
        switch (method) {
            case 0: // scanRay (toX, toY, toZ)
                return new Object[] { -1 };
                
            case 1: // scanRadiusW (radius)
                if (arguments.length == 1) {
                    int radius;
                    
                    try { 
                        radius = ((Double)arguments[0]).intValue();
                    } catch (Exception e) { radius = 0; }
                    
                    if (radius != 0 && isEnergyEnoughForScanRadiusW(radius)) {
                        // Consume energy
                        this.currentEnergyValue -= radius * radius;
                        
                        //System.out.println("Start scanning...");
                        // Begin searching
                        scanRadius = radius;
                        cooldownTime = 0;
                        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 1 + 2);
                    } else {
                        results = null;
                        System.out.println("Radius: " + radius + " | Enough energy: " + isEnergyEnoughForScanRadiusW(radius));
                    }
                }
                
                return new Object[] { 0 };
                
            case 2: // getResultsCountW
                if (results != null) {
                    return new Integer[] { results.size() };
                }
                
                break;
                
            case 3: // getResultW
                if (arguments.length == 1 && (results != null)) {
                    int index;
                    
                    try { 
                        index = ((Double)arguments[0]).intValue();
                    } catch (Exception e) { index = -1; }
                    
                    if (index > -1 && index < results.size()) {
                    
                        TileEntityReactor res = results.get(index);

                        return new Object[] { ((String)res.coreFrequency), ((Integer)res.xCoord), ((Integer)res.yCoord), ((Integer)res.zCoord) };
                    }
                }
                
            case 4: // getEnergyLevel
                return new Integer[] { this.getCurrentEnergyValue()};
                
            case 5: // getRadarX
                return new Integer[] { this.xCoord };
                
            case 6: // getRadarY
                return new Integer[] { this.yCoord };
                
            case 7: // getRadarZ
                return new Integer[] { this.zCoord };
                
        }
        
        return new Object[] { 0 };
    }

    @Override
    public boolean canAttachToSide(int side) {
        return true;
    }

    @Override
    public void attach(IComputerAccess computer) {
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
    }

    @Override
    public void detach(IComputerAccess computer) {
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);
    }

    // IEnergySink methods implementation
    @Override
    public int demandsEnergy() {
        return (MAX_ENERGY_VALUE - currentEnergyValue);
    }

    @Override
    public int injectEnergy(Direction directionFrom, int amount) {
        // Избыток энергии
        int leftover = 0;

        currentEnergyValue += amount;
        if (getCurrentEnergyValue() > MAX_ENERGY_VALUE) {
            leftover = (getCurrentEnergyValue() - MAX_ENERGY_VALUE);
            currentEnergyValue = MAX_ENERGY_VALUE;
        }

        return leftover;
    }

    @Override
    public int getMaxSafeInput() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
        return true;
    }

    @Override
    public boolean isAddedToEnergyNet() {
        return addedToEnergyNet;
    }

    /**
     * @return the currentEnergyValue
     */
    public int getCurrentEnergyValue() {
        return currentEnergyValue;
    }

    @Override
    public void invalidate() {
        if (addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
        super.invalidate();
    }

}
