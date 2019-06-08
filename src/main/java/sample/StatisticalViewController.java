package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import sample.exceptions.NoSuchCurrencyException;
import sample.exceptions.NotCompatibileArraysException;
import sample.mongoDB.MongoDBClient;
import sample.mongoDB.MongoOperations;
import sample.statistical.Algorithms;
import sample.validators.CheckboxValidator;
import sample.validators.DateValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StatisticalViewController {
    private static final Logger LOGGER = LogManager.getLogger(StatisticalViewController.class.getName());

    private int boxesCheckedCount;
    private MongoDBClient mongoDBClient;
    private CheckboxValidator checkboxValidator = new CheckboxValidator();
    private DateValidator dateValidator = new DateValidator();
    private Algorithms algorithms = new Algorithms();

    public CheckBox gbpBox;
    public CheckBox usdBox;
    public CheckBox chfBox;
    public CheckBox eurBox;
    public CheckBox jpyBox;
    public TextField startDate;
    public TextField endDate;
    public Button rPearsonButton;
    public TextField correlationField;
    public TextField varianceField;
    public Button basicStatsButton;
    public TextField averageField;

    @FXML
    private void initialize() {
        boxesCheckedCount = 0;
        CheckBox[] checkBoxes = new CheckBox[]{gbpBox, usdBox, chfBox, eurBox, jpyBox};
        for (CheckBox checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        boxesCheckedCount++;
                    } else {
                        boxesCheckedCount--;
                    }
                }
            });
        }
    }

    public void setMongoDBClient(MongoDBClient mongoDBClient) {
        this.mongoDBClient = mongoDBClient;
    }

    public void calculatePearsonsR(ActionEvent event) {
        if (!checkboxValidator.checkIfNumberOfCheckedBoxesMatchesExpected(boxesCheckedCount, 2)) {
            correlationField.setText("ERROR");
            LOGGER.warn("Unexpected number of checkboxes selected. Expected: 2, Selected: " + boxesCheckedCount);
            return;
        }
        boolean[] checkboxes = checkCurrencyCheckboxes();
        List<double[]> arguments = new ArrayList<>();
        for (int i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i]) {
                try {
                    MongoOperations mongoOperations = mongoDBClient.getOperation(getCurrencyCode(i));
                    if (dateValidator.checkIfDateFormatIsCorrect(startDate, endDate)) {
                        List<Document> results = mongoOperations.findRecordsInDateRange(mongoDBClient, startDate.getText(), endDate.getText());
                        double[] rates = createRatesArray(results);
                        arguments.add(rates);
                    }
                } catch (NoSuchCurrencyException e) {
                    correlationField.setText("ERROR");
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        try {
            BigDecimal correlation = algorithms.rPearson(arguments.get(0), arguments.get(1));
            correlationField.setText(correlation.toPlainString());
        } catch (NotCompatibileArraysException e) {
            correlationField.setText("ERROR");
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void calculateBasicStatistics(ActionEvent event) {
        if (!checkboxValidator.checkIfNumberOfCheckedBoxesMatchesExpected(boxesCheckedCount, 1)) {
            varianceField.setText("ERROR");
            averageField.setText("ERROR");
            LOGGER.warn("Unexpected number of checkboxes selected. Expected: 1, Selected: " + boxesCheckedCount);
            return;
        }
        varianceField.clear();
        averageField.clear();
        calculateAverage();
        calculateVariance();
    }

    private void calculateAverage() {
        try {
            MongoOperations mongoOperations = mongoDBClient.getOperation(getCodeFromCheckbox());
            if (dateValidator.checkIfDateFormatIsCorrect(startDate, endDate)) {
                List<Document> results = mongoOperations.findRecordsInDateRange(mongoDBClient, startDate.getText(), endDate.getText());
                double[] rates = createRatesArray(results);
                BigDecimal average = algorithms.getAverage(rates);
                averageField.setText(average.toPlainString());
            } else {
                averageField.setText("ERROR");
            }
        } catch (NoSuchCurrencyException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void calculateVariance() {
        try {
            MongoOperations mongoOperations = mongoDBClient.getOperation(getCodeFromCheckbox());
            if (dateValidator.checkIfDateFormatIsCorrect(startDate, endDate)) {
                List<Document> results = mongoOperations.findRecordsInDateRange(mongoDBClient, startDate.getText(), endDate.getText());
                double[] rates = createRatesArray(results);
                BigDecimal variance = algorithms.variance(rates);
                varianceField.setText(variance.toPlainString());
            } else {
                varianceField.setText("ERROR");
            }
        } catch (NoSuchCurrencyException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private boolean[] checkCurrencyCheckboxes() {
        boolean[] checkboxes = new boolean[5];
        checkboxes[0] = gbpBox.isSelected();
        checkboxes[1] = usdBox.isSelected();
        checkboxes[2] = chfBox.isSelected();
        checkboxes[3] = eurBox.isSelected();
        checkboxes[4] = jpyBox.isSelected();
        LOGGER.debug("Checking checkboxes: " + "GBP: " + gbpBox.isSelected()
                + " USD: " + usdBox.isSelected()
                + " CHF: " + chfBox.isSelected()
                + " EUR: " + eurBox.isSelected()
                + " JPY: " + jpyBox.isSelected());
        return checkboxes;
    }

    private String getCodeFromCheckbox() throws NoSuchCurrencyException {
        boolean[] checkboxes = checkCurrencyCheckboxes();
        for (int i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i]) {
                return getCurrencyCode(i);
            }
        }
        throw new NoSuchCurrencyException("Corresponding currency code doesn't match checkbox.");
    }

    private String getCurrencyCode(int i) {
        String[] codes = {"GBP", "USD", "CHF", "EUR", "JPY"};
        return codes[i];
    }

    private double[] createRatesArray(List<Document> results) {
        double[] rates = new double[results.size()];
        int iterator = 0;
        for (Document document : results) {
            double rate = document.getDouble("mid");
            rates[iterator] = rate;
            iterator++;
        }
        return rates;
    }

}
