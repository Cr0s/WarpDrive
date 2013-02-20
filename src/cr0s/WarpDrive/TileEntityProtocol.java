/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cr0s.WarpDrive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;

/**
 * Protocol block tile entity
 * @author Cr0s
 */
public class TileEntityProtocol extends TileEntity {
    public Boolean[] bits;
    
    public ArrayList<Integer> input;
    public ArrayList<Integer> output;
    
    public boolean RX, TX, RST;
    
    //TileEntityReactor warpCore; // Warp core entity
    TileEntity dataCable;       // Data cable entity
    
    private int ticks = 0;
    
    
    
    private final int UPDATE_TIMEOUT = 5; // seconds
    private final int TRANSFER_RATE = 1;  // ticks
    private final int MAX_INPUT_BUFFER_SIZE = 25; // 25 bytes per packet
    
    // Variables
    private int distance = 0;
    private int direction = 0;
    private int mode = 0;
    
    private boolean jumpFlag = false;
    private boolean summonFlag = false;
    private String toSummon = "";
    
    // Gabarits
    private int front, right, up;
    private int back, left, down;
    
    // Player attaching
    public ArrayList<String> players = new ArrayList();
    public String playersString = "";
    
    boolean ready = false;                // Ready to operate (valid assembly)
    
    @SideOnly(Side.SERVER)
    @Override
    public void updateEntity() {
        ticks++;
        
        ready = dataCable != null;
        
        if (ticks == UPDATE_TIMEOUT) {
            this.dataCable = searchRedPowerCable(); 
            
            ticks = 0;
        }
        
        if (!ready) { 
            return; 
        }

        bits = readCableStates();

        if (dataCable == null || bits == null || bits[8] == null || bits.length == 0) {
            return;
        }
        
        RX  = bits[8];
        TX  = bits[9];
        RST = bits[11];
        
        if (input == null) {
            input = new ArrayList<Integer>();
        }

        if (output == null) {
            output = new ArrayList<Integer>();
        }        
        
        if (RST) {
            System.out.println("RST RECV");
            if (TX) { analyzeInputData(); } else if (RX) { /* sendOutputData();*/ }
            input.clear();
            RST = false;
            resetCableStates();
        } else if (TX) {
            int byteFromCable = readByte();
            System.out.println("Byte from cable: " + byteFromCable + "");
            input.add(byteFromCable);
            
            if (input.size() > MAX_INPUT_BUFFER_SIZE) {
                System.out.println("[!] Buffer overflow.");
            }
            
            resetCableStates();
        } else if (RX) {
            
        }
    }
    
    public TileEntity searchRedPowerCable() {
        TileEntity result;

        result = worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord);
        if (result != null && result.toString().contains("TileCable")) {
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord);
        if (result != null && result.toString().contains("TileCable")) {
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1);
        if (result != null && result.toString().contains("TileCable")) {
            return result;
        }

        result = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1);
        if (result != null && result.toString().contains("TileCable")) {
             return result;
        }

        return null;
    }

    public Boolean[] readCableStates() {
        if (dataCable == null) { return null; }
        
        NBTTagCompound tag = new NBTTagCompound();
        dataCable.writeToNBT(tag);

        byte states[] = tag.getByteArray("pwrs"); // Получить массив состояний кабеля
        if (states == null) {
            return null;
        }

        Boolean[] locCableStates = new Boolean[16];

        for (int i = 0; i < 16; i++) {
            locCableStates[i] = (states[i] != 0);
        }
        
/*        
        if (RX || TX) {
            System.out.println("[C] Cable bits: " + s);
        }
*/        
        return locCableStates;        
    }
    
    private int readByte() {
        int result = 0;
        
        //String binaryString = "";
        Boolean[] states = readCableStates();
        
        // Get first 8 bits
        for (int i = 0; i < 8; i++) {
            result += ((states[i]) ? 1 : 0) * Math.pow((int)2, i);
        }
        
        return result;
    }
    
    private void analyzeInputData() {
        Integer packetID;
        
        if (input == null || input.isEmpty()) {
            return;
        }
        
        packetID = this.input.get(0);
        
        System.out.println("---- PACKET RECV ----");
        System.out.println("PacketID: " + packetID);
        System.out.print("Data (" + (input.size() - 1) + " bytes): ");
        for (int i = 1; i < input.size(); i++) {
            System.out.print(input.get(i));
        }
        System.out.print("\n");
        System.out.println("---- ----");
        
        switch (packetID) {
            case 0xFF:
                    doJump();
                break;
            case 0xFE:
                    setSummonAllFlag(true);
                    break;
                
            case 0x00:   // SETDST
                if (input.size() == 2) {
                    setJumpDistance(input.get(1));
                }
                break;
            case 0x01:  // SETDIR
                if (input.size() == 2) {
                    setDirection(input.get(1));
                }
                break;
            case 0x02:  // SETMODE
                if (input.size() == 2) {
                    setMode(input.get(1));
                }
                break;
                
            case 0x03: // SETPST
                if (input.size() == 4) {
                    System.out.println("Setting positive gabarits: f: " + input.get(1) + " r: " + input.get(2) + " u: " + input.get(3));
                    setFront(input.get(1));
                    setRight(input.get(2));
                    setUp(input.get(3));
                }
                break;
            case 0x04: // SETNEG
                if (input.size() == 4) {
                    System.out.println("Setting negative gabarits: f: " + input.get(1) + " r: " + input.get(2) + " u: " + input.get(3));
                    setBack(input.get(1));
                    setLeft(input.get(2));
                    setDown(input.get(3));
                }
                break;                
                
        }
        
        input.clear();
    }
    
    private void setJumpDistance(int distance) {
        System.out.println("Setting jump distance: " + distance);
        this.distance = distance;
    }

    private void setMode(int mode) {
        System.out.println("Setting mode: " + mode);
        this.mode = mode;
    }    
    
    private void setDirection(int dir) {
        if (dir == 1) { dir = -1; } else if (dir == 2) { dir = -2; } else if (dir == 255) { dir = 270; }
        System.out.println("Setting direction: " + dir);
        this.direction = dir;
    }
    
    private void doJump() {
        //System.out.println("Jumping!");
        setJumpFlag(true);
    }
    
    private void resetCableStates() {
        if (dataCable == null) {
            return;
        }
        NBTTagCompound tag = new NBTTagCompound();
        dataCable.writeToNBT(tag);

        byte states[] = new byte[16];

        tag.setByteArray("pwrs", states);
        dataCable.readFromNBT(tag);        
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        
        setMode(tag.getInteger("mode"));
        
        setFront(tag.getInteger("front"));
        setRight(tag.getInteger("right"));
        setUp(tag.getInteger("up"));
        
        setBack(tag.getInteger("back"));
        setLeft(tag.getInteger("left"));
        setDown(tag.getInteger("down"));
        
        setDistance(tag.getInteger("distance"));
        
        playersString = tag.getString("players");
        updatePlayersList();        
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        
        updatePlayersString();
        tag.setString("players", playersString);    
        
        tag.setInteger("mode", this.mode);
        
        tag.setInteger("front", this.front);
        tag.setInteger("right", this.right);
        tag.setInteger("up", this.up);
        
        tag.setInteger("back", this.back);
        tag.setInteger("left", this.left);
        tag.setInteger("down", this.down);
        
        tag.setInteger("distance", this.distance);
    }  
    
    public void attachPlayer(EntityPlayer ep) {
        for (int i = 0; i < players.size(); i++) {
            String nick = players.get(i);
            
            if (ep.username.equals(nick)) {
                ep.sendChatToPlayer("[WarpCtrlr] Detached.");
                players.remove(i);
                return;
            }
        }
        
        ep.attackEntityFrom(DamageSource.generic, 1);
        ep.sendChatToPlayer("[WarpCtrlr] Successfully attached.");
        players.add(ep.username);
        updatePlayersString();
    }
    
    public void updatePlayersString() {
        String nick;
        this.playersString = "";
        
        for (int i = 0; i < players.size(); i++) {
            nick = players.get(i);
            
            this.playersString += nick + "|";
        }
    }
    
    public void updatePlayersList() {
        String[] playersArray = playersString.split("\\|");

        for (int i = 0; i < playersArray.length; i++) {
            String nick = playersArray[i];
            
            if (!nick.isEmpty()) {
                players.add(nick);
            }
        }
    }
    
    public String getAttachedPlayersList() {
        String list = "";
        
        for (int i = 0; i < this.players.size(); i++) {
            String nick = this.players.get(i);
            
            list += nick + ((i == this.players.size() - 1)? "" : ", ");
        }
        
        if (players.isEmpty()) {
            list = "<nobody>";
        }
        
        return list;
    }    

    /**
     * @return the jumpFlag
     */
    public boolean isJumpFlag() {
        return jumpFlag;
    }

    /**
     * @param jumpFlag the jumpFlag to set
     */
    public void setJumpFlag(boolean jumpFlag) {
        this.jumpFlag = jumpFlag;
    }

    /**
     * @return the front
     */
    public int getFront() {
        return front;
    }

    /**
     * @param front the front to set
     */
    public void setFront(int front) {
        this.front = front;
    }

    /**
     * @return the right
     */
    public int getRight() {
        return right;
    }

    /**
     * @param right the right to set
     */
    public void setRight(int right) {
        this.right = right;
    }

    /**
     * @return the up
     */
    public int getUp() {
        return up;
    }

    /**
     * @param up the up to set
     */
    public void setUp(int up) {
        this.up = up;
    }

    /**
     * @return the back
     */
    public int getBack() {
        return back;
    }

    /**
     * @param back the back to set
     */
    public void setBack(int back) {
        this.back = back;
    }

    /**
     * @return the left
     */
    public int getLeft() {
        return left;
    }

    /**
     * @param left the left to set
     */
    public void setLeft(int left) {
        this.left = left;
    }

    /**
     * @return the down
     */
    public int getDown() {
        return down;
    }

    /**
     * @param down the down to set
     */
    public void setDown(int down) {
        this.down = down;
    }
    
    public void setDistance(int distance) {
        this.distance = distance;
    }
    
    public int getDistance() {
        return this.distance;
    }

    /**
     * @return the mode
     */
    public int getMode() {
        return mode;
    }

    /**
     * @return the direction
     */
    public int getDirection() {
        return direction;
    }

    /**
     * @return the summonFlag
     */
    public boolean isSummonAllFlag() {
        return summonFlag;
    }

    /**
     * @param summonFlag the summonFlag to set
     */
    public void setSummonAllFlag(boolean summonFlag) {
        this.summonFlag = summonFlag;
    }
}
