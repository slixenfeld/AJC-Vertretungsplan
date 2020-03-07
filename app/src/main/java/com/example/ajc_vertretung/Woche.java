package com.example.ajc_vertretung;
import java.util.Calendar;

public class Woche {

    public boolean displayWoche_msg = false;
    public int button_woche = 0;
    public int woche = 0;

    public Woche() {}

    public String getWoche() {
        Calendar cal = Calendar.getInstance();
        woche = cal.get(Calendar.WEEK_OF_YEAR);
        String out = "";
        if (woche < 10) {
            out = "0" + (woche+button_woche);
        }else{
            out = ""+(woche+button_woche);
        }
        return out;
    }

    public String getWocheString() {
        if (button_woche == 1) {
            return "nächste Woche";
        } else if (button_woche == -1) {
            return "letzte Woche";
        } else if (button_woche == 0) {
            return "diese Woche";
        }
        return "";
    }
}
