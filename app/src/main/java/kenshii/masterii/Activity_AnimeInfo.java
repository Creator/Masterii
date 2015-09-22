package kenshii.masterii;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;

import java.lang.reflect.Method;

public class Activity_AnimeInfo extends AppCompatActivity {
    SharedPreferences savedCookies;
    AutoCompleteTextView userEmail;
    EditText userPass;
    CheckBox userRemember;
    Method method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anime_info);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        //Toolbar will now take on default Action Bar characteristics
        setSupportActionBar(toolbar);
    }
}
