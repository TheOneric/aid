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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoSession;

/**
 * Can deal with everything regarding URL navigation.
 * Currently only used to limit domains to anime-on-demand.de; interface has many more callbacks.
 */
public class GeckoNavigationDelegate implements GeckoSession.NavigationDelegate {

    private final Config config;
    private final Activity mainAct;

    public GeckoNavigationDelegate(GeckoActivity gact) {
        this.config = gact.getConfig();
        this.mainAct = gact;
    }

    public GeckoResult<AllowOrDeny> onLoadRequest(@NonNull GeckoSession session,
                                                  @NonNull LoadRequest  request)
    {
        final String TAG = "core/uricontrol";

        // Only open "anime-on-demand.de" sites in-app
        if(!config.limitURIs() || Util.isAoDUri(request.uri)) {
            return GeckoResult.fromValue(AllowOrDeny.ALLOW);
        } else {
            Log.d(TAG, "Deny non-aod uri: ["+request.uri+"]");
            mainAct.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(request.uri))
            );
            return GeckoResult.fromValue(AllowOrDeny.DENY);
        }
    }

}
