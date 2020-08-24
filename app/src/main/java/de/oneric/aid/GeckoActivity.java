package de.oneric.aid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;


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

        session.open(geckoRuntime);
        geckoView.setSession(session);
        session.loadUri("https://anime-on-demand.de/");

        hideControl.onSystemUiVisibilityChange(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(hideControl);
    }


}
