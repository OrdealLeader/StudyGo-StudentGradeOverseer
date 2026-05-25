package com.example.studygo_studentgradeoverseer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.studygo_studentgradeoverseer.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        
        // Define bottom navigation tabs as top-level destinations to hide the back arrow
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboard,
                R.id.courseInputFragment, 
                R.id.SimulatorFragment, 
                R.id.ResultsFragment, 
                R.id.SettingsFragment)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener(((controller, destination, arguments) ->{
            int id = destination.getId();
            if (id == R.id.FirstFragment || 
                id == R.id.taskInputFragment || 
                id == R.id.taskEditFragment || 
                id == R.id.courseDetailFragment) {
                navView.setVisibility(View.GONE);
            } else {
                navView.setVisibility(View.VISIBLE);
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


}