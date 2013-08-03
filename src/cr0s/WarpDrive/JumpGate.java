package cr0s.WarpDrive;

import net.minecraft.util.AxisAlignedBB;

public class JumpGate {
    public String name;
    public int xCoord, yCoord, zCoord;

    public JumpGate(String name, int x, int y, int z) {
        this.name = name;
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
    }
    public JumpGate(String line) {
        String[] params = line.split(":");

        if (params.length < 4) {
            return;
        }

        name = params[0];
        xCoord = Integer.parseInt(params[1]);
        yCoord = Integer.parseInt(params[2]);
        zCoord = Integer.parseInt(params[3]);
    }

    public AxisAlignedBB getGateAABB() {
        int xmin, ymin, zmin;
        int xmax, ymax, zmax;
        
        xmin = xCoord - (JumpGateGenerator.GATE_LENGTH_HALF * 2);
        xmax = xCoord + (JumpGateGenerator.GATE_LENGTH_HALF * 2);
        
        ymin = yCoord - (JumpGateGenerator.GATE_SIZE_HALF);
        ymax = yCoord + (JumpGateGenerator.GATE_SIZE_HALF);
        
        zmin = zCoord - (JumpGateGenerator.GATE_SIZE_HALF);
        zmax = zCoord + (JumpGateGenerator.GATE_SIZE_HALF);
        
        return AxisAlignedBB.getBoundingBox(xmin, ymin, zmin, xmax, ymax, zmax);
    }
    
    @Override
    public String toString() {
        return name + ":" + xCoord + ":" + yCoord + ":" + zCoord;
    }
    
    public String toNiceString() {
        return name + " (" + xCoord + "; " + zCoord + ")";
    }
}