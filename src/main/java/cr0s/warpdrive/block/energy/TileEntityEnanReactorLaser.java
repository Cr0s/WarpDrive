package cr0s.warpdrive.block.energy;

import cpw.mods.fml.common.Optional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockUpdateDetector;
import cr0s.warpdrive.block.TileEntityAbstractLaser;
import cr0s.warpdrive.block.TileEntityLaserMedium;
import cr0s.warpdrive.conf.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityEnanReactorLaser extends TileEntityAbstractLaser implements IBlockUpdateDetector {
	Vector3 myVec;
	Vector3 reactorVec;
	ForgeDirection side = ForgeDirection.UNKNOWN;
	TileEntityLaserMedium booster;
	TileEntityEnanReactorCore reactor;
	
	private boolean isFirstUpdate = true;
	
	public TileEntityEnanReactorLaser() {
		methodsArray = new String[] { "energy", "hasReactor", "side", "sendLaser" };
		peripheralName = "warpdriveEnanReactorLaser";
	}
	
	public TileEntityEnanReactorCore scanForReactor() {
		reactor = null;
		TileEntity tileEntity;
		// I AM ON THE NORTH SIDE
		side = ForgeDirection.UNKNOWN;
		tileEntity = worldObj.getTileEntity(xCoord, yCoord, zCoord + 2);
		if (tileEntity instanceof TileEntityEnanReactorCore && worldObj.isAirBlock(xCoord, yCoord, zCoord + 1)) {
			side = ForgeDirection.NORTH;
			reactor = (TileEntityEnanReactorCore) tileEntity;
		}
		
		// I AM ON THE SOUTH SIDE
		tileEntity = worldObj.getTileEntity(xCoord, yCoord, zCoord - 2);
		if (tileEntity instanceof TileEntityEnanReactorCore && worldObj.isAirBlock(xCoord, yCoord, zCoord - 1)) {
			side = ForgeDirection.SOUTH;
			reactor = (TileEntityEnanReactorCore) tileEntity;
		}
		
		// I AM ON THE WEST SIDE
		tileEntity = worldObj.getTileEntity(xCoord + 2, yCoord, zCoord);
		if (tileEntity instanceof TileEntityEnanReactorCore && worldObj.isAirBlock(xCoord + 1, yCoord, zCoord)) {
			side = ForgeDirection.WEST;
			reactor = (TileEntityEnanReactorCore) tileEntity;
		}
		
		// I AM ON THE EAST SIDE
		tileEntity = worldObj.getTileEntity(xCoord - 2, yCoord, zCoord);
		if (tileEntity instanceof TileEntityEnanReactorCore && worldObj.isAirBlock(xCoord - 1, yCoord, zCoord)) {
			side = ForgeDirection.EAST;
			reactor = (TileEntityEnanReactorCore) tileEntity;
		}
		
		setMetadata();
		
		if (reactor != null) {
			reactorVec = new Vector3(reactor).translate(0.5);
		}
		return reactor;
	}
	
	private void setMetadata() {
		int metadata = 0;
		if (side != ForgeDirection.UNKNOWN) {
			metadata = side.ordinal() - 1;
		}
		if (getBlockMetadata() != metadata) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 3);
		}
	}
	
	public TileEntityLaserMedium scanForBooster() {
		booster = null;
		TileEntity tileEntity;
		tileEntity = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
		if (tileEntity != null && tileEntity instanceof TileEntityLaserMedium) {
			booster = (TileEntityLaserMedium) tileEntity;
		}
		
		tileEntity = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
		if (tileEntity != null && tileEntity instanceof TileEntityLaserMedium) {
			booster = (TileEntityLaserMedium) tileEntity;
		}
		
		return booster;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (isFirstUpdate) {
			isFirstUpdate = false;
			scanForReactor();
			scanForBooster();
			myVec = new Vector3(this).translate(0.5);
		}
	}
	
	public void unlink() {
		side = ForgeDirection.UNKNOWN;
		setMetadata();
	}
	
	@Override
	public void updatedNeighbours() {
		super.updatedNeighbours();
		scanForBooster();
		scanForReactor();
	}
	
	private void laserReactor(int energy) {
		if (energy <= 0) {
			return;
		}
		
		scanForBooster();
		scanForReactor();
		if (booster == null)
			return;
		if (reactor == null)
			return;
		if (booster.consumeEnergy(energy, false)) {
			if (WarpDriveConfig.LOGGING_ENERGY && WarpDriveConfig.LOGGING_LUA) {
				WarpDrive.logger.info("ReactorLaser on " + side.toString() + " side sending " + energy);
			}
			reactor.decreaseInstability(side, energy);
			PacketHandler.sendBeamPacket(worldObj, myVec, reactorVec, 0.1F, 0.2F, 1.0F, 25, 50, 100);
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}
	
	// ComputerCraft methods
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = methodsArray[method];
		if (methodName.equals("energy")) {
			scanForBooster();
			if (booster == null) {
				return new Object[] { 0, 0 };
			} else {
				return new Object[] { booster.getEnergyStored(), booster.getMaxEnergyStored() };
			}
			
		} else if (methodName.equals("hasReactor")) {
			return new Object[] { scanForReactor() != null };
			
		} else if (methodName.equals("sendLaser")) {
			if (arguments.length >= 1) {
				laserReactor(toInt(arguments[0]));
			}
			
		} else if (methodName.equals("side")) {
			return new Object[] { side.ordinal() - 2 };
		}
		return null;
	}
}