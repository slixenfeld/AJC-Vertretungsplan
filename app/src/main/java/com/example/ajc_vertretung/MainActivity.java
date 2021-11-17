package com.example.ajc_vertretung;

import static com.example.ajc_vertretung.Md5Utility.md5Java;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String Version = "1.5";

    WebView webView;
    TextView textView;
    Button helpButton;
    ImageButton editButton;
    EditText input_Klasse;
    EditText input_Password;
    EditText input_Username;
    ImageButton lastButton;
    ImageButton nextButton;

    String configKlasse = "";
    String configPassword = "";
    String configUsername = "";

    String passwordHash = "d074848ed641d3aaa124117b4860fcc3";
    String usernameHash = "c7600c41f46cc2b77bf5787ca97e0bab";

    InputOutput io = new InputOutput();
    Woche woche = new Woche();


    private boolean isLoginValid() throws NoSuchAlgorithmException {
        return (md5Java(configUsername).equals(usernameHash) && md5Java(configPassword).equals(passwordHash));
    }

    private void displayMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void load_Website() {
        loadConfig();

        if(woche.displayWoche_msg) {
            displayMsg( woche.getWocheString());
        }

        webView.loadUrl("https://ajc-bk.dyndns.org:8008/Vertretung-Online");
        try {
            String postData = "KL=1&klassen="+configKlasse+"&woche="+woche.getWoche();
            webView.postUrl("https://ajc-bk.dyndns.org:8008/Vertretung-Online/stdplan_anzeige.php",postData.getBytes());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.web);
        helpButton = (Button) findViewById(R.id.button);
        editButton = (ImageButton) findViewById(R.id.button2);
        lastButton = (ImageButton) findViewById(R.id.imageButton);
        nextButton = (ImageButton) findViewById(R.id.imageButton2);
        textView = (TextView) findViewById(R.id.textView_woche);

        helpButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        lastButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        loadConfig();
        try {
            if (isLoginValid()) {
                setup_WebView(webView);
                load_Website();
            } else {
                Toast.makeText(getApplicationContext(), "Falsche Login Daten",Toast.LENGTH_LONG).show();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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
                handler.proceed(configUsername, configPassword);
            }
        });
    }

    public void saveConfig(String username, String password, String klasse) {
        FileOutputStream fos = null;
        String configText = username + ";" + password + ";" + klasse;
        try {
            fos = openFileOutput(InputOutput.FILE_NAME, MODE_PRIVATE);
            fos.write(configText.getBytes());
        } catch (Exception ex){
            ex.printStackTrace(); } finally {
            if (fos != null) { try { fos.close();
            } catch (IOException e) { e.printStackTrace(); } } }
    }

    public void loadConfig() {
        FileInputStream fis = null;
        io.configLines.clear();
        try {
            fis = openFileInput(InputOutput.FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String text;
            while ((text = br.readLine()) != null) {
                sb.append(text);
            }
            String loaded = sb.toString();
            Collections.addAll(io.configLines, loaded.split(";"));

            configUsername = io.configLines.get(0);
            configPassword = io.configLines.get(1);
            configKlasse = io.configLines.get(2);

        } catch (Exception ex){
            ex.printStackTrace();
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

    private void saveInput() {
        configKlasse = input_Klasse.getText().toString();
        configUsername = input_Username.getText().toString();
        configPassword = input_Password.getText().toString();

        saveConfig(
                input_Username.getText().toString(),
                input_Password.getText().toString(),
                input_Klasse.getText().toString()
        );

        Toast.makeText(getApplicationContext(), configKlasse,Toast.LENGTH_LONG).show();
    }

    @SuppressLint("NonConstantResourceId")
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
                )).setCancelable(true);

                alert = a_builder.create();
                alert.show();

                ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

                break;

            case R.id.button2:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Bitte Login Daten und Klasse eingeben, Beispiel: WGY1b, ITA2b, IFIS1, ...");

                LinearLayout configInputLayout = new LinearLayout(this);
                configInputLayout.setOrientation(LinearLayout.VERTICAL);

                input_Username = new EditText(this);
                input_Username.setText(configUsername);
                input_Username.setWidth(400);
                input_Username.setHint("Benutzername");

                input_Password = new EditText(this);
                input_Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input_Password.setText(configPassword);
                input_Password.setWidth(400);
                input_Password.setHint("Passwort");

                input_Klasse = new EditText(this);
                input_Klasse.setText(configKlasse);
                input_Klasse.setWidth(400);
                input_Klasse.setHint("Klasse");

                configInputLayout.addView(input_Username);
                configInputLayout.addView(input_Password);
                configInputLayout.addView(input_Klasse);

                builder.setView(configInputLayout);

                builder.setPositiveButton("Speichern",new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            saveInput();

                            if (isLoginValid()) {
                                woche.displayWoche_msg = false;
                                setup_WebView(webView);
                                load_Website();
                                Toast.makeText(getApplicationContext(), configKlasse,Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Falsche Login Daten",Toast.LENGTH_LONG).show();
                            }
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                });

                alert = builder.create();
                alert.show();

                break;

            case R.id.imageButton:
                if(woche.button_woche!=-1) woche.button_woche--;
                woche.displayWoche_msg = true;
                textView.setText(woche.getWocheString());
                load_Website();
                break;

            case R.id.imageButton2:
                if(woche.button_woche!=1) woche.button_woche++;
                woche.displayWoche_msg = true;
                textView.setText(woche.getWocheString());
                load_Website();
                break;

        }
    }
}
