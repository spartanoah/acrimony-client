/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl.data;

import com.ibm.icu.util.Holiday;
import com.ibm.icu.util.SimpleHoliday;
import java.util.ListResourceBundle;

public class HolidayBundle_es_MX
extends ListResourceBundle {
    private static final Holiday[] fHolidays = new Holiday[]{SimpleHoliday.NEW_YEARS_DAY, new SimpleHoliday(1, 5, 0, "Constitution Day"), new SimpleHoliday(2, 21, 0, "Benito Ju\u00e1rez Day"), SimpleHoliday.MAY_DAY, new SimpleHoliday(4, 5, 0, "Cinco de Mayo"), new SimpleHoliday(5, 1, 0, "Navy Day"), new SimpleHoliday(8, 16, 0, "Independence Day"), new SimpleHoliday(9, 12, 0, "D\u00eda de la Raza"), SimpleHoliday.ALL_SAINTS_DAY, new SimpleHoliday(10, 2, 0, "Day of the Dead"), new SimpleHoliday(10, 20, 0, "Revolution Day"), new SimpleHoliday(11, 12, 0, "Flag Day"), SimpleHoliday.CHRISTMAS};
    private static final Object[][] fContents = new Object[][]{{"holidays", fHolidays}};

    public synchronized Object[][] getContents() {
        return fContents;
    }
}

