package trikita.kumquat;

import android.content.Context;
import android.os.Handler;
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

import trikita.kumquat.State.Card;
import trikita.kumquat.State.ConnectionStatus;
import trikita.kumquat.State.MqttServer;

public class MqttController implements Store.Middleware<Action, State> {
    private final static String tag = "MqttController";
    private final static long INIT_CONNECT_DELAY = 100; // 100ms

    private Context mContext;
    private static Map<String, MqttAndroidClient> mClients = Collections.synchronizedMap(new HashMap<>());

    private Handler mHandler = new Handler();

    public MqttController(Context context, List<MqttServer> connections) {
        mContext = context;

        mHandler.postDelayed(() -> {
            String id;
            MqttServer ms;
            for (MqttServer conn : connections) {
                Log.d(tag, "MQTT client init");
                initClient(conn.id(), conn);
            }
        }, INIT_CONNECT_DELAY);
    }

    @Override
    public void dispatch(Store<Action, State> store, Action action, Store.NextDispatcher<Action> next) {
        if (action.type instanceof Actions.Connection) {
            Actions.Connection type = (Actions.Connection) action.type;
            String id;
            switch (type) {
                case CONNECT:
                    id = (String) action.value;
                    if (store.getState().getConnection(id).status() == ConnectionStatus.DISCONNECTED) {
                        System.out.println("CONNECT " + id);
                        System.out.println("store: " + store);
                        System.out.println("state: " + store.getState());
                        System.out.println("conn: " + store.getState().getConnection(id));
                        initClient(id, store.getState().getConnection(id));
                    } else {
                        System.out.println("CONNECT " + id + ". Already in progress");
                        return;
                    }
                    break;
                case DISCONNECT:
                    id = (String) action.value;
                    System.out.println("DISCONNECT " + id);
                    System.out.println("store: " + store);
                    System.out.println("state: " + store.getState());
                    System.out.println("conn: " + store.getState().getConnection(id));
                    if (store.getState().getConnection(id).status() != ConnectionStatus.DISCONNECTED) {
                        disconnect(id);
                    }
                    break;
            }
        } else if (action.type instanceof Actions.Topic) {
            Actions.Topic type = (Actions.Topic) action.type;
            Card card;
            switch (type) {
                case CREATE:
                    card = (Card) action.value;
                    System.out.println("CREATE: card="+card.id());
                    for (Card c : store.getState().cards()) {
                        System.out.println("existing card " + c.id() + " " + c.topic());
                    }
                    subscribe(card);
                    break;
            }
        }
        next.dispatch(action);
    }

    private void initClient(String id, MqttServer ms) {
        MqttAndroidClient client = new MqttAndroidClient(mContext, ms.host()+":"+ms.port(), ms.clientId());
        mClients.put(id, client);
        try {
            System.out.println("Start connecting to " + id + ". Status " + ms.status().toString());
            connect(id);
        } catch (MqttException e) {
            Log.d(tag, "Failed to connect to "+ms.host()+":"+ms.port()+". Status " + ms.status().toString());
            mClients.remove(id);
            e.printStackTrace();
        }
    }

    private void connect(String connId) throws MqttException {
        Log.d(tag, "connect(): "+connId);
        MqttAndroidClient client = mClients.get(connId);
        if (client == null) {
            Log.d(tag, "could not create MQTT client");
            return;
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable e) {
                Log.d(tag, "Connection lost =====================");
                if (e != null) {
                    e.printStackTrace();
                }
                System.out.println("Status "+connId+": "+App.state().getConnection(connId).status());
                if (App.state().getConnection(connId).status() != ConnectionStatus.DISCONNECTED) {
                    App.dispatch(new Action<>(Actions.Connection.CONNECT, connId));
                }
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(tag, "Incoming message: " + new String(message.getPayload()));
                java.util.List<Card> cards = App.state().getCardsByTopic(topic, connId);
                if (cards.size() != 0) {
                    System.out.println("messageArrived(): conn "+connId+" topic "+topic);
                    for (Card c : cards) {
                        c = ImmutableCard.copyOf(c).withValue(new String(message.getPayload()));
                        App.dispatch(new Action<>(Actions.Topic.MODIFY, c));
                    }
                } else {
                    System.out.println("messageArrived(): no card for conn "+connId+" with topic "+topic);
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) { }
        });

        IMqttToken token = client.connect();
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(tag, "Connected successfully");
                for (Card c : App.state().cards()) {
                    if (c.connId().equals(connId)) {
                        System.out.println("subscripting: serverId="+connId+" topic="+c.topic());
                        subscribe(c);
                    }
                }
                System.out.println("Status "+App.state().getConnection(connId).status());
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

    private void subscribe(Card c) {
        MqttAndroidClient client = mClients.get(c.connId());
        if (client == null) {
            throw new RuntimeException("No MQTT client with id " + c.connId());
        }

        System.out.println("subscribe(): card="+c.id()+" topic="+c.topic());

        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(c.topic(), qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscripted to the topic "+asyncActionToken.getTopics()[0]);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to subscribe to the topic "+asyncActionToken.getTopics()[0]);
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void disconnect(String id) {
        try {
            System.out.println("disconnect(): "+ id + " " + App.state().getConnection(id).status().toString());
            mClients.get(id).disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        } finally {
            mClients.remove(id);
        }
    }
}
