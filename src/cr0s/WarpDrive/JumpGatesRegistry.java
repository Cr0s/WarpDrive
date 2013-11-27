package cr0s.WarpDrive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;

public final class JumpGatesRegistry
{
    private File db;
    private ArrayList<JumpGate> gates = new ArrayList<JumpGate>();

    //@SideOnly(Side.CLIENT)
    public JumpGatesRegistry()
    {
        db = MinecraftServer.getServer().getFile("gates.txt");

        try
        {
            loadGates();
        }
        catch (IOException ex)
        {
            Logger.getLogger(JumpGatesRegistry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveGates() throws IOException
    {
        PrintWriter out = new PrintWriter(new FileWriter(db));

        // Write each string in the array on a separate line
        for (JumpGate jg : gates)
        {
            out.println(jg);
        }

        out.close();
    }

    public void loadGates() throws IOException
    {
        System.out.println("[JUMP GATES] Loading jump gates from gates.txt...");
        BufferedReader bufferedreader;
        bufferedreader = new BufferedReader(new FileReader(db));
        String s1;

        while ((s1 = bufferedreader.readLine()) != null)
        {
            gates.add(new JumpGate(s1));
        }

        bufferedreader.close();
        System.out.println("[JUMP GATES] Loaded " + gates.size() + " jump gates.");
    }

    public void addGate(JumpGate jg)
    {
        gates.add(jg);
    }

    public boolean addGate(String name, int x, int y, int z)
    {
        // Gate already exists
        if (findGateByName(name) != null)
        {
            return false;
        }

        addGate(new JumpGate(name, x, y, z));

        try
        {
            saveGates();
        }
        catch (IOException ex)
        {
            Logger.getLogger(JumpGatesRegistry.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    public void removeGate(String name)
    {
        JumpGate jg;

        for (int i = 0; i < gates.size(); i++)
        {
            jg = gates.get(i);

            if (jg.name.equalsIgnoreCase(name))
            {
                gates.remove(i);
                return;
            }
        }

        try
        {
            saveGates();
        }
        catch (IOException ex)
        {
            Logger.getLogger(JumpGatesRegistry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JumpGate findGateByName(String name)
    {
        for (JumpGate jg : gates)
        {
            if (jg.name.equalsIgnoreCase(name))
            {
                return jg;
            }
        }

        return null;
    }

    public String jumpGatesList()
    {
        String result = "";

        for (JumpGate jg : gates)
        {
            result += jg.toNiceString() + "\n";
        }

        return result;
    }

    public JumpGate findNearestGate(int x, int y, int z)
    {
        System.out.println(jumpGatesList());
        double minDistance = -1;
        JumpGate res = null;

        for (JumpGate jg : gates)
        {
            double d3 = jg.xCoord - x;
            double d4 = jg.yCoord - y;
            double d5 = jg.zCoord - z;
            double distance = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
            System.out.println("Checking gate: " + jg.name + ", distance: " + distance);

            if (minDistance == -1 || distance < minDistance)
            {
                System.out.println("Setting " + jg.name + " as nearest");
                minDistance = distance;
                res = jg;
            }
        }

        return res;
    }
}
