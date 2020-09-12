package de.oneric.aid;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A data obejct that holds information about the
 * app configuration as chosen by the user.
 * Only really makes sense to be created through the
 * static 'readConfiguration()'.
 *
 * There are methods for all regular settings.
 *
 * Since this app is single-task anyway this is used like a singleton:
 * a reference to one instance is kept in the main activity.
 */
public class Config {

    // It seemed a bit silly to create an own class for these few static values,
    // and since there was no better place, I üut them here.
    public static final String DOMAIN_AOD  = "anime-on-demand.de";


    // Now for the actual Config stuff
    private final SharedPreferences sprefs;

    // All default config values (where applicable)
    private static final boolean DEF_SAVE_PW = false;

    // All keys (good thing at least the variable names make sense)
    private static final String KEY_SAVE_PW = "nsamode";
    private static final String KEY_USER    = "okyakusama";
    private static final String KEY_PW      = "likelyinsufficient";

    public Config(Context context) {
        this.sprefs = context.getSharedPreferences("config", Context.MODE_PRIVATE);
    }


    public boolean rememberLogin() {
        return sprefs.getBoolean(KEY_SAVE_PW, DEF_SAVE_PW);
    }

    public String[] retrieveCredentials() {
        return new String[] {
            sprefs.getString(KEY_USER, ""),
            sprefs.getString(KEY_PW, "")
        };
    }


    // Apparently SDK version 19 doesn't yet support Java 8 default LambdaInterfaces,
    // so let's define our own instead of Consumer<SharedPreferences.Editor>
    private interface ConfigWriter {
        public void writeChanges(SharedPreferences.Editor editor);
    }

    private boolean edit(ConfigWriter writer, boolean syn) {
        SharedPreferences.Editor editor = sprefs.edit();
        writer.writeChanges(editor);
        if(syn)
            return editor.commit();
        editor.apply();
        return true;
    }

    public void storeRememberLogin(boolean b) {
        edit(e -> e.putBoolean(KEY_SAVE_PW, b), false);
    }

    public void storeCredentials(String u, String p) {
        edit(e -> {
            e.putString(KEY_USER, u);
            e.putString(KEY_PW, p);
        }, false);
    }

    /**
     * Purge all credential information from memory and disk.
     * @return false if an IO error occured; credentials may still be on disk.
     */
    public boolean nukeCredentials() {
        return edit(e -> {
            e.remove(KEY_USER);
            e.remove(KEY_PW);
        }, true);
    }



}
