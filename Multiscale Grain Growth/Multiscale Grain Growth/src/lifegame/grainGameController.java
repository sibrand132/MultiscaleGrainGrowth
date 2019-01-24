/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lifegame;

//import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.Pair;
import javax.swing.JFileChooser;
import jdk.nashorn.internal.runtime.regexp.joni.ast.ConsAltNode;

public class grainGameController extends AnchorPane
{
    @FXML AnchorPane rootPanel;
    @FXML SplitPane phantomPanel;
    @FXML Canvas drawField;
    
    @FXML ChoiceBox noiCombo;
    @FXML Button startBtn;
    @FXML Button randomizeBtn;
    @FXML Button clearBtn;
    @FXML Button importBtn;
    @FXML Button exportBtn;
    @FXML Button blockSubBtn;
    @FXML Button blockDpBtn;
    @FXML CheckBox inclusionsCheck;
    
    @FXML Label textAlertLabel;
    @FXML Pane alertPanel;
    
    protected final Object alertPanelMutex;
    
    protected GrainLogic gameLogic;
    protected GraphicsContext graphicsContext;
    
    protected Point contextClick;
    protected ContextMenu contextMenu;
    protected boolean simInProgress;
    
    /**
     * CELL-DRAWING SPECYFIC
     */
    
    protected int cellSize;
    protected int cellMargin;
    protected int leftMargin;
    protected int topMargin;
    
    /**
     * FINAL RECIVED VARIABLES
     */
    final protected int randomRatio;
    
    public grainGameController(String neightMode, String randomMode, int boardSize, int BC, int randomRatio, int radiusVal, int advancedConditionChance)
    {
        FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource("grainGame.fxml"));
        
        fXMLLoader.setRoot(this);
        fXMLLoader.setController(this);
        
        try
        {
            fXMLLoader.load();
        }
        catch(IOException exc)
        {
            throw new RuntimeException(exc);
        }
        
        gameLogic = new GrainLogic(boardSize);
        gameLogic.neighType = neightMode;
        gameLogic.randomType = randomMode;
        gameLogic.periodicMode = BC;
        gameLogic.radiusVal = radiusVal;
        gameLogic.randomChanceVal = advancedConditionChance >= 0 && advancedConditionChance <= 100 ? advancedConditionChance : 60;
        
        this.randomRatio = randomRatio;
        
        simInProgress = false;
        alertPanelMutex = new Object();
        
        initUI();
        drawCurrentState();
    }
    
    @FXML
    public void onStartBtnClicked(MouseEvent evt)
    {
        
        if (simInProgress)
        {
            startBtn.setText("Start");
            simInProgress = false;
            return;
        }
        else
        {
            startBtn.setText("Stop");
            simInProgress = true;
        }
        
        final GrainLogic finalLogic = gameLogic;
        int timeInterval = 1000 / 20;
        final int finIntervalRef = timeInterval;
        
        if (!gameLogic.selectedGrains.isEmpty())
            gameLogic.deselectAllGrains();
        
        redrawState();
        
        Thread simThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                int iterationsNumber = 0;
                if (noiCombo.getValue().equals("Continous"))
                    iterationsNumber = Integer.MAX_VALUE;
                else
                    iterationsNumber = Integer.valueOf((String)noiCombo.getValue());
                
                for (int i=0; i<iterationsNumber; i++)
                {
                    if(!simInProgress)
                        return;
                    
                    Platform.runLater(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            finalLogic.iterate();
                            drawCurrentState();
                        }
                    });
                    try
                    {
                        Thread.sleep(finIntervalRef);
                    }
                    catch(InterruptedException exc)
                    {
                        System.err.println(exc);
                    }
                }
                
                Platform.runLater(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        simInProgress = false;
                        startBtn.setText("Start");
//                        System.out.println("Elapsed : " + ((System.currentTimeMillis() - currentTime)/1000));
                    }
                });
            }
        });
        
        simThread.start();
        redrawState();
    }
    
    @FXML
    public void onRandomizeBtnClicked(MouseEvent evt)
    {
        boolean randomizeState = false;
        
        if (inclusionsCheck.isSelected())
            gameLogic.randomizeInclusions(randomRatio);
        
        if (!(inclusionsCheck.isSelected() && gameLogic.iterationsElapsed > 0))
            randomizeState = gameLogic.randomizeCells(randomRatio);
        
        if (!randomizeState)
        {
            textAlertLabel.setText("UNABLE TO RANDOMIZE MORE CELLS!");
            
            alertPanel.setOpacity(0.0);
            alertPanel.setVisible(true);
            
            FadeTransition fade = new FadeTransition(Duration.millis(2000), alertPanel);
            fade.setFromValue(0.0);
            fade.setToValue(0.6);

            fade.play();
            
            Timer alertWindowTimer = new Timer(true);
            alertWindowTimer.schedule(new TimerTask()
            {

                @Override
                public void run()
                {
                    Platform.runLater(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            FadeTransition fade = new FadeTransition(Duration.millis(200), alertPanel);
                            fade.setFromValue(0.6);
                            fade.setToValue(0.0);
                            fade.setOnFinished(new EventHandler<ActionEvent>()
                            {

                                @Override
                                public void handle(ActionEvent event)
                                {
                                    synchronized (alertPanelMutex)
                                    {
                                        alertPanel.setVisible(false);
                                    }
                                }
                            });
                            
                            synchronized (alertPanelMutex)
                            {
                                fade.play();
                            }
                        }
                    });
                }
            }, 5000);
        }
        drawCurrentState();
    }
    
    @FXML
    public void onClearBtnClicked(MouseEvent evt)
    {
        gameLogic.clearTable();
        drawCurrentState();
    }
    
    @FXML
    public void onImportBtnClicked(MouseEvent evt)
    {   
        FileChooser importFileDialog = new FileChooser();
        importFileDialog.setTitle("Choose file");
        importFileDialog.getExtensionFilters().add((new FileChooser.ExtensionFilter("Text or image formats", Arrays.asList("*.txt","*.bmp","*.gif","*.png","*.csv"))));
        
        loadState(importFileDialog.showOpenDialog(getScene().getWindow()));
    }
    
    @FXML
    public void onExportBtnClicked(MouseEvent evt)
    {
        Dialog alertWindow = new Alert(Alert.AlertType.ERROR);
        alertWindow.setTitle("Error in file saving");
        
        FileChooser exportFileDialog = new FileChooser();
        exportFileDialog.setTitle("Choose name and location1");
        exportFileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text document","*.txt"));
        exportFileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("Bitmap","*.bmp"));
        exportFileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("GIF image","*.gif"));
        exportFileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG image","*.png"));
        exportFileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("Stylesheet","*.csv"));
        
        File selectedFile = exportFileDialog.showSaveDialog(getScene().getWindow());
        try
        {
            IOHandler.TableToFile(gameLogic.gameTable, exportFileDialog.getSelectedExtensionFilter().getExtensions().get(0), selectedFile);
        }
        catch(IOException exc)
        {
            alertWindow.setContentText("Cannot save file content. Details : " + exc.getLocalizedMessage());
            alertWindow.show();
        }
    }
    
    //substructure
    @FXML
    public void onBlockSubBtnClicked(MouseEvent evt)
    {
        Dialog alertWindow = new Alert(Alert.AlertType.WARNING);
        alertWindow.setTitle("Cannot block grains");
        alertWindow.setContentText("No valid grains has been selected. Try to select some of them by clicking on them.");
        
        if (gameLogic.selectedGrains.isEmpty())
            alertWindow.show();
        
        for (int i=0; i<gameLogic.selectedGrains.size(); i++)
            gameLogic.lockSelectedGrain(gameLogic.selectedGrains.get(i));
        
        gameLogic.clearTable(gameLogic.selectedGrains.toArray(new GrainCell[gameLogic.selectedGrains.size()]));
        gameLogic.deselectAllGrains();
        gameLogic.randomizeCells(randomRatio * 2);        
    }
    
    //dualphase 
    @FXML
    public void onBlockDpBtnClicked(MouseEvent evt)
    {
        Dialog alertWindow = new Alert(Alert.AlertType.WARNING);
        alertWindow.setTitle("Cannot block grains");
        alertWindow.setContentText("No valid grains has been selected. Try to select some of them by clicking on them.");
        
        if (gameLogic.selectedGrains.isEmpty())
            alertWindow.show();
        
        ArrayList<GrainCell> tmpSelectedList = new ArrayList<>(gameLogic.selectedGrains);
        
        for (int i=0; i<gameLogic.selectedGrains.size(); i++)
            gameLogic.lockSelectedGrain(gameLogic.selectedGrains.get(i));
            
        gameLogic.clearTable(gameLogic.selectedGrains.toArray(new GrainCell[gameLogic.selectedGrains.size()]));
        gameLogic.deselectAllGrains();
        
        Color commonColor = gameLogic.getRandomColor();
        for (int i=0; i<tmpSelectedList.size(); i++)
            gameLogic.recolorSelectedGrain(tmpSelectedList.get(i), commonColor);
        
        gameLogic.randomizeCells(randomRatio * 3);
    }
    
    @FXML
    public void onBoundariesColorClicked(MouseEvent evt)
    {
        TextInputDialog dialog = new TextInputDialog("Width");
        dialog.setTitle("Border coloring");
        dialog.setHeaderText("Select width of borders");
        dialog.setContentText("(1-5 values prefered) :");
        
        int width = 0;
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
            width = Integer.parseInt(result.get());
        
        ArrayList<GrainCell> tmpSelectedList = new ArrayList<>(gameLogic.selectedGrains);        
        
        Color color = Color.BLACK;
        for (int i=0; i < width; i++)
        {
            if(!tmpSelectedList.isEmpty()){
                gameLogic.clearAndLeaveSelectedBorders(color, tmpSelectedList);
            }
            else{
                gameLogic.clearAndLeaveBorders(color);
            }
            
            color = gameLogic.getRandomColor();
        }
            
        redrawState();
    }
    
    @FXML
    public void onContextMenuShowup(MouseEvent evt)
    {
        if (evt.getButton() == MouseButton.SECONDARY)
            contextMenu.show(this, evt.getSceneX(), evt.getSceneY());
    }
    
    protected void initUI()
    {
        graphicsContext = drawField.getGraphicsContext2D();
        calculateCellSize();
        
        drawField.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if (event.getButton() == MouseButton.PRIMARY && event.isControlDown())
                {
                    int[] valTable = getScreenCords((int)event.getX(), (int)event.getY());
                    if (valTable[0] >= gameLogic.size || valTable[1] >= gameLogic.size || valTable[0] < 0 || valTable[1] < 0)
                        return;
                    
                    Alert infoWindow = new Alert(Alert.AlertType.INFORMATION);
                    infoWindow.setTitle("Grain details");
                    infoWindow.setContentText(gameLogic.getGrainInfo(valTable[0], valTable[1]));
                    
                    infoWindow.show();
                    return;
                }
                
                if (gameLogic.randomType.equals("manual"))
                {
                    int[] valTable = getScreenCords((int)event.getX(), (int)event.getY());
                    if (event.getButton() == MouseButton.PRIMARY)
                    {
                        if (valTable[0] >= 0 && valTable[0] < gameLogic.size && valTable[1] >= 0 
                            && valTable[1] < gameLogic.size)
                        {
                            gameLogic.createRandomGrain(valTable[0], valTable[1]);
                            drawCurrentState();
                        }
                    }
                    else if (event.getButton() == MouseButton.SECONDARY)
                    {
                        contextClick = new Point();
                        contextClick.x = valTable[0];
                        contextClick.y = valTable[1];
                        contextMenu.show(drawField, (int)event.getX(), (int)event.getY());
                    }
                }
                else
                {
                    if (event.getButton() == MouseButton.PRIMARY)
                    {
                        int[] valTable = getScreenCords((int)event.getX(), (int)event.getY());
                        if (valTable[0] >= gameLogic.size || valTable[1] >= gameLogic.size || valTable[0] < 0 || valTable[1] < 0)
                            return;
                        
                        gameLogic.selectGrain(valTable[0], valTable[1]);
                        redrawState();
                    }
                }
            }
        });
        
        startBtn.setOnMouseEntered(new EventHandler<MouseEvent>()
        {

            @Override
            public void handle(MouseEvent event)
            {
                startBtn.setStyle("-fx-background-color: linear-gradient(#8e8e8e 0%,  #8e8e8e 100%);");
            }
        });
        
        startBtn.setOnMouseExited(new EventHandler<MouseEvent>()
        {

            @Override
            public void handle(MouseEvent event)
            {
                 startBtn.setStyle("-fx-background-color: linear-gradient(#686868 0%, #232723 25%, #373837 75%, #757575 100%);");
            }
        });
        
        final ArrayList<String> noiChoiceList = new ArrayList<>();
        noiChoiceList.add("1");
        noiChoiceList.add("10");
        noiChoiceList.add("50");
        noiChoiceList.add("100");
        noiChoiceList.add("500");
        noiChoiceList.add("2000");
        noiChoiceList.add("Continous");
        
        noiCombo.setItems(new ObservableListBase()
        {

            @Override
            public Object get(int index)
            {
                return noiChoiceList.get(index);
            }

            @Override
            public int size()
            {
                return noiChoiceList.size();
            }
        });
        
        noiCombo.setValue(noiChoiceList.get(0));
        
        setContextMenuItems();
    }
    
    protected void drawCurrentState()
    {       
        for (int i=0; i<gameLogic.size; i++)
        {
            for (int j=0; j<gameLogic.size; j++)
            {
                if (gameLogic.previousStepTable == null || gameLogic.previousStepTable[i][j] != gameLogic.gameTable[i][j])
                {
                    if (gameLogic.gameTable[i][j].state)
                        graphicsContext.setFill(gameLogic.gameTable[i][j].color); 
                    else
                        graphicsContext.setFill(Color.WHITE); 

                    graphicsContext.fillRect(leftMargin + (j * (cellSize + cellMargin)),
                        topMargin + (i * (cellSize + cellMargin)), cellSize, cellSize);
                }

            }
        }
    }
    
    protected void redrawState()
    {
        for (int i=0; i<gameLogic.size; i++)
        {
            for (int j=0; j<gameLogic.size; j++)
            {
                if (gameLogic.gameTable[i][j].state)
                    graphicsContext.setFill(gameLogic.gameTable[i][j].color); 
                else
                    graphicsContext.setFill(Color.WHITE); 

                graphicsContext.fillRect(leftMargin + (j * (cellSize + cellMargin)),
                    topMargin + (i * (cellSize + cellMargin)), cellSize, cellSize);
            }
        }
    }
    
    protected void loadState(File stateFile)
    {
        Dialog alertWindow = new Alert(Alert.AlertType.ERROR);
        alertWindow.setTitle("Error in file reading");
        alertWindow.setContentText("Cannot interpret file content. Check if file is compatible with extension or if it still exists.");
        
        if (stateFile == null || !stateFile.exists())
        {
            alertWindow.show();
            return;
        }
        
        try
        {
            gameLogic.loadCells(IOHandler.FileToTable(stateFile));
            
            calculateCellSize();
            drawCurrentState();
        }
        catch (IOException exc)
        {
            alertWindow.setContentText("Cannot interpret file content. Details : " + exc.getLocalizedMessage());
            alertWindow.show();
            return;
        }
    }
    
    protected void calculateCellSize()
    {
        int screenSpace = 1080 - (50 + 75);      // Stands for : Screen max height - 2 times margin size
        cellMargin = 5;
        cellSize = 50;
        
        if (getScreenCover() > screenSpace)
        {
            if (gameLogic.size > 100)
                cellMargin = 0;
            else
                cellMargin = 2;
            
            if (getScreenCover() > screenSpace)
            {
                cellSize = (int) ((screenSpace - (cellMargin * (gameLogic.size -1 ))) / gameLogic.size);
                if (cellSize == 0)
                    cellSize = 1;
            }
            
            leftMargin = (1920 - getScreenCover()) / 2;
            topMargin = (1080 - getScreenCover()) / 2;
        }
    }
    
    protected int getScreenCover()
    {
        return (cellMargin * (gameLogic.size - 1)) + (cellSize * gameLogic.size); 
    }
    
    protected int[] getScreenCords(int x, int y)
    {
        int widthVal = (x - leftMargin) / (cellSize + cellMargin);
        int heightVal = (y - topMargin) / (cellSize + cellMargin);
        
        int[] retTable = new int[2];
        retTable[1] = widthVal;
        retTable[0] = heightVal;
        
        return retTable;
    }
    
    /**
     * UI-CONFIG FUNCTIONS
     */
    
    protected void setContextMenuItems()
    {
        MenuItem circularInc = new MenuItem("Add circular inclusion");
        MenuItem squareInc = new MenuItem("Add square inclusion");
        MenuItem detailedInc = new MenuItem("Add inclusion...");
        
        circularInc.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if (contextClick != null)
                {
                    int[] valTable = {contextClick.x, contextClick.y};
                    
                    if (valTable[0] >= 0 && valTable[0] < gameLogic.size && valTable[1] >= 0 
                            && valTable[1] < gameLogic.size)
                    {
                        gameLogic.createInclusionStructure(valTable[0], valTable[1], 10, GrainLogic.InclusionType.circular);
                    }

                    contextClick = null;
                    drawCurrentState();
                }
                
                contextMenu.hide();
            }
        });
        
        squareInc.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if (contextClick != null)
                {
                    int[] valTable = {contextClick.x, contextClick.y};
                    
                    if (valTable[0] >= 0 && valTable[0] < gameLogic.size && valTable[1] >= 0 
                            && valTable[1] < gameLogic.size)
                    {
                        gameLogic.createInclusionStructure(valTable[0], valTable[1], 3, GrainLogic.InclusionType.square);
                    }

                    contextClick = null;
                    drawCurrentState();
                }
                
                contextMenu.hide();
            }
        });
        
        detailedInc.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if (contextClick != null)
                {
                    GrainLogic.InclusionType selectedType = null;
                    int selectedRadius = 0;
                    
                    Dialog<Pair<Integer,String>> customWindow = new ChoiceDialog<>();
                    customWindow.setTitle("Inclusion settings");
                    customWindow.setHeaderText("Select details of inclusion");
                    
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(20,150,10,10));
                    
                    TextField sizeField = new TextField();
                    sizeField.setPromptText("Grain size (numeric)");
                    ChoiceBox typeField = new ChoiceBox();
                    typeField.getItems().addAll("Circular","Square");
                    
                    grid.add(new Label("Grain size:"),0,0);
                    grid.add(sizeField,1,0);
                    grid.add(new Label("Grain type:"),0,1);
                    grid.add(typeField,1,1);
                    
                    customWindow.getDialogPane().setContent(grid);
                    customWindow.setResultConverter(acceptButton -> 
                    {
                        if (acceptButton != ButtonType.CANCEL)
                            return new Pair<>(Integer.parseInt(sizeField.getText()), typeField.getValue().toString());
                        return null;
                    });
                    
                    Optional<Pair<Integer,String>> result = null;
                    try
                    {
                        result = customWindow.showAndWait();
                        if (result == null)
                            return;
                    }
                    catch (Exception e)
                    {
                        System.err.println("- Wrong data inserted -");
                    }
                    
                    selectedRadius = result.get().getKey();
                    if (result.get().getValue().equals("Circular"))
                        selectedType = GrainLogic.InclusionType.circular;
                    else if (result.get().getValue().equals("Square"))
                            selectedType = GrainLogic.InclusionType.square;
                    else
                        return;
                    
                    int[] valTable = {contextClick.x, contextClick.y};
                    
                    if (valTable[0] >= 0 && valTable[0] < gameLogic.size && valTable[1] >= 0 
                            && valTable[1] < gameLogic.size)
                    {
                        gameLogic.createInclusionStructure(valTable[0], valTable[1], selectedRadius, selectedType);
                    }

                    contextClick = null;
                    drawCurrentState();
                }
                
                contextMenu.hide();
            }
        });
        
        contextMenu = new ContextMenu(circularInc, squareInc, detailedInc);
        
    }
    
    
}
