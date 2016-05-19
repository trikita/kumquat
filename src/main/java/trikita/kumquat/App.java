package trikita.kumquat;

import android.app.Application;

import trikita.anvil.Anvil;
import trikita.jedux.Action;
import trikita.jedux.Store;

public class App extends Application {

    private static App sInstance;

    private Store<Action, State> store;
    private WindowController windowController;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        this.windowController = new WindowController();

        PersistenceController persistenceController = new PersistenceController(this);
        State initialState = persistenceController.getSavedState();
        if (initialState == null) {
            initialState = State.getDefault();
        }
        MqttController mqtt = new MqttController(this, initialState.connections());

        this.store = new Store<>(new State.Reducer(),
                initialState,
                this.windowController,
                persistenceController,
                mqtt);

        this.store.subscribe(Anvil::render);
    }

    public static State state() {
        return sInstance.store.getState();
    }

    public static State dispatch(Action action) {
        return sInstance.store.dispatch(action);
    }

    public static WindowController getWindowController() {
        return sInstance.windowController;
    }
}
