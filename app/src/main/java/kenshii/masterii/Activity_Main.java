package kenshii.masterii;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.Toast;

import org.json.JSONArray;

public class Activity_Main extends AppCompatActivity {
    public JSONArray jsonArray_anime = null;
    public Menu menuInstance = null;
    TabLayout tabLayout = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        //Toolbar will now take on default Action Bar characteristics
        setSupportActionBar(toolbar);
        //You can now use and reference the ActionBar
        getSupportActionBar().setTitle("Anime");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final AppCompatActivity thisActivity = this;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (menuInstance == null)
            menuInstance = menu;
        if (tabLayout == null) {
            tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            tabLayout.addTab(tabLayout.newTab().setText("Recent"));
            tabLayout.addTab(tabLayout.newTab().setText("TV"));
            tabLayout.addTab(tabLayout.newTab().setText("Movie"));
            tabLayout.addTab(tabLayout.newTab().setText("OVA"));
            tabLayout.addTab(tabLayout.newTab().setText("Special"));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
            final PageAdapter_Browse adapter = new PageAdapter_Browse(getSupportFragmentManager(), tabLayout.getTabCount(), this, menuInstance);
            viewPager.setAdapter(adapter);
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onPopupButtonClick(MenuItem button) {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_filter));
        popup.getMenuInflater().inflate(R.menu.menu_test, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(Activity_Main.this,
                        "Clicked popup menu item " + item.getTitle(),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        popup.show();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}