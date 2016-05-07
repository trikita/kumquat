package trikita.kumquat;

import android.content.Context;

import trikita.anvil.RenderableView;

import static trikita.anvil.DSL.*;

public class ConnectionsScreen extends RenderableView {

    public ConnectionsScreen(Context context) {
        super(context);
    }

    @Override
    public void view() {
        textView(() -> {
            text("CONNECTIONS SCREEN");
        });
    }
}
