package trikita.kumquat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import trikita.anvil.Anvil;
import trikita.jedux.Action;

import static trikita.anvil.DSL.*;

public class ConnectionEditorActivity extends AppCompatActivity implements Anvil.Renderable {

    private State.MqttServer conn = ImmutableMqttServer.builder()
            .id(State.generateId())
            .uri("")
            .clientId("")
            .status(State.ConnectionStatus.DISCONNECTED)
            .build();

    private boolean create = true;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String id = getIntent().getStringExtra("id");
        if (id != null) {
            this.conn = ImmutableMqttServer.copyOf(App.state().getConnection(id));
            this.create = false;
        }
        setContentView(Anvil.mount(new FrameLayout(this), this));
    }

    @Override
    public void view() {
        linearLayout(() -> {
            size(FILL, FILL);
            orientation(LinearLayout.VERTICAL);

            editText(() -> {
                text(conn.uri());
                onTextChanged(s -> {
                    conn = ImmutableMqttServer.copyOf(conn).withUri(s.toString());
                });
            });

            button(() -> {
                size(FILL, WRAP);
                onClick(v -> {
                    if (create) {
                        App.dispatch(new Action<>(Actions.Connection.CREATE, conn));
                    } else {
                        App.dispatch(new Action<>(Actions.Connection.MODIFY, conn));
                    }
                    finish();
                });
            });
        });
    }
}
