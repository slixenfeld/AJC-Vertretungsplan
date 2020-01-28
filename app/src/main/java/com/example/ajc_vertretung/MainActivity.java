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
    WebView webView;
    Button helpButton;
    Button editButton;
    EditText input;

    final String Version = "1.1";
    final String username = "";
    final String password = "";

    String user_Klasse = "";

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
            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME,
                    Toast.LENGTH_LONG).show();
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.web);
        helpButton = (Button) findViewById(R.id.button);
        editButton = (Button) findViewById(R.id.button2);

        helpButton.setOnClickListener(this);
        editButton.setOnClickListener(this);

        loadKlasseFromConfig();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.setWebViewClient(new WebViewClient(){

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


        Calendar cal = Calendar.getInstance();
        int woche = cal.get(Calendar.WEEK_OF_YEAR);
        String woche_string = "";
        if (woche < 10) {
            woche_string = "0" + woche;
        }else{
            woche_string = ""+woche;
        }

        Toast.makeText(getApplicationContext(),user_Klasse,Toast.LENGTH_LONG).show();

        webView.loadUrl("https://ajc-bk.dyndns.org:8008/Vertretung-Online");
        try {
            String postData = "KL=1&klassen="+user_Klasse+"&woche="+woche_string;
         webView.postUrl("https://ajc-bk.dyndns.org:8008/Vertretung-Online/stdplan_anzeige.php",postData.getBytes());
        }catch(Exception e){}


    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.button:
                AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
                a_builder.setMessage(Html.fromHtml("Github: <a href=\"https://github.com/slxfld/AJC-Vertretungsplan\">slxfld/AJC-Vertretungsplan</a>" +
                        "\n" +
                        "Version: "+Version))


                        .setCancelable(true);

                AlertDialog alert = a_builder.create();
                alert.show();

                ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                break;

            case R.id.button2:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
              //  builder.setTitle("Klasse");
                builder.setMessage("Bitte Klasse eingeben:");

                input = new EditText(this);
                builder.setView(input);

                builder.setPositiveButton("Speichern",new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String klasse =  input.getText().toString();
                        saveKlasseToConfig();
                        Toast.makeText(getApplicationContext(),klasse,Toast.LENGTH_LONG).show();
                        System.exit(0);
                    }
                });

                AlertDialog ad = builder.create();

                ad.show();

                break;

        }
    }
}
