/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cr0s.WarpDrive;

import net.minecraft.entity.Entity;

/**
 * Класс объекта движущегося в корабле энтити
 * @author user
 */
public class MovingEntity {
    public int oldX;
    public int oldY;
    public int oldZ;
    
    public Entity entity;
    
    public MovingEntity(Entity e, int x, int y, int z) {
        this.entity = e;
        
        this.oldX = x;
        this.oldY = y;
        this.oldZ = z;
    }
}
