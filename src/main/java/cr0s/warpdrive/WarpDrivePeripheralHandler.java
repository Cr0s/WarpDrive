package cr0s.warpdrive;

import cpw.mods.fml.common.Optional;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

@Optional.InterfaceList({
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft")
})
public class WarpDrivePeripheralHandler implements IPeripheralProvider {
	public void register() {
		ComputerCraftAPI.registerPeripheralProvider(this);
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
//		WarpDrive.debugPrint("Checking Peripheral at " + x + ", " + y + ", " + z);
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof IPeripheral && ((IPeripheral) te).getType() != null) {
			return (IPeripheral)te;
		}
		return null;
	}
}