package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.bson.Document;
import sample.dataTransferObjects.TableCurrencyObject;
import sample.exceptions.NoSuchCurrencyException;
import sample.mongoDB.MongoDBClient;
import sample.mongoDB.MongoOperations;

import java.time.LocalDate;
import java.util.List;

public class DBViewController {

    public CheckBox gbpCheckbox;
    public CheckBox usdCheckbox;
    public CheckBox chfCheckbox;
    public CheckBox eurCheckbox;
    public CheckBox jpyCheckbox;
    public TextField startDate;
    public TextField endDate;
    public Button findResults;
    public TableView resultsTable;
    public TableColumn dateCol;
    public TableColumn gbpCol;
    public TableColumn usdCol;
    public TableColumn chfCol;
    public TableColumn eurCol;
    public TableColumn jpyCol;

    private MongoDBClient mongoDBClient;
    private ObservableList<TableCurrencyObject> tableCurrencyObjects;

    @FXML
    private void initialize() {
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
                    MongoOperations mongoOperations = mongoDBClient.getOperation(getCurrencyCode(i));
                    List<Document> results = mongoOperations.findAllRecords(mongoDBClient);
                    createTableCurrencyObjects(results);
                } catch (NoSuchCurrencyException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        resultsTable.setItems(tableCurrencyObjects);
    }

    private void initializeColumns() {
        dateCol.setCellValueFactory(new PropertyValueFactory<TableCurrencyObject, LocalDate>("date"));
        TableColumn[] columns = {gbpCol, usdCol, chfCol, eurCol, jpyCol};
        boolean[] checkboxes = checkCurrencyCheckboxes();
        for (int i = 0; i < checkboxes.length; i++) {
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
        String date;
        String code = "";
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
}
