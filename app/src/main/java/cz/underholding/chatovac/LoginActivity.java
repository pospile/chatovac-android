package cz.underholding.chatovac;

import android.app.Dialog;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.geniusforapp.fancydialog.FancyAlertDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sirvar.robin.RobinActivity;
import com.sirvar.robin.Theme;

import org.json.JSONObject;

import cz.underholding.chatovac.dbs.DBS;
import cz.underholding.chatovac.dbs.MetaData;
import io.objectbox.Box;

import static java.security.AccessController.getContext;

public class LoginActivity extends RobinActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // DO NOT call setContentView();

        // Set title for each screen
        setLoginTitle("Sign in to Chatovač");
        setSignupTitle("Welcome to Chatovač");
        setForgotPasswordTitle("Forgot Password");

        // Set logo for screens
        setImage(getResources().getDrawable(R.drawable.ic_chat));

        // Use custom font across all views
        //setFont(Typeface.createFromAsset(getAssets(), "Montserrat-Medium.ttf"));

        // Choose theme (default is LIGHT)
        setTheme(Theme.LIGHT);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Log.e("NOT_ALLOWED", "NOT LOGGING IN IS NOT ALLOWED");
    }

    @Override
    protected void onLogin(final String email, String password) {

        String android_id = Settings.Secure.getString(LoginActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Ion.with(LoginActivity.this)
                .load("http://10.0.2.2:3000/user/login")
                .setBodyParameter("name", email)
                .setBodyParameter("pass", password)
                .setBodyParameter("device_id", "android_"+android_id)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        try {
                            Log.e("RES", result.getAsJsonObject("token").get("token").getAsString() + " " + result.getAsJsonObject("user").get("id").getAsInt());
                            Box<MetaData> metaDataBox = DBS.getInstance().getBoxStore().boxFor(MetaData.class);
                            MetaData data = new MetaData(email, result.getAsJsonObject("token").get("token").getAsString(), result.getAsJsonObject("user").get("id").getAsInt(), "http://10.0.2.2:3000");
                            metaDataBox.put(data);
                            LoginActivity.this.finish();
                        }
                        catch (Exception err) {
                            FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(LoginActivity.this)
                                    .setTextTitle("ERROR")
                                    .setTextSubTitle("bad server response")
                                    .setBody("Please try to check your email/password.")
                                    .setPositiveButtonText("Continue")
                                    .setPositiveColor(R.color.colorPrimary)
                                    .setOnPositiveClicked(new FancyAlertDialog.OnPositiveClicked() {
                                        @Override
                                        public void OnClick(View view, Dialog dialog) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setBodyGravity(FancyAlertDialog.TextGravity.CENTER)
                                    .setTitleGravity(FancyAlertDialog.TextGravity.CENTER)
                                    .setSubtitleGravity(FancyAlertDialog.TextGravity.CENTER)
                                    .setCancelable(false)
                                    .build();
                            alert.show();
                        }
                    }
                });
    }

    @Override
    protected void onSignup(String name, String email, String password) {

    }

    @Override
    protected void onForgotPassword(String email) {

    }

    @Override
    protected void onGoogleLogin() {

    }

    @Override
    protected void onFacebookLogin() {

    }
}