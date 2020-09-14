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

    private final SharedPreferences sprefs;

    // All default config values (where applicable)
    private static final boolean DEF_SAVE_PW = false;
    private static final boolean DEF_URILIMIT = true;

    // All keys (good thing at least the variable names make sense)
    private static final String KEY_SAVE_PW  = "nsamode";
    private static final String KEY_USER     = "okyakusama";
    private static final String KEY_PW       = "likelyinsufficient";
    private static final String KEY_URILIMIT = "stayfocused";

    public Config(Context context) {
        this.sprefs = context.getSharedPreferences("config", Context.MODE_PRIVATE);
    }


    public boolean rememberLogin() {
        return sprefs.getBoolean(KEY_SAVE_PW, DEF_SAVE_PW);
    }

    public boolean limitURIs() {
        return sprefs.getBoolean(KEY_URILIMIT, DEF_URILIMIT);
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

    public void storeLimitURIs(boolean b) {
        edit(e -> e.putBoolean(KEY_URILIMIT, b), false);
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
