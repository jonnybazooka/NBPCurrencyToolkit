package sample.dataTransferObjects;

public class TestSetObject extends TrainingSetObject {
    private String date;

    public TestSetObject(double gbpRateT0, double gbpRateT1, double gbpRateT2, double gbpRateT3, double usdRateT0, double usdRateT1, double usdRateT2, double usdRateT3, double chfRateT0, double chfRateT1, double chfRateT2, double chfRateT3, double eurRateT0, double eurRateT1, double eurRateT2, double eurRateT3, String date) {
        super(gbpRateT0, gbpRateT1, gbpRateT2, gbpRateT3, usdRateT0, usdRateT1, usdRateT2, usdRateT3, chfRateT0, chfRateT1, chfRateT2, chfRateT3, eurRateT0, eurRateT1, eurRateT2, eurRateT3);
        this.date = date;
    }

    public String getDate() {
        return date;
    }


}
