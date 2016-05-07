package trikita.kumquat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import java.util.Arrays;

import trikita.anvil.Anvil;
import trikita.anvil.RenderableAdapter;
import trikita.anvil.RenderableView;
import trikita.jedux.Action;

import static trikita.anvil.DSL.*;
import static trikita.anvil.support.v4.Supportv4DSL.*;

public class KumquatActivity extends AppCompatActivity {

    private NavigationScreen navigationScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigationScreen = new NavigationScreen(this);
        setContentView(navigationScreen);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return navigationScreen.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @SuppressLint("ViewConstructor")
    private static class NavigationScreen extends RenderableView {

        private final AppCompatActivity activity;
        private DrawerLayout drawer;
        private ActionBarDrawerToggle drawerToggle;

        private RenderableAdapter navAdapter = RenderableAdapter.withItems(Arrays.asList(State.Navigation.values()), this::navigationItemView);

        public NavigationScreen(AppCompatActivity activity) {
            super(activity);
            this.activity = activity;
        }

        @Override
        public void view() {
            drawerLayout(() -> {
                init(() -> {
                    drawer = Anvil.currentView();
                    drawerToggle = new ActionBarDrawerToggle(activity, drawer, R.string.drawer_open, R.string.drawer_close);
                    drawer.addDrawerListener(drawerToggle);
                    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    activity.getSupportActionBar().setHomeButtonEnabled(true);
                    drawerToggle.syncState();
                });
                size(FILL, FILL);
                v(App.state().screen().viewClass, () -> {
                    size(FILL, FILL);
                });
                listView(() -> {
                    size(dip(240), FILL);
                    layoutGravity(START);
                    adapter(navAdapter);
                    backgroundColor(Color.WHITE);
                    onItemClick((av, v, pos, id) -> {
                        State.Navigation nav = State.Navigation.values()[pos];
                        post(() -> App.dispatch(new Action<>(Actions.Navigation.NAVIGATE, nav)));
                        drawer.closeDrawer(av);
                    });
                });
            });
        }

        private void navigationItemView(int index, State.Navigation item) {
            textView(() -> {
                size(FILL, dip(48));
                text(item.nameResource);
            });
        }

        public boolean onOptionsItemSelected(MenuItem item) {
            return drawerToggle.onOptionsItemSelected(item);
        }
    }
}
