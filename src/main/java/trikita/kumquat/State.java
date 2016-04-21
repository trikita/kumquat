package trikita.kumquat;

import java.util.UUID;

import com.github.andrewoma.dexx.collection.List;
import com.github.andrewoma.dexx.collection.IndexedLists;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import trikita.jedux.Action;
import trikita.jedux.Store;

@Value.Immutable
@Gson.TypeAdapters
public abstract class State {

    private final static String DEFAULT_HOST = "tcp://192.168.9.124";
    private final static String DEFAULT_PORT = "1883";
    private final static String DEFAULT_CLIENT_ID = "kumquat-example";

    public static enum ConnectionStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTED;
    
        public static String toString(ConnectionStatus status) {
            if (status == CONNECTING) return "Connecting";
            if (status == CONNECTED) return "Connected";
            if (status == DISCONNECTED) return "Disconnected";
            return "";
        }
    };

    @Value.Immutable
    @Gson.TypeAdapters
    public static abstract class MqttServer {
        abstract String id();
        abstract String host();
        abstract String port();
        abstract String clientId();
        abstract ConnectionStatus status();
    }

    abstract List<MqttServer> connections();

    MqttServer getConnection(String id) {
        for (MqttServer ms : connections()) {
            if (ms.id().equals(id)) {
                return ms;
            }
        }
        return null;
    }
    
    static State getDefault() {
        return ImmutableState.builder()
            .connections(IndexedLists.of(ImmutableMqttServer.builder()
                        .id(generateId())
                        .host(DEFAULT_HOST)
                        .port(DEFAULT_PORT)
                        .clientId(DEFAULT_CLIENT_ID)
                        .status(ConnectionStatus.DISCONNECTED)
                        .build()))
            .build();
    }

    static String generateId() {
        return UUID.randomUUID().toString();
    }

    static class Reducer implements Store.Reducer<Action, State> {

        public State reduce(Action action, State old) {
            return ImmutableState.builder().from(old)
                .connections(reduceConnections(action, old.connections()))
                .build();
        }

        List<MqttServer> reduceConnections(Action action, List<MqttServer> connections) {
            if (action.type instanceof Actions.Connection) {
                Actions.Connection type = (Actions.Connection) action.type;
                switch (type) {
                    case CONNECT:
                        return withStatus(connections, (String) action.value, ConnectionStatus.CONNECTING);
                    case CONNECTED:
                        return withStatus(connections, (String) action.value, ConnectionStatus.CONNECTED);
                    case DISCONNECT:
                        return withStatus(connections, (String) action.value, ConnectionStatus.DISCONNECTED);
                    case ADD:
                    case EDIT:
                    case REMOVE:
                }
            }
            return connections;
        }

        static List<MqttServer> withStatus(List<MqttServer> list, String id, ConnectionStatus status) {
            int index = 0;
            for (MqttServer ms : list) {
                if (ms.id().equals(id)) {
                    break;
                }
                index++;
            }
            return IndexedLists.copyOf(list).set(index, ImmutableMqttServer.copyOf(list.get(index)).withStatus(status));
        }
    }
}
