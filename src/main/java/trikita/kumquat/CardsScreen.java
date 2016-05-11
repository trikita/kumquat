package trikita.kumquat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.github.andrewoma.dexx.collection.List;

import java.util.HashSet;
import java.util.Set;

import trikita.anvil.Anvil;
import trikita.anvil.RenderableRecyclerViewAdapter;
import trikita.anvil.RenderableView;
import trikita.anvil.cardview.v7.CardViewv7DSL;
import trikita.anvil.design.DesignDSL;
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL;

import static trikita.anvil.DSL.*;

public class CardsScreen extends RenderableView {

    private RenderableRecyclerViewAdapter mAdapter = new CardAdapter();

    public CardsScreen(Context context) {
        super(context);
    }

    protected List<State.Card> cards() {
        return App.state().cards();
    }

    protected boolean hasFAB() {
        return true;
    }

    @Override
    public void view() {
        mAdapter.notifyDataSetChanged();
        RecyclerViewv7DSL.recyclerView(() -> {
            RecyclerViewv7DSL.gridLayoutManager(2);
            RecyclerViewv7DSL.hasFixedSize(false);
            RecyclerViewv7DSL.itemAnimator(new DefaultItemAnimator());
            RecyclerViewv7DSL.adapter(mAdapter);
        });
        if (hasFAB()) {
            DesignDSL.floatingActionButton(() -> {
                size(WRAP, WRAP);
                margin(dip(32));
                DesignDSL.compatElevation(dip(4));
                layoutGravity(BOTTOM | END);
                onClick(v -> {
                    Intent intent = new Intent(getContext(), CardEditorActivity.class);
                    v.getContext().startActivity(intent);
                });
            });
        }
    }

    private class CardAdapter extends RenderableRecyclerViewAdapter implements ActionMode.Callback {

        private final Set<String> selected = new HashSet<>();

        private ActionMode actionMode = null;

        @Override
        public int getItemCount() {
            return cards().size();
        }

        @Override
        public void view(RecyclerView.ViewHolder holder) {
            int pos = holder.getAdapterPosition();
            String cardId = App.state().cards().get(pos).id();
            CardViewv7DSL.cardView(() -> {
                size(FILL, FILL);
                margin(dip(8));
                onClick((v) -> {
                    if (actionMode == null) {
                        Intent intent = new Intent(getContext(), CardEditorActivity.class);
                        intent.putExtra("id", cardId);
                        v.getContext().startActivity(intent);
                    } else {
                        toggleSelection(cardId);
                    }
                });
                onLongClick((v) -> {
                    if (actionMode == null) {
                        actionMode = ((AppCompatActivity) getContext()) .startSupportActionMode(this);
                        selected.add(cardId);
                    }
                    return false;
                });

                linearLayout(() -> {
                    size(FILL, FILL);
                    orientation(LinearLayout.VERTICAL);
                    if (actionMode != null) {
                        backgroundColor(selected.contains(cardId) ? 0x77ffff00 : 0x77777777);
                    } else {
                        backgroundColor(0);
                    }

                    textView(() -> {
                        size(FILL, WRAP);
                        text(App.state().cards().get(pos).value());
                    });

                    textView(() -> {
                        size(FILL, WRAP);
                        text(App.state().cards().get(pos).topic());
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
            App.dispatch(new Action<>(Actions.Topic.REMOVE, new HashSet<>(selected)));
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
