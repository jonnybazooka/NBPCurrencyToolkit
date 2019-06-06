package sample.validators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DateValidator {
    private static final Logger LOGGER = LogManager.getLogger(DateValidator.class.getName());

    public List<String[]> getStartEndDates(String startDate, String endDate) {
        List<String[]> datePairs = new ArrayList<>();
        LocalDate localStartDate = LocalDate.parse(startDate);
        LocalDate localEndDate = LocalDate.parse(endDate);
        long diff = ChronoUnit.DAYS.between(localStartDate, localEndDate);
        while (diff > 365) {
            String[] pair = new String[2];
            pair[0] = localStartDate.toString();
            pair[1] = LocalDate.from(localStartDate).plusDays(365).toString();
            datePairs.add(pair);
            localStartDate = LocalDate.from(localStartDate).plusDays(366);
            diff = ChronoUnit.DAYS.between(localStartDate, localEndDate);
            LOGGER.debug("Creating date pair: " + pair[0] + " , " + pair[1]);
        }
        String[] pair = new String[2];
        pair[0] = localStartDate.toString();
        pair[1] = localEndDate.toString();
        datePairs.add(pair);
        LOGGER.debug("Creating date pair: " + pair[0] + " , " + pair[1]);
        return datePairs;
    }
}
