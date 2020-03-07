package com.example.ajc_vertretung;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputOutput {

    public static final String FILE_NAME = "ajcv-config.txt";
    public String configKlasse = "";

    public InputOutput() {

    }

    public String getConfigKlasse() {
        return configKlasse;
    }

    public void setConfigKlasse(String configKlasse) {
        this.configKlasse = configKlasse;
    }
}
