/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myUIComponents;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;


public class ValidationLabel extends ContextMenu
{ 
    private final MenuItem item;
    
    public ValidationLabel()
    {    
        item = new MenuItem();
        item.setDisable(true);
        setAutoHide(true);
    }
    
    public void setMessage(String messageText, String messageColor)
    {
        item.setText(messageText);
        item.setStyle("-fx-text-fill: " + messageColor);
    }
    
    public void showAsValidationTooltip(Node parentNode)
    {
        super.show(parentNode, Side.RIGHT, 10.0, 10.0);
    }
}
