package trikita.kumquat;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import static trikita.anvil.DSL.*;
import trikita.anvil.Anvil;
import trikita.anvil.appcompat.v7.AppCompatv7DSL;
import trikita.anvil.design.DesignDSL;

import trikita.jedux.Action;

public class ConnectionEditorActivity extends AppCompatActivity implements Anvil.Renderable {

    private State.MqttServer conn;

    private boolean create = true;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String id = getIntent().getStringExtra("id");
        if (id != null) {
            this.conn = ImmutableMqttServer.copyOf(App.state().getConnection(id));
            this.create = false;
        } else {
            this.conn = ImmutableMqttServer.builder()
                    .id(State.generateId())
                    .name("My connection")
                    .uri("")
                    .clientId("")
                    .status(State.ConnectionStatus.DISCONNECTED)
                    .username("").password("")
                    .willTopic("").willPayload("").willQoS(0).willRetain(false)
                    .build();
        }
        setContentView(Anvil.mount(new FrameLayout(this), this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.om_save) {
            if (create) {
                App.dispatch(new Action<>(Actions.Connection.CREATE, conn));
            } else {
                App.dispatch(new Action<>(Actions.Connection.MODIFY, conn));
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void view() {
        linearLayout(() -> {
            orientation(LinearLayout.VERTICAL);

            AppCompatv7DSL.toolbar(() -> {
                init(() -> {
                    setSupportActionBar(Anvil.currentView());
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeButtonEnabled(true);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                });
                size(FILL, dip(54));
                backgroundColor(0xff2ecc71);
                DesignDSL.compatElevation(dip(4));

                textView(() -> {
                    size(WRAP, WRAP);
                    text(conn.name());
                    textSize(sip(20));
                    layoutGravity(CENTER_VERTICAL);
                    textColor(Color.WHITE);
                });
            });

            scrollView(() -> {
                linearLayout(() -> {
                    size(FILL, WRAP);
                    orientation(LinearLayout.VERTICAL);
                    margin(dip(8));
                });
            });
        });
    }
}
