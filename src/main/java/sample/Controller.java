package sample;

import com.google.api.client.http.apache.ApacheHttpTransport;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import sample.dataAccessObjects.CurrencyDAO;
import sample.dataTransferObjects.Currency;
import sample.exceptions.NoSuchCurrencyException;
import sample.mongoDB.MongoDBClient;
import sample.mongoDB.MongoOperations;
import sample.validators.DateValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    ApacheHttpTransport apache;

    public Pane mainPane;
    public TextArea results;
    public Button executeQuery;
    public TextField endDate;
    public TextField startDate;
    public Label query;
    public ChoiceBox currencyBox;
    public Button saveToDBButton;
    public Button databaseView;

    private String selectedCurrencyCode;
    private ObservableList<String> currencies;
    private List<Currency> currencyListHolder;
    private MongoDBClient mongoDBClient;

    @FXML
    private void initialize() {
        this.apache = new ApacheHttpTransport();
        selectedCurrencyCode = "gbp/";
        currencies = FXCollections.observableArrayList("GBP", "EUR", "USD", "CHF", "JPY");
        currencyBox.setItems(currencies);
        currencyBox.setValue("GBP");
        currencyListHolder = new ArrayList<>();
        mongoDBClient = new MongoDBClient();
    }

    public void changeCurrency(ActionEvent event) {
        final String[] currencyChoices = new String[]{"gbp/", "eur/", "usd/", "chf/", "jpy/"};
        currencyBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                selectedCurrencyCode = currencyChoices[newValue.intValue()];
                query.setText(buildHttpQuery());
            }
        });
    }

    public String buildHttpQuery() {
        StringBuilder stringBuilder = new StringBuilder("http://api.nbp.pl/api/exchangerates/rates/a/");
        stringBuilder.append(selectedCurrencyCode);
        stringBuilder.append(startDate.getText()).append("/");
        stringBuilder.append(endDate.getText()).append("/");
        stringBuilder.append("?format=json");
        results.clear();
        return stringBuilder.toString();
    }

    private String buildHttpQuery(String start, String end) {
        StringBuilder stringBuilder = new StringBuilder("http://api.nbp.pl/api/exchangerates/rates/a/");
        stringBuilder.append(selectedCurrencyCode);
        stringBuilder.append(start).append("/");
        stringBuilder.append(end).append("/");
        stringBuilder.append("?format=json");
        return stringBuilder.toString();
    }

    public synchronized void executeHttpQuery() throws IOException {
        currencyListHolder.clear();
        List<String[]> datePairs = new DateValidator().getStartEndDates(startDate.getText(), endDate.getText());
        for (String[] pair : datePairs) {
            HttpGet httpGet = new HttpGet(buildHttpQuery(pair[0], pair[1]));
            HttpResponse response = apache.getHttpClient().execute(httpGet);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            CurrencyDAO currencyDAO = new CurrencyDAO();
            currencyListHolder.addAll(currencyDAO.mapCurrencyResponse(responseString));
        }
        for (Currency currency : currencyListHolder) {
            results.appendText(currency.toString());
        }
    }

    public void saveToDatabase(ActionEvent event) {
        MongoOperations mongoOperations = null;
        try {
            mongoOperations = mongoDBClient.getOperation(currencyBox.getValue().toString());
        } catch (NoSuchCurrencyException e) {
            results.clear();
            results.appendText(e.getMessage());
        }
        for (Currency currency : currencyListHolder) {
            if (mongoOperations != null) {
                mongoOperations.insertNewRecord(mongoDBClient, currency);
            }
        }
    }

    public void goToDatabaseView(ActionEvent event) throws IOException {
        BorderPane dbView = FXMLLoader.load(getClass().getResource("/dbView.fxml"));
        mainPane.getChildren().setAll(dbView);
    }
}
