package trikita.kumquat;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import trikita.jedux.Action;
import trikita.jedux.Store;

public class PersistenceController implements Store.Middleware<Action, State> {

    private final SharedPreferences mPreferences;
    private final Gson mGson;

    public PersistenceController(Context c) {
        mPreferences = c.getSharedPreferences("data", 0);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersState());
        mGson = gsonBuilder.create();
    }

    public State getSavedState() {
        if (mPreferences.contains("data")) {
            String json = mPreferences.getString("data", "");
            try {
                return mGson.fromJson(json, ImmutableState.class);
            } catch (Exception ignored) {}
        }
        return null;
    }

    @Override
    public void dispatch(Store<Action, State> store, Action action,
                         Store.NextDispatcher<Action> nextDispatcher) {
        nextDispatcher.dispatch(action);
        String json = mGson.toJson(store.getState());
        mPreferences.edit().putString("data", json).apply();
    }
}

