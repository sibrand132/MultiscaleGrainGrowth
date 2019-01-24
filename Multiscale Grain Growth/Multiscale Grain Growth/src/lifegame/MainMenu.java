/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lifegame;

import com.sun.javafx.beans.event.AbstractNotifyListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Optional;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import myUIComponents.ValidationLabel;


public class MainMenu extends AnchorPane
{
    private Stage mainStage;
    
    @FXML AnchorPane rootPanel;
    @FXML Button runButton;
    
    /**
     * LEFT-SIDE PANELS UI
     */
        
    @FXML TextField gridSizeField;
    @FXML Slider randomRatioSld;
            
    // (grain-growth specyfic)
    
    @FXML Pane grainPanel;
    @FXML TextField randomRatioGrainField;
    @FXML ChoiceBox neightChoice;
    @FXML ChoiceBox grainChoice;
    @FXML RadioButton grain_perRadio;
    @FXML RadioButton grain_zeroSrcRadio;
    @FXML TextField probabilityField;
    
    @FXML CheckBox recrystal_checkbox;
    
    private Pane actualPane;
    public Scene menuScene;
    
    private Integer radiusValue = 1;
    private Integer probablityValue = 60;
    
    public MainMenu(Stage mainStage)
    {
        this.mainStage = mainStage;
        
        FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
        
        fXMLLoader.setController(this);
        fXMLLoader.setRoot(this);
   
        try
        {
            fXMLLoader.load();
        }
        catch(IOException exc)
        {
            throw new RuntimeException(exc);
        }
        
        uiConfig();
    }
    
    private void uiConfig()
    {
        actualPane = grainPanel;
        
              
        /**
         * GRIDSIZE FIELD
         */
        
        gridSizeField.setOnKeyTyped(new EventHandler<KeyEvent>()        // VALIDATION DOES NOT WORKS!
        {

            private void showError(String errorText)
            {
                ValidationLabel label = new ValidationLabel();
                label.setMessage(errorText, "red");
                label.showAsValidationTooltip(gridSizeField.getParent());
            }
            
            private void showAccept(String acceptText)
            {
                ValidationLabel label = new ValidationLabel();
                label.setMessage(acceptText, "value OK");
                label.showAsValidationTooltip(gridSizeField.getParent());
            }
            
            @Override
            public void handle(KeyEvent event)
            {
                if (event.getCharacter().codePointAt(0) >= 48 && event.getCharacter().codePointAt(0) <= 57)
                {
                    int fieldValue = 0;
                    try
                    {
                        fieldValue = Integer.parseInt(gridSizeField.getText());
                    }
                    catch(NumberFormatException exc)
                    {
                        gridSizeField.setText("");
                        showError("Ops! Wrong value entered");
                    }
                    
                    if (fieldValue > 10 && fieldValue < 900)
                        showAccept("Correct!");
                    else
                        showError("Value must be between 10 and 900");
                }
                else
                {
                    showError("Only numeric values are acceptable");
                    gridSizeField.setText("");
                }
           }
        });
        
        /**
         * RANDOMRATIO SLIDER
         */
        
        randomRatioSld.setLabelFormatter(new StringConverter<Double>()
        {

            @Override
            public String toString(Double object)
            {
                return object.intValue() + " %";
            }

            @Override
            public Double fromString(String string)
            {
                return Double.valueOf(string.substring(0, string.indexOf(" %")));
            }
        });
             
        /**
         * NEIGHTBOURCHOICE : GRAIN
         */
        
        ArrayList<String> neightChoiceList = new ArrayList<>();
        neightChoiceList.add("Moore");
        neightChoiceList.add("Advanced");
        
        neightChoice.setItems(new ObservableListBase<String>()
        {

            @Override
            public String get(int index)
            {
                return neightChoiceList.get(index);
            }

            @Override
            public int size()
            {
                return neightChoiceList.size();
            }
        });
        
        neightChoice.setValue("Moore");
        
        /**
         * GRAINCHOICE : GRAIN
         */

        ArrayList<String> grainChoiceList = new ArrayList<>();
        grainChoiceList.add("Random");
        grainChoiceList.add("Manual selection");
        
        grainChoice.setItems(new ObservableListBase<String>()
        {

            @Override
            public String get(int index)
            {
                return grainChoiceList.get(index);
            }

            @Override
            public int size()
            {
                return grainChoiceList.size();
            }
        });
        
        grainChoice.setValue("Random");
          
        grainChoice.valueProperty().addListener(new ChangeListener()
        {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                if (newValue.equals("Radius"))
                {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Random radius");
                    dialog.setHeaderText("Random grain generation radius");
                    dialog.setContentText("Enter random radius (1 to grid size)");
                    
                    boolean valueValidate = false;
                    
                    while (!valueValidate)
                    {
                        Optional<String> radiusVal = dialog.showAndWait();
                        
                        try
                        {
                            radiusValue = Integer.valueOf(radiusVal.get());
                            if (!(radiusValue > 1 && radiusValue < 955))
                            {
                                dialog.setContentText("Enter proper value! (1 to grid size)");
                            }
                            else
                            {
                                valueValidate = true;
                            }
                        }
                        catch(NumberFormatException exc)
                        {
                            dialog.setContentText("Enter proper value! (1 to grid size)");
                        }
                    }

                }
            }
        });
        
        /**
         * RANDOMRATIOGRIDSLIDER : GRAIN
         */
        
        randomRatioGrainField.setTooltip(new Tooltip("Enter the number of grains which will" 
                + " be generated"));
                     
        
        neightChoice.valueProperty().addListener(new ChangeListener<String>()
        {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (newValue.equals("Advanced"))
                    probabilityField.setDisable(false);
                else
                    probabilityField.setDisable(true);
            }
        });
    }
    
    private void setCurrentPanel(Pane currentPane)
    {
        if (actualPane != currentPane)
        {
            currentPane.setOpacity(0);
            currentPane.setVisible(true);
            
            if (actualPane != null)
            {
                FadeTransition transition = new FadeTransition(Duration.millis(400), actualPane);
                transition.setFromValue(0.7);
                transition.setToValue(0);
                transition.setOnFinished(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    { 
                        actualPane.setVisible(false);
                        
                        FadeTransition transition = new FadeTransition(Duration.millis(400), currentPane);
                        transition.setFromValue(0);
                        transition.setToValue(0.7);
                        transition.play();
                        
                        actualPane = currentPane;
                    }
                });
                transition.play();
            }
            else
            {
                FadeTransition transition = new FadeTransition(Duration.seconds(1), currentPane);
                transition.setFromValue(0);
                transition.setToValue(0.7);
                transition.play();
                
                actualPane = currentPane;
            }   
        }
    }
    
    private void componentFade(boolean visible, Node component)
    {
        FadeTransition transition = new FadeTransition(Duration.millis(400), component);
        double fromVal = 1.0;
        double endVal = 0;
        
        if (!visible)
        {
            fromVal = 0.0;
            endVal = 1.0;
            component.setOpacity(0.0);
            component.setVisible(true);
        }
        
        transition.setFromValue(fromVal);
        transition.setToValue(endVal);
        transition.setOnFinished(new EventHandler<ActionEvent>()
        {

            @Override
            public void handle(ActionEvent event)
            {
                Platform.runLater(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        if (visible)
                            component.setVisible(false);
                    }
                });
            }
        });
        
        transition.play();
        

    }
    
    @FXML 
    public void onRunButtonClicked(MouseEvent evt)
    {
        
        int gridSize;
                
        try
        {
            gridSize = Integer.parseInt(gridSizeField.getText());

            if (gridSize < 20 || gridSize > 955)
                throw new NumberFormatException();
        }
        catch(NumberFormatException exc)
        {
            Dialog errDialog = new Alert(Alert.AlertType.WARNING);
            errDialog.setContentText("Please enter proper grid size value (20 - 955).");
            errDialog.show();
            return;
        }
                int bcSelected;
                if (grain_zeroSrcRadio.isSelected())
                    bcSelected = 0;
                else
                    bcSelected = 2;
                
                String neighModeSelected = "moore";
                try
                {                   
                    if (neightChoice.getValue().equals("Advanced"))
                        neighModeSelected = "advanced";
                }
                catch(NullPointerException exc)
                {
                    Dialog errDialog = new Alert(Alert.AlertType.WARNING);
                    errDialog.setContentText("Please select neighbour mode first.");
                    errDialog.show();
                    return;
                }

                String randomModeSelected = "random";
                    try
                    {
                        if (grainChoice.getValue().equals("Manual selection"))
                            randomModeSelected = "manual";
                    }
                    catch(NullPointerException exc)
                    {
                        Dialog errDialog = new Alert(Alert.AlertType.WARNING);
                        errDialog.setContentText("Please select grain generation mode first.");
                        errDialog.show();
                        return;
                    }
                
   
                int gridRatioCount;
                
                    try
                    {
                        gridRatioCount = Integer.parseInt(randomRatioGrainField.getText());

                        if (gridRatioCount < 0 || gridRatioCount > gridSize)
                            throw new NumberFormatException();
                    }
                    catch(NumberFormatException exc)
                    {
                        Dialog errDialog = new Alert(Alert.AlertType.WARNING);
                        errDialog.setContentText("Please enter proper random grain count value (1 - " +
                                gridSize + ").");
                        errDialog.show();
                        return;

                    }
                int probabilityValue = 70;
                if(!probabilityField.getText().equals(""))
                    probabilityValue= Integer.parseInt(probabilityField.getText());       
                
                
                grainGameController gameDesign = new grainGameController(neighModeSelected, randomModeSelected,
                        gridSize,bcSelected, gridRatioCount, radiusValue, probabilityValue);
                
                final grainGameController staticRefGameDesign = gameDesign;
                
                Scene gameScene = new Scene(gameDesign);
                mainStage.setHeight(900);
                mainStage.setWidth(1200);
                mainStage.setScene(gameScene);
                mainStage.setFullScreen(true);
                mainStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
                {

                    @Override
                    public void handle(KeyEvent event)
                    {
                        if (event.getCode() == KeyCode.ESCAPE)
                        {
                                    
                            mainStage.setWidth(405);
                            mainStage.setHeight(720);
                            mainStage.setScene(menuScene);
                        }
                    }
                });
                
            
        
    }
}
