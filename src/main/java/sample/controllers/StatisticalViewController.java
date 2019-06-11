package sample.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public Button correlationButton;
    public TextField correlationField;
    public TextField varianceField;
    public Button basicStatsButton;
    public TextField averageField;
    public TextField rangeField;
    public TextField deviationField;
    public TextField coefficientField;
    public TextField covarianceField;
    public TextField regressionField;
    public TextField reversedRegressionField;
    public Button backToDBButton;
    public Button neuralNetButton;

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

    public void calculateCorrelationStatistics(ActionEvent event) {
        if (!checkboxValidator.checkIfNumberOfCheckedBoxesMatchesExpected(boxesCheckedCount, 2)) {
            covarianceField.setText("ERROR");
            correlationField.setText("ERROR");
            reversedRegressionField.setText("ERROR");
            regressionField.setText("ERROR");
            LOGGER.warn("Unexpected number of checkboxes selected. Expected: 2, Selected: " + boxesCheckedCount);
            return;
        }
        boolean[] checkboxes = checkCurrencyCheckboxes();
        calculatePearsonsR(checkboxes);
        calculateCovariance(checkboxes);
        calculateRegressionFunction(checkboxes);
    }

    private void calculatePearsonsR(boolean[] checkboxes) {
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

    private void calculateCovariance(boolean[] checkboxes) {
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
                    covarianceField.setText("ERROR");
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        try {
            BigDecimal covariance = algorithms.covariance(arguments.get(0), arguments.get(1));
            covarianceField.setText(covariance.setScale(4, RoundingMode.HALF_UP).toPlainString());
        } catch (NotCompatibileArraysException e) {
            covarianceField.setText("ERROR");
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void calculateRegressionFunction(boolean[] checkboxes) {
        List<double[]> arguments = new ArrayList<>();
        List<String> currencies = new ArrayList<>();
        for (int i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i]) {
                try {
                    MongoOperations mongoOperations = mongoDBClient.getOperation(getCurrencyCode(i));
                    currencies.add(getCurrencyCode(i));
                    if (dateValidator.checkIfDateFormatIsCorrect(startDate, endDate)) {
                        List<Document> results = mongoOperations.findRecordsInDateRange(mongoDBClient, startDate.getText(), endDate.getText());
                        double[] rates = createRatesArray(results);
                        arguments.add(rates);
                    }
                } catch (NoSuchCurrencyException e) {
                    regressionField.setText("ERROR");
                    reversedRegressionField.setText("ERROR");
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        try {
            BigDecimal[] regressionCoefficients = algorithms.getRegression(arguments.get(0), arguments.get(1));
            BigDecimal[] regressionCoefficientsReversed = algorithms.getRegression(arguments.get(1), arguments.get(0));
            String regressionFunction = currencies.get(1) + " = " + regressionCoefficients[0].setScale(3, RoundingMode.HALF_UP) + " + "
                    + regressionCoefficients[1].setScale(3, RoundingMode.HALF_UP) + " * " + currencies.get(0);
            String regressionFunctionReversed = currencies.get(0) + " = " + regressionCoefficientsReversed[0].setScale(3, RoundingMode.HALF_UP) + " + "
                    + regressionCoefficientsReversed[1].setScale(3, RoundingMode.HALF_UP) + " * " + currencies.get(1);
            regressionField.setText(regressionFunction);
            reversedRegressionField.setText(regressionFunctionReversed);
        } catch (NotCompatibileArraysException e) {
            regressionField.setText("ERROR");
            reversedRegressionField.setText("ERROR");
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void calculateBasicStatistics(ActionEvent event) {
        if (!checkboxValidator.checkIfNumberOfCheckedBoxesMatchesExpected(boxesCheckedCount, 1)) {
            varianceField.setText("ERROR");
            averageField.setText("ERROR");
            rangeField.setText("ERROR");
            deviationField.setText("ERROR");
            coefficientField.setText("ERROR");
            LOGGER.warn("Unexpected number of checkboxes selected. Expected: 1, Selected: " + boxesCheckedCount);
            return;
        }
        varianceField.clear();
        averageField.clear();
        rangeField.clear();
        deviationField.clear();
        BigDecimal average = calculateAverage();
        calculateRange();
        calculateVariance();
        BigDecimal deviation = calculateStandardDeviation();
        calculateCoefficientOfVariation(deviation, average);
    }

    private BigDecimal calculateAverage() {
        try {
            MongoOperations mongoOperations = mongoDBClient.getOperation(getCodeFromCheckbox());
            if (dateValidator.checkIfDateFormatIsCorrect(startDate, endDate)) {
                List<Document> results = mongoOperations.findRecordsInDateRange(mongoDBClient, startDate.getText(), endDate.getText());
                double[] rates = createRatesArray(results);
                BigDecimal average = algorithms.getAverage(rates);
                averageField.setText(average.setScale(4, RoundingMode.HALF_UP).toPlainString());
                return average;
            } else {
                averageField.setText("ERROR");
            }
        } catch (NoSuchCurrencyException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private void calculateRange() {
        try {
            MongoOperations mongoOperations = mongoDBClient.getOperation(getCodeFromCheckbox());
            if (dateValidator.checkIfDateFormatIsCorrect(startDate, endDate)) {
                List<Document> results = mongoOperations.findRecordsInDateRange(mongoDBClient, startDate.getText(), endDate.getText());
                double[] rates = createRatesArray(results);
                BigDecimal range = algorithms.getRange(rates);
                rangeField.setText(range.setScale(4, RoundingMode.HALF_UP).toPlainString());
            } else {
                rangeField.setText("ERROR");
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
                varianceField.setText(variance.setScale(4, RoundingMode.HALF_UP).toPlainString());
            } else {
                varianceField.setText("ERROR");
            }
        } catch (NoSuchCurrencyException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private BigDecimal calculateStandardDeviation() {
        try {
            MongoOperations mongoOperations = mongoDBClient.getOperation(getCodeFromCheckbox());
            if (dateValidator.checkIfDateFormatIsCorrect(startDate, endDate)) {
                List<Document> results = mongoOperations.findRecordsInDateRange(mongoDBClient, startDate.getText(), endDate.getText());
                double[] rates = createRatesArray(results);
                BigDecimal deviation = algorithms.standardDeviation(rates);
                deviationField.setText(deviation.setScale(4, RoundingMode.HALF_UP).toPlainString());
                return deviation;
            } else {
                deviationField.setText("ERROR");
            }
        } catch (NoSuchCurrencyException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private void calculateCoefficientOfVariation(BigDecimal deviation, BigDecimal average) {
        BigDecimal coefficient = deviation.divide(average, RoundingMode.HALF_UP);
        coefficient = coefficient.multiply(BigDecimal.valueOf(100));
        coefficientField.setText(coefficient.setScale(2, RoundingMode.HALF_UP).toPlainString().concat(" %"));
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

    public void goToDatabaseView(ActionEvent event) throws IOException {
        BorderPane dbView = FXMLLoader.load(getClass().getResource("/dbView.fxml"));
        Scene scene = new Scene(dbView);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
        LOGGER.debug("Switching view to dbView");
    }

    public void goToNeuralNetView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/neuralNetworkView.fxml"));
        Parent statsView = loader.load();
        Scene scene = new Scene(statsView);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
        NeuralNetViewController controller = loader.<NeuralNetViewController>getController();
        controller.setMongoDBClient(mongoDBClient);
        LOGGER.debug("Switching to statistical view.");
        window.show();
    }
}
