package trikita.kumquat;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import static trikita.anvil.DSL.*;
import trikita.anvil.Anvil;
import trikita.anvil.RenderableAdapter;
import trikita.anvil.appcompat.v7.AppCompatv7DSL;
import trikita.anvil.cardview.v7.CardViewv7DSL;
import trikita.anvil.design.DesignDSL;

import trikita.jedux.Action;

import trikita.kumquat.State.Card;
import trikita.kumquat.State.CardType;
import trikita.kumquat.State.MqttServer;

public class CardEditorActivity extends AppCompatActivity implements Anvil.Renderable {
    private final static int RADIOBUTTON_BASE_ID = 100;

    private Card card;
    private CardType cardType;
    private boolean create = true;

    private RenderableAdapter mConnAdapter = RenderableAdapter
        .withItems(App.state().connections().asList(), (index, item) -> {
            textView(() -> {
                size(FILL, dip(50));
                minWidth(dip(200));
                padding(dip(5), 0);
                gravity(LEFT|CENTER_VERTICAL);
                text(item.name());
                textSize(sip(18));
                typeface("fonts/Roboto-Light.ttf");
            });
        });

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        App.getWindowController().setWindow(getWindow());

        String id = getIntent().getStringExtra("id");
        if (id != null) {
            this.card = ImmutableCard.copyOf(App.state().getCard(id));
            this.create = false;
        } else {
            this.card = ImmutableCard.builder()
                    .id(State.generateId())
                    .name("Card A")
                    .topic("")
                    .value("")
                    .connId("")
                    .subQoS(0)
                    .params(ImmutableTextCardParams.builder().build())
                    .build();
        }
        this.cardType = State.cardType(this.card);
        setContentView(Anvil.mount(new FrameLayout(this), this));

        App.dispatch(new Action<>(Actions.Card.MODIFY_TYPE, cardType));
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

            AppCompatv7DSL.toolbar(() -> {
                init(() -> {
                    setSupportActionBar(Anvil.currentView());
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeButtonEnabled(true);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                });
                size(FILL, dip(54));
                backgroundColor(cardType.primaryColor);
                DesignDSL.compatElevation(dip(4));

                textView(() -> {
                    size(WRAP, WRAP);
                    text(card.name());
                    textSize(sip(20));
                    layoutGravity(CENTER_VERTICAL);
                    textColor(Color.WHITE);
                });
            });

            scrollView(() -> {
                size(FILL, WRAP);

                linearLayout(() -> {
                    size(FILL, WRAP);
                    orientation(LinearLayout.VERTICAL);

                    textView(() -> {
                        size(FILL, WRAP);
                        padding(dip(10));
                        text("TYPE");
                    });

                    CardViewv7DSL.cardView(() -> {
                        size(FILL, WRAP);
                        margin(dip(8), dip(4));

                        cardTypePicker();
                    });

                    textView(() -> {
                        size(FILL, WRAP);
                        padding(dip(10));
                        text("GENERAL");
                    });

                    CardViewv7DSL.cardView(() -> {
                        size(FILL, WRAP);
                        margin(dip(8), dip(4));

                        linearLayout(() -> {
                            orientation(LinearLayout.VERTICAL);
                            margin(dip(5));

                            DesignDSL.textInputLayout(() -> {
                                DesignDSL.hintEnabled(true);
                                DesignDSL.hint("Name");
                                margin(0, dip(5));

                                AppCompatv7DSL.appCompatEditText(() -> {
                                    text(card.name());
                                    typeface("fonts/Roboto-Light.ttf");
                                    onTextChanged(s -> {
                                        card = ImmutableCard.copyOf(card).withName(s.toString());
                                    });
                                });
                            });

                            DesignDSL.textInputLayout(() -> {
                                DesignDSL.hintEnabled(true);
                                DesignDSL.hint("Topic");
                                margin(0, dip(5));

                                AppCompatv7DSL.appCompatEditText(() -> {
                                    text(card.topic());
                                    typeface("fonts/Roboto-Light.ttf");
                                    onTextChanged(s -> {
                                        card = ImmutableCard.copyOf(card).withTopic(s.toString());
                                    });
                                });
                            });

                            AppCompatv7DSL.appCompatSpinner(() -> {
                                adapter(mConnAdapter);
                                margin(0, dip(5));
                                onItemSelected((parent, view, pos, id) -> {
                                    State.MqttServer ms = (MqttServer) parent.getAdapter().getItem(pos);
                                    card = ImmutableCard.copyOf(card).withConnId(ms.id());
                                });
                            });

                            radioGroup(() -> {
                                orientation(LinearLayout.HORIZONTAL);
                                gravity(CENTER_VERTICAL);
                                margin(0, dip(5));
                                onCheckedChange((RadioGroup rgrp, int checkedId) -> {
                                    card = ImmutableCard.copyOf(card).withSubQoS(checkedId-100);
                                });

                                textView(() -> {
                                    size(0, WRAP);
                                    weight(1);
                                    margin(dip(5), 0, dip(25), 0);
                                    text("QoS");
                                    textSize(sip(18));
                                    typeface("fonts/Roboto-Light.ttf");
                                });

                                for (int i = 0; i < 3; i++) {
                                    radiobutton(i);
                                }
                            });
                        });
                    });

                    cardTypeLayout();
                });
            });
        });
    }

    private void cardTypePicker() {
        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.getPaint().setColor(cardType.primaryColor);

        linearLayout(() -> {
            size(FILL, dip(72));
            gravity(CENTER_VERTICAL);
            margin(dip(15), 0, dip(10), 0);

            imageView(() -> {
                size(dip(48), dip(48));
                padding(dip(12));
                imageResource(cardType.iconResource);
                scaleType(ImageView.ScaleType.CENTER_INSIDE);
                backgroundDrawable(circle);
            });

            textView(() -> {
                size(0, WRAP);
                weight(1);
                margin(dip(25), 0);
                text(cardType.title);
                textSize(sip(18));
                typeface("fonts/Roboto-Light.ttf");
            });

            textView(() -> {
                size(dip(48), dip(48));
                text("\ue3c9");
                textSize(sip(25));
                textColor(0xff999999);
                typeface("fonts/MaterialIcons-Regular.ttf");
                gravity(CENTER);
            });
        });
    }

            linearLayout(() -> {
                size(FILL, WRAP);

                editText(() -> {
                    size(0, WRAP);
                    weight(1);
                    text(card.topic());
    private void radiobutton(final int i) {
        AppCompatv7DSL.appCompatRadioButton(() -> {
            id(RADIOBUTTON_BASE_ID + i);
            size(0, WRAP);
            weight(1);
            text(""+i);
            textSize(sip(18));
            typeface("fonts/Roboto-Light.ttf");
            checked(card.subQoS() == i);
        });
    }

    private void cardTypeLayout() {
        if (card.params() instanceof Card.ButtonCardParams) {
            cardSpecificSectionTitle();
            buttonParams();
        } else if (card.params() instanceof Card.SwitchCardParams) {
            cardSpecificSectionTitle();
            switchParams();
        } else if (card.params() instanceof Card.SlidebarCardParams) {
            cardSpecificSectionTitle();
            slidebarParams();
        }
    }

    private void cardSpecificSectionTitle() {
        textView(() -> {
            size(FILL, WRAP);
            padding(dip(10));
            text("CARD-SPECIFIC");
        });
    }

    private void buttonParams() {
        CardViewv7DSL.cardView(() -> {
            size(FILL, WRAP);
            margin(dip(8), dip(4));

            DesignDSL.textInputLayout(() -> {
                DesignDSL.hintEnabled(true);
                DesignDSL.hint("Payload");
                margin(dip(5));

                AppCompatv7DSL.appCompatEditText(() -> {
                    text(((Card.ButtonCardParams) card.params()).payload());
                    typeface("fonts/Roboto-Light.ttf");
                    onTextChanged(s -> {
                        card = ImmutableCard.copyOf(card)
                            .withParams(ImmutableButtonCardParams.copyOf((Card.ButtonCardParams) card.params())
                                .withPayload(s.toString()));
                    });
                });
            });
        });
    }

    private void switchParams() {
        CardViewv7DSL.cardView(() -> {
            size(FILL, WRAP);
            margin(dip(8), dip(4));

            linearLayout(() -> {
                orientation(LinearLayout.VERTICAL);
                margin(dip(5));

                DesignDSL.textInputLayout(() -> {
                    DesignDSL.hintEnabled(true);
                    DesignDSL.hint("Switch ON payload");

                    AppCompatv7DSL.appCompatEditText(() -> {
                        text(((Card.SwitchCardParams) card.params()).onPayload());
                        typeface("fonts/Roboto-Light.ttf");
                        onTextChanged(s -> {
                            card = ImmutableCard.copyOf(card)
                                .withParams(ImmutableSwitchCardParams.copyOf((Card.SwitchCardParams) card.params())
                                    .withOnPayload(s.toString()));
                        });
                    });
                });

                DesignDSL.textInputLayout(() -> {
                    DesignDSL.hintEnabled(true);
                    DesignDSL.hint("Switch OFF payload");

                    AppCompatv7DSL.appCompatEditText(() -> {
                        text(((Card.SwitchCardParams) card.params()).offPayload());
                        typeface("fonts/Roboto-Light.ttf");
                        onTextChanged(s -> {
                            card = ImmutableCard.copyOf(card)
                                .withParams(ImmutableSwitchCardParams.copyOf((Card.SwitchCardParams) card.params())
                                    .withOffPayload(s.toString()));
                        });
                    });
                });
            });
        });
    }

    private void slidebarParams() {
        CardViewv7DSL.cardView(() -> {
            size(FILL, WRAP);
            margin(dip(8), dip(4));

            linearLayout(() -> {
                margin(dip(5));
                gravity(CENTER_VERTICAL);

                DesignDSL.textInputLayout(() -> {
                    size(0, WRAP);
                    weight(1);
                    DesignDSL.hintEnabled(true);
                    DesignDSL.hint("Min value");

                    AppCompatv7DSL.appCompatEditText(() -> {
                        text("" + ((Card.SlidebarCardParams) card.params()).min());
                        typeface("fonts/Roboto-Light.ttf");
                        onTextChanged(s -> {
                            try {
                                float f = Float.parseFloat(s.toString());
                                card = ImmutableCard.copyOf(card)
                                    .withParams(ImmutableSlidebarCardParams.copyOf((Card.SlidebarCardParams) card.params())
                                        .withMin(f));
                            } catch (NumberFormatException e) {
                                // TODO
                            }
                        });
                    });
                });

                DesignDSL.textInputLayout(() -> {
                    size(0, WRAP);
                    weight(1);
                    margin(dip(10), 0);
                    DesignDSL.hintEnabled(true);
                    DesignDSL.hint("Max value");

                    AppCompatv7DSL.appCompatEditText(() -> {
                        text("" + ((Card.SlidebarCardParams) card.params()).max());
                        typeface("fonts/Roboto-Light.ttf");
                        onTextChanged(s -> {
                            try {
                                float f = Float.parseFloat(s.toString());
                                card = ImmutableCard.copyOf(card)
                                    .withParams(ImmutableSlidebarCardParams.copyOf((Card.SlidebarCardParams) card.params())
                                        .withMax(f));
                            } catch (NumberFormatException e) {
                                // TODO
                            }
                        });
                    });
                });

                DesignDSL.textInputLayout(() -> {
                    size(0, WRAP);
                    weight(1);
                    DesignDSL.hintEnabled(true);
                    DesignDSL.hint("Step");

                    AppCompatv7DSL.appCompatEditText(() -> {
                        text("" + ((Card.SlidebarCardParams) card.params()).step());
                        typeface("fonts/Roboto-Light.ttf");
                        onTextChanged(s -> {
                            try {
                                float f = Float.parseFloat(s.toString());
                                card = ImmutableCard.copyOf(card)
                                    .withParams(ImmutableSlidebarCardParams.copyOf((Card.SlidebarCardParams) card.params())
                                        .withStep(f));
                            } catch (NumberFormatException e) {
                                // TODO
                            }
                        });
                    });
                });
            });
        });
    }
}
