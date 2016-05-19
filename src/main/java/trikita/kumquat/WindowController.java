package trikita.kumquat;

import android.os.Build;
import android.view.Window;

import trikita.jedux.Action;
import trikita.jedux.Store;

public class WindowController implements Store.Middleware<Action, State> {
    private Window mWindow;

    public void setWindow(Window w) {
        mWindow = w;
    }

    @Override
    public void dispatch(Store<Action, State> store, Action action, Store.NextDispatcher<Action> next) {
        if (mWindow == null) {
            next.dispatch(action);
            return;
        }
        if (action.type instanceof Actions.Card && (Actions.Card) action.type == Actions.Card.MODIFY_TYPE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow.setStatusBarColor(((State.CardType) action.value).primaryDarkColor);
                return;
            }
        }
        next.dispatch(action);
    }
}
