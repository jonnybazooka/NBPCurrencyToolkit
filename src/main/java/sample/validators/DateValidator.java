package sample.validators;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DateValidator {

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
        }
        String[] pair = new String[2];
        pair[0] = localStartDate.toString();
        pair[1] = localEndDate.toString();
        datePairs.add(pair);
        return datePairs;
    }
}
