package de.oneric.aid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;


public class GeckoActivity extends AppCompatActivity {

    private GeckoView geckoView = null;
    private GeckoRuntime geckoRuntime = null;

    View.OnSystemUiVisibilityChangeListener hideControl = (int vis) -> {
        if((vis & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0) {
            //Not visible; hide on-screen buttons again
            geckoView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        //Init GeckoView
        geckoView = findViewById(R.id.geckoview);
        GeckoSession session = new GeckoSession();
        if(geckoRuntime == null)
            geckoRuntime = GeckoRuntime.create(this);
        setUpGecko(geckoRuntime, session);

        session.open(geckoRuntime);
        geckoView.setSession(session);
        session.loadUri("https://anime-on-demand.de/");

        hideControl.onSystemUiVisibilityChange(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(hideControl);
    }


    private void setUpGecko(GeckoRuntime runtime, GeckoSession session) {
        final String LOG_TAG = "setUpGecko";

        geckoRuntime.getSettings().setRemoteDebuggingEnabled(true);
        geckoRuntime.getSettings().setLoginAutofillEnabled(true);
        GeckoResult<WebExtension> res = geckoRuntime.getWebExtensionController().ensureBuiltIn(
                "resource://android/assets/aod-touchable/",
                "aod-touchable@oneric.stub"
        );
        res.then(we -> {
            Log.d(LOG_TAG, "Successfully Installed: "+we.id);
            return GeckoResult.fromValue(0);
        }, ex -> {
            Log.e(LOG_TAG, "Webextension installation failed: " + ex.getMessage());
            return GeckoResult.fromValue(0);
        });
    }


}
