package trikita.kumquat;

import android.content.Context;

import trikita.anvil.RenderableView;

import static trikita.anvil.DSL.*;

public class FavouritesScreen extends RenderableView {

    public FavouritesScreen(Context context) {
        super(context);
    }

    @Override
    public void view() {
        textView(() -> {
            text("FAVOURITES SCREEN");
        });
    }
}
