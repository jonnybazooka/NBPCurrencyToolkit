package sample.statistical;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sample.dataTransferObjects.CorrelationObject;
import sample.exceptions.NotCompatibileArraysException;
import sample.validators.AlgorithmsValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

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

        return numerator.divide(denominator, RoundingMode.HALF_UP);
    }

    public BigDecimal covariance(double[] ratesCur1, double[] ratesCur2) throws NotCompatibileArraysException {
        if (!new AlgorithmsValidator().validateIfArraysAreEqualLength(ratesCur1, ratesCur2)) {
            throw new NotCompatibileArraysException("Arrays must be of equal length.");
        }
        BigDecimal observations = BigDecimal.valueOf(ratesCur1.length);
        BigDecimal sumOfMultiplicationsXY = BigDecimal.ZERO;
        for (int i = 0; i < ratesCur1.length; i++) {
            CorrelationObject correlationObject = new CorrelationObject(ratesCur1[i], ratesCur2[i]);
            sumOfMultiplicationsXY = sumOfMultiplicationsXY.add(correlationObject.getXmultiplyY());
        }
        BigDecimal averageOfX = getAverage(ratesCur1);
        BigDecimal averageOfY = getAverage(ratesCur2);
        BigDecimal multiplicationOfAverages = averageOfX.multiply(averageOfY);
        BigDecimal sOMXYbyObs = sumOfMultiplicationsXY.divide(observations, RoundingMode.HALF_UP);
        return sOMXYbyObs.subtract(multiplicationOfAverages);
    }

    public BigDecimal[] getRegression(double[] ratesCur1, double[] ratesCur2) throws NotCompatibileArraysException {
        if (!new AlgorithmsValidator().validateIfArraysAreEqualLength(ratesCur1, ratesCur2)) {
            throw new NotCompatibileArraysException("Arrays must be of equal length.");
        }
        BigDecimal observations = BigDecimal.valueOf(ratesCur1.length);
        BigDecimal sumOfMultiplicationsXY = BigDecimal.ZERO;
        BigDecimal sumOfX = BigDecimal.ZERO;
        BigDecimal sumOfY = BigDecimal.ZERO;
        BigDecimal sumOfXsquared = BigDecimal.ZERO;
        for (int i = 0; i < ratesCur1.length; i++) {
            CorrelationObject correlationObject = new CorrelationObject(ratesCur1[i], ratesCur2[i]);
            sumOfMultiplicationsXY = sumOfMultiplicationsXY.add(correlationObject.getXmultiplyY());
            sumOfX = sumOfX.add(correlationObject.getRateX());
            sumOfY = sumOfY.add(correlationObject.getRateY());
            sumOfXsquared = sumOfXsquared.add(correlationObject.getXsqared());
        }
        BigDecimal numeratorEx1 = observations.multiply(sumOfMultiplicationsXY);
        BigDecimal numeratorEx2 = sumOfX.multiply(sumOfY);
        BigDecimal numerator = numeratorEx1.subtract(numeratorEx2);
        BigDecimal denominatorEx1 = observations.multiply(sumOfXsquared);
        BigDecimal denominatorEx2 = sumOfX.pow(2);
        BigDecimal denominator = denominatorEx1.subtract(denominatorEx2);
        BigDecimal coeff_b = numerator.divide(denominator, RoundingMode.HALF_UP);
        BigDecimal averageX = getAverage(ratesCur1);
        BigDecimal averageY = getAverage(ratesCur2);
        BigDecimal coeff_bX = coeff_b.multiply(averageX);
        BigDecimal coeff_a = averageY.subtract(coeff_bX);
        return new BigDecimal[]{coeff_a, coeff_b};
    }

    public BigDecimal variance(double[] ratesCur) {
        BigDecimal observations = BigDecimal.valueOf(ratesCur.length);
        BigDecimal average = getAverage(ratesCur);
        BigDecimal sumOfSmallestSquares = BigDecimal.ZERO;
        for (double d : ratesCur) {
            BigDecimal bigD = BigDecimal.valueOf(d);
            BigDecimal diff = bigD.subtract(average);
            BigDecimal smSquare = diff.pow(2);
            sumOfSmallestSquares = sumOfSmallestSquares.add(smSquare);
        }
        return sumOfSmallestSquares.divide(observations, RoundingMode.HALF_UP);
    }

    public BigDecimal standardDeviation(double[] ratesCur) {
        BigDecimal variance = variance(ratesCur);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }

    public BigDecimal getRange(double[] ratesCur) {
        double maxRate = Arrays.stream(ratesCur).max().getAsDouble();
        double minRate = Arrays.stream(ratesCur).min().getAsDouble();
        BigDecimal max = BigDecimal.valueOf(maxRate);
        BigDecimal min = BigDecimal.valueOf(minRate);
        return max.subtract(min);
    }

    public BigDecimal getAverage(double[] doubleArray) {
        BigDecimal observations = BigDecimal.valueOf(doubleArray.length);
        BigDecimal sum = BigDecimal.ZERO;
        for (double d : doubleArray) {
            BigDecimal bigD = BigDecimal.valueOf(d);
            sum = sum.add(bigD);
        }
        return sum.divide(observations, RoundingMode.HALF_UP);
    }

    public BigDecimal gaussianStandarization(double[] ratesCur, double rate) {
        BigDecimal average = getAverage(ratesCur);
        BigDecimal stDeviation = standardDeviation(ratesCur);
        BigDecimal numerator = BigDecimal.valueOf(rate).subtract(average);
        return numerator.divide(stDeviation, RoundingMode.HALF_UP);
    }
}
