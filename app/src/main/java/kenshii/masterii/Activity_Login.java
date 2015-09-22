package kenshii.masterii;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Activity_Login extends AppCompatActivity {
    SharedPreferences savedCookies;
    AutoCompleteTextView userEmail;
    EditText userPass;
    CheckBox userRemember;
    Callback loginCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException e) {
            backgroundShortToast("Login failed with exception: " + e.getMessage());
        }

        @Override
        public void onResponse(Response response) {
            if (response.code()!=200) {
                backgroundShortToast("Login failed with error code: " + Integer.toString(response.code()));
                return;
            }
            try {
                String[] cookieSplit = new String[0];
                JSONObject responseJSON = new JSONObject(response.body().string());
                handleLogin_Attempt(response.headers("Set-Cookie"), responseJSON);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void backgroundShortToast(final String msg) {
        final Context context = Activity_Login.this;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void backgroundAlertDialog(final String msg) {
        final Context context = Activity_Login.this;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                alertDialog.show();
            }
        });
    }

    public void handleLogin_Attempt(List<String> responseCookies, JSONObject responseJSON) {
        String[] cookieSplit = null;
        for (String cookie : responseCookies) {
            if (cookie.contains("remember_") && userRemember.isChecked()) {
                cookieSplit = cookie.split(";")[0].split("=");
                SharedPreferences.Editor cookieEditor = savedCookies.edit();
                cookieEditor.putString("loginCookie_name", cookieSplit[0]);
                cookieEditor.putString("loginCookie_value", cookieSplit[1]);
                cookieEditor.commit();
            } else if (cookie.contains("laravel_session") && !userRemember.isChecked()) {
                cookieSplit = cookie.split(";")[0].split("=");
            }
        }
        try {
            if (responseJSON.getJSONObject("data").getBoolean("logged_in")) //Login succeeded.
                handleLogin_Success(cookieSplit);
            else
                handleLogin_Failure(responseJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void handleLogin_Success(String[] cookieSplit) {
        backgroundShortToast("Successfully logged in.");
        Intent intent = new Intent(Activity_Login.this, Activity_Main.class);
        intent.putExtra("loginCookie_name", cookieSplit[0]);
        intent.putExtra("loginCookie_value", cookieSplit[1]);
        startActivity(intent);
        finish();
    }

    public void handleLogin_Failure(JSONObject responseJSON) {
        try {
            JSONArray errorsArray = responseJSON.getJSONObject("data").getJSONArray("errors");
            String strProblems = "The following error"
                    + ((errorsArray.length() > 1) ? "s" : "")
                    + " occurred while attempting to log you in:\n\n";
            for ( int i = 0; i < errorsArray.length(); i++ ) {
                strProblems += (errorsArray.getString(i) + "\n");
            }
            backgroundAlertDialog(strProblems);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userEmail = (AutoCompleteTextView)findViewById(R.id.login_email);
        userPass = (EditText)findViewById(R.id.login_password);
        userRemember = (CheckBox)findViewById(R.id.login_remember);
        savedCookies = getSharedPreferences("savedCookies", Context.MODE_PRIVATE);
        if (savedCookies.contains("loginCookie_name")) {
            Intent intent = new Intent(Activity_Login.this, Activity_Main.class);
            intent.putExtra("loginCookie_name", savedCookies.getString("loginCookie_name",null));
            intent.putExtra("loginCookie_value", savedCookies.getString("loginCookie_value",null));
            startActivity(intent);
            finish();
        }
        else {
            Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    public void onLoginClick(View view) {
        if (userEmail.getText().length() == 0 && userPass.getText().length() == 0) {
            String strProblems = "The following error"
                    + ((userEmail.getText().length() == 0 && userPass.getText().length() == 0)?"s":"")
                    + " occurred while attempting to log you in:\n\n";
            if (userEmail.getText().length() == 0)
                strProblems += "The email field is required.\n";
            if (userPass.getText().length() == 0)
                strProblems += "The password field is required.\n";
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(strProblems)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody loginAttempt_Body = new FormEncodingBuilder()
                .add("email", userEmail.getText().toString())
                .add("password", userPass.getText().toString())
                .add("remember", (userRemember.isChecked() ? "on" : ""))
                .build();
        Request loginAttempt_Req = new Request.Builder()
                .url("http://www.masterani.me/auth/sign-in")
                .header("User-Agent", getString(R.string.app_name) + " Android App")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .post(loginAttempt_Body)
                .build();

        client.newCall(loginAttempt_Req).enqueue(loginCallback);
    }

    public void onRegisterClick(View view) {
        Intent intent = new Intent(Activity_Login.this, Activity_Register.class);
        intent.putExtra("userEmail",userEmail.getText());
        intent.putExtra("userPass", userPass.getText());
        startActivity(intent);
    }
}
