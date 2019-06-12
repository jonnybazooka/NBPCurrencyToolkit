package sample.validators;


import javafx.scene.control.TextField;

public class TextFieldValidator {

    public boolean checkIfEmpty(TextField textField) {
        if (textField.getText().equals("")) {
            return true;
        }
        return false;
    }
}
