/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lifegame;

import javafx.scene.paint.Color;


public class GrainCell
{
    
    public GrainCell()
    {
        state = false;
        grainID = "NO GRAIN";
        color = null;
        dislocationVal = 0.00;
        type = Type.grain;
    }
    
    public void CreateCopy(GrainCell original)
    {
        this.state = original.state;
        this.grainID = original.grainID;
        this.color = original.color;
        this.dislocationVal = original.dislocationVal;
        this.type = original.type;
    }    
    
    public boolean state;
    public String grainID;
    public Color color;
    public double dislocationVal;
    public Type type;
    
    public int iPos;
    public int jPos;
    
    public enum Type {grain, inclusion}
}
