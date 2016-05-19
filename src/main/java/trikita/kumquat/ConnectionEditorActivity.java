package trikita.kumquat;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import static trikita.anvil.DSL.*;
import trikita.anvil.Anvil;
import trikita.anvil.appcompat.v7.AppCompatv7DSL;
import trikita.anvil.design.DesignDSL;

import trikita.jedux.Action;

public class ConnectionEditorActivity extends AppCompatActivity implements Anvil.Renderable {
    private final static int RADIOBUTTON_BASE_ID = 100;

    private State.MqttServer conn;

    private boolean create = true;

    private boolean withWill;

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
                    .host("")
                    .port("")
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
        } else if (item.getItemId() == android.R.id.home) {
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
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
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

                    DesignDSL.textInputLayout(() -> {
                        DesignDSL.hintEnabled(true);
                        DesignDSL.hint("Name");

                        AppCompatv7DSL.appCompatEditText(() -> {
                            text(conn.name());
                            typeface("fonts/Roboto-Light.ttf");
                            onTextChanged(s -> {
                                conn = ImmutableMqttServer.copyOf(conn).withName(s.toString());
                            });
                        });
                    });

                    linearLayout(() -> {
                        size(FILL, WRAP);

                        DesignDSL.textInputLayout(() -> {
                            size(0, WRAP);
                            weight(1);
                            margin(0, 0, dip(10), 0);
                            DesignDSL.hintEnabled(true);
                            DesignDSL.hint("Host");

                            AppCompatv7DSL.appCompatEditText(() -> {
                                text(conn.host());
                                typeface("fonts/Roboto-Light.ttf");
                                onTextChanged(s -> {
                                    conn = ImmutableMqttServer.copyOf(conn).withHost(s.toString());
                                });
                            });
                        });

                        DesignDSL.textInputLayout(() -> {
                            size(dip(70), WRAP);
                            DesignDSL.hintEnabled(true);
                            DesignDSL.hint("Port");

                            AppCompatv7DSL.appCompatEditText(() -> {
                                text(conn.port());
                                typeface("fonts/Roboto-Light.ttf");
                                onTextChanged(s -> {
                                    conn = ImmutableMqttServer.copyOf(conn).withPort(s.toString());
                                });
                            });
                        });
                    });

                    DesignDSL.textInputLayout(() -> {
                        DesignDSL.hintEnabled(true);
                        DesignDSL.hint("Client ID");

                        AppCompatv7DSL.appCompatEditText(() -> {
                            text(conn.clientId());
                            typeface("fonts/Roboto-Light.ttf");
                            onTextChanged(s -> {
                                conn = ImmutableMqttServer.copyOf(conn).withClientId(s.toString());
                            });
                        });
                    });

                    linearLayout(() -> {
                        size(FILL, WRAP);
                        margin(dip(5), dip(25), dip(5), 0);

                        textView(() -> {
                            size(0, WRAP);
                            weight(1);
                            text("Will");
                            textSize(sip(18));
                            typeface("fonts/Roboto-Light.ttf");
                        });

                        AppCompatv7DSL.switchCompat(() -> {
                            size(WRAP, WRAP);
                            layoutGravity(CENTER_VERTICAL);
                            onCheckedChange((CompoundButton btn, boolean check) -> {
                                withWill = check;
                            });
                            checked(withWill);
                        });
                    });

                    linearLayout(() -> {
                        size(FILL, WRAP);
                        orientation(LinearLayout.VERTICAL);
                        visibility(withWill);

                        DesignDSL.textInputLayout(() -> {
                            DesignDSL.hintEnabled(true);
                            DesignDSL.hint("Topic");

                            AppCompatv7DSL.appCompatEditText(() -> {
                                text(conn.willTopic());
                                typeface("fonts/Roboto-Light.ttf");
                                onTextChanged(s -> {
                                    conn = ImmutableMqttServer.copyOf(conn).withWillTopic(s.toString());
                                });
                            });
                        });

                        DesignDSL.textInputLayout(() -> {
                            DesignDSL.hintEnabled(true);
                            DesignDSL.hint("Message");

                            AppCompatv7DSL.appCompatEditText(() -> {
                                text(conn.willPayload());
                                typeface("fonts/Roboto-Light.ttf");
                                onTextChanged(s -> {
                                    conn = ImmutableMqttServer.copyOf(conn).withWillPayload(s.toString());
                                });
                            });
                        });

                        AppCompatv7DSL.appCompatCheckBox(() -> {
                            margin(0, dip(10));
                            text("Retain");
                            textSize(sip(18));
                            typeface("fonts/Roboto-Light.ttf");
                            onCheckedChange((CompoundButton btn, boolean check) -> {
                                conn = ImmutableMqttServer.copyOf(conn).withWillRetain(check);
                            });
                            checked(conn.willRetain());
                        });

                        textView(() -> {
                            size(FILL, WRAP);
                            margin(dip(5));
                            text("QoS");
                            textSize(sip(18));
                            typeface("fonts/Roboto-Light.ttf");
                        });

                        radioGroup(() -> {
                            orientation(LinearLayout.HORIZONTAL);
                            margin(dip(10), dip(5));
                            onCheckedChange((RadioGroup rgrp, int checkedId) -> {
                                System.out.println("onCheckedChange(): "+checkedId);
                                conn = ImmutableMqttServer.copyOf(conn).withWillQoS(checkedId-RADIOBUTTON_BASE_ID);
                            });

                            for (int i = 0; i < 3; i++) {
                                radiobutton(i);
                            }
                        });
                    });
                });
            });
        });
    }

    private void radiobutton(final int i) {
        AppCompatv7DSL.appCompatRadioButton(() -> {
            id(RADIOBUTTON_BASE_ID + i);
            size(0, WRAP);
            weight(1);
            text(""+i);
            textSize(sip(18));
            typeface("fonts/Roboto-Light.ttf");
            checked(conn.willQoS() == i);
        });
    }
}
