package crm.ui;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Форматирование значений без изменения внешнего вида компонентов.
 */
public final class Format {

    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU"));
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Format() {
    }

    public static String money(double value) {
        return MONEY.format(value) + " BYN";
    }

    public static String date(LocalDate value) {
        return value == null ? "—" : DATE.format(value);
    }

    public static String plural(long count, String one, String few, String many) {
        long mod100 = count % 100;
        long mod10 = count % 10;
        if (mod100 >= 11 && mod100 <= 14) {
            return many;
        }
        if (mod10 == 1) {
            return one;
        }
        if (mod10 >= 2 && mod10 <= 4) {
            return few;
        }
        return many;
    }
}
