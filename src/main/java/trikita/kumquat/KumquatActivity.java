package trikita.kumquat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class KumquatActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new ListLayout(this));
    }
}
