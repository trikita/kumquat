package trikita.kumquat;

import android.app.Application;

import trikita.anvil.Anvil;
import trikita.jedux.Action;
import trikita.jedux.Store;

public class App extends Application {

    private static App sInstance;

    private Store<Action, State> store;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        PersistenceController persistenceController = new PersistenceController(this);
        State initialState = persistenceController.getSavedState();
        if (initialState == null) {
            initialState = State.getDefault();
        }
        MqttController mqtt = new MqttController(this, initialState.connections());

        this.store = new Store<>(new State.Reducer(), initialState, persistenceController, mqtt);

        this.store.subscribe(Anvil::render);
    }

    public static State state() {
        return sInstance.store.getState();
    }

    public static State dispatch(Action action) {
        return sInstance.store.dispatch(action);
    }
}


