package trikita.kumquat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.github.andrewoma.dexx.collection.List;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static trikita.anvil.DSL.*;
import trikita.anvil.Anvil;
import trikita.anvil.RenderableRecyclerViewAdapter;
import trikita.anvil.RenderableView;
import trikita.anvil.appcompat.v7.AppCompatv7DSL;
import trikita.anvil.cardview.v7.CardViewv7DSL;
import trikita.anvil.design.DesignDSL;
import trikita.anvil.recyclerview.v7.RecyclerViewv7DSL;

import trikita.jedux.Action;

import trikita.kumquat.State.CardType;

public class CardsScreen extends RenderableView {

    private CardAdapter mAdapter = new CardAdapter();

    private boolean mLockUiUpdate = false;

    public CardsScreen(Context context) {
        super(context);
    }

    protected List<State.Card> cards() {
        return App.state().cards();
    }

    protected boolean hasFAB() {
        return true;
    }

    @Override
    public void view() {
        if (!mLockUiUpdate) {
            mAdapter.notifyDataSetChanged();
        }
        linearLayout(() -> {
            orientation(LinearLayout.VERTICAL);

            AppCompatv7DSL.toolbar(() -> {
                init(() -> {
                    ((AppCompatActivity) getContext()).setSupportActionBar(Anvil.currentView());
                    ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    ((AppCompatActivity) getContext()).getSupportActionBar().setHomeButtonEnabled(true);
                    ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayShowTitleEnabled(false);
                    ((KumquatActivity) getContext()).drawerToggle().syncState();
                });
                size(FILL, dip(54));
                backgroundColor(0xff2ecc71);
                DesignDSL.compatElevation(dip(4));
                AppCompatv7DSL.navigationOnClickListener(v -> {
                    System.out.println("navigationOnClick()");
                    ((KumquatActivity) getContext()).drawer().openDrawer(GravityCompat.START);
                });

                textView(() -> {
                    size(WRAP, WRAP);
                    text("Cards");
                    textSize(sip(20));
                    layoutGravity(CENTER_VERTICAL);
                    textColor(Color.WHITE);
                });
            });

            frameLayout(() -> {
                size(FILL, 0);
                weight(1);

                RecyclerViewv7DSL.recyclerView(() -> {
                    init(() -> {
                        ItemTouchHelper.Callback cb = new CardTouchHelperCallback();
                        ItemTouchHelper touchHelper = new ItemTouchHelper(cb);
                        touchHelper.attachToRecyclerView(Anvil.currentView());
                    });
                    size(FILL, WRAP);
                    RecyclerViewv7DSL.gridLayoutManager(2);
                    RecyclerViewv7DSL.hasFixedSize(false);
                    RecyclerViewv7DSL.itemAnimator(new DefaultItemAnimator());
                    RecyclerViewv7DSL.adapter(mAdapter);
                });

                if (hasFAB()) {
                    DesignDSL.floatingActionButton(() -> {
                        size(WRAP, WRAP);
                        margin(dip(24));
                        layoutGravity(BOTTOM | END);
                        imageResource(R.drawable.ic_add);
                        DesignDSL.backgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{0xff27ae60}));
                        DesignDSL.compatElevation(dip(4));
                        onClick(v -> {
                            Intent intent = new Intent(getContext(), CardEditorActivity.class);
                            v.getContext().startActivity(intent);
                        });
                    });
                }
            });
        });
    }

    private class CardAdapter extends RenderableRecyclerViewAdapter implements ActionMode.Callback {

        private final Set<String> selected = new HashSet<>();

        private ActionMode actionMode = null;

        @Override
        public int getItemCount() {
            return cards().size();
        }

        @Override
        public int getItemViewType(int position) {
            return Arrays.asList(CardType.values()).indexOf(State.cardType(cards().get(position)));
        }

        @Override
        public void view(RecyclerView.ViewHolder holder) {
            int pos = holder.getAdapterPosition();
            String cardId = cards().get(pos).id();

            CardViewv7DSL.cardView(() -> {
                size(FILL, FILL);
                margin(dip(8));
                onClick((v) -> {
                    if (actionMode == null) {
                        Intent intent = new Intent(getContext(), CardEditorActivity.class);
                        intent.putExtra("id", cardId);
                        v.getContext().startActivity(intent);
                    } else {
                        toggleSelection(cardId);
                    }
                });
                onLongClick((v) -> {
                    if (actionMode == null) {
                        actionMode = ((AppCompatActivity) getContext()) .startSupportActionMode(this);
                        selected.add(cardId);
                    }
                    return false;
                });

                linearLayout(() -> {
                    size(FILL, FILL);
                    orientation(LinearLayout.VERTICAL);
                    margin(dip(10));
                    if (actionMode != null) {
                        backgroundColor(selected.contains(cardId) ? 0x77ffff00 : 0x77777777);
                    } else {
                        backgroundColor(0);
                    }

                    cardItem(pos, CardType.values()[holder.getItemViewType()]);
                });
            });
        }

        private void cardItem(int pos, CardType type) {
            linearLayout(() -> {
                size(FILL, WRAP);
                gravity(TOP);

                frameLayout(() -> {
                    size(0, dip(100));
                    weight(1);

                    switch (type) {
                        case BUTTON:
                            buttonItem(cards().get(pos).name().charAt(0));
                            break;
                        case SWITCH:
                            switchItem();
                            break;
                        case SLIDEBAR:
                            slidebarItem(pos);
                            break;
                        default:
                            textView(() -> {
                                size(WRAP, dip(100));
                                layoutGravity(CENTER);
                                gravity(CENTER);
                                text(""+type+" "+cards().get(pos).value());
                                typeface("fonts/Roboto-Light.ttf");
                                textSize(sip(24));
                            });
                    }
                });

                textView(() -> {
                    size(WRAP, FILL);
                    gravity(TOP);
                    text("\ue838");
                    textColor(type.primaryColor);
                    textSize(sip(20));
                    typeface("fonts/MaterialIcons-Regular.ttf");
                });
            });
            linearLayout(() -> {
                gravity(CENTER_VERTICAL);

                textView(() -> {
                    size(0, WRAP);
                    weight(1);
                    layoutGravity(CENTER_VERTICAL);
                    text(cards().get(pos).name());
                    textSize(sip(18));
                    textColor(type.primaryColor);
                    typeface("fonts/Roboto-Light.ttf");
                });

                textView(() -> {
                    size(WRAP, WRAP);
                    text("\ue3c9");
                    textColor(type.primaryColor);
                    textSize(sip(20));
                    typeface("fonts/MaterialIcons-Regular.ttf");
                });
            });
        }

        private void buttonItem(char ch) {
            ShapeDrawable circle = new ShapeDrawable(new OvalShape());
            circle.getPaint().setColor(0xff95a5a6);

            textView(() -> {
                size(dip(64), dip(64));
                layoutGravity(CENTER);
                backgroundDrawable(circle);
                text(Character.toString(ch));
                textColor(Color.WHITE);
                textSize(sip(25));
                typeface("fonts/Roboto-Light.ttf");
                allCaps(true);
                gravity(CENTER);
            });
        }

        private void switchItem() {
            AppCompatv7DSL.switchCompat(() -> {
                size(WRAP, WRAP);
                layoutGravity(CENTER);
            });
        }

        private void slidebarItem(int pos) {
            linearLayout(() -> {
                size(FILL, FILL);
                gravity(CENTER_VERTICAL);

                linearLayout(() -> {
                    size(FILL, WRAP);
                    orientation(LinearLayout.VERTICAL);

                    linearLayout(() -> {
                        size(FILL, WRAP);
                        orientation(LinearLayout.VERTICAL);
                        gravity(CENTER_HORIZONTAL);
                        margin(0, 0, 0, dip(10));

                        linearLayout(() -> {
                            size(WRAP, WRAP);
                            gravity(CENTER_VERTICAL);

                            textView(() -> {
                                size(dip(32), dip(32));
                                text("\u2014");
                                textSize(sip(24));
                                typeface("fonts/Roboto-Light.ttf");
                                textColor(0xff777777);
                                gravity(CENTER);
                            });

                            textView(() -> {
                                size(WRAP, WRAP);
                                padding(dip(5));
                                margin(dip(3), 0);
                                text("0");
                                textSize(sip(20));
                                typeface("fonts/Roboto-Light.ttf");
                                gravity(CENTER);
                            });

                            textView(() -> {
                                size(dip(32), dip(32));
                                text("+");
                                textSize(sip(24));
                                typeface("fonts/Roboto-Light.ttf");
                                textColor(0xff777777);
                                gravity(CENTER);
                            });
                        });
                    });

                    AppCompatv7DSL.appCompatSeekBar(() -> {
                        size(FILL, WRAP);
                        padding(dip(10), 0);
                    });
                });
            });
        }

        private void toggleSelection(String id) {
            if (selected.contains(id)) {
                selected.remove(id);
                if (selected.size() == 0 && actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                }
            } else {
                selected.add(id);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.multi_selector_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            App.dispatch(new Action<>(Actions.Topic.REMOVE, new HashSet<>(selected)));
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selected.clear();
            actionMode = null;
            Anvil.render();
        }

        public void exitActionMode() {
            if (actionMode != null) {
                actionMode.finish();
            }
        }
    }

    private class CardTouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            // Set movement flags based on the layout manager
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder holder, int direction) { }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            mLockUiUpdate = true;
            App.dispatch(new Action<>(Actions.Topic.MOVE, new Pair(source.getAdapterPosition(), target.getAdapterPosition())));

            // Notify the adapter of the move
            mAdapter.exitActionMode();
            mAdapter.notifyItemMoved(source.getAdapterPosition(), target.getAdapterPosition());

            return true;
        }

        @Override
        public void clearView(RecyclerView r, RecyclerView.ViewHolder holder) {
            super.clearView(r, holder);
            mLockUiUpdate = false;
            Anvil.render();
        }
    }
}
