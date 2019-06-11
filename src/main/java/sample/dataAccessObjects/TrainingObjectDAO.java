package sample.dataAccessObjects;

import sample.dataTransferObjects.TrainingSetObject;
import sample.exceptions.NoSuchCurrencyException;
import sample.statistical.Algorithms;

public class TrainingObjectDAO {
    private Algorithms algorithms;

    public TrainingObjectDAO() {
        this.algorithms = new Algorithms();
    }

    public double[] getTrainingSet(TrainingSetObject object) {
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

    public double[] getTargetSet(TrainingSetObject object, String code) throws NoSuchCurrencyException {
        switch (code) {
            case "USD":
                if (object.getUsd_change_t0() >= 0) {
                    return new double[]{1,0};
                } else {
                    return new double[]{0,1};
                }
            case "GBP":
                if (object.getGbp_change_t0() >= 0) {
                    return new double[]{1,0};
                } else {
                    return new double[]{0,1};
                }
            case "CHF":
                if (object.getChf_change_t0() >= 0) {
                    return new double[]{1,0};
                } else {
                    return new double[]{0,1};
                }
            case "EUR":
                if (object.getEur_change_t0() >= 0) {
                    return new double[]{1,0};
                } else {
                    return new double[]{0,1};
                }
            default:
                throw new NoSuchCurrencyException("Error. Currency not recognized.");
        }
    }
}
