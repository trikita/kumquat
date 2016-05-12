package trikita.kumquat;

import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.Set;
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

    private final static String DEFAULT_SERVER = "tcp://192.168.57.1:1883";

    public enum Navigation {
        CONNECTIONS(ConnectionsScreen.class, R.string.nav_connections),
        CARDS(CardsScreen.class, R.string.nav_cards),
        FAVOURITES(FavouritesScreen.class, R.string.nav_favourites);

        public final Class<? extends View> viewClass;
        public final int nameResource;

        Navigation(Class<? extends View> viewClass, int nameResource) {
            this.viewClass = viewClass;
            this.nameResource = nameResource;
        }
    }

    public enum ConnectionStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTED;
    
        public static String toString(ConnectionStatus status) {
            if (status == CONNECTING) return "Connecting";
            if (status == CONNECTED) return "Connected";
            if (status == DISCONNECTED) return "Disconnected";
            return "";
        }
    }

    @Value.Immutable
    @Gson.TypeAdapters
    public static abstract class MqttServer {
        public abstract String id();
        public abstract String name();
        public abstract String uri();
        public abstract String clientId();
        public abstract String username();
        public abstract String password();
        public abstract String willTopic();
        public abstract String willPayload();
        public abstract int willQoS();
        public abstract boolean willRetain();
        // TODO: SSL
        public abstract ConnectionStatus status();
    }

    @Value.Immutable
    @Gson.TypeAdapters
    public static abstract class Card {
        public abstract String id();
        public abstract String connId();
        public abstract String name();
        public abstract String topic();
        public abstract String value();
        public abstract int subQoS();
        public abstract CardParams params();

        @Gson.ExpectedSubtypes({
                TextCardParams.class,
                EditTextCardParams.class,
                ButtonCardParams.class,
                ToggleCardParams.class,
                SliderCardParams.class
        })
        public interface CardParams {}
        @Value.Immutable public interface TextCardParams extends CardParams {}
        @Value.Immutable public interface EditTextCardParams extends CardParams {}
        @Value.Immutable public interface ButtonCardParams extends CardParams {
            String payload();
        }
        @Value.Immutable public interface ToggleCardParams extends CardParams {
            String onPayload();
            String offPayload();
        }
        @Value.Immutable public interface SliderCardParams extends CardParams {
            float min();
            float max();
            float step();
        }
    }

    public abstract List<MqttServer> connections();
    public abstract List<Card> cards();
    public abstract List<Card> favourites();
    public abstract Navigation screen();

    MqttServer getConnection(String id) {
        //System.out.println("Requested connection: " + id);
        for (MqttServer ms : connections()) {
            //System.out.println("Iterating connection: " + ms.id());
            if (ms.id().equals(id)) {
                return ms;
            }
        }
        System.out.println("Connection not found: " + id);
        return null;
    }
    
    Card getCard(String id) {
        for (Card c : cards()) {
            if (c.id().equals(id)) {
                return c;
            }
        }
        return null;
    }

    java.util.List<Card> getCardsByTopic(String topic, String connId) {
        java.util.List<Card> list = new ArrayList<>();
        for (Card c : cards()) {
            if (c.connId().equals(connId) && c.topic().equals(topic)) {
                list.add(c);
            }
        }
        return list;
    }

    static State getDefault() {
        return ImmutableState.builder()
                .connections(IndexedLists.of(ImmutableMqttServer.builder()
                        .id(generateId())
                        .name("Default")
                        .uri(DEFAULT_SERVER)
                        .clientId("")
                        .status(ConnectionStatus.DISCONNECTED)
                        .username("")
                        .password("")
                        .willTopic("")
                        .willPayload("")
                        .willQoS(0)
                        .willRetain(false)
                        .build()))
            .cards(defaultCards())
            .favourites(IndexedLists.of())
            .screen(Navigation.CONNECTIONS)
            .build();
    }

    private static List<Card> defaultCards() {
        List<Card> list = IndexedLists.of();
        String[] topics = { "1", "2", "3", "4", "5", "6", "7" };
        for (String topic : topics) {
            list = list.append(ImmutableCard.builder()
                    .id(generateId())
                    .name("")
                    .topic(topic)
                    .value("")
                    .connId("")
                    .subQoS(0)
                    .params(ImmutableTextCardParams.builder().build())
                    .build());
        }
        return list;
    }

    static String generateId() {
        return UUID.randomUUID().toString();
    }

    static class Reducer implements Store.Reducer<Action, State> {

        public State reduce(Action action, State old) {
            return ImmutableState.builder().from(old)
                    .connections(reduceConnections(action, old.connections()))
                    .cards(reduceCards(action, old.cards()))
                    .screen(reduceNavigation(action, old.screen()))
                    .build();
        }

        private Navigation reduceNavigation(Action action, Navigation screen) {
            if (action.type instanceof Actions.Navigation) {
                return (Navigation) action.value;
            }
            return screen;
        }

        private List<Card> reduceCards(Action action, List<Card> cards) {
            if (action.type instanceof Actions.Topic) {
                Actions.Topic type = (Actions.Topic) action.type;
                switch (type) {
                    case PUBLISH:
                        break;
                    case CREATE:
                        return cards.append((Card) action.value);
                    case MODIFY:
                        Card modifiedCard = (Card) action.value;
                        int index = cardIndexOf(cards, modifiedCard.id());
                        return IndexedLists.copyOf(cards).set(index, modifiedCard);
                    case MOVE:
                        int from = ((Pair<Integer, Integer>) action.value).first;
                        int to = ((Pair<Integer, Integer>) action.value).second;
                        Card moved = cards.get(from);
                        List<Card> updated = IndexedLists.of();
                        if (from < to) {
                            for (int i = 0; i < cards.size(); i++) {
                                if (i == from) {
                                    continue;
                                }
                                updated = updated.append(cards.get(i));
                                if (i == to) {
                                    updated = updated.append(moved);
                                }
                            }
                        } else {
                            for (int i = 0; i < cards.size(); i++) {
                                if (i == from) {
                                    continue;
                                }
                                if (i == to) {
                                    updated = updated.append(moved);
                                }
                                updated = updated.append(cards.get(i));
                            }
                        }
                        return updated;
                    case REMOVE:
                        List<Card> filtered = IndexedLists.of();
                        Set<String> removed = (Set<String>) action.value;
                        for (Card c : cards) {
                            if (!removed.contains(c.id())) {
                                filtered = filtered.append(c);
                            }
                        }
                        return filtered;
                }
            }
            return cards;
        }

        private List<MqttServer> reduceConnections(Action action, List<MqttServer> connections) {
            if (action.type instanceof Actions.Connection) {
                Actions.Connection type = (Actions.Connection) action.type;
                switch (type) {
                    case CONNECT:
                        return withStatus(connections, (String) action.value, ConnectionStatus.CONNECTING);
                    case CONNECTED:
                        return withStatus(connections, (String) action.value, ConnectionStatus.CONNECTED);
                    case DISCONNECT:
                        return withStatus(connections, (String) action.value, ConnectionStatus.DISCONNECTED);
                    case CREATE:
                        return connections.append((MqttServer) action.value);
                    case MODIFY:
                        MqttServer modifiedConn = (MqttServer) action.value;
                        int i = connIndexOf(connections, modifiedConn.id());
                        return IndexedLists.copyOf(connections).set(i, modifiedConn);
                    case REMOVE:
                        List<MqttServer> filtered = IndexedLists.of();
                        Set<String> removed = (Set<String>) action.value;
                        for (MqttServer conn : connections) {
                            if (!removed.contains(conn.id())) {
                                filtered = filtered.append(conn);
                            }
                        }
                        return filtered;
                }
            }
            return connections;
        }

        static int cardIndexOf(List<Card> list, String id) {
            int index = 0;
            for (Card card : list) {
                if (card.id().equals(id)) {
                    return index;
                }
                index++;
            }
            return -1;
        }

        static int connIndexOf(List<MqttServer> list, String id) {
            int index = 0;
            for (MqttServer ms : list) {
                if (ms.id().equals(id)) {
                    return index;
                }
                index++;
            }
            return -1;
        }

        static List<MqttServer> withStatus(List<MqttServer> list, String id, ConnectionStatus status) {
            int i = connIndexOf(list, id);
            return IndexedLists.copyOf(list).set(i, ImmutableMqttServer.copyOf(list.get(i)).withStatus(status));
        }
    }
}
