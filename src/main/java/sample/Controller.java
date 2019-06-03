package sample;

import com.google.api.client.http.apache.ApacheHttpTransport;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import sample.dataAccessObjects.CurrencyDAO;
import sample.dataTransferObjects.Currency;
import sample.validators.DateValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    ApacheHttpTransport apache;

    public TextArea results;
    public Button executeQuery;
    public TextField endDate;
    public TextField startDate;
    public Label query;
    public ChoiceBox currencyBox;

    private String selectedCurrencyCode;
    private ObservableList<String> currencies;
    private List<Currency> currencyListHolder;

    @FXML
    private void initialize() {
        this.apache = new ApacheHttpTransport();
        selectedCurrencyCode = "gbp/";
        currencies = FXCollections.observableArrayList("GBP", "EUR", "USD", "CHF", "JPY");
        currencyBox.setItems(currencies);
        currencyBox.setValue("GBP");
        currencyListHolder = new ArrayList<>();
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
}