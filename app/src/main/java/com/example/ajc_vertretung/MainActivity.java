package com.example.ajc_vertretung;

import static com.example.ajc_vertretung.Md5Utility.md5Java;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String Version = "1.5.1";

    private WebView webView;
    private TextView textView;
    protected Button helpButton;

    private EditText inputField_Klasse;
    private EditText inputField_Password;
    private EditText inputField_Username;

    private String configKlasse = "";
    private String configPassword = "";
    private String configUsername = "";

    public final String requestURL = "https://ajc-bk.dyndns.org:8008/Vertretung-Online/stdplan_anzeige.php";

    public static final String CONFIG_FILE_NAME = "ajcv-config.txt";
    public ArrayList<String> configLines = new ArrayList<>();

    Woche woche = new Woche();

    private boolean isLoginValid() throws NoSuchAlgorithmException {
        String passwordHash = "d074848ed641d3aaa124117b4860fcc3";
        String usernameHash = "c7600c41f46cc2b77bf5787ca97e0bab";
        return (md5Java(configUsername).equals(usernameHash) && md5Java(configPassword).equals(passwordHash));
    }

    private void displayMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void displayNotification(String title, String message) {
        @SuppressLint({"WrongConstant", "UnspecifiedImmutableFlag"})
        PendingIntent pi = PendingIntent.getActivity(this,
                0,
                new Intent(this, MainActivity.class),
                Intent.FLAG_ACTIVITY_NEW_TASK);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(0, new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true).setContentIntent(pi).build());
    }

    private void load_Website() {

        loadConfig();

        if(woche.displayWoche_msg) displayMsg(configKlasse + ", " + woche.getWocheString());

        try {
            String requestPayload = "KL=1&klassen="+configKlasse+"&woche="+woche.getAktuellewoche();
            webView.postUrl(requestURL, requestPayload.getBytes());
        } catch(Exception e) {
            Log.e("Request Error", e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.web);
        helpButton = (Button) findViewById(R.id.button);
        ImageButton editButton = (ImageButton) findViewById(R.id.button2);
        ImageButton lastButton = (ImageButton) findViewById(R.id.imageButton);
        ImageButton nextButton = (ImageButton) findViewById(R.id.imageButton2);
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
                displayMsg("Falsche Login Daten");
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
                //Mit Login Daten anmelden
                handler.proceed(configUsername, configPassword);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // URL Filter für HTML Processing
               webView.loadUrl("javascript:HTMLOUT.processHTML(document.documentElement.outerHTML);");
            }

        });
    }

    public void saveConfig(String username, String password, String klasse) {
        FileOutputStream fos = null;
        String configText = username + ";" + password + ";" + klasse;
        try {
            fos = openFileOutput(CONFIG_FILE_NAME, MODE_PRIVATE);
            fos.write(configText.getBytes());
        } catch (Exception ex){
            ex.printStackTrace(); } finally {
            if (fos != null) { try { fos.close();
            } catch (IOException e) { e.printStackTrace(); } } }
    }

    public void loadConfig() {
        FileInputStream fis = null;
        configLines.clear();
        try {
            fis = openFileInput(CONFIG_FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String text;
            while ((text = br.readLine()) != null) {
                sb.append(text);
            }
            String loaded = sb.toString();
            Collections.addAll(configLines, loaded.split(";"));

            configUsername = configLines.get(0);
            configPassword = configLines.get(1);
            configKlasse = configLines.get(2);

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

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void setWebViewConfig(WebView wv) {
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setAppCacheEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.addJavascriptInterface(new HTMLProcessorInterface(), "HTMLOUT");

        wv.setInitialScale(145);
    }

    private void saveInput() {
        configKlasse = inputField_Klasse.getText().toString();
        configUsername = inputField_Username.getText().toString();
        configPassword = inputField_Password.getText().toString();

        saveConfig(
                inputField_Username.getText().toString(),
                inputField_Password.getText().toString(),
                inputField_Klasse.getText().toString()
        );
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
                                "Version: "+Version +
                                "<p></p>" +
                                "Die Login Daten befinden sich auf einem Zettel hinter der Glasscheibe im Schulgebäude 1."
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

                inputField_Username = new EditText(this);
                inputField_Username.setText(configUsername);
                inputField_Username.setWidth(400);
                inputField_Username.setHint("Benutzername");

                inputField_Password = new EditText(this);
                inputField_Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputField_Password.setText(configPassword);
                inputField_Password.setWidth(400);
                inputField_Password.setHint("Passwort");

                inputField_Klasse = new EditText(this);
                inputField_Klasse.setText(configKlasse);
                inputField_Klasse.setWidth(400);
                inputField_Klasse.setHint("Klasse");

                configInputLayout.addView(inputField_Username);
                configInputLayout.addView(inputField_Password);
                configInputLayout.addView(inputField_Klasse);

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
                            } else {
                                displayMsg("Falsche Login Daten");
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
                if(woche.wocheWert !=-1) woche.wocheWert--;
                woche.displayWoche_msg = true;
                textView.setText(woche.getWocheString());
                load_Website();
                break;

            case R.id.imageButton2:
                if(woche.wocheWert !=1) woche.wocheWert++;
                woche.displayWoche_msg = true;
                textView.setText(woche.getWocheString());
                load_Website();
                break;

        }
    }
}
