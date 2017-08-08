package org.zkoss.essentials.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by Sam on 26-Jul-17.
 */
public class CurrencyUtil {

    private static final String CURRENCY_DECIMAL_FORMAT_STR = "####,###,###.00";

    private static final DecimalFormat currencyDecimalFormat = new DecimalFormat();

    public static String getCurrencyFormat(BigDecimal dollars) {
        if ( dollars == null ) return "";
        DecimalFormat currencyDecimalFormat = new DecimalFormat( CURRENCY_DECIMAL_FORMAT_STR );
        return currencyDecimalFormat.format( dollars );
    }
}
