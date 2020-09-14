/*
    This file is part of AiD.

    AiD is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AiD is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AiD.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2020  Oneric
*/
package de.oneric.aid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;


public class GeckoActivity extends AppCompatActivity {

    private static final String TAG = "core/activity";

    /**
     * This app is single-task anyway and the codebase will always remain
     * rather small, so I just let everything be global state singletons, lol.
     * (at least these assumptions hold as long as I'm the unpaid maintainer
     *   - if you forked this to add a bunch of shiny new features you should
     *   consider taking a look at which parts of the code need access t what
     *   and split things up accordingly to help keep it tidy)
     */
    private GeckoView geckoView = null;
    private GeckoRuntime geckoRuntime = null;
    private GeckoSession geckoSession = null;
    private Config config = null;

    public GeckoRuntime getRuntime() { return geckoRuntime; }
    public GeckoSession getSession() { return geckoSession; }
    public GeckoView    getView()    { return geckoView;    }
    public Config       getConfig()  { return config;       }


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

    //TODO: Limit GeckoView to anime-on-demand.de, using:
    //GeckoSession.NavigationDelegate.html#onLoadRequest


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        config = new Config(this.getApplicationContext());

        //Init GeckoView
        geckoView = findViewById(R.id.geckoview);
        geckoSession = new GeckoSession();
        if(geckoRuntime == null)
            geckoRuntime = GeckoRuntime.create(this);
        setUpGecko(geckoRuntime, geckoSession);

        geckoSession.loadUri("https://"+Util.DOMAIN_AOD+"/");

        hideControl.onSystemUiVisibilityChange(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(hideControl);
    }


    private void setUpGecko(GeckoRuntime runtime, GeckoSession session) {
        final String TAG = "core/setUp";

        session.setPromptDelegate(new GeckoPromptHandler(this));
        session.setNavigationDelegate(new GeckoNavigationDelegate(this));
        runtime.setLoginStorageDelegate(new GeckoAutofillHandler(this.config));

        GeckoRuntimeSettings settings = geckoRuntime.getSettings();
        settings.setRemoteDebuggingEnabled(true);
        settings.setLoginAutofillEnabled(true);


        GeckoResult<WebExtension> res = geckoRuntime.getWebExtensionController().ensureBuiltIn(
                "resource://android/assets/aod-touchable/",
                "aod-touchable@oneric.stub"
        );
        res.then(we -> {
            Log.d(TAG, "Successfully Installed: "+we.id);
            return GeckoResult.fromValue(0);
        }, ex -> {
            Log.e(TAG, "Webextension installation failed: " + ex.getMessage());
            return GeckoResult.fromValue(0);
        });

        geckoSession.open(geckoRuntime);
        geckoView.setSession(geckoSession);
        geckoView.setAutofillEnabled(true);
    }


}
