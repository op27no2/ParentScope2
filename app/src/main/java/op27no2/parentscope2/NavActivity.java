package op27no2.parentscope2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;


public class NavActivity extends AppCompatActivity implements
        FragmentChangeListener{
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private MenuAdapter mMenuAdapter;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private ArrayList<String> mDataset= new ArrayList<String>();
    private ImageView deleteButton;
    private ImageView calendarButton;
    private ImageView helpButton;
    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        checkPermissions();
        System.out.println("Nav Activity Started");

        deleteButton = (ImageView) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminActivity fragment = (AdminActivity) getSupportFragmentManager().findFragmentByTag("admin");
                fragment.deletePressed();
            }
        });
        helpButton = (ImageView) findViewById(R.id.help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminActivity fragment = (AdminActivity) getSupportFragmentManager().findFragmentByTag("admin");
                fragment.helpPressed();
            }
        });
        calendarButton = (ImageView) findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminActivity fragment = (AdminActivity) getSupportFragmentManager().findFragmentByTag("admin");
                fragment.calendarPressed();
            }
        });

       // goToFragment(new AdminActivity(), false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);



        System.out.println("Setting up DuoDrawer");
        final DuoDrawerLayout drawerLayout = (DuoDrawerLayout) findViewById(R.id.drawer);
        DuoDrawerToggle drawerToggle = new DuoDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        System.out.println("Setting up DuoDrawer options");
        final ArrayList<String> options = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.options)));

        DuoMenuView duoMenuView = (DuoMenuView) findViewById(R.id.menu);
        final DuoMenuAdapter menuAdapter = new DuoMenuAdapter(options);
        duoMenuView.setAdapter(menuAdapter);
        duoMenuView.setOnMenuClickListener(new DuoMenuView.OnMenuClickListener() {
            @Override
            public void onFooterClicked() {
                // If the footer view contains a button
                // it will launch this method on the button click.
                // If the view does not contain a button it will listen
                // to the root view click.
            }

            @Override
            public void onHeaderClicked() {

            }

            @Override
            public void onOptionClicked(int position, Object objectClicked) {
                // Set the toolbar title
                setTitle(options.get(position));

                // Set the right options selected
                menuAdapter.setViewSelected(position, true);
                ImageView calendar = findViewById(R.id.calendar_button);
                ImageView delete = findViewById(R.id.delete_button);
                ImageView help = findViewById(R.id.help_button);

                // Navigate to the right fragment
                switch (position) {
                    case 0:
                        System.out.println("DuoDrawer 0");
                        checkLoginType();

                        break;

                    case 1:
                        System.out.println("DuoDrawer 1");
                        goToFragment(new SettingsActivity(), false, "settings");
                        calendar.setVisibility(View.GONE);
                        delete.setVisibility(View.GONE);
                        help.setVisibility(View.GONE);
                        break;

                    case 2:
                        System.out.println("DuoDrawer 2");
                        goToFragment(new HelpActivity(), false, "help");
                        calendar.setVisibility(View.GONE);
                        delete.setVisibility(View.GONE);
                        help.setVisibility(View.GONE);
                        break;

                    case 3:
                        System.out.println("DuoDrawer 3");
                        goToFragment(new UpgradeActivity(), false, "upgrade");
                        calendar.setVisibility(View.GONE);
                        delete.setVisibility(View.GONE);
                        help.setVisibility(View.GONE);
                        break;


                    default:
                        System.out.println("DuoDrawer Default");
                        goToFragment(new AdminActivity(), false, "admin");
                        if(prefs.getString("mode","").equals("admin")){
                            calendar.setVisibility(View.VISIBLE);
                            delete.setVisibility(View.VISIBLE);
                            help.setVisibility(View.VISIBLE);
                        }else {
                            calendar.setVisibility(View.GONE);
                            delete.setVisibility(View.GONE);
                            help.setVisibility(View.GONE);
                        }
                        break;
                }

                drawerLayout.closeDrawer();

            }

        });



        checkLoginType();


    }




    private void goToFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();



        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        int commit = transaction.replace(R.id.container, fragment, tag).commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();




        return super.onOptionsItemSelected(item);
    }

    public void checkLoginType(){
        String type = prefs.getString("type","");
        System.out.println("type = "+type);
        Intent btintent = null;
        Intent btintent2 = null;
        ImageView calendar = findViewById(R.id.calendar_button);
        ImageView delete = findViewById(R.id.delete_button);
        ImageView help = findViewById(R.id.help_button);

        if(type.equals("monitored")){
            goToFragment(new PasswordActivity(), false, "password");
            calendar.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            help.setVisibility(View.GONE);

        }else if(type.equals("admin")){
            goToFragment(new AdminActivity(), false, "admin");
                calendar.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                help.setVisibility(View.VISIBLE);
        }else{
            goToFragment(new SelectActivity(), false, "select");
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, Boolean addToStack, String tag) {

        FragmentManager fragmentManager = getSupportFragmentManager();;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment, tag);
        if(addToStack) {
            fragmentTransaction.addToBackStack(fragment.toString());
        }
        fragmentTransaction.commit();
    }
    @Override
    public void replaceFragmentWithTransition(Fragment fragment, Boolean addToStack, ImageView img, String tag) {

        FragmentManager fragmentManager = getSupportFragmentManager();;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment, tag);
        fragmentTransaction.addSharedElement(img, ViewCompat.getTransitionName(img));
        if(addToStack) {
            fragmentTransaction.addToBackStack(fragment.toString());
        }
        fragmentTransaction.commit();
    }



    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }

}