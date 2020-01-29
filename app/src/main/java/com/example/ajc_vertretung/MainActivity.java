package com.example.ajc_vertretung;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String FILE_NAME = "ajcv-config.txt";
    final String Version = "1.3.1";
    final String username = "";
    final String password = "";
    WebView webView;
    Button helpButton;
    Button editButton;
    EditText input;

    ImageButton lastButton;
    ImageButton nextButton;
    int button_woche = 0;
    boolean display_woche_msg = false;

    String user_Klasse = "";

    int woche;

    public void loadKlasseFromConfig() {
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text);
            }

            user_Klasse = sb.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveKlasseToConfig() {
        String text = input.getText().toString();
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(text.getBytes());

            input.getText().clear();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void readKlasseFromConfig() {
    String klasse = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("ajcv-config.txt"));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                String everything = sb.toString();
                klasse = everything;
            } finally {
                br.close();

            }
        }catch(Exception ex){}
        user_Klasse = klasse;
    }

    private void setup_WebView(WebView wv) {
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setAppCacheEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.setInitialScale(145);

        wv.setWebViewClient(new WebViewClient(){

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //SSL Zertifikat Warnung überspringen
                handler.proceed();
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                //Mit Logindaten anmelden
                handler.proceed(username, password);
            }
        });
    }


    private void load_Website(WebView wv) {
        loadKlasseFromConfig();

        Calendar cal = Calendar.getInstance();
        woche = cal.get(Calendar.WEEK_OF_YEAR);
        String woche_string = "";
        if (woche < 10) {
            woche_string = "0" + (woche+button_woche);
        }else{
            woche_string = ""+(woche+button_woche);
        }

        if(display_woche_msg) {
            if (button_woche == 1) {
                Toast.makeText(getApplicationContext(), "nächste Woche", Toast.LENGTH_SHORT).show();
            } else if (button_woche == -1) {
                Toast.makeText(getApplicationContext(), "letzte Woche", Toast.LENGTH_SHORT).show();
            } else if (button_woche == 0) {
                Toast.makeText(getApplicationContext(), "diese Woche", Toast.LENGTH_SHORT).show();
            } else if (button_woche < -1) {
                String vor_string = "";
                for (int i = 1; i < (-button_woche); i++) {
                    vor_string += "vor";
                }
                vor_string += "letzte Woche";
                Toast.makeText(getApplicationContext(), vor_string, Toast.LENGTH_SHORT).show();
            } else if (button_woche > 1) {
                String nach_string = "";
                for (int i = 1; i < button_woche; i++) {
                    nach_string += "über";
                }
                nach_string += "nächste Woche";
                Toast.makeText(getApplicationContext(), nach_string, Toast.LENGTH_SHORT).show();
            }
        }

        webView.loadUrl("https://ajc-bk.dyndns.org:8008/Vertretung-Online");
        try {
            String postData = "KL=1&klassen="+user_Klasse+"&woche="+woche_string;
            webView.postUrl("https://ajc-bk.dyndns.org:8008/Vertretung-Online/stdplan_anzeige.php",postData.getBytes());
        }catch(Exception e){}

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.web);
        helpButton = (Button) findViewById(R.id.button);
        editButton = (Button) findViewById(R.id.button2);
        lastButton = (ImageButton) findViewById(R.id.imageButton);
        nextButton = (ImageButton) findViewById(R.id.imageButton2);


        helpButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        lastButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);


        setup_WebView(webView);

        load_Website(webView);
    }

    @Override
    public void onClick(View v) {
        AlertDialog alert;
        switch(v.getId())
        {
            case R.id.button:
                AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
                a_builder.setTitle("App Info");
                a_builder.setMessage(Html.fromHtml("<html><body> " +
                                "Github: <a href=\"https://github.com/slxfld/AJC-Vertretungsplan\">slxfld/AJC-Vertretungsplan</a> " +
                                "<br/>" +
                                "Download: <a href=\"https://git.io/JvY3Y\">git.io/JvY3Y</a> </body></html>"+
                                "<br/>" +
                                "Version: "+Version
                ))


                        .setCancelable(true);

                alert = a_builder.create();
                alert.show();

                ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

                break;

            case R.id.button2:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Bitte Klasse eingeben, Beispiel: WGY1b, ITA2b, IFIS1, ...");

                input = new EditText(this);
                builder.setView(input);

                builder.setPositiveButton("Speichern",new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String klasse =  input.getText().toString();
                        saveKlasseToConfig();
                        Toast.makeText(getApplicationContext(),klasse,Toast.LENGTH_LONG).show();
                        display_woche_msg = false;
                        load_Website(webView);
                    }
                });

                alert = builder.create();
                alert.show();

                break;

            case R.id.imageButton:
                button_woche--;
                display_woche_msg = true;
                load_Website(webView);
                break;

            case R.id.imageButton2:

                button_woche++;
                display_woche_msg = true;
                load_Website(webView);
                break;

        }
    }
}
