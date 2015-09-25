package cr0s.warpdrive.block.energy;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.item.ItemIC2reactorLaserFocus;
import cr0s.warpdrive.network.PacketHandler;

public class TileEntityIC2reactorLaserMonitor extends TileEntityAbstractEnergy {
	private int ticks = WarpDriveConfig.IC2_REACTOR_COOLING_INTERVAL_TICKS;
	
	public TileEntityIC2reactorLaserMonitor() {
		super();
		IC2_sinkTier = 2;
		IC2_sourceTier = 2;
	}
	
	private static int[] deltaX = {-2, 2, 0, 0, 0, 0};
	private static int[] deltaY = { 0, 0,-2, 2, 0, 0};
	private static int[] deltaZ = { 0, 0, 0, 0,-2, 2};
	
	// returns IReactor tile entities
	@Optional.Method(modid = "IC2")
	private Set<IReactor> findReactors() {
		Set<IReactor> output = new HashSet<IReactor>();
		
		for(int i = 0; i < deltaX.length; i++) {
			TileEntity tileEntity = worldObj.getTileEntity(xCoord + deltaX[i], yCoord + deltaY[i], zCoord + deltaZ[i]);
			if (tileEntity == null) {
				continue;
			}
			
			if (tileEntity instanceof IReactor) {
				output.add((IReactor)tileEntity);
				
			} else if (tileEntity instanceof IReactorChamber) {
				IReactor reactor = ((IReactorChamber)tileEntity).getReactor();
				if (reactor == null) {
					continue;
				}
				
				// ignore if we're right next to the reactor
				ChunkCoordinates coords = reactor.getPosition();
				if ( Math.abs(coords.posX - xCoord) == 1
				  || Math.abs(coords.posY - yCoord) == 1
				  || Math.abs(coords.posZ - zCoord) == 1) {
					continue;
				}
				
				output.add(reactor);
			}
		}
		return output;
	}
	
	@Optional.Method(modid = "IC2")
	private boolean coolReactor(IReactor reactor) {
		boolean didCoolReactor = false;
		for(int x = 0; x < 9 && !didCoolReactor; x++) {
			for(int y = 0; y < 6 && !didCoolReactor; y++) {
				ItemStack item = reactor.getItemAt(x, y);
				if (item != null) {
					if (item.getItem() instanceof ItemIC2reactorLaserFocus) {
						int heatInLaserFocus = item.getItemDamage();
						int heatRemovable = (int) Math.floor(Math.min(getEnergyStored() / WarpDriveConfig.IC2_REACTOR_ENERGY_PER_HEAT, heatInLaserFocus));
						if (heatRemovable > 0) {
							didCoolReactor = true;
							if (consumeEnergy((int) Math.ceil(heatRemovable * WarpDriveConfig.IC2_REACTOR_ENERGY_PER_HEAT), false)) {
								item.setItemDamage(heatInLaserFocus - heatRemovable);
							}
						}
					}
				}
			}
		}
		return didCoolReactor;
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public void updateEntity() {
		super.updateEntity();
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		
		ticks--;
		if (ticks <= 0)  {
			ticks = WarpDriveConfig.IC2_REACTOR_COOLING_INTERVAL_TICKS;
			Vector3 myPos = new Vector3(this).translate(0.5);
			Set<IReactor> reactors = findReactors();
			if (reactors.size() == 0) {
				return;
			}
			
			for(IReactor reactor : reactors) {
				if (coolReactor(reactor)) {
					PacketHandler.sendBeamPacket(worldObj, myPos, new Vector3(reactor.getPosition()).translate(0.5D), 0.0f, 0.8f, 1.0f, 20, 0, 20);
				}
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public String getStatus() {
		Set<IReactor> reactors = findReactors();
		return getBlockType().getLocalizedName()
			+ String.format(" energy level is %.0f/%.0f EU.", convertInternalToEU(getEnergyStored()), convertInternalToEU(getMaxEnergyStored()))
			+ ((reactors == null || reactors.size() == 0) ? " No reactor found!" : " " + reactors.size() + " reactor(s) connected.");
	}
	
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.IC2_REACTOR_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return true;
	}
}
