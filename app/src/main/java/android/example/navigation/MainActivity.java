package android.example.navigation;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.example.navigation.databinding.ActivityMainBinding;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean weatherButton = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ////////////////////navigation Bar////////////////////

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        ////////////////////navigation Bar////////////////////

        TextView weather_text = findViewById(R.id.weather_info);
        TextView location_text = findViewById(R.id.location_info);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder weather_display = new StringBuilder();
                StringBuilder location_display = new StringBuilder();
                if(weatherButton){
                    weather_display.append("天気:");
                    weather_display.append("晴れ");
                    location_display.append("緯度:");
                    location_display.append(42);
                    location_display.append("  ");
                    location_display.append("経度:");
                    location_display.append(54);
                    weather_text.setText(weather_display.toString());
                    location_text.setText(location_display.toString());
                    weatherButton = false;
                }else{
                    weather_text.setText("");
                    location_text.setText("");
                    weatherButton = true;
                }
            }
        });
    }



}