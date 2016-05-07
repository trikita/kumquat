package trikita.kumquat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

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
        RecyclerViewv7DSL.recyclerView(() -> {
            RecyclerViewv7DSL.layoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
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

    private class ConnectionAdapter extends RenderableRecyclerViewAdapter {
        @Override
        public int getItemCount() {
            return App.state().connections().size();
        }

        @Override
        public void view(RecyclerView.ViewHolder holder) {
            int pos = holder.getAdapterPosition();
            CardViewv7DSL.cardView(() -> {
                size(FILL, WRAP);
                margin(dip(8));
                onClick((v) -> {
                    Intent intent = new Intent(getContext(), ConnectionEditorActivity.class);
                    // TODO pass connection ID
                    v.getContext().startActivity(intent);
                });

                linearLayout(() -> {
                    size(FILL, WRAP);
                    margin(dip(12));
                    orientation(LinearLayout.VERTICAL);

                    textView(() -> {
                        size(WRAP, WRAP);
                        text(App.state().connections().get(pos).host()+":"+App.state().connections().get(pos).port());
                    });
                    textView(() -> {
                        size(WRAP, WRAP);
                        text(App.state().connections().get(pos).clientId());
                    });
                    textView(() -> {
                        size(WRAP, WRAP);
                        text(State.ConnectionStatus.toString(App.state().connections().get(pos).status()));
                        allCaps(true);
                    });
                });
            });
        }
    }
}
