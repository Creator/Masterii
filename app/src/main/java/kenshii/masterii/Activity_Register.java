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
import android.text.SpannableString;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Activity_Register extends AppCompatActivity {
    SharedPreferences savedCookies;
    AutoCompleteTextView userName;
    AutoCompleteTextView userEmail;
    EditText userPass;
    EditText userPassConfirm;
    CheckBox userRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        userName = (AutoCompleteTextView)findViewById(R.id.register_name);
        userEmail = (AutoCompleteTextView)findViewById(R.id.register_email);
        userPass = (EditText)findViewById(R.id.register_password);
        userPassConfirm = (EditText)findViewById(R.id.register_passwordconfirm);
        userRemember = (CheckBox)findViewById(R.id.register_remember);
        savedCookies = getSharedPreferences("savedCookies", Context.MODE_PRIVATE);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            userEmail.setText((SpannableString)b.get("userEmail"));
            userPass.setText((SpannableString) b.get("userPass"));
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
    }

    public void handleLogin(List<String> responseHeaders) {
        for (String cookie : responseHeaders) {
            if (cookie.contains("remember_")) { //Login succeeded.
                String[] cookieSplit = cookie.split(";")[0].split("=");
                SharedPreferences.Editor cookieEditor = savedCookies.edit();
                cookieEditor.putString("loginCookie_name", cookieSplit[0]);
                cookieEditor.putString("loginCookie_value", cookieSplit[1]);
                cookieEditor.commit();
                Intent intent = new Intent(Activity_Register.this, Activity_Main.class);
                intent.putExtra("loginCookie_name", cookieSplit[0]);
                intent.putExtra("loginCookie_value", cookieSplit[1]);
                startActivity(intent);
                finish();
            }
        }
    }

    public void handleRegistrationSuccess(List<String> responseHeaders) {
        if (userRemember.isChecked()) {
            OkHttpClient client = new OkHttpClient();
            RequestBody loginAttempt_Body = new FormEncodingBuilder()
                    .add("email", userEmail.getText().toString())
                    .add("password", userPass.getText().toString())
                    .add("remember", "on")
                    .build();
            Request loginAttempt_Req = new Request.Builder()
                    .url("http://www.masterani.me/auth/sign-in")
                    .header("User-Agent", getString(R.string.app_name) + " Android App")
                    .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                    .post(loginAttempt_Body)
                    .build();
            client.newCall(loginAttempt_Req).enqueue(loginCallback);
        } else {
            for (String cookie : responseHeaders) {
                if (cookie.contains("laravel_session")) {
                    String[] cookieSplit = cookie.split(";")[0].split("=");
                    Intent intent = new Intent(Activity_Register.this, Activity_Main.class);
                    intent.putExtra("loginCookie_name", cookieSplit[0]);
                    intent.putExtra("loginCookie_value", cookieSplit[1]);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    Callback loginCallback = new Callback() { // Required in order to get the 'Remember me' cookie.
        @Override
        public void onFailure(Request request, IOException e) {
            backgroundShortToast("Login failed with exception: " + e.getMessage());
        }

        @Override
        public void onResponse(Response response) {
            if (response.code() != 200) {
                backgroundShortToast("Login failed with error code: " + Integer.toString(response.code()));
                return;
            }
            handleLogin(response.headers("Set-Cookie"));
        }
    };

    Callback registrationCallback_Attempt = new Callback() { // Required in order to register.
        @Override
        public void onFailure(Request request, IOException e) {
            backgroundShortToast("Registration failed with exception: " + e.getMessage());
        }

        @Override
        public void onResponse(Response response) {
            if (response.code()!=422) { // Registration was a success.
                handleRegistrationSuccess(response.headers("Set-Cookie"));
                return;
            }
            JSONObject responseJSON = null;
            String strProblems = null;
            try {
                responseJSON = new JSONObject(response.body().string()); // Registration was a failure.
                strProblems = "The following error"
                        + ((responseJSON.length() > 1) ? "s" : "")
                        + " occurred while attempting to register your account:\n\n";
                for ( Iterator<String> iter = responseJSON.keys(); iter.hasNext();) {
                    String key = iter.next();
                    for ( int i = 0; i < responseJSON.getJSONArray(key).length(); i++ )
                        strProblems += (responseJSON.getJSONArray(key).getString(i) + "\n");
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            backgroundAlertDialog(strProblems);
        }
    };

    public void backgroundShortToast(final String msg) {
        final Context context = Activity_Register.this;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void backgroundAlertDialog(final String msg) {
        final Context context = Activity_Register.this;
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

    public void onRegClick(View view) {
        OkHttpClient client = new OkHttpClient();
        client.setFollowRedirects(false);
        RequestBody registrationAttempt_Body = new FormEncodingBuilder()
                .add("name", userName.getText().toString())
                .add("email", userEmail.getText().toString())
                .add("password", userPass.getText().toString())
                .add("password_confirmation", userPassConfirm.getText().toString())
                .build();
        Request registrationAttempt_Req = new Request.Builder()
                .url("http://www.masterani.me/auth/register")
                .header("User-Agent", getString(R.string.app_name) + " Android App")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Referer", "http://www.masterani.me/auth/register")
                .post(registrationAttempt_Body)
                .build();
        client.newCall(registrationAttempt_Req).enqueue(registrationCallback_Attempt);
    }
}
