package sample.validators;

public class CheckboxValidator {

    public boolean checkIfNumberOfCheckedBoxesMatchesExpected(int boxesChecked, int expected) {
        return boxesChecked == expected;
    }
}
