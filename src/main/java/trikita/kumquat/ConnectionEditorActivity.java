package trikita.kumquat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import trikita.anvil.Anvil;

import static trikita.anvil.DSL.*;

public class ConnectionEditorActivity extends AppCompatActivity implements Anvil.Renderable {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(Anvil.mount(new FrameLayout(this), this));
    }

    @Override
    public void view() {
        textView(() -> {
            text("Connection editor");
        });
    }
}
