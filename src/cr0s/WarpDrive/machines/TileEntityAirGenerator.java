package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.Vector3;
import cr0s.WarpDrive.WarpDrive;
import cr0s.WarpDrive.WarpDriveConfig;

public class TileEntityAirGenerator extends WarpTE
{

    private final int RF_PER_AIRBLOCK = 10;
    private final int MAX_ENERGY_VALUE = 36 * RF_PER_AIRBLOCK;
    private int currentEnergyValue = 0;

    private int cooldownTicks = 0;
    private final float AIR_POLLUTION_INTERVAL = 4; // seconds

    private final int START_CONCENTRATION_VALUE = 45;

    @Override 
    public int getMaxEnergyStored()
    {
    	return MAX_ENERGY_VALUE;
    }
    
    @Override
    public void updateEntity()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            return;

        // Air generator works only in spaces
        if (!isASpaceDim())
            return;

        if (removeEnergy(RF_PER_AIRBLOCK,true)) //if we have enough energy
        {
            if (cooldownTicks++ > AIR_POLLUTION_INTERVAL * 20) //if we've waited a second since last releasing air
            {
                cooldownTicks = 0; //reset the cooldown
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 2); // set enabled texture
                releaseAir();
            }
        }
        else //if no energy
        {
            if (cooldownTicks++ > 20) //if we've waited a second
            {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2); // set disabled texture
                cooldownTicks = 0; //reset the cooldown
            }
        }
    }

    private void releaseAir()
    {
    	Vector3[] offsets = WarpTE.getAdjacentSideOffsets();
    	for(Vector3 offset: offsets)
    	{
    		if(removeEnergy(RF_PER_AIRBLOCK,false))
    		{
    			int x= xCoord + offset.intX();
    			int y= yCoord + offset.intY();
    			int z= zCoord + offset.intZ();
    			if(worldObj.isAirBlock(x, y, z))
    				worldObj.setBlock(x, y, z, WarpDriveConfig.airID, START_CONCENTRATION_VALUE, 2);
    		}
    	}
    }
}
