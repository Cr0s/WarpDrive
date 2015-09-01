package cr0s.warpdrive.block.collection;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.VectorI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityLaserTreeFarm extends TileEntityAbstractMiner {
	private boolean active = false;

	private int mode = 0;
	private boolean doLeaves = false;
	private boolean silkTouchLeaves = false;
	private boolean treeTap = false;

	private final int radiusDefault = 8;
	private final int scanWait = 40;
	private final int mineWait = 4;
	private int delayMul = 4;

	private int totalHarvested = 0;

	private int scan = 0;
	private int radiusX = radiusDefault;
	private int radiusZ = radiusDefault;

	LinkedList<VectorI> logs;
	private int logIndex = 0;

	public TileEntityLaserTreeFarm() {
		super();
		laserOutputSide = ForgeDirection.UP;
		IC2_sinkTier = 2;
		IC2_sourceTier = 2;
		peripheralName = "warpdriveLaserTreefarm";
		methodsArray = new String[] {
				"start",
				"stop",
				"radius",
				"leaves",
				"silkTouch",
				"silkTouchLeaves",
				"treetap",
				"state"
		};
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (active) {
			scan++;
			if (mode == 0) {
				if (scan >= scanWait) {
					scan = 0;
					logs = scanTrees();
					if(logs.size() > 0)
						mode = treeTap ? 2 : 1;
					logIndex = 0;
				}
			} else {
				if (scan >= mineWait * delayMul) {
					scan = 0;

					if (logIndex >= logs.size()) {
						mode = 0;
						return;
					}
					VectorI pos = logs.get(logIndex);
					Block block = worldObj.getBlock(pos.x, pos.y, pos.z);

					if (mode == 1) {
						int cost = calculateBlockCost(block);
						if (consumeEnergyFromBooster(cost, true)) {
							if (isLog(block) || (doLeaves && isLeaf(block))) {
								delayMul = 1;
								if (isRoomForHarvest()) {
									if (consumeEnergyFromBooster(cost, false)) {
										if (isLog(block)) {
											delayMul = 4;
											totalHarvested++;
										}
										harvestBlock(pos);
									} else {
										return;
									}
								} else {
									return;
								}
							}
							logIndex++;
						}
					} else if(mode == 2) {
						int cost = calculateBlockCost(block);
						if (consumeEnergyFromBooster(cost, true)) {
							if (isRoomForHarvest()) {
								if (block.isAssociatedBlock(WarpDriveConfig.IC2_rubberWood)) {
									int metadata = worldObj.getBlockMetadata(pos.x, pos.y, pos.z);
									if (metadata >= 2 && metadata <= 5) {
										if (WarpDriveConfig.LOGGING_COLLECTION) {
											WarpDrive.logger.info("wetspot found");
										}
										if (consumeEnergyFromBooster(cost, false)) {
											ItemStack resin = WarpDriveConfig.IC2_Resin.copy();
											resin.stackSize = (int) Math.round(Math.random() * 4);
											dumpToInv(resin);
											worldObj.setBlockMetadataWithNotify(pos.x, pos.y, pos.z, metadata + 6, 3);
											laserBlock(pos);
											totalHarvested++;
											delayMul = 4;
										} else {
											return;
										}
									} else {
										delayMul = 1;
									}
								} else if (isLog(block)) {
									if (consumeEnergyFromBooster(cost, false)) {
										delayMul = 4;
										totalHarvested++;
										harvestBlock(pos);
									} else {
										return;
									}
								} else if (isLeaf(block)) {
									if (consumeEnergyFromBooster(cost, true)) {
										delayMul = 1;
										harvestBlock(pos);
									} else {
										return;
									}
								}
							} else {
								return;
							}
							logIndex++;
						}
					}
				}
			}
		}
	}

	private static boolean isLog(Block block) {
		return WarpDriveConfig.BLOCKS_LOGS.contains(block);
	}

	private static boolean isLeaf(Block block) {
		return WarpDriveConfig.BLOCKS_LEAVES.contains(block);
	}

	private static void addTree(LinkedList<VectorI> list, VectorI newTree) {
		if (WarpDriveConfig.LOGGING_COLLECTION) {
			WarpDrive.logger.info("Adding tree position:" + newTree.x + "," + newTree.y + "," + newTree.z);
		}
		list.add(newTree);
	}

	private LinkedList<VectorI> scanTrees() {
		int xmax, zmax;
		int xmin, zmin;
		xmax = xCoord + radiusX;
		xmin = xCoord - radiusX;
		zmax = zCoord + radiusZ;
		zmin = zCoord - radiusZ;
		
		LinkedList<VectorI> logPositions = new LinkedList<VectorI>();
		
		for(int x = xmin; x <= xmax; x++) {
			for(int z = zmin; z <= zmax; z++) {
				Block block = worldObj.getBlock(x, yCoord, z);
				if (isLog(block)) {
					VectorI pos = new VectorI(x, yCoord, z);
					logPositions.add(pos);
					scanNearby(logPositions, x, yCoord, z, 0);
				}
			}
		}
		return logPositions;
	}

	private void scanNearby(LinkedList<VectorI> current, int x, int y, int z, int d) {
		int[] deltas = {0, -1, 1};
		for(int dx : deltas) {
			for(int dy = 1; dy >= 0; dy--) {
				for(int dz : deltas) {
					Block block = worldObj.getBlock(x + dx, y + dy, z + dz);
					if (isLog(block) || (doLeaves && isLeaf(block))) {
						VectorI pos = new VectorI(x + dx, y + dy, z + dz);
						if (!current.contains(pos)) {
							addTree(current, pos);
							if (d < 35) {
								scanNearby(current,x+dx,y+dy,z+dz,d+1);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("radiusX", radiusX);
		tag.setInteger("radiusZ", radiusZ);
		tag.setBoolean("doLeaves", doLeaves);
		tag.setBoolean("active", active);
		tag.setBoolean("treetap", treeTap);
		tag.setBoolean("silkTouchLeaves", silkTouchLeaves);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		radiusX = tag.getInteger("radiusX");
		if (radiusX == 0) {
			radiusX = radiusDefault;
		}
		radiusX = clamp(WarpDriveConfig.TREE_FARM_MIN_RADIUS, WarpDriveConfig.TREE_FARM_MAX_RADIUS, radiusX);
		radiusZ = tag.getInteger("radiusZ");
		if (radiusZ == 0) {
			radiusZ = radiusDefault;
		}
		radiusZ = clamp(WarpDriveConfig.TREE_FARM_MIN_RADIUS, WarpDriveConfig.TREE_FARM_MAX_RADIUS, radiusZ);
		
		doLeaves = tag.getBoolean("doLeaves");
		active   = tag.getBoolean("active");
		treeTap  = tag.getBoolean("treetap");
		silkTouchLeaves = tag.getBoolean("silkTouchLeaves");
	}

	// OpenComputer callback methods
	// FIXME: implement OpenComputers...

	// ComputerCraft IPeripheral methods implementation
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = methodsArray[method];
		if (methodName.equals("start")) {
			if (!active) {
				mode = 0;
				totalHarvested = 0;
				active = true;
			}
			return new Boolean[] { true };
			
		} else if (methodName.equals("stop")) {
			active = false;
			
		} else if (methodName.equals("radius")) {
			try {
				if (arguments.length == 1) {
					radiusX = clamp(WarpDriveConfig.TREE_FARM_MIN_RADIUS, WarpDriveConfig.TREE_FARM_MAX_RADIUS, toInt(arguments[0]));
					radiusZ = radiusX;
				} else if (arguments.length == 2) {
					radiusX = clamp(WarpDriveConfig.TREE_FARM_MIN_RADIUS, WarpDriveConfig.TREE_FARM_MAX_RADIUS, toInt(arguments[0]));
					radiusZ = clamp(WarpDriveConfig.TREE_FARM_MIN_RADIUS, WarpDriveConfig.TREE_FARM_MAX_RADIUS, toInt(arguments[1]));
				}
			} catch(NumberFormatException e) {
				radiusX = radiusDefault;
				radiusZ = radiusDefault;
			}
			return new Integer[] { radiusX , radiusZ };
			
		} else if (methodName.equals("leaves")) {
			try {
				if (arguments.length > 0) {
					doLeaves = toBool(arguments[0]);
				}
			} catch(Exception e) {

			}
			return new Boolean[] { doLeaves };
			
		} else if (methodName.equals("silkTouch")) {
			try {
				silkTouch(arguments[0]);
			} catch(Exception e) {
				silkTouch(false);
			}
			return new Object[] { silkTouch() };
			
		} else if (methodName.equals("silkTouchLeaves")) {
			try {
				if (arguments.length >= 1) {
					silkTouchLeaves = toBool(arguments[0]);
				}
			} catch(Exception e) {
				silkTouchLeaves = false;
			}
			return new Object[] { silkTouchLeaves };
			
		} else if (methodName.equals("treetap")) {
			try {
				if (arguments.length >= 1) {
					treeTap = toBool(arguments[0]);
				}
			} catch(Exception e) {
				treeTap = false;
			}
			return new Object[] { treeTap };
			
		} else if (methodName.equals("state")) {
			String state = active ? (mode==0?"scanning" : (mode == 1 ? "harvesting" : "tapping")) : "inactive";
			return new Object[] { state, radiusX, radiusZ, energy(), totalHarvested };
		}
		return null;
	}

	//ABSTRACT LASER IMPLEMENTATION
	@Override
	protected boolean silkTouch(Block block) {
		if (isLeaf(block)) {
			return silkTouchLeaves;
		}
		return silkTouch();
	}

	@Override
	protected boolean canSilkTouch() {
		return true;
	}

	@Override
	protected int minFortune() {
		return 0;
	}

	@Override
	protected int maxFortune() {
		return 0;
	}

	@Override
	protected float getColorR() {
		return 0.2f;
	}

	@Override
	protected float getColorG() {
		return 0.7f;
	}

	@Override
	protected float getColorB() {
		return 0.4f;
	}
}
