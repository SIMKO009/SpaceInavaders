package sk.spsepo.spaceinvaders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void new_game(View view) {
        // switch to activity_space_invaders.xml with new game parameter
        Intent intent = new Intent(this, SpaceInvadersActivity.class);
        intent.putExtra("continue", false);
        startActivity(intent);
    }

    public void continue_last_save(View view) {
        // switch to activity_space_invaders.xml with continue game parameter
        Intent intent = new Intent(this, SpaceInvadersActivity.class);
        intent.putExtra("continue", true);
        startActivity(intent);
    }

    public void show_tutorial(View view) {
        Intent intent = new Intent(this, tutorial.class);
        startActivity(intent);
    }
}