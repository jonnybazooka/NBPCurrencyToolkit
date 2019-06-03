package sample.dataTransferObjects;

import java.time.LocalDate;

public class Currency {
    private String code;
    private String currency;
    private LocalDate effectiveDate;
    private double midRate;

    public Currency() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public double getMidRate() {
        return midRate;
    }

    public void setMidRate(double midRate) {
        this.midRate = midRate;
    }

    @Override
    public String toString() {
        return code + " : " + currency + ", Date: " + effectiveDate.toString() + " , Mid Exchange Rate: " + midRate + "\n";
    }
}
