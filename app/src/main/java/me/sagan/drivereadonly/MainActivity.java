package me.sagan.drivereadonly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView a = (TextView) findViewById(R.id.status);
        a.setText(ok() ? "working" : "not working");
    }

    public boolean ok() {
        return false;
    }
}
