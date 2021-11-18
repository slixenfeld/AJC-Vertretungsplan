package com.example.ajc_vertretung;
import java.util.Calendar;

public class Woche {

    public boolean displayWoche_msg = false;
    public int wocheWert = 0;
    public int aktuellewoche = 0;

    public Woche() {

    }

    public String getAktuellewoche() {
        Calendar cal = Calendar.getInstance();
        aktuellewoche = cal.get(Calendar.WEEK_OF_YEAR);
        String out = "";
        if (aktuellewoche < 10) {
            out = "0" + (aktuellewoche + wocheWert);
        }else{
            out = ""+(aktuellewoche + wocheWert);
        }
        return out;
    }

    public String getWocheString() {
        if (wocheWert == 1) {
            return "nächste Woche";
        } else if (wocheWert == -1) {
            return "letzte Woche";
        } else if (wocheWert == 0) {
            return "diese Woche";
        }
        return "";
    }
}
