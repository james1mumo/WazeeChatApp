package com.dekut.wazeechatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dekut.wazeechatapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkPhoneNumber();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as  a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void checkPhoneNumber(){
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        String phone = prefs.getString("phone", "");


        if (phone.isEmpty()){
            getPhoneNumber();
        }


    }

    private void getPhoneNumber(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Phone Number");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String phone = input.getText().toString();
            SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
            editor.putString("phone", phone);
            editor.apply();

            startActivity(new Intent(getBaseContext(), MainActivity.class));
            finish();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            checkPhoneNumber();
        });

        builder.setCancelable(false);
        builder.show();
    }
}