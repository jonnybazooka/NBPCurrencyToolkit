package sample.dataTransferObjects;

public class TrainingSetObject {
    private double gbp_change_t1;
    private double gbp_change_t2;
    private double usd_change_t1;
    private double usd_change_t2;
    private double chf_change_t1;
    private double chf_change_t2;
    private double eur_change_t1;
    private double eur_change_t2;

    private double gbp_change_t0;
    private double usd_change_t0;
    private double chf_change_t0;
    private double eur_change_t0;

    public TrainingSetObject(double gbpRateT0, double gbpRateT1, double gbpRateT2, double gbpRateT3
            , double usdRateT0, double usdRateT1, double usdRateT2, double usdRateT3
            , double chfRateT0, double chfRateT1, double chfRateT2, double chfRateT3
            , double eurRateT0, double eurRateT1, double eurRateT2, double eurRateT3) {
        this.gbp_change_t1 = gbpRateT1 - gbpRateT2;
        this.gbp_change_t2 = gbpRateT2 - gbpRateT3;
        this.usd_change_t1 = usdRateT1 - usdRateT2;
        this.usd_change_t2 = usdRateT2 - usdRateT3;
        this.chf_change_t1 = chfRateT1 - chfRateT2;
        this.chf_change_t2 = chfRateT2 - chfRateT3;
        this.eur_change_t1 = eurRateT1 - eurRateT2;
        this.eur_change_t2 = eurRateT2 - eurRateT3;
        this.gbp_change_t0 = gbpRateT0 - gbpRateT1;
        this.usd_change_t0 = usdRateT0 - usdRateT1;
        this.chf_change_t0 = chfRateT0 - chfRateT1;
        this.eur_change_t0 = eurRateT0 - eurRateT1;
    }

    public double getGbp_change_t1() {
        return gbp_change_t1;
    }

    public double getGbp_change_t2() {
        return gbp_change_t2;
    }

    public double getUsd_change_t1() {
        return usd_change_t1;
    }

    public double getUsd_change_t2() {
        return usd_change_t2;
    }

    public double getChf_change_t1() {
        return chf_change_t1;
    }

    public double getChf_change_t2() {
        return chf_change_t2;
    }

    public double getEur_change_t1() {
        return eur_change_t1;
    }

    public double getEur_change_t2() {
        return eur_change_t2;
    }

    public double getGbp_change_t0() {
        return gbp_change_t0;
    }

    public double getUsd_change_t0() {
        return usd_change_t0;
    }

    public double getChf_change_t0() {
        return chf_change_t0;
    }

    public double getEur_change_t0() {
        return eur_change_t0;
    }
}
