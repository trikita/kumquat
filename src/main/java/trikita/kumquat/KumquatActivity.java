package trikita.kumquat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import static trikita.anvil.DSL.*;
import static trikita.anvil.support.v4.Supportv4DSL.*;
import trikita.anvil.Anvil;
import trikita.anvil.RenderableView;
import trikita.anvil.design.DesignDSL;

import trikita.jedux.Action;

public class KumquatActivity extends AppCompatActivity {

    private NavigationScreen navigationScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigationScreen = new NavigationScreen(this);
        setContentView(navigationScreen);
    }

    public DrawerLayout drawer() {
        return navigationScreen.getDrawer();
    }

    public ActionBarDrawerToggle drawerToggle() {
        return navigationScreen.getDrawerToggle();
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

        public NavigationScreen(AppCompatActivity activity) {
            super(activity);
            this.activity = activity;
        }

        public DrawerLayout getDrawer() {
            return drawer;
        }

        public ActionBarDrawerToggle getDrawerToggle() {
            return drawerToggle;
        }

        @Override
        public void view() {
            drawerLayout(() -> {
                init(() -> {
                    drawer = Anvil.currentView();
                    drawerToggle = new ActionBarDrawerToggle(activity, drawer, R.string.drawer_open, R.string.drawer_close);
                    drawer.addDrawerListener(drawerToggle);
                    drawerToggle.setDrawerIndicatorEnabled(true);
                    drawerToggle.syncState();
                });
                size(FILL, FILL);
                fitsSystemWindows(true);

                v(App.state().screen().viewClass, () -> {
                    size(FILL, FILL);
                });

                DesignDSL.navigationView(() -> {
                    init(() -> {
                        NavigationView navbar = Anvil.currentView();
                        navbar.inflateMenu(R.menu.drawer);
                        // FIXME depend on the currently displayed screen
                        navbar.setCheckedItem(R.id.nav_connections);
                    });
                    size(WRAP, FILL);
                    layoutGravity(GravityCompat.START);
                    fitsSystemWindows(true);
                    DesignDSL.navigationItemSelectedListener(item -> {
                        item.setChecked(true);
                        State.Navigation nav = State.Navigation.from(item.getItemId());
                        post(() -> App.dispatch(new Action<>(Actions.Navigation.NAVIGATE, nav)));
                        drawer.closeDrawers();
                        return true;
                    });
                });
            });
        }

        public boolean onOptionsItemSelected(MenuItem item) {
            return drawerToggle.onOptionsItemSelected(item);
        }
    }
}
