package trikita.kumquat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import static trikita.anvil.DSL.*;
import trikita.anvil.Anvil;
import trikita.anvil.RenderableAdapter;

import trikita.jedux.Action;

import trikita.kumquat.State.MqttServer;

public class CardEditorActivity extends AppCompatActivity implements Anvil.Renderable {

    private State.Card card;
    private boolean create = true;

    private RenderableAdapter mServerAdapter = RenderableAdapter
        .withItems(App.state().connections().asList(), (index, item) -> {
            textView(() -> {
                size(FILL, dip(50));
                minWidth(dip(200));
                padding(dip(7), 0);
                gravity(LEFT|CENTER_VERTICAL);
                text(item.uri());
            });
        });

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String id = getIntent().getStringExtra("id");
        if (id != null) {
            this.card = ImmutableCard.copyOf(App.state().getCard(id));
            this.create = false;
        } else {
            this.card = ImmutableCard.builder()
                    .id(State.generateId())
                    .name("")
                    .topic("")
                    .value("")
                    .connId("")
                    .subQoS(0)
                    .params(ImmutableTextCardParams.builder().build())
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
                App.dispatch(new Action<>(Actions.Topic.CREATE, card));
            } else {
                App.dispatch(new Action<>(Actions.Topic.MODIFY, card));
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

            linearLayout(() -> {
                size(FILL, WRAP);

                editText(() -> {
                    size(0, WRAP);
                    weight(1);
                    text(card.topic());
                    onTextChanged(s -> {
                        card = ImmutableCard.copyOf(card).withTopic(s.toString());
                    });
                });

                button(() -> {
                    size(WRAP, WRAP);
                    text("Add");
                    onClick(v -> {
                        System.out.println("creating card: " + card.topic() + " id="+card.id());
                        if (create) {
                            App.dispatch(new Action<>(Actions.Topic.CREATE, card));
                        } else {
                            App.dispatch(new Action<>(Actions.Topic.MODIFY, card));
                        }
                        finish();
                    });
                });
            });

            spinner(() -> {
                adapter(mServerAdapter);
                onItemSelected((parent, view, pos, id) -> {
                    State.MqttServer ms = (MqttServer) parent.getAdapter().getItem(pos);
                    System.out.println("selected server id " + ms.id());
                    card = ImmutableCard.copyOf(card).withConnId(ms.id());
                });
            });
        });
    }
}
