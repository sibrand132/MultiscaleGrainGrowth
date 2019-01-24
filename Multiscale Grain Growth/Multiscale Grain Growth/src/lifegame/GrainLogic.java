/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lifegame;

import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import javafx.scene.paint.Color;
import javafx.util.Pair;


public class GrainLogic
{
    /**
     * NEIGHBOUR TYPES : "moore","advanced"
     * RANDOM TYPES : "random","even","radius","manual"
     */
    
    public ArrayList<GrainCell> avaiableGrains;
    public ArrayList<Point> radiusPoints;
    public ArrayList<GrainCell> selectedGrains;
    
    public GrainCell[][] gameTable;
    public GrainCell[][] previousStepTable;
    public Integer size;
    public int periodicMode;
    public String neighType;
    public String randomType;
    public int radiusVal;
    public int randomChanceVal;
    
    public double iterationsElapsed;
    
    
    public enum InclusionType {circular, square}

    private final String aliveSign = "[ * ]";
    private final String deadSign = "[ X ]";        
    
    public GrainLogic(int size)
    {
        this.size = size;
        periodicMode = 0;
        
        generateTable();
    }

    private void generateTable()
    {   
        
        iterationsElapsed = 0.000;        
        
        gameTable = new GrainCell[size][size];
        avaiableGrains = new ArrayList<>();
        selectedGrains = new ArrayList<>();
        radiusPoints = new ArrayList<>();
        
        for (int i=0; i<size; i++)
            for (int j=0; j<size; j++)
            {
                gameTable[i][j] = new GrainCell();
            }
    }

    public void iterate()
    {                                
        GrainCell[][] nextStepArray = new GrainCell[size][size];
        previousStepTable = gameTable;
        
        iterationsElapsed += 0.001;         // Increasing time step value
        
        double dislocationSum = 0.0;
        
        for(int i=0; i<size; i++)
        {
            for (int j=0; j<size; j++)
            {
                if (!gameTable[i][j].state)
                {
                    nextStepArray[i][j] = makeSentence(constructLocalAreaArray(i,j));
                }
                else
                {
                    nextStepArray[i][j] = gameTable[i][j];                                        
                    dislocationSum = dislocationSum + nextStepArray[i][j].dislocationVal;
                }                    
            }
        }        
        
        gameTable = nextStepArray;
    }
    
//mooore
    private GrainCell[][] constructLocalAreaArray(int i, int j)
    {
        GrainCell[][] localArea = new GrainCell[3][3];
        
        for (int k=-1; k<2; k++)
        {
            for (int l=-1; l<2; l++)
            {
                localArea[1+k][1+l] = getCell(i+k, j+l);
            }
        }

        localArea[1][1] = getCell(i, j);       
        
        return localArea;
    }
    
    private GrainCell makeSentence(GrainCell[][] localArea)
    {
        Map<String,Integer> neighbourMap = getLocalCount(localArea);
             
        /**
         * STANDARD CONDITIONS CHECK
         */
        
        
        if(neighbourMap.size() > 0)
        {            
            ArrayList<String> topGrainsArray = new ArrayList<>();
            
            int maxCountValue = Collections.max(neighbourMap.values());
            int maxCountTmp = maxCountValue;
            
            boolean drawChange = false;
            
            if (neighType.equals("advanced"))
            {
                if (maxCountTmp < 5)
                {
                    GrainCell[][] closestArray = cloneLocalArray(localArea);
                    
                    closestArray[0][0] = new GrainCell();
                    closestArray[0][2] = new GrainCell();
                    closestArray[2][0] = new GrainCell();
                    closestArray[2][2] = new GrainCell();
                    
                    Map<String,Integer> closestNeighbourMap = getLocalCount(closestArray);
                    maxCountTmp = closestNeighbourMap.isEmpty() ? 0 : Collections.max(closestNeighbourMap.values());
                    
                    if (maxCountTmp < 3)
                    {
                        closestArray = cloneLocalArray(localArea);
                        closestArray[0][1] = new GrainCell();
                        closestArray[1][0] = new GrainCell();
                        closestArray[1][2] = new GrainCell();
                        closestArray[2][1] = new GrainCell();
                        
                        closestNeighbourMap = getLocalCount(closestArray);
                        maxCountTmp = closestNeighbourMap.isEmpty() ? 0 : Collections.max(closestNeighbourMap.values());
                        
                        if (maxCountTmp < 3)
                            drawChange = true;                            
                        else
                        {
                            localArea = closestArray;
                            neighbourMap = closestNeighbourMap;
                            maxCountValue = maxCountTmp;
                        }
                    }
                    else
                    {
                        localArea = closestArray;
                        neighbourMap = closestNeighbourMap;
                        maxCountValue = maxCountTmp;
                    }
                    
                }
            }
            
            for (Map.Entry<String,Integer> entry : neighbourMap.entrySet())
            {
                if (entry.getValue() == maxCountValue)
                {
                    topGrainsArray.add(entry.getKey());
                }
            }
            
            String winnerID = "";
            
            if (topGrainsArray.size() == 1)
                winnerID = topGrainsArray.get(0);
            else if (topGrainsArray.isEmpty())
                return localArea[1][1];
            else
            {
                Random rnd = new Random();
                winnerID = topGrainsArray.get(rnd.nextInt(topGrainsArray.size() - 1));
            }
            
            for (int h=0; h<3; h++)
                for (int k=0; k<3; k++)
                {
                    if (winnerID.equals(localArea[h][k].grainID))
                    {
                            Random rndGen = new Random();
                            if (drawChange && rndGen.nextInt(100) > randomChanceVal){                                
                                return localArea[1][1];
                            }
                            
                            
                            return localArea[h][k];                        
                    }                        
                }    
        }
        else
        {
            return localArea[1][1];
        }
        
        return null;
    }
    
    
    /**
     * MISCELLANEOUS FUNCTIONS
     */
    
    public boolean randomizeCells(int randomCount)
    {
        
        Random randDim = new Random();
          
            while(randomCount > 0)
            {
                createRandomGrain(randDim.nextInt(size), randDim.nextInt(size));
                randomCount--;
            }
        
        
        return true;
    }
    
    public void randomizeInclusions(int randomCount)
    {
        Random randGen = new Random();
        InclusionType type = InclusionType.circular;

        int drawsRemains = randomCount / 4;
        
        if (iterationsElapsed == 0)
        {
            while (drawsRemains > 0)
            {
                if (randGen.nextBoolean())
                    type = InclusionType.square;
                else
                    type = InclusionType.circular;
                
                createInclusionStructure(randGen.nextInt(size), randGen.nextInt(size), randGen.nextInt(5), type);
                drawsRemains--;
            }            
        }
        else
        {
            LinkedList<Integer[]> borderGrains = getBorderGrains();
            if (borderGrains.size() == 0)
                return;
            
            while (drawsRemains > 0)
            {
                Integer[] drawedGrain = borderGrains.get(randGen.nextInt(borderGrains.size()));
                if (drawedGrain == null)
                    break;
                
                if (randGen.nextBoolean())
                    type = InclusionType.square;
                else
                    type = InclusionType.circular;
                            
                createInclusionStructure(drawedGrain[0], drawedGrain[1], randGen.nextInt(5), type);
                drawsRemains--;
            } 
        }
    }
    
    public void loadCells(GrainCell[][] rawData)
    {
        size = rawData.length;
        clearTable();
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
            {
                if (rawData[i][j] != null)
                {
                    GrainCell tmpCell = rawData[i][j];
                    
                    if (tmpCell.color != Color.BLACK && !tmpCell.color.toString().equals("0x000000ff"))
                    {
                        tmpCell.grainID = "LOADED(" + tmpCell.color.toString() + ")";
                        tmpCell.state = true;
                    }
                    else
                    {
                        tmpCell.grainID = "INCLUSION(" + tmpCell.color.toString() + ")";
                        tmpCell.state = true;
                        tmpCell.type = GrainCell.Type.inclusion;
                    }
                                        
                    gameTable[i][j] = tmpCell;
                    
                    boolean newGrain = true;
                    for (GrainCell rootCell : avaiableGrains)
                    {
                        if (rootCell.color == tmpCell.color)
                        {
                            newGrain = false;
                            break;
                        }
                    }
                    if (newGrain)
                        avaiableGrains.add(tmpCell);
                }
                else
                    gameTable[i][j] = new GrainCell();
            }
    }
    
    
    public GrainCell createRandomGrain(int i, int j)
    {
        if (gameTable[i][j] != null && gameTable[i][j].state)
            return gameTable[i][j];
        
        GrainCell randomCell = new GrainCell();
        randomCell.color = getRandomColor();
        randomCell.grainID = "RANDOMIZED (" + randomCell.color.toString() + ")";
        randomCell.state = true;

        gameTable[i][j] = randomCell;
        avaiableGrains.add(randomCell);
        
        return randomCell;
    }
    
    public void createInclusionStructure(int i, int j, int radius, InclusionType type)
    {
        if (type == InclusionType.circular)
        {          
            GrainCell inclusionCell = new GrainCell();
            inclusionCell.type = GrainCell.Type.inclusion;
            inclusionCell.state = true;
            inclusionCell.color = Color.BLACK;
            inclusionCell.grainID = "INCLUSION";
            
            for (int y = -radius; y <= radius; y++)
                for (int x = -radius; x <= radius; x++)
                    if ((x * x) + (y * y) <= (radius * radius) && x+i >= 0 && y+j >= 0 && x+i<size && y+j < size)
                        gameTable[x+i][y+j] = inclusionCell;
            return;
            
        }
        else if (type == InclusionType.square)
        {            
            GrainCell inclusionCell = new GrainCell();
            inclusionCell.type = GrainCell.Type.inclusion;
            inclusionCell.state = true;
            inclusionCell.color = Color.BLACK;
            inclusionCell.grainID = "INCLUSION";
            
            for (int a=-radius; a < radius; a++)
                for (int b=-radius; b < radius; b++)
                {
                    int x = i + a;
                    int y = j + b;
                    
                    if (x >= size || x < 0 || y >= size || y < 0)
                        continue;
                    
                    gameTable[x][y] = inclusionCell;
                }
        }
    }
    
    public boolean checkPointsDistance(int radius, Point newPoint)
    {        
        for (int i=newPoint.y-radius; i<newPoint.y+radius; i++)
        {
            for (int j=newPoint.x-radius; j<newPoint.x+radius; j++)
            {
                if (getCell(i, j).state)
                    return false;
            }
        }
        
        return true;
    }
    
    public Color getRandomColor()
    {
        Random r = new Random();
        Color color = new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), 1.0);
        
        boolean colorUnique = true;
        while (!colorUnique)
        {
            colorUnique = true;
            for(GrainCell entry : avaiableGrains)
            {
                if (entry.color == color)
                {
                    colorUnique = false;
                    color = new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), 1.0);
                }                    
            }
        }
        
        return color;
    }
    
    private LinkedList<Integer[]> getBorderGrains()
    {
        LinkedList<Integer[]> borderGrains = new LinkedList<>();
        
        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++)
            {
                if (gameTable[i][j] == null || gameTable[i][j].state == false || gameTable[i][j].type == GrainCell.Type.inclusion)
                    continue;
                
                if (checkIfBordered(i, j))
                {
                    Integer[] position = new Integer[2];
                    position[0] = i;
                    position[1] = j;
                    borderGrains.add(position);
                }
            }
        
        return borderGrains;
    }
    
    public void clearAndLeaveBorders(Color color)
    {
        GrainCell[][] tmpTable = new GrainCell[size][size];
        avaiableGrains.clear();
        
        for (int i=0; i<size; i++)
            for (int j=0; j<size; j++)
            {                    
                if (gameTable[i][j] != null && checkIfBordered(i, j))
                {
                   GrainCell borderGrain = new GrainCell();
                   borderGrain.color =  gameTable[i][j].state ? (gameTable[i][j].type == GrainCell.Type.inclusion ? gameTable[i][j].color : color) : color;
                   borderGrain.state = true;
                   borderGrain.type = GrainCell.Type.inclusion;
                   borderGrain.grainID = "BORDER INCLUSION";
                   
                   avaiableGrains.add(borderGrain);
                   tmpTable[i][j] = borderGrain;
                }
                else
                    tmpTable[i][j] = new GrainCell();
            }
        
        gameTable = tmpTable;
    }
    
    public void clearAndLeaveSelectedBorders(Color color,  ArrayList<GrainCell> tmpSelectedList){
        GrainCell[][] tmpTable = new GrainCell[size][size];
        avaiableGrains.clear();
        
        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++)
                tmpTable[i][j] = new GrainCell();
        
        for (int i=0; i<size; i++)
            for (int j=0; j<size; j++)
            { 
                for(int k=0; k<tmpSelectedList.size(); k++)                    
                if(gameTable[i][j] != null && checkIfGrainBorder(i, j, tmpSelectedList.get(k))){                    
                   GrainCell borderGrain = new GrainCell();
                   borderGrain.color =  gameTable[i][j].state ? (gameTable[i][j].type == GrainCell.Type.inclusion ? gameTable[i][j].color : color) : color;
                   borderGrain.state = true;
                   borderGrain.type = GrainCell.Type.inclusion;
                   borderGrain.grainID = "BORDER INCLUSION";
                   
                   avaiableGrains.add(borderGrain);
                   tmpTable[i][j] = borderGrain;
                }               
            }
        gameTable = tmpTable;
    }
    
    
    private Map<String, Integer> getLocalCount(GrainCell[][] localArea)
    {
        Map<String,Integer> neighbourMap = new HashMap<>();
        
        /**
         * STANDARD CONDITIONS CHECK
         */
        
        for (int i=0; i<3; i++)
        {
            for (int j=0; j<3; j++)
            {
                if (localArea[i][j].state && localArea[i][j].type == GrainCell.Type.grain)
                {
                    if (neighbourMap.containsKey(localArea[i][j].grainID))
                    {
                        Integer tmpVal = neighbourMap.get(localArea[i][j].grainID);
                        tmpVal++;
                        neighbourMap.replace(localArea[i][j].grainID, tmpVal);
                    }
                    else
                    {
                        neighbourMap.put(localArea[i][j].grainID, 1);
                    }                        
                }
            }
        }
        
        return neighbourMap;
    }
    
    private LinkedList<GrainCell> getSelectedGrain(GrainCell grain)
    {
        LinkedList<GrainCell> grains = new LinkedList<>();
        
        for (int i=0; i<size; i++)
            for (int j=0; j<size; j++)
            {
                if (gameTable[i][j].grainID.equals(grain.grainID))
                    grains.add(gameTable[i][j]);
            }
        
        return grains;
    }
    
    public void lockSelectedGrain(GrainCell grain)
    {
        LinkedList<GrainCell> grains = getSelectedGrain(grain);
        for (int i=0; i < grains.size(); i++)
            grains.get(i).type = GrainCell.Type.inclusion;
    }
    
    public void recolorSelectedGrain(GrainCell grain, Color newColor)
    {
        LinkedList<GrainCell> grains = getSelectedGrain(grain);
        for (int i=0; i < grains.size(); i++)
            grains.get(i).color = newColor;
    }
    
    public void selectGrain(int x, int y)
    {   
        if (gameTable[x][y] == null || !gameTable[x][y].state)
            return;
        
        LinkedList<GrainCell> sameGrains = getSelectedGrain(gameTable[x][y]);
        
        GrainCell originalGrain = new GrainCell();
        originalGrain.CreateCopy(gameTable[x][y]);
        selectedGrains.add(originalGrain);
        
        for (int i=0; i < sameGrains.size(); i++)
            sameGrains.get(i).color = sameGrains.get(i).color.brighter();
    }
    
    public String getGrainInfo(int x, int y)
    {
        String infoText = "";
        if (gameTable[x][y] == null || !gameTable[x][y].state)
        {
            infoText += "No grain at position (" + x + "," + y + ")";
            return infoText;
        }
        
        infoText += "Grain at position (" + x + "," + y + ")\n";
        infoText += gameTable[x][y].type == GrainCell.Type.grain ? "Type : Grain\n" : "Type : Inclusion or locked grain\n";
        infoText += "Color RGB : " + gameTable[x][y].color.getRed() + " | " + gameTable[x][y].color.getGreen() + " | " + gameTable[x][y].color.getBlue() + "\n";
        infoText += "ID : " + gameTable[x][y].grainID;
        
        return infoText;
    }
    
    public void deselectAllGrains()
    {
        for (int i=0; i < selectedGrains.size(); i++)
        {
            LinkedList<GrainCell> sameGrains = getSelectedGrain(selectedGrains.get(i));
            for (int j=0; j < sameGrains.size(); j++)
                sameGrains.get(j).color = selectedGrains.get(i).color; 
        }
        
        selectedGrains.clear();
    }
    
    public void clearTable()
    {
        GrainCell emptyCell = new GrainCell();
        gameTable = new GrainCell[size][size];
        
        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++)
                gameTable[i][j] = emptyCell;
        
        avaiableGrains = new ArrayList<>();
        iterationsElapsed = 0.0;
        
    }
    
    public void clearTable(GrainCell[] cellsToPreserve)
    {
        GrainCell emptyCell = new GrainCell();
        GrainCell[][] newGameTable = new GrainCell[size][size];
        avaiableGrains = new ArrayList<>();
        
        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++)
            {
                newGameTable[i][j] = emptyCell;
                
                for (GrainCell singleCell : cellsToPreserve)
                    if (gameTable[i][j].grainID.equals(singleCell.grainID))
                    {
                        newGameTable[i][j] = gameTable[i][j];
                        avaiableGrains.add(gameTable[i][j]);
                        break;
                    }
            }
             
        gameTable = newGameTable;
        iterationsElapsed = 0.0;
        
    }
    

    private int boolToInt(boolean value)
    {
        return value ? 1 : 0;
    }
    
    
    /**
     * BC-SAFE GET AND SET FOR THE BOARD
     */
    
    public void setCell(int i, int j)
    {
        if (i < 0)
            i = size + i;
        if (i >= size)
            i = 0 + (i - size);
        if (j < 0)
            j = size + j;
        if (j >= size)
            j = 0 + (j - size);
        
        gameTable[i][j].state = true;
    }
    
    public GrainCell getCell(int i, int j)
    {
        if (periodicMode == 2)
        {
            if (i < 0)
                i = size + i;
            if (i >= size)
                i = 0 + (i - size);
            if (j < 0)
                j = size + j;
            if (j >= size)
                j = 0 + (j - size);
            
            return gameTable[i][j];
        }
        else if (periodicMode == 0)
        {
            if (i < 0)
                i = 0;
            if (i >= size)
                i = size - 1;
            if (j < 0)
                j = 0;
            if (j >= size)
                j = size - 1;
            
            return gameTable[i][j];
        }
       
        return null;
    }
    
    private boolean checkIfBordered(int x, int y)
    {
        for(int i=-1; i<1; i++)
            for(int j=-1; j<1; j++)
            {
                if (i == 0 && j == 0)
                    continue;
                if (x+i < 0 || x+i >= size || y + j < 0 || y + j >=size)
                    continue;
                if (gameTable[x+i][y+j] != null && gameTable[x][y].grainID.equals(gameTable[x+i][y+j].grainID))
                    continue;
                else
                    return true;
            }
        
        if (gameTable[x][y].state && gameTable[x][y].type == GrainCell.Type.inclusion)
            return true;
       
        return false;
    }
    
    private boolean checkIfGrainBorder(int x, int y, GrainCell cell){
        for(int i=-1; i<1; i++)
            for(int j=-1; j<1; j++)
            {
                if (i == 0 && j == 0)
                    continue;
                if (x+i < 0 || x+i >= size || y + j < 0 || y + j >=size)
                    continue;
              if (gameTable[x+i][y+j].grainID.equals(cell.grainID) && !gameTable[x][y].grainID.equals(cell.grainID)  )
                  return true;                  
              else if (gameTable[x][y].grainID.equals(cell.grainID) && !gameTable[x+i][y+j].grainID.equals(cell.grainID)  )
                  return true;
              else
                  continue;
              
            }
        return false;
    }
    
    
    private GrainCell[][] cloneLocalArray(GrainCell[][] original)
    {
        GrainCell[][] cloned = new GrainCell[3][3];
        for (int i=0; i<3; i++)
            for (int j=0; j<3; j++)
            {
                cloned[i][j] = new GrainCell();
                if (original[i][j].state)
                    cloned[i][j].CreateCopy(original[i][j]);
            }
                
        
        return cloned;        
    }
    
}
