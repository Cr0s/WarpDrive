package cr0s.WarpDrive;

import java.util.ArrayList;

import cr0s.WarpDrive.machines.TileEntityReactor;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

/** Registry of active Warp Cores in world
 * @author Cr0s
 */
public class WarpCoresRegistry
{
	private ArrayList<TileEntityReactor> registry;

	public WarpCoresRegistry()
	{
		registry = new ArrayList<TileEntityReactor>();
	}

	public int searchCoreInRegistry(TileEntityReactor core)
	{
		int res = -1;

		for (int i = 0; i < registry.size(); i++)
		{
			TileEntityReactor c = registry.get(i);

			if (c.xCoord == core.xCoord && c.yCoord == core.yCoord && c.zCoord == core.zCoord)
			{
				return i;
			}
		}

		return res;
	}

	public boolean isCoreInRegistry(TileEntityReactor core)
	{
		return (searchCoreInRegistry(core) != -1);
	}

	public void updateInRegistry(TileEntityReactor core)
	{
		int idx = searchCoreInRegistry(core);

		// update
		if (idx != -1)
		{
			registry.set(idx, core);
		}
		else
		{
			registry.add(core);
		}
	}

	public void removeFromRegistry(TileEntityReactor core)
	{
		int idx;

		if ((idx = searchCoreInRegistry(core)) != -1)
		{
			registry.remove(idx);
		}
	}

	public ArrayList<TileEntityReactor> searchWarpCoresInRadius(int x, int y, int z, int radius)
	{
		ArrayList<TileEntityReactor> res = new ArrayList<TileEntityReactor>();

		for (TileEntityReactor c : registry)
		{
			double d3 = c.xCoord - x;
			double d4 = c.yCoord - y;
			double d5 = c.zCoord - z;
			double distance = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);

			if (distance <= radius && !(c.controller == null || c.controller.getMode() == 0) && !isCoreHidden(c))
			{
				res.add(c);
			}
		}

		return res;
	}

	public void printRegistry()
	{
		System.out.println("WarpCores registry:");
		removeDeadCores();

		for (TileEntityReactor c : registry)
		{
			System.out.println(c.coreFrequency + " (" + c.xCoord + "; " + c.yCoord + "; " + c.zCoord + ")");
		}
	}

	final int LOWER_HIDE_POINT = 18;
	private boolean isCoreHidden(TileEntityReactor core)
	{
		if (core.isolationBlocksCount > 5)
		{
			int randomNumber = core.worldObj.rand.nextInt(150);

			if (randomNumber < LOWER_HIDE_POINT + core.isolationBlocksCount)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isWarpCoreIntersectsWithOthers(TileEntityReactor core)
	{
		AxisAlignedBB aabb1, aabb2;
		removeDeadCores();

		for (TileEntityReactor c : registry)
		{
			// Skip cores in other worlds
			if (c.worldObj != core.worldObj)
			{
				continue;
			}

			// Skip self
			if (c.xCoord == core.xCoord && c.yCoord == core.yCoord && c.zCoord == core.zCoord)
			{
				continue;
			}

			// Skip offline warp cores
			if (c.controller == null || c.controller.getMode() == 0 || !c.prepareToJump())
			{
				continue;
			}

			// Search for nearest warp cores
			double d3 = c.xCoord - core.xCoord;
			double d4 = c.yCoord - core.yCoord;
			double d5 = c.zCoord - core.zCoord;
			double distance = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);

			if (distance <= (2 * WarpDriveConfig.WC_MAX_SHIP_SIDE) - 1)
			{
				// Check for warpfields intersections
				core.prepareToJump(); // calculate spatial parameters
				c.prepareToJump();
				
				// Compare warp-fields for intersection
				aabb1 = AxisAlignedBB.getBoundingBox(core.minX, core.minY, core.minZ, core.maxX, core.maxY, core.maxZ);
				aabb2 = AxisAlignedBB.getBoundingBox(c.minX, c.minY, c.minZ, c.maxX, c.maxY, c.maxZ);

				if (aabb1.intersectsWith(aabb2))
				{
					return true;
				}
			}
		}

		return false;
	}

	public void removeDeadCores()
	{
		LocalProfiler.start("WCR.removeDeadCores()");
		ArrayList<TileEntityReactor> oldRegistry = (ArrayList<TileEntityReactor>) registry.clone();

		for (TileEntityReactor c : registry)
			if (c == null || c.worldObj == null || c.worldObj.getBlockId(c.xCoord, c.yCoord, c.zCoord) != WarpDriveConfig.coreID || c.worldObj.getBlockTileEntity(c.xCoord, c.yCoord, c.zCoord) != c || c.worldObj.getBlockTileEntity(c.xCoord, c.yCoord, c.zCoord).isInvalid())
				oldRegistry.remove(c);

		// Update old registry to new witout dead cores
		this.registry = (ArrayList<TileEntityReactor>) oldRegistry.clone();
		LocalProfiler.stop();
	}

	// TODO: fix it to normal work in client
	/*public boolean isEntityInsideAnyWarpField(Entity e) {
		AxisAlignedBB aabb1, aabb2;

		double x = e.posX;
		double y = e.posY;
		double z = e.posZ;

		for (TileEntityReactor c : registry) {
			// Skip offline or disassembled warp cores
			if (c.controller == null || !c.prepareToJump()) {
				System.out.println("Skipping " + c);
				if (c.controller == null) {
					System.out.println("Controller is null!");
					continue;
				}

				if (c.controller.getMode() == 0) {
					System.out.println("Mode is zero!");
					continue;
				}

				if (!c.prepareToJump()) {
					System.out.println("prepareToJump() returns false!");
					continue;
				}
				continue;
			}

			if (c.minX <= x && c.maxX >= x && c.minY <= y && c.maxY >= y && c.minZ <= z && c.maxZ >= z) {
				return true;
			}
		}

		return false;
	}*/
}
