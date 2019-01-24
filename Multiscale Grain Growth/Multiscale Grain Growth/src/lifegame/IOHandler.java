/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lifegame;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;

public class IOHandler
{
    public static GrainCell[][] FileToTable(File stateFile) throws IOException
    {
        GrainCell[][] tmpGrid = null;
        
        String extension = "";
        if (stateFile.getName().lastIndexOf('.') > 0)
            extension = stateFile.getName().substring(stateFile.getName().lastIndexOf('.'));
        else
            throw new IOException("Cannot determine extension of the file");
            
        switch(extension)
        {
            case ".txt":
            {
                BufferedReader reader = new BufferedReader(new FileReader(stateFile));
                
                try
                {
                    String line = "";
                    line = reader.readLine();
                    String[] cellData = line.split(" ");

                    tmpGrid = new GrainCell[Integer.parseInt(cellData[0])][Integer.parseInt(cellData[1])];

                    while ((line = reader.readLine()) != null)
                    {                    
                        cellData = line.split(" ");
                        GrainCell tmpCell = new GrainCell();

                        tmpCell.iPos = Integer.parseInt(cellData[0]);
                        tmpCell.jPos = Integer.parseInt(cellData[1]);
                        tmpCell.color = new Color(Double.parseDouble(cellData[2]), Double.parseDouble(cellData[3]), Double.parseDouble(cellData[4]), 1.0);

                        tmpGrid[tmpCell.iPos][tmpCell.jPos] = tmpCell;
                    }
                }
                catch(Exception exc)
                {
                    throw new IOException("Interpretation error - " + exc.getLocalizedMessage());
                }
                finally
                {
                    reader.close();
                }
                
                break;
            }
            case ".csv":
            {
                BufferedReader reader = new BufferedReader(new FileReader(stateFile));
                
                try
                {
                    reader.readLine();
                    String line = "";
                    line = reader.readLine();
                    reader.readLine();
                    String[] cellData = line.split(";");

                    tmpGrid = new GrainCell[Integer.parseInt(cellData[0])][Integer.parseInt(cellData[1])];

                    while ((line = reader.readLine()) != null)
                    {                    
                        cellData = line.split(";");
                        GrainCell tmpCell = new GrainCell();

                        tmpCell.iPos = Integer.parseInt(cellData[0]);
                        tmpCell.jPos = Integer.parseInt(cellData[1]);
                        tmpCell.color = new Color(Double.parseDouble(cellData[2]), Double.parseDouble(cellData[3]), Double.parseDouble(cellData[4]), 1.0);

                        tmpGrid[tmpCell.iPos][tmpCell.jPos] = tmpCell;
                    }
                }
                catch(Exception exc)
                {
                    throw new IOException("Interpretation error - " + exc.getLocalizedMessage());
                }
                finally
                {
                    reader.close();
                }
                break;
            }
            case ".gif":
            case ".png":
            case ".bmp":
            {
                BufferedImage inputImg = ImageIO.read(stateFile);
                int size = inputImg.getHeight() / 3;
                tmpGrid = new GrainCell[size][size];
                
                for (int i=0; i<size; i++)
                    for (int j=0; j<size; j++)
                    {
                        GrainCell tmpCell = null;
                        java.awt.Color cellRgb = new java.awt.Color(inputImg.getRGB(i * 3, j * 3));
                        if (cellRgb.getRed() != 255 && cellRgb.getGreen() != 255 && cellRgb.getBlue() != 255)
                        {
                            Color cellFXRGb = Color.rgb(cellRgb.getRed(), cellRgb.getGreen(), cellRgb.getBlue());
                            tmpCell = new GrainCell();
                            tmpCell.iPos = i;
                            tmpCell.jPos = j;
                            tmpCell.color = cellFXRGb;
                        }
                        
                        tmpGrid[i][j] = tmpCell;
                    }
                
                break;
            }
        }
        
        return tmpGrid;
    }
    
    public static void TableToFile(GrainCell[][] grainState, String extension, File stateFile) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(stateFile));
        int size = grainState.length;
        
        switch(extension)
        {
            case "*.txt":
            {
                writer.write(size + " " + size);
                writer.newLine();
                
                for(int i = 0; i < size; i++)
                    for(int j = 0; j < size; j++)
                    {
                        if (grainState[i][j].state)
                        {
                            
                            writer.write(i + " " + j + " " + (grainState[i][j].color.getRed()) + " " + (grainState[i][j].color.getGreen()) + " " + (grainState[i][j].color.getBlue()));
                            writer.newLine();
                        }
                    }
                
                writer.close();
                
                break;
            }
            case "*.csv":
            {
                writer.write("Size (x);Size (y)");
                writer.newLine();
                writer.write(size + ";" + size);
                writer.newLine();
                writer.write("X-cordinate;Y-cordinate;Color (red);Color (green);Color (blue);;;(Color component values must be given in 0-1 range");
                writer.newLine();
                
                for(int i = 0; i < size; i++)
                    for(int j = 0; j < size; j++)
                    {
                        if (grainState[i][j].state)
                        {
                            
                            writer.write(i + ";" + j + ";" + (grainState[i][j].color.getRed()) + ";" + (grainState[i][j].color.getGreen()) + ";" + (grainState[i][j].color.getBlue()));
                            writer.newLine();
                        }
                    }
                
                writer.close();
                break;
            }
            case "*.gif":
            case "*.png":
            case "*.bmp":
            { 
                Canvas fileCanvas = new Canvas(size * 3, size * 3);
                GraphicsContext context = fileCanvas.getGraphicsContext2D();
                
                for (int i = 0; i < size; i++)
                {
                    for (int j=0; j < size; j++)
                    {
                        Color fillColor = Color.WHITE;
                        if (grainState[i][j].state)
                            fillColor = grainState[i][j].color;
                        
                        context.setFill(fillColor);
                        context.fillRect(i * 3, j * 3, 3, 3);
                    }
                }
                
                BufferedImage buffer = SwingFXUtils.fromFXImage(fileCanvas.snapshot(null, null), null);
                if (extension.equals("*.png") || extension.equals("*.bmp"))
                    ImageIO.write(buffer, "png", stateFile);
                else
                    ImageIO.write(buffer, "gif", stateFile);
                break;
            }
        }
    }
}
