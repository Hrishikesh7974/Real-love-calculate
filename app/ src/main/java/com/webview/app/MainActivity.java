package com.webview.app;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.*;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    InterstitialAd mInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this);

        loadInterstitial();

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JSInterface(), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectButtonListener();
            }
        });

        webView.loadUrl("https://real-love-calculator.github.io");
    }

    private void loadInterstitial() {
        InterstitialAd.load(this,
                "ca-app-pub-3940256099942544/1033173712",
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        mInterstitial = ad;
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError e) {
                        mInterstitial = null;
                    }
                });
    }

    private void injectButtonListener() {
        String js = "javascript:(function(){var b=document.getElementsByTagName('button');"
                + "for(var i=0;i<b.length;i++){if(b[i].innerText.includes('Calculate')){"
                + "b[i].onclick=function(){Android.showAd();return false;}}}})();";
        webView.evaluateJavascript(js, null);
    }

    public class JSInterface {
        @JavascriptInterface
        public void showAd() {
            runOnUiThread(() -> {
                if (mInterstitial != null) {
                    mInterstitial.show(MainActivity.this);
                    mInterstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override public void onAdDismissedFullScreenContent() {
                            loadInterstitial();
                            webView.evaluateJavascript("javascript:calculateLove()", null);
                        }
                    });
                } else {
                    webView.evaluateJavascript("javascript:calculateLove()", null);
                    loadInterstitial();
                }
            });
        }
    }
}
