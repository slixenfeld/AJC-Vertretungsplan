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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String Version = "1.4";
    final String username = "";
    final String password = "";

    WebView webView;
    TextView textView;
    Button helpButton;
    Button editButton;
    EditText input;
    ImageButton lastButton;
    ImageButton nextButton;

    InputOutput io = new InputOutput();
    Woche woche = new Woche();


    private void displayMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void load_Website(WebView wv) {

        loadKlasse();

        if(woche.displayWoche_msg) {
            displayMsg( woche.getWocheString());
        }

        webView.loadUrl("https://ajc-bk.dyndns.org:8008/Vertretung-Online");
        try {
            String postData = "KL=1&klassen="+io.configKlasse+"&woche="+woche.getWoche();
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
        textView = (TextView) findViewById(R.id.textView_woche);

        helpButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        lastButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        setup_WebView(webView);
        load_Website(webView);
    }

    private void setup_WebView(WebView wv) {
        setWebViewConfig(wv);
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

    public void loadKlasse() {
        FileInputStream fis = null;
        String out = "";
        try {
            fis = openFileInput(io.FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String text;
            while ((text = br.readLine()) != null)
                sb.append(text);
            out = sb.toString();

        } catch (Exception ex){ } finally { if (fis != null) {
            try { fis.close(); } catch (IOException e) {
                e.printStackTrace(); } } }
        io.configKlasse = out;
    }

    public void saveKlasse( String klasse) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(io.FILE_NAME, MODE_PRIVATE);
            fos.write(klasse.getBytes());
        } catch (Exception ex){
            ex.printStackTrace(); } finally {
            if (fos != null) { try { fos.close();
            } catch (IOException e) { e.printStackTrace(); } } }
    }

    private void setWebViewConfig(WebView wv) {
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setAppCacheEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setUseWideViewPort(true);

        wv.setInitialScale(145);
    }

    private void saveKlasseToConfig() {
        String klasseText = input.getText().toString();
        saveKlasse(klasseText);
        input.getText().clear();
        Toast.makeText(getApplicationContext(),klasseText,Toast.LENGTH_LONG).show();
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
                        saveKlasseToConfig();
                        woche.displayWoche_msg = false;
                        load_Website(webView);
                    }
                });

                alert = builder.create();
                alert.show();

                break;

            case R.id.imageButton:
                if(woche.button_woche!=-1) woche.button_woche--;
                woche.displayWoche_msg = true;
                textView.setText(woche.getWocheString());
                load_Website(webView);
                break;

            case R.id.imageButton2:
                if(woche.button_woche!=1) woche.button_woche++;
                woche.displayWoche_msg = true;
                textView.setText(woche.getWocheString());
                load_Website(webView);
                break;

        }
    }
}
