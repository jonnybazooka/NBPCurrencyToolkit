package sample.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class ConfirmationPopupWindowController {
    public static boolean confirmed = false;

    public Button confirmButton;
    public Button cancelButton;

    public void confirm(ActionEvent event) {
        confirmed = true;
        confirmButton.getScene().getWindow().hide();
    }

    public void cancel(ActionEvent event) {
        confirmed = false;
        cancelButton.getScene().getWindow().hide();
    }


}
