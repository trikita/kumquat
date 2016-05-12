package trikita.kumquat;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.andrewoma.dexx.collection.IndexedLists;
import com.github.andrewoma.dexx.collection.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import trikita.jedux.Action;
import trikita.jedux.Store;

public class PersistenceController implements Store.Middleware<Action, State> {

    private final SharedPreferences mPreferences;
    private final Gson mGson;

    public PersistenceController(Context c) {
        mPreferences = c.getSharedPreferences("data", 0);
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(new TypeToken<List<State.Card>>() {}.getType(),
                (JsonSerializer<List<State.Card>>) (src, typeOfSrc, context) -> context.serialize(src.asList()));
        gsonBuilder.registerTypeAdapter(new TypeToken<List<State.MqttServer>>() {}.getType(),
                (JsonSerializer<List<State.MqttServer>>) (src, typeOfSrc, context) -> context.serialize(src.asList()));

        gsonBuilder.registerTypeAdapter(new TypeToken<List<State.Card>>() {}.getType(),
                (JsonDeserializer<List<State.Card>>) (src, typeOfSrc, context) ->
                        IndexedLists.copyOf((Iterable<State.Card>)
                                context.deserialize(src, new TypeToken<java.util.List<State.Card>>(){}.getType())));
        gsonBuilder.registerTypeAdapter(new TypeToken<List<State.MqttServer>>() {}.getType(),
                (JsonDeserializer<List<State.MqttServer>>) (src, typeOfSrc, context) ->
                        IndexedLists.copyOf((Iterable<State.MqttServer>)
                                context.deserialize(src, new TypeToken<java.util.List<State.MqttServer>>(){}.getType())));
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersState());

        mGson = gsonBuilder.create();
    }

    public State getSavedState() {
        if (mPreferences.contains("data")) {
            String json = mPreferences.getString("data", "");
            System.out.println("Load JSON: " + json);
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
        System.out.println("Save JSON: " + json);
        mPreferences.edit().putString("data", json).apply();
    }
}

