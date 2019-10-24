package tadeas_musil.tv_series_tracker.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DateUtils {

    private static String timezone;

    public static LocalDate getCurrentDate() {
        return LocalDate.now(ZoneId.of(timezone));
    }

    public static Period getAge(LocalDate date) {
        return Period.between(date, getCurrentDate());
    }

    @Value("${app.timezone}")
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

}