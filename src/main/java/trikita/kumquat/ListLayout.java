package trikita.kumquat;

import android.content.Context;
import android.widget.LinearLayout;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import trikita.anvil.recyclerview.Recycler;
import trikita.anvil.RenderableRecyclerViewAdapter;
import trikita.anvil.cardview.v7.CardViewv7DSL;

import trikita.anvil.RenderableView;
import static trikita.anvil.DSL.*;

import trikita.kumquat.State.ConnectionStatus;
import trikita.kumquat.State.MqttServer;

public class ListLayout extends RenderableView {

    private RenderableRecyclerViewAdapter mAdapter = new ConnectionAdapter();

    public ListLayout(Context c) {
        super(c);
    }

    public void view() {
        Recycler.view(() -> {
            Recycler.hasFixedSize(false);
            Recycler.layoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            Recycler.itemAnimator(new DefaultItemAnimator());
            Recycler.adapter(mAdapter);
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

                linearLayout(() -> {
                    size(0, WRAP);
                    weight(1);
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
                        text(ConnectionStatus.toString(App.state().connections().get(pos).status()));
                        allCaps(true);
                    });
                });
            });
        }
    }

}
