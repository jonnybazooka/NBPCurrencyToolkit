package sample.dataTransferObjects;

import java.time.LocalDate;
import java.util.Objects;

public class TableCurrencyObject {
    private LocalDate date;
    private double gbpRate;
    private double usdRate;
    private double chfRate;
    private double eurRate;
    private double jpyRate;

    public TableCurrencyObject() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getGbpRate() {
        return gbpRate;
    }

    public void setGbpRate(double gbpRate) {
        this.gbpRate = gbpRate;
    }

    public double getUsdRate() {
        return usdRate;
    }

    public void setUsdRate(double usdRate) {
        this.usdRate = usdRate;
    }

    public double getChfRate() {
        return chfRate;
    }

    public void setChfRate(double chfRate) {
        this.chfRate = chfRate;
    }

    public double getEurRate() {
        return eurRate;
    }

    public void setEurRate(double eurRate) {
        this.eurRate = eurRate;
    }

    public double getJpyRate() {
        return jpyRate;
    }

    public void setJpyRate(double jpyRate) {
        this.jpyRate = jpyRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableCurrencyObject that = (TableCurrencyObject) o;
        return date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
