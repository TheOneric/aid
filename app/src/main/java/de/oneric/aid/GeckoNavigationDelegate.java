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
        if(!config.limitURIs()
           || request.uri
                .replaceFirst("^https?://", "").replaceAll("(/.*)+$", "")
                .matches("[^/]*\\.?anime-on-demand.de$")
                //Why is [^/]* neccessary ? Does it always need to match _the whole_ string ?
        ) {
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
