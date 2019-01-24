/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lifegame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Baka
 */
public class LifeGame extends Application
{
    private MainMenu mainMenu;
    
    @Override
    public void start(Stage primaryStage)
    {
        showStartScene(primaryStage);
    }
    
    public void showStartScene(Stage primaryStage)
    {
        mainMenu = new MainMenu(primaryStage);
        Scene scene = new Scene(mainMenu, mainMenu.getWidth(), mainMenu.getHeight());
        mainMenu.menuScene = scene;
        

        primaryStage.setTitle("Multiscale");
        primaryStage.setOnHiding(new EventHandler<WindowEvent>()
        {

            @Override
            public void handle(WindowEvent event)
            {
                Platform.exit();
            }
        });
        
        primaryStage.setScene(scene);
        primaryStage.setWidth(405);
        primaryStage.setHeight(720);
        primaryStage.setResizable(false);
        
        primaryStage.show();
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((screenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((screenBounds.getHeight() - primaryStage.getHeight()) / 2);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
    
}
