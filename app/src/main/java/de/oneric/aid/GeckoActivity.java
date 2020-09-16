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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        config = new Config(this.getApplicationContext());

        // It's a lot of stuff for such a simple GUI
        // Inits geckoView
        setUpGUIShit();

        //Init GeckoView
        geckoSession = new GeckoSession();
        if(geckoRuntime == null)
            geckoRuntime = GeckoRuntime.create(this);
        setUpGecko(geckoRuntime, geckoSession);

        geckoSession.loadUri("https://"+Util.DOMAIN_AOD+"/");
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


    // ------------  GUI STUFF BELOW, YOU'VE BEEN WARNED -------------------- //

    private void setUpGUIShit() {
        setContentView(R.layout.activity_fullscreen);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        geckoView = findViewById(R.id.geckoview);

        // Let controls pop in when needed and default to being hidden
        onSysUIVisibilityChange(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this::onSysUIVisibilityChange);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.aidmenu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.login_toggle).setChecked(config.rememberLogin());
        menu.findItem(R.id.uri_toggle).setChecked(config.limitURIs());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.go_immersive:
                onSysUIVisibilityChange(View.SYSTEM_UI_FLAG_FULLSCREEN);
                break;
            case R.id.login_toggle:
                item.setChecked(!item.isChecked());
                config.storeRememberLogin(item.isChecked());
                break;
            case R.id.login_clear_action:
                if(config.nukeCredentials()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Logins gelöscht")
                            .setMessage("Erfolgreich Logindaten von Dateisystem gelöscht.\n"
                                        + "Die Gecko-Sitzung kann uU die Logins noch im Cache "
                                        + "behalten haben. In dem Fall bitte die App beenden und in "
                                        + "den Einstellungen den App-Cache leeren, "
                                        + "oder das Gerät neustarten.")
                            .setPositiveButton("Ok", null)
                        .create().show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Fehlschlag")
                            .setMessage("Konnte Logindaten aufgrund eines IO-Errors nicht löschen!"
                                        + "\nVersuche es erneut, bei wiederholten Fehlschlägen auch"
                                        + "nach Gerätneustart, können die Daten notfalls durch"
                                        + "Deinstallation der App gelöscht werden.")
                            .setNeutralButton("Ok", null)
                        .create().show();
                }
                this.getCacheDir().delete();
                break;
            case R.id.uri_toggle:
                item.setChecked(!item.isChecked());
                config.storeLimitURIs(item.isChecked());
                break;
            default:
                Log.e(TAG, "Option Menu action not yet implemented! ["
                        + item.getItemId() + "; "
                        + item.getTitle()  + "]");
                return false;
        }

        return true;
    }

    private void onSysUIVisibilityChange(int vis) {
        if((vis & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0) {
            //Not visible; hide on-screen buttons again
            geckoView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }
    }

}
