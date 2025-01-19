package sk.spsepo.spaceinvaders;

import android.app.Activity;
import android.os.Bundle;

public class SpaceInvadersActivity extends Activity {

    private SpaceInvadersView spaceInvadersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the parameter from MainActivity
        boolean continueGame = getIntent().getBooleanExtra("continue", false);

        // Initialize SpaceInvadersView with the parameter
        spaceInvadersView = new SpaceInvadersView(this, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels, continueGame);
        setContentView(spaceInvadersView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        spaceInvadersView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        spaceInvadersView.resume();
    }
}