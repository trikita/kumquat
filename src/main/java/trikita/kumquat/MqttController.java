package trikita.kumquat;

import android.content.Context;
import android.util.Log;

import com.github.andrewoma.dexx.collection.List;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import org.eclipse.paho.android.service.*;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallback;

import trikita.jedux.Action;
import trikita.jedux.Store;

import trikita.kumquat.State.ConnectionStatus;
import trikita.kumquat.State.MqttServer;

public class MqttController implements Store.Middleware<Action, State> {
    private final static String tag = "MqttController";

    private Context mContext;
    private static Map<String, MqttAndroidClient> mClients = Collections.synchronizedMap(new HashMap<>());

    public MqttController(Context context, List<MqttServer> connections) {
        mContext = context;

        String id;
        MqttServer ms;
        for (MqttServer conn : connections) {
            initClient(conn.id(), conn);
        }
    }

    @Override
    public void dispatch(Store<Action, State> store, Action action, Store.NextDispatcher<Action> next) {
        if (action.type instanceof Actions.Connection) {
            Actions.Connection type = (Actions.Connection) action.type;
            String id;
            switch (type) {
                case CONNECT:
                    id = (String) action.value;
                    initClient(id, store.getState().getConnection(id));
                    break;
                case DISCONNECT:
                    id = (String) action.value;
                    if (store.getState().getConnection(id).status() != ConnectionStatus.DISCONNECTED) {
                        disconnect(id);
                    }
                    break;
            }
        }
        next.dispatch(action);
    }

    private void initClient(String id, MqttServer ms) {
        MqttAndroidClient client = new MqttAndroidClient(mContext, ms.host()+":"+ms.port(), ms.clientId());
        mClients.put(id, client);
        try {
            connect(id);
        } catch (MqttException e) {
            Log.d(tag, "Failed to connect to "+ms.host()+":"+ms.port());
            mClients.remove(id);
            e.printStackTrace();
        }
    }

    private void connect(String connId) throws MqttException {
        MqttAndroidClient client = mClients.get(connId);
        assert client != null;

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable e) {
                Log.d(tag, "Connection lost");
                e.printStackTrace();
                App.dispatch(new Action<>(Actions.Connection.CONNECT, connId));
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(tag, "Incoming message: " + new String(message.getPayload()));
                // TODO emit an action
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) { }
        });

        IMqttToken token = client.connect();
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(tag, "Connected successfully");
                //subscribe();
                App.dispatch(new Action<>(Actions.Connection.CONNECTED, connId));
            }
            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                Log.d(tag, "Failed to connect");
                App.dispatch(new Action<>(Actions.Connection.DISCONNECT, connId));
                e.printStackTrace();
            }
        });
    }

    private void disconnect(String id) {
        try {
            mClients.get(id).disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
