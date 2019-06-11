package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import sample.dataAccessObjects.TestObjectDAO;
import sample.dataAccessObjects.TrainingObjectDAO;
import sample.dataTransferObjects.TestSetObject;
import sample.dataTransferObjects.TrainingSetObject;
import sample.exceptions.NoSuchCurrencyException;
import sample.mongoDB.MongoDBClient;
import sample.mongoDB.MongoOperations;
import sample.neuralNetwork.Network;
import sample.neuralNetwork.TrainSet;
import sample.validators.DateValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NeuralNetViewController {
    private static final Logger LOGGER = LogManager.getLogger(NeuralNetViewController.class.getName());

    public TextField trainStartDate;
    public TextField trainEndDate;
    public TextField testStartDate;
    public TextField testEndDate;
    public CheckBox gbpBox;
    public CheckBox usdBox;
    public CheckBox chfBox;
    public CheckBox eurBox;
    public Button loadDataButton;
    public TextField cycles;
    public TextField batchSize;
    public Button trainButton;
    public TextArea textArea;
    public Button loadTestButton;
    public Button testButton;

    private MongoDBClient mongoDBClient;
    private DateValidator dateValidator;
    private TrainSet trainSet;
    private Network network;
    private List<TestSetObject> testSet;

    @FXML
    private void initialize() {
        this.dateValidator = new DateValidator();
        this.trainSet = new TrainSet(8, 2);
        this.network = new Network(8, 5, 3, 2);
    }

    public void setMongoDBClient(MongoDBClient mongoDBClient) {
        this.mongoDBClient = mongoDBClient;
    }

    public void loadDataForTraining(ActionEvent event) {
        TrainingObjectDAO trainingObjectDAO = new TrainingObjectDAO();
        final String[] currencies = new String[]{"GBP", "USD", "CHF", "EUR"};
        String selectedCurrency = "";
        List<double[]> currencyRatesList = new ArrayList<>();
        try {
            selectedCurrency = getCodeFromCheckbox();
            for (String currency : currencies) {
                MongoOperations mongoOperations = mongoDBClient.getOperation(currency);
                if (!dateValidator.checkIfDateFormatIsCorrect(trainStartDate, trainEndDate)) {
                    LOGGER.warn("Incorrect date format. Couldn't load data for training.");
                    return;
                }
                List<Document> results = mongoOperations.findRecordsInDateRange(mongoDBClient, trainStartDate.getText(), trainEndDate.getText());
                double[] rates = createRatesArray(results);
                currencyRatesList.add(rates);
            }
        } catch (NoSuchCurrencyException e) {
            LOGGER.error(e.getMessage(), e);
        }
        List<TrainingSetObject> trainingSetObjects = createTrainingObjects(currencyRatesList);
        try {
            for (TrainingSetObject object : trainingSetObjects) {
                double[] in = trainingObjectDAO.getTrainingSet(object);
                double[] target = trainingObjectDAO.getTargetSet(object, selectedCurrency);
                trainSet.addData(in, target);
            }
        } catch (NoSuchCurrencyException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("Training set loaded successfully. Number of objects: " + trainSet.size());
    }

    public void loadTestData(ActionEvent event) {
        this.testSet = new ArrayList<>();
        final String[] currencies = new String[]{"GBP", "USD", "CHF", "EUR"};
        String selectedCurrency = "";
        List<double[]> currencyRatesList = new ArrayList<>();
        List<String> datesList = new ArrayList<>();
        try {
            selectedCurrency = getCodeFromCheckbox();
            for (String currency : currencies) {
                MongoOperations mongoOperations = mongoDBClient.getOperation(currency);
                if (!dateValidator.checkIfDateFormatIsCorrect(testStartDate, testEndDate)) {
                    LOGGER.warn("Incorrect date format. Couldn't load data for testing.");
                    return;
                }
                List<Document> results = mongoOperations.findRecordsInDateRange(mongoDBClient, testStartDate.getText(), testEndDate.getText());
                double[] rates = createRatesArray(results);
                currencyRatesList.add(rates);
            }
            MongoOperations mongoOperations = mongoDBClient.getOperation(selectedCurrency);
            List<Document> result = mongoOperations.findRecordsInDateRange(mongoDBClient, testStartDate.getText(), testEndDate.getText());
            datesList = getDates(result);
        } catch (NoSuchCurrencyException e) {
            LOGGER.error(e.getMessage(), e);
        }
        testSet = createTestObjects(currencyRatesList, datesList);
        LOGGER.debug("Test set loaded successfully. Number of objects: " + testSet.size());
    }

    public void train(ActionEvent event) {
        int loops = Integer.parseInt(cycles.getText());
        int batch = Integer.parseInt(batchSize.getText());
        network.train(trainSet, loops, batch);
        LOGGER.debug("Network training complete.");
    }

    public void test(ActionEvent event) {
        textArea.clear();
        TestObjectDAO testObjectDAO = new TestObjectDAO();
        double goodPredictions = 0;
        double goodHighCertaintyPredictions = 0;
        double allPredictions = 0;
        double allHighCertaintyPredictions = 0;
        try {
            for (TestSetObject testObject : testSet) {
                double[] test = testObjectDAO.getTrainingSet(testObject);
                double[] output = network.calculate(test);
                textArea.appendText(testObjectDAO.getExpectedResult(testObject, getCodeFromCheckbox()).concat("\n"));
                textArea.appendText("\t CALCULATED: " + Arrays.toString(output).concat(getVector(output)).concat("\n"));
                if (getVector(output).equals(" RISE ") && testObjectDAO.getExpectedChange(testObject, getCodeFromCheckbox()) >= 0) {
                    if (output[0] > 0.75) {
                        goodHighCertaintyPredictions++;
                    }
                    goodPredictions++;
                } else if (getVector(output).equals(" DROP ") && testObjectDAO.getExpectedChange(testObject, getCodeFromCheckbox()) < 0) {
                    if (output[0] < 0.25) {
                        goodHighCertaintyPredictions++;
                    }
                    goodPredictions++;
                }
                allPredictions++;
                if (output[0] > 0.75 || output[0] < 0.25) {
                    allHighCertaintyPredictions++;
                }
            }
            double successRate = (goodPredictions/allPredictions)*100;
            double highCertaintyPredictionRate = (goodHighCertaintyPredictions/allHighCertaintyPredictions)*100;
            textArea.appendText(String.format("Successful Prediction Rate: %.3f %% \n", successRate));
            textArea.appendText(String.format("High Certainty Prediction Rate: %.3f %% \n", highCertaintyPredictionRate));
        } catch (NoSuchCurrencyException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String getVector(double[] output) {
        if (output[0] > output[1]) {
            return " RISE ";
        }
        return " DROP ";
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

    private List<String> getDates(List<Document> result) {
        List<String> dates = new ArrayList<>();
        for (Document document : result) {
            String date = document.getString("date");
            dates.add(date);
        }
        return dates;
    }

    private List<TrainingSetObject> createTrainingObjects(List<double[]> currencyRatesList) {
        List<TrainingSetObject> trainingSetObjects = new ArrayList<>();
        for (int i = 3; i < currencyRatesList.get(1).length; i++) {
            double gbpRateT0 = currencyRatesList.get(0)[i];
            double gbpRateT1 = currencyRatesList.get(0)[i-1];
            double gbpRateT2 = currencyRatesList.get(0)[i-2];
            double gbpRateT3 = currencyRatesList.get(0)[i-3];
            double usdRateT0 = currencyRatesList.get(1)[i];
            double usdRateT1 = currencyRatesList.get(1)[i-1];
            double usdRateT2 = currencyRatesList.get(1)[i-2];
            double usdRateT3 = currencyRatesList.get(1)[i-3];
            double chfRateT0 = currencyRatesList.get(2)[i];
            double chfRateT1 = currencyRatesList.get(2)[i-1];
            double chfRateT2 = currencyRatesList.get(2)[i-2];
            double chfRateT3 = currencyRatesList.get(2)[i-3];
            double eurRateT0 = currencyRatesList.get(3)[i];
            double eurRateT1 = currencyRatesList.get(3)[i-1];
            double eurRateT2 = currencyRatesList.get(3)[i-2];
            double eurRateT3 = currencyRatesList.get(3)[i-3];
            TrainingSetObject trainingSetObject = new TrainingSetObject(gbpRateT0, gbpRateT1, gbpRateT2, gbpRateT3
                    , usdRateT0, usdRateT1, usdRateT2, usdRateT3
                    , chfRateT0, chfRateT1, chfRateT2, chfRateT3
                    , eurRateT0, eurRateT1, eurRateT2, eurRateT3);
            trainingSetObjects.add(trainingSetObject);
        }
        return trainingSetObjects;
    }

    private List<TestSetObject> createTestObjects(List<double[]> currencyRatesList, List<String> dates) {
        List<TestSetObject> testSetObjects = new ArrayList<>();
        for (int i = 3; i < currencyRatesList.get(1).length; i++) {
            double gbpRateT0 = currencyRatesList.get(0)[i];
            double gbpRateT1 = currencyRatesList.get(0)[i-1];
            double gbpRateT2 = currencyRatesList.get(0)[i-2];
            double gbpRateT3 = currencyRatesList.get(0)[i-3];
            double usdRateT0 = currencyRatesList.get(1)[i];
            double usdRateT1 = currencyRatesList.get(1)[i-1];
            double usdRateT2 = currencyRatesList.get(1)[i-2];
            double usdRateT3 = currencyRatesList.get(1)[i-3];
            double chfRateT0 = currencyRatesList.get(2)[i];
            double chfRateT1 = currencyRatesList.get(2)[i-1];
            double chfRateT2 = currencyRatesList.get(2)[i-2];
            double chfRateT3 = currencyRatesList.get(2)[i-3];
            double eurRateT0 = currencyRatesList.get(3)[i];
            double eurRateT1 = currencyRatesList.get(3)[i-1];
            double eurRateT2 = currencyRatesList.get(3)[i-2];
            double eurRateT3 = currencyRatesList.get(3)[i-3];
            String date = dates.get(i);
            TestSetObject testSetObject = new TestSetObject(gbpRateT0, gbpRateT1, gbpRateT2, gbpRateT3
                    , usdRateT0, usdRateT1, usdRateT2, usdRateT3
                    , chfRateT0, chfRateT1, chfRateT2, chfRateT3
                    , eurRateT0, eurRateT1, eurRateT2, eurRateT3
                    , date);
            testSetObjects.add(testSetObject);
        }
        return testSetObjects;
    }

    private boolean[] checkCurrencyCheckboxes() {
        boolean[] checkboxes = new boolean[4];
        checkboxes[0] = gbpBox.isSelected();
        checkboxes[1] = usdBox.isSelected();
        checkboxes[2] = chfBox.isSelected();
        checkboxes[3] = eurBox.isSelected();
        LOGGER.debug("Checking checkboxes: " + "GBP: " + gbpBox.isSelected()
                + " USD: " + usdBox.isSelected()
                + " CHF: " + chfBox.isSelected()
                + " EUR: " + eurBox.isSelected());
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
        String[] codes = {"GBP", "USD", "CHF", "EUR"};
        return codes[i];
    }
}
