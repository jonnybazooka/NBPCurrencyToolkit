package sample.statistical;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sample.dataTransferObjects.CorrelationObject;
import sample.exceptions.NotCompatibileArraysException;
import sample.validators.AlgorithmsValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Algorithms {
    private static final Logger LOGGER = LogManager.getLogger(Algorithms.class.getName());

    public BigDecimal rPearson(double[] ratesCur1, double[] ratesCur2) throws NotCompatibileArraysException {
        BigDecimal sumOfRateX = BigDecimal.ZERO;
        BigDecimal sumOfRateY = BigDecimal.ZERO;
        BigDecimal sumOfMultiplicationsXY = BigDecimal.ZERO;
        BigDecimal sumOfSquareX = BigDecimal.ZERO;
        BigDecimal sumOfSquareY = BigDecimal.ZERO;

        if (!new AlgorithmsValidator().validateIfArraysAreEqualLength(ratesCur1, ratesCur2)) {
            throw new NotCompatibileArraysException("Arrays must be of equal length.");
        }
        BigDecimal numberOfLines = BigDecimal.valueOf(ratesCur1.length);
        for (int i = 0; i < ratesCur1.length; i++) {
            CorrelationObject correlationObject = new CorrelationObject(ratesCur1[i], ratesCur2[i]);
            sumOfRateX = sumOfRateX.add(correlationObject.getRateX());
            sumOfRateY = sumOfRateY.add(correlationObject.getRateY());
            sumOfMultiplicationsXY = sumOfMultiplicationsXY.add(correlationObject.getXmultiplyY());
            sumOfSquareX = sumOfSquareX.add(correlationObject.getXsqared());
            sumOfSquareY = sumOfSquareY.add(correlationObject.getYsquared());
        }
        BigDecimal n1 = numberOfLines.multiply(sumOfMultiplicationsXY);
        BigDecimal n2 = sumOfRateX.multiply(sumOfRateY);
        BigDecimal numerator = n1.subtract(n2);

        BigDecimal d1 = numberOfLines.multiply(sumOfSquareX);
        BigDecimal d2 = sumOfRateX.pow(2);
        BigDecimal d1d2 = d1.subtract(d2);
        BigDecimal d3 = numberOfLines.multiply(sumOfSquareY);
        BigDecimal d4 = sumOfRateY.pow(2);
        BigDecimal d3d4 = d3.subtract(d4);
        BigDecimal d1d2d3d4 = d1d2.multiply(d3d4);
        BigDecimal denominator = BigDecimal.valueOf(Math.sqrt(d1d2d3d4.doubleValue()));

        return numerator.divide(denominator, RoundingMode.HALF_DOWN);
    }
}
