package cr0s.WarpDrive;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

public class WarpDrivePeripheralHandler implements IPeripheralProvider
{

	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side)
	{
		WarpDrive.debugPrint("Checking " + x + "," + y +"," + z);
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof IPeripheral)
			return (IPeripheral)te;
		return null;
	}

}
