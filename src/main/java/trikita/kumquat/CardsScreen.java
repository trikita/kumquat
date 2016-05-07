package trikita.kumquat;

import android.content.Context;

import trikita.anvil.RenderableView;

import static trikita.anvil.DSL.*;

public class CardsScreen extends RenderableView {
    public CardsScreen(Context context) {
        super(context);
    }

    @Override
    public void view() {
        textView(() -> {
            text("CARDS SCREEN");
        });
    }
}
