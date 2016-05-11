package trikita.kumquat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import com.github.andrewoma.dexx.collection.List;

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

    private class CardAdapter extends RenderableRecyclerViewAdapter {

        @Override
        public void view(RecyclerView.ViewHolder holder) {
            int pos = holder.getAdapterPosition();
            CardViewv7DSL.cardView(() -> {
                size(FILL, FILL);
                margin(dip(8));

                linearLayout(() -> {
                    size(FILL, FILL);
                    orientation(LinearLayout.VERTICAL);

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

        @Override
        public int getItemCount() {
            System.out.println("getItemCount = " + cards().size());
            return cards().size();
        }
    }
}
