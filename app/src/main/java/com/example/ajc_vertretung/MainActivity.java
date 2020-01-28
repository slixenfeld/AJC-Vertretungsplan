package com.example.ajc_vertretung;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
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
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    WebView webView;
    Button helpButton;

    final String Version = "1.1";
    final String username = "Benutzername";
    final String password = "Passwort";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.web);
        helpButton = (Button) findViewById(R.id.button);

        helpButton.setOnClickListener(this);

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

        webView.loadUrl("https://ajc-bk.dyndns.org:8008/Vertretung-Online");
    }

    @Override
    public void onClick(View v) {

        AlertDialog.Builder a_builder = new AlertDialog.Builder(this);
        a_builder.setMessage(Html.fromHtml("Github: <a href=\"https://github.com/slxfld/AJC-Vertretungsplan\">slxfld/AJC-Vertretungsplan</a>" +
                "\n" +
                "Version: "+Version  ))
                .setCancelable(true);

        AlertDialog alert = a_builder.create();
        alert.show();

        ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

    }

}
