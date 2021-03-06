package trikita.kumquat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.HashSet;
import java.util.Set;

import static trikita.anvil.DSL.*;
import trikita.anvil.Anvil;
import trikita.anvil.RenderableRecyclerViewAdapter;
import trikita.anvil.RenderableView;
import trikita.anvil.appcompat.v7.AppCompatv7DSL;
import trikita.anvil.cardview.v7.CardViewv7DSL;
import trikita.anvil.design.DesignDSL;
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL;

import trikita.jedux.Action;
import trikita.kumquat.State.ConnectionStatus;

public class ConnectionsScreen extends RenderableView {

    private RenderableRecyclerViewAdapter mAdapter = new ConnectionAdapter();

    public ConnectionsScreen(Context context) {
        super(context);
    }

    public void view() {
        linearLayout(() -> {
            orientation(LinearLayout.VERTICAL);

            AppCompatv7DSL.toolbar(() -> {
                init(() -> {
                    ((AppCompatActivity) getContext()).setSupportActionBar(Anvil.currentView());
                    ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    ((AppCompatActivity) getContext()).getSupportActionBar().setHomeButtonEnabled(true);
                    ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayShowTitleEnabled(false);
                    ((KumquatActivity) getContext()).drawerToggle().syncState();
                });
                size(FILL, dip(54));
                backgroundColor(0xff2ecc71);
                DesignDSL.compatElevation(dip(4));
                AppCompatv7DSL.navigationOnClickListener(v -> {
                    ((KumquatActivity) getContext()).drawer().openDrawer(GravityCompat.START);
                });

                textView(() -> {
                    size(WRAP, WRAP);
                    text("Kumquat");
                    textSize(sip(20));
                    layoutGravity(CENTER_VERTICAL);
                    textColor(Color.WHITE);
                });
            });

            frameLayout(() -> {
                RecyclerViewv7DSL.recyclerView(() -> {
                    RecyclerViewv7DSL.linearLayoutManager();
                    RecyclerViewv7DSL.hasFixedSize(true);
                    RecyclerViewv7DSL.itemAnimator(new DefaultItemAnimator());
                    RecyclerViewv7DSL.adapter(mAdapter);
                    size(FILL, WRAP);
                });

                DesignDSL.floatingActionButton(() -> {
                    size(WRAP, WRAP);
                    margin(dip(24));
                    layoutGravity(BOTTOM | END);
                    imageResource(R.drawable.ic_add);
                    DesignDSL.backgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{0xff27ae60}));
                    DesignDSL.compatElevation(dip(4));
                    onClick(v -> {
                        Intent intent = new Intent(getContext(), ConnectionEditorActivity.class);
                        v.getContext().startActivity(intent);
                    });
                });
            });
        });

        post(() -> {
            mAdapter.notifyDataSetChanged();
        });
    }

    private class ConnectionAdapter extends RenderableRecyclerViewAdapter implements ActionMode.Callback {

        private final Set<String> selected = new HashSet<>();

        private ActionMode actionMode = null;

        @Override
        public int getItemCount() {
            return App.state().connections().size();
        }

        @Override
        public void view(RecyclerView.ViewHolder holder) {
            int pos = holder.getAdapterPosition();
            String connId = App.state().connections().get(pos).id();

            char ch = App.state().connections().get(pos).name().charAt(0);
            ShapeDrawable circle = new ShapeDrawable(new OvalShape());
            circle.getPaint().setColor(0xff3498db);

            CardViewv7DSL.cardView(() -> {
                size(FILL, WRAP);
                margin(dip(8), dip(4));
                onClick((v) -> {
                    if (actionMode == null) {
                        Intent intent = new Intent(getContext(), ConnectionEditorActivity.class);
                        intent.putExtra("id", connId);
                        v.getContext().startActivity(intent);
                    } else {
                        toggleSelection(connId);
                    }
                });
                onLongClick((v) -> {
                    if (actionMode == null) {
                        actionMode = ((AppCompatActivity) getContext()).startSupportActionMode(this);
                        selected.add(connId);
                    } else {
                        toggleSelection(connId);
                    }
                    return true;
                });

                linearLayout(() -> {
                    size(FILL, WRAP);
                    margin(dip(10), dip(18));
                    gravity(CENTER_VERTICAL);

                    textView(() -> {
                        size(dip(48), dip(48));
                        margin(0, 0, dip(15), 0);
                        backgroundDrawable(circle);
                        text(Character.toString(ch));
                        textColor(Color.WHITE);
                        textSize(sip(25));
                        typeface("fonts/Roboto-Light.ttf");
                        allCaps(true);
                        gravity(CENTER);
                        layoutGravity(CENTER);
                    });

                    linearLayout(() -> {
                        size(0, WRAP);
                        weight(1);
                        orientation(LinearLayout.VERTICAL);

                        textView(() -> {
                            visibility(actionMode != null);
                            size(WRAP, WRAP);
                            text(selected.contains(connId) ? "X" : "O");
                        });

                        textView(() -> {
                            size(WRAP, WRAP);
                            margin(0, 0, 0, dip(5));
                            text(App.state().connections().get(pos).name());
                            textSize(sip(16));
                            textColor(0xff000000);
                            typeface("fonts/Roboto-Light.ttf");
                            allCaps(true);
                            ellipsize(TextUtils.TruncateAt.MARQUEE);
                            singleLine(true);
                        });
                        textView(() -> {
                            size(WRAP, WRAP);
                            text(App.state().connections().get(pos).host()+":"+App.state().connections().get(pos).port());
                            textSize(sip(14));
                            textColor(0xff777777);
                            typeface("fonts/Roboto-Light.ttf");
                            ellipsize(TextUtils.TruncateAt.MARQUEE);
                            singleLine(true);
                        });
                    });

                    AppCompatv7DSL.switchCompat(() -> {
                        size(WRAP, WRAP);
                        layoutGravity(CENTER_VERTICAL);
                        enabled(App.state().connections().get(pos).status() != ConnectionStatus.CONNECTING);
                        onCheckedChange((CompoundButton btn, boolean check) -> {
                            if (check) {
                                App.dispatch(new Action<>(Actions.Connection.CONNECT, connId));
                            } else {
                                App.dispatch(new Action<>(Actions.Connection.DISCONNECT, connId));
                            }
                        });
                        checked(App.state().connections().get(pos).status() != ConnectionStatus.DISCONNECTED);
                    });
                });
            });
        }

        private void toggleSelection(String id) {
            if (selected.contains(id)) {
                selected.remove(id);
                if (selected.size() == 0 && actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                }
            } else {
                selected.add(id);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.multi_selector_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            for (String id : selected) {
                App.dispatch(new Action<>(Actions.Connection.DISCONNECT, id));
            }
            App.dispatch(new Action<>(Actions.Connection.REMOVE, new HashSet<>(selected)));
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selected.clear();
            actionMode = null;
            Anvil.render();
        }
    }
}
