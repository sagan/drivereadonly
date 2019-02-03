package me.sagan.drivereadonly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final SharedPreferences sharedPref = this.getSharedPreferences("config",0);
    CheckBox checkBox = (CheckBox)findViewById(R.id.enabledCheckbox);
    checkBox.setChecked(sharedPref.getBoolean("enabled", false));
    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("enabled", isChecked);
        editor.commit();
      }
    });

    Button hpButton = (Button) findViewById(R.id.hp);
    hpButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sagan/drivereadonly"));
        startActivity(browserIntent);
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    TextView a = (TextView) findViewById(R.id.status);
    a.setText(ok() ? "loaded" : "not loaded");
  }

  @Override
  public void onPause() {
    super.onPause();

    // Set preferences permissions to be world readable
    // Workaround for Android N and above since MODE_WORLD_READABLE will cause security exception and FC.
    final File dataDir = new File(getApplicationInfo().dataDir);
    final File prefsDir = new File(dataDir, "shared_prefs");
    final File prefsFile = new File(prefsDir, "config.xml");

    if (prefsFile.exists()) {
      dataDir.setReadable(true, false);
      dataDir.setExecutable(true, false);
      prefsDir.setReadable(true, false);
      prefsDir.setExecutable(true, false);
      prefsFile.setReadable(true, false);
      prefsFile.setExecutable(true, false);
    }
  }
  public boolean ok() {
    Log.i("fake", "isModuleActive");
    return false;
  }
}
