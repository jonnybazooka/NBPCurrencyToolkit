package sample.dataAccessObjects;

import org.json.JSONArray;
import org.json.JSONObject;
import sample.dataTransferObjects.Currency;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDAO {

    public List<Currency> mapCurrencyResponse(String responseString) {
        List<Currency> currencyList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(responseString);
        JSONArray rates = jsonObject.getJSONArray("rates");
        for (int i = 0; i < rates.length(); i++) {
            String code = jsonObject.getString("code");
            String currency = jsonObject.getString("currency");
            JSONObject rate = jsonObject.getJSONArray("rates").getJSONObject(i);
            LocalDate date = LocalDate.parse(rate.getString("effectiveDate"));
            double mid = rate.getDouble("mid");
            Currency curr = new Currency();
            curr.setCode(code);
            curr.setCurrency(currency);
            curr.setEffectiveDate(date);
            curr.setMidRate(mid);
            currencyList.add(curr);
        }
        return currencyList;
    }
}
