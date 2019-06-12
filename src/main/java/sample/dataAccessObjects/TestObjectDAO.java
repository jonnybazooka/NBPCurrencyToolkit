package sample.dataAccessObjects;

import sample.dataTransferObjects.TestSetObject;
import sample.exceptions.NoSuchCurrencyException;

public class TestObjectDAO {

    public TestObjectDAO() {
    }

    public double[] getTrainingSet(TestSetObject object) {
        return new double[]{
                object.getGbp_change_t1() * 1000,
                object.getGbp_change_t2() * 1000,
                object.getUsd_change_t1() * 1000,
                object.getUsd_change_t2() * 1000,
                object.getChf_change_t1() * 1000,
                object.getChf_change_t2() * 1000,
                object.getEur_change_t1() * 1000,
                object.getEur_change_t2() * 1000
        };
    }

    public String getExpectedResult(TestSetObject object, String code) throws NoSuchCurrencyException {
        switch (code) {
            case "GBP":
                return String.format("GBP --> Date: %s | Real change: %.4f ", object.getDate(), object.getGbp_change_t0());
            case "USD":
                return String.format("USD --> Date: %s | Real change: %.4f ", object.getDate(), object.getUsd_change_t0());
            case "CHF":
                return String.format("CHF --> Date: %s | Real change: %.4f ", object.getDate(), object.getChf_change_t0());
            case "EUR":
                return String.format("EUR --> Date: %s | Real change: %.4f ", object.getDate(), object.getEur_change_t0());
            default:
                throw new NoSuchCurrencyException("Currency code not recognized.");
        }
    }

    public double getExpectedChange(TestSetObject object, String code) throws NoSuchCurrencyException {
        switch (code) {
            case "GBP":
                return object.getGbp_change_t0();
            case "USD":
                return object.getUsd_change_t0();
            case "CHF":
                return object.getChf_change_t0();
            case "EUR":
                return object.getEur_change_t0();
            default:
                throw new NoSuchCurrencyException("Currency code not recognized.");
        }
    }
}
