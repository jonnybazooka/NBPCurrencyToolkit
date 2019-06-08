package sample.dataTransferObjects;

import java.math.BigDecimal;

public class CorrelationObject {

    private BigDecimal rateX;
    private BigDecimal rateY;

    public CorrelationObject(double x, double y) {
        this.rateX = BigDecimal.valueOf(x);
        this.rateY = BigDecimal.valueOf(y);
    }

    public BigDecimal getXmultiplyY() {
        return rateX.multiply(rateY);
    }

    public BigDecimal getXsqared() {
        return rateX.pow(2);
    }

    public BigDecimal getYsquared() {
        return rateY.pow(2);
    }

    public BigDecimal getRateX() {
        return rateX;
    }

    public BigDecimal getRateY() {
        return rateY;
    }
}
