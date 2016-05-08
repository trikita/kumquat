package trikita.kumquat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.HashSet;
import java.util.Set;

import trikita.anvil.Anvil;
import trikita.anvil.RenderableRecyclerViewAdapter;
import trikita.anvil.RenderableView;
import trikita.anvil.cardview.v7.CardViewv7DSL;
import trikita.anvil.design.DesignDSL;
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL;
import trikita.jedux.Action;

import static trikita.anvil.DSL.*;

public class ConnectionsScreen extends RenderableView {

    private RenderableRecyclerViewAdapter mAdapter = new ConnectionAdapter();

    public ConnectionsScreen(Context context) {
        super(context);
    }

    public void view() {
        mAdapter.notifyDataSetChanged();
        RecyclerViewv7DSL.recyclerView(() -> {
            RecyclerViewv7DSL.linearLayoutManager();
            RecyclerViewv7DSL.hasFixedSize(true);
            RecyclerViewv7DSL.itemAnimator(new DefaultItemAnimator());
            RecyclerViewv7DSL.adapter(mAdapter);
        });
        DesignDSL.floatingActionButton(() -> {
            size(WRAP, WRAP);
            margin(dip(32));
            DesignDSL.compatElevation(dip(4));
            layoutGravity(BOTTOM | END);
            onClick(v -> {
                Intent intent = new Intent(getContext(), ConnectionEditorActivity.class);
                v.getContext().startActivity(intent);
            });
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
            CardViewv7DSL.cardView(() -> {
                size(FILL, WRAP);
                margin(dip(8));
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
                        actionMode = ((AppCompatActivity) getContext()) .startSupportActionMode(this);
                        selected.add(connId);
                    } else {
                        toggleSelection(connId);
                    }
                    return true;
                });

                linearLayout(() -> {
                    size(FILL, WRAP);
                    margin(dip(12));
                    orientation(LinearLayout.VERTICAL);

                    textView(() -> {
                        visibility(actionMode != null);
                        size(WRAP, WRAP);
                        text(selected.contains(connId) ? "X" : "O");
                    });

                    textView(() -> {
                        size(WRAP, WRAP);
                        text(App.state().connections().get(pos).uri());
                    });
                    textView(() -> {
                        size(WRAP, WRAP);
                        text(State.ConnectionStatus.toString(App.state().connections().get(pos).status()));
                        allCaps(true);
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
