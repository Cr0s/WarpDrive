package cr0s.warpdrive.data;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Transition planes between dimensions to land on a planet or take off from it to reach space.
 *
 * @author LemADEC
 */
public class Planet implements Cloneable {
	public int dimensionId;
	public int dimensionCenterX, dimensionCenterZ;
	public int borderSizeX, borderSizeZ;
	public int spaceCenterX, spaceCenterZ;
	
	public Planet() {
		this(0, 0, 0, 5000, 5000, 0, 0);
	}
	
	public Planet(int parDimensionId, int parDimensionCenterX, int parDimensionCenterZ, int parBorderSizeX, int parBorderSizeZ, int parSpaceCenterX, int parSpaceCenterZ) {
		this.dimensionId = parDimensionId;
		this.spaceCenterX = parSpaceCenterX;
		this.spaceCenterZ = parSpaceCenterZ;
		this.dimensionCenterX = parDimensionCenterX;
		this.dimensionCenterZ = parDimensionCenterZ;
		this.borderSizeX = parBorderSizeX;
		this.borderSizeZ = parBorderSizeZ;
	}
	
	public Planet(NBTTagCompound nbt) {
		readFromNBT(nbt);
	}
	
	/**
	 * Makes a new copy of this TransitionPlane. Prevents variable referencing problems.
	 */
	@Override
	public Planet clone() {
		return new Planet(dimensionId, dimensionCenterX, dimensionCenterZ, borderSizeX, borderSizeZ, spaceCenterX, spaceCenterZ);
	}
	
	/**
	 * Check if current coordinates allow to take off from this dimension to reach space. It's up to caller to verify if this transition plane match current dimension.
	 *
	 * @param currentPosition current position in the planet/dimension
	 * @return distance to transition borders, 0 if take off is possible
	 */
	public int isValidToSpace(VectorI currentPosition) {
		if ((Math.abs(currentPosition.x - dimensionCenterX) <= borderSizeX) && (Math.abs(currentPosition.z - dimensionCenterZ) <= borderSizeZ)) {
			return 0;
		}
		return (int) Math.sqrt(
				  Math.pow(Math.max(0D, Math.abs(currentPosition.x - dimensionCenterX) - borderSizeX), 2.0D)
				+ Math.pow(Math.max(0D, Math.abs(currentPosition.z - dimensionCenterZ) - borderSizeZ), 2.0D));
	}
	
	/**
	 * Check if current space coordinates allow to enter this dimension atmosphere from space. It's up to caller to verify if we're actually in space.
	 *
	 * @param currentPosition current position in space
	 * @return distance to transition borders, 0 if entry is possible
	 */
	public int isValidFromSpace(VectorI currentPosition) {
		if ((Math.abs(currentPosition.x - spaceCenterX) <= borderSizeX) && (Math.abs(currentPosition.z - spaceCenterZ) <= borderSizeZ)) {
			return 0;
		}
		return (int) Math.sqrt(Math.pow(Math.max(0D, Math.abs(currentPosition.x - spaceCenterX) - borderSizeX), 2.0D)
				+ Math.pow(Math.max(0D, Math.abs(currentPosition.z - spaceCenterZ) - borderSizeZ), 2.0D));
	}
	
	public void readFromNBT(NBTTagCompound tag) {
		this.dimensionId = tag.getInteger("dimensionId");
		this.dimensionCenterX = tag.getInteger("dimensionCenterX");
		this.dimensionCenterZ = tag.getInteger("dimensionCenterZ");
		this.borderSizeX = tag.getInteger("borderSizeX");
		this.borderSizeZ = tag.getInteger("borderSizeZ");
		this.spaceCenterX = tag.getInteger("spaceCenterX");
		this.spaceCenterZ = tag.getInteger("spaceCenterZ");
	}
	
	public void writeToNBT(NBTTagCompound tag) {
		tag.setInteger("dimensionId", dimensionId);
		tag.setInteger("dimensionCenterX", dimensionCenterX);
		tag.setInteger("dimensionCenterZ", dimensionCenterZ);
		tag.setInteger("borderSizeX", borderSizeX);
		tag.setInteger("borderSizeZ", borderSizeZ);
		tag.setInteger("spaceCenterX", spaceCenterX);
		tag.setInteger("spaceCenterZ", spaceCenterZ);
	}
	
	@Override
	public int hashCode() {
		return dimensionId << 16 + (dimensionCenterX >> 10) << 8 + (dimensionCenterZ >> 10);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Planet) {
			Planet planet = (Planet) object;
			return this.dimensionId == planet.dimensionId
				&& this.dimensionCenterX == planet.dimensionCenterX
				&& this.dimensionCenterZ == planet.dimensionCenterZ
				&& this.borderSizeX == planet.borderSizeX
				&& this.borderSizeZ == planet.borderSizeZ
				&& this.spaceCenterX == planet.spaceCenterX
				&& this.spaceCenterZ == planet.spaceCenterZ;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "Planet [Dimension " + dimensionId + "(" + dimensionCenterX + ", " + dimensionCenterZ + ")"
				+ " Border(" + borderSizeX + ", " + borderSizeZ + ")"
				+ " Space(" + spaceCenterX + ", " + spaceCenterZ + ")]";
	}
}