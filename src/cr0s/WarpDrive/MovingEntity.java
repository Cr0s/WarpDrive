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
public class MovingEntity
{
    public double oldX;
    public double oldY;
    public double oldZ;

    public Entity entity;

    public MovingEntity(Entity e)
    {
        entity = e;
        oldX = e.posX;
        oldY = e.posY;
        oldZ = e.posZ;
    }
}
