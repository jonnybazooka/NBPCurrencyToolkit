package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import sample.dataTransferObjects.TableCurrencyObject;
import sample.exceptions.NoSuchCurrencyException;
import sample.mongoDB.MongoDBClient;
import sample.mongoDB.MongoOperations;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class DBViewController {
    private static final Logger LOGGER = LogManager.getLogger(DBViewController.class.getName());

    public CheckBox gbpCheckbox;
    public CheckBox usdCheckbox;
    public CheckBox chfCheckbox;
    public CheckBox eurCheckbox;
    public CheckBox jpyCheckbox;
    public TextField startDate;
    public TextField endDate;
    public Button findResults;
    public Button deleteRecords;
    public TableView resultsTable;
    public TableColumn dateCol;
    public TableColumn gbpCol;
    public TableColumn usdCol;
    public TableColumn chfCol;
    public TableColumn eurCol;
    public TableColumn jpyCol;
    public BorderPane dbViewPane;

    private MongoDBClient mongoDBClient;
    private ObservableList<TableCurrencyObject> tableCurrencyObjects;

    @FXML
    private void initialize() {
        LOGGER.debug("Initializing DBViewController");
        this.mongoDBClient = new MongoDBClient();
        tableCurrencyObjects = FXCollections.observableArrayList();
    }

    public void fetchResultsFromDB(ActionEvent event) {
        tableCurrencyObjects.clear();
        initializeColumns();
        boolean[] checkboxes = checkCurrencyCheckboxes();
        for (int i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i]) {
                try {
                    List<Document> results;
                    MongoOperations mongoOperations = mongoDBClient.getOperation(getCurrencyCode(i));
                    if (startDate.getText().matches("\\d{4}-\\d{2}-\\d{2}") && endDate.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                        results = mongoOperations.findRecordsInDateRange(mongoDBClient, startDate.getText(), endDate.getText());
                        LOGGER.debug("Fetching results from database for date range: " + startDate.getText() + " , " + endDate.getText());
                    } else {
                        results = mongoOperations.findAllRecords(mongoDBClient);
                        LOGGER.debug("Fetching all results from database.");
                    }
                    createTableCurrencyObjects(results);
                } catch (NoSuchCurrencyException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        resultsTable.setItems(tableCurrencyObjects);
    }

    public void deleteRecordsFromDatabase(ActionEvent event) {
        if (!confirmDelete()) {
            LOGGER.warn("Delete action canceled. No confirmation.");
            return;
        }
        tableCurrencyObjects.clear();
        boolean[] checkboxes = checkCurrencyCheckboxes();
        for (int i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i]) {
                try {
                    MongoOperations mongoOperations = mongoDBClient.getOperation(getCurrencyCode(i));
                    if (startDate.getText().matches("\\d{4}-\\d{2}-\\d{2}") && endDate.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                        long deletedCount = mongoOperations.deleteRecordsInDateRange(mongoDBClient, startDate.getText(), endDate.getText());
                        LOGGER.debug("Deleting records from database for date range: " + startDate.getText() + " , " + endDate.getText());
                    } else {
                        long deletedCount = mongoOperations.deleteAllRecords(mongoDBClient);
                        LOGGER.debug("Deleting all records from database.");
                    }
                } catch (NoSuchCurrencyException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    private void initializeColumns() {
        dateCol.setCellValueFactory(new PropertyValueFactory<TableCurrencyObject, LocalDate>("date"));
        TableColumn[] columns = {gbpCol, usdCol, chfCol, eurCol, jpyCol};
        boolean[] checkboxes = checkCurrencyCheckboxes();
        for (int i = 0; i < checkboxes.length; i++) {
            LOGGER.debug("Setting CellValueFactory for: " + checkboxes.length + " table columns.");
            if(checkboxes[i]) {
                columns[i].setCellValueFactory(new PropertyValueFactory<TableCurrencyObject, Double>(getCurrencyRateString(i)));
            }
        }
    }

    private boolean[] checkCurrencyCheckboxes() {
        boolean[] checkboxes = new boolean[5];
        checkboxes[0] = gbpCheckbox.isSelected();
        checkboxes[1] = usdCheckbox.isSelected();
        checkboxes[2] = chfCheckbox.isSelected();
        checkboxes[3] = eurCheckbox.isSelected();
        checkboxes[4] = jpyCheckbox.isSelected();
        LOGGER.debug("Checking checkboxes: " + "GBP: " + gbpCheckbox.isSelected()
                + " USD: " + usdCheckbox.isSelected()
                + " CHF: " + chfCheckbox.isSelected()
                + " EUR: " + eurCheckbox.isSelected()
                + " JPY: " + jpyCheckbox.isSelected());
        return checkboxes;
    }

    private String getCurrencyCode(int i) {
        String[] codes = {"GBP", "USD", "CHF", "EUR", "JPY"};
        return codes[i];
    }

    private String getCurrencyRateString(int i) {
        String[] rates = {"gbpRate", "usdRate", "chfRate", "eurRate", "jpyRate"};
        return rates[i];
    }

    private void createTableCurrencyObjects(List<Document> results) {
        LOGGER.debug("Creating TableCurrencyObjects for: " + results.size() + " results.");
        String date;
        String code;
        double mid;
        for (Document document : results) {
            date = document.getString("date");
            code = document.getString("code");
            mid = document.getDouble("mid");
            TableCurrencyObject testObject = new TableCurrencyObject();
            testObject.setDate(LocalDate.parse(date));
            if (!tableCurrencyObjects.contains(testObject)) {
                TableCurrencyObject currencyObject = new TableCurrencyObject();
                currencyObject.setDate(LocalDate.parse(date));
                setExchangeRate(currencyObject, code, mid);
                tableCurrencyObjects.add(currencyObject);
            } else {
                TableCurrencyObject currencyObject = findObject(testObject);
                setExchangeRate(currencyObject, code, mid);
            }
        }
    }

    private TableCurrencyObject findObject(TableCurrencyObject testObject) {
        for (TableCurrencyObject tco : tableCurrencyObjects) {
            if (tco.equals(testObject)) {
                return tco;
            }
        }
        return null;
    }

    private void setExchangeRate(TableCurrencyObject currencyObject, String code, double mid) {
        switch (code) {
            case "GBP":
                currencyObject.setGbpRate(mid);
                break;
            case "USD":
                currencyObject.setUsdRate(mid);
                break;
            case "CHF":
                currencyObject.setChfRate(mid);
                break;
            case "EUR":
                currencyObject.setEurRate(mid);
                break;
            case "JPY":
                currencyObject.setJpyRate(mid);
                break;
        }
    }

    private boolean confirmDelete() {
        try {
            SplitPane popup = FXMLLoader.load(getClass().getResource("/confirmationPopupWindow.fxml"));
            Stage popupStage = new Stage();
            Scene scene = new Scene(popup);
            popupStage.setScene(scene);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (IOException e) {
            LOGGER.error("Opening popup window failed. " + e.getMessage(), e);
        }
        return ConfirmationPopupWindowController.confirmed;
    }
}
