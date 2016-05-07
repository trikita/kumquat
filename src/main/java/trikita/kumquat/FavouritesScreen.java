package trikita.kumquat;

import android.content.Context;

import com.github.andrewoma.dexx.collection.List;

import trikita.anvil.RenderableView;

import static trikita.anvil.DSL.*;

public class FavouritesScreen extends CardsScreen {

    public FavouritesScreen(Context context) {
        super(context);
    }

    @Override
    protected List<State.Card> cards() {
        return App.state().favourites();
    }

    @Override
    protected boolean hasFAB() {
        return false;
    }
}
