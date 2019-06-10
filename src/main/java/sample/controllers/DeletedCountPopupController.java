package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DeletedCountPopupController {
    public static long deletedCount = 0;

    public Label messageBox;
    public Button closeButton;

    @FXML
    private void initialize() {
        messageBox.setText(deletedCount + " lines of records has been successfully deleted.");
    }

    public void close(ActionEvent event) {
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.close();
    }
}
