package cr0s.WarpDrive;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.Configuration;

public class WarpDriveConfig {
	private Configuration config;
	
	public Set<Integer> valuableOres;
	public Set<Integer> minerValuables;
	
	public int coreID, controllerID, radarID, isolationID, airID, airgenID, gasID, laserID, miningLaserID, particleBoosterID, liftID, laserCamID, camID, monitorID, iridiumID;
	/*
	 *     public final static int WARP_CORE_BLOCKID = 500;
    public final static int PROTOCOL_BLOCK_BLOCKID = 501;
    public final static int RADAR_BLOCK_BLOCKID = 502;
    public final static int ISOLATION_BLOCKID = 503;
    public final static int AIR_BLOCKID = 504;
    public final static int AIRGEN_BLOCKID = 505;
    public final static int GAS_BLOCKID = 506;
    
    public final static int LASER_BLOCK_BLOCKID = 507;
    public final static int MINING_LASER_BLOCK_BLOCKID = 508;
    public final static int PARTICLE_BOOSTER_BLOCKID = 509;
    public final static int LIFT_BLOCKID = 510;
    
    public final static int LASER_BLOCKCAM_BLOCKID = 512;
    public final static int CAMERA_BLOCKID = 513;
    public final static int MONITOR_BLOCKID = 514;
    
    public final static int IRIDIUM_BLOCKID = 515;
	 */
	public WarpDriveConfig(Configuration config) {
		this.config = config;
		
		this.valuableOres = new HashSet<Integer>();
		this.minerValuables = new HashSet<Integer>();
	}
	
	public void loadAndSave() {
		this.config.load();
		
		this.coreID = this.config.getBlock("core", 500).getInt();
		this.controllerID = this.config.getBlock("controller", 501).getInt();
		
		this.radarID = this.config.getBlock("radar", 502).getInt();
		this.isolationID = this.config.getBlock("isolation", 503).getInt();
		
		this.airID = this.config.getBlock("air", 504).getInt();
		this.airgenID = this.config.getBlock("airgen", 505).getInt();
		this.gasID = this.config.getBlock("gas", 506).getInt();
		
		this.laserID = this.config.getBlock("laser", 507).getInt();
		this.miningLaserID = this.config.getBlock("mininglaser", 508).getInt();
		this.particleBoosterID = this.config.getBlock("particlebooster", 509).getInt();
		this.liftID = this.config.getBlock("lift", 510).getInt();
		
		this.laserCamID = this.config.getBlock("lasercam", 512).getInt();
		this.camID = this.config.getBlock("camera", 513).getInt();
		this.monitorID = this.config.getBlock("monitor", 514).getInt();
		
		this.iridiumID = this.config.getBlock("iridium", 515).getInt();
		
		this.config.save();
	}
}
