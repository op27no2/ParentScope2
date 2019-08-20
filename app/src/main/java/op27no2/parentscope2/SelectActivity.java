package op27no2.parentscope2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SelectActivity extends Fragment {

    private Button adminButton;
    private Button monitoredButton;
    private Button enterButton;
    private EditText editText;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private LinearLayout passwordLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.select_activity, container, false);

        prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        passwordLayout = (LinearLayout) view.findViewById(R.id.password_layout);
        passwordLayout.setVisibility(View.GONE);
        editText = (EditText) view.findViewById(R.id.edit_text);

        checkLoginType();

        //Admin
        adminButton = (Button) view.findViewById(R.id.admin);
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdminActivity frag = new AdminActivity();
                showOtherFragment(frag, false);

                edt.putString("type","admin");
                edt.commit();
            }
        });

        //Show Monitored
        monitoredButton = (Button) view.findViewById(R.id.monitor);
        monitoredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordLayout.setVisibility(View.VISIBLE);
            }
        });

        //Monitored
        enterButton = (Button) view.findViewById(R.id.enter);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edt.putString("Password", editText.getText().toString());
                edt.putString("type","monitored");
                edt.commit();

                MonitoredActivity frag = new MonitoredActivity();
                showOtherFragment(frag, false);
            }
        });

        return view;
    }


    public void checkLoginType(){
        String type = prefs.getString("type","");
        System.out.println("type = "+type);
        Intent btintent = null;
        Intent btintent2 = null;
        if(type.equals("monitored")){
            MonitoredActivity frag = new MonitoredActivity();
            showOtherFragment(frag, false);

        }else if(type.equals("admin")){
            AdminActivity frag = new AdminActivity();
            showOtherFragment(frag, false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    @Override
    public void onStop() {
        super.onStop();


    }

    public void showOtherFragment(Fragment fr, Boolean addToStack)
    {
        FragmentChangeListener fc=(FragmentChangeListener)getActivity();
        fc.replaceFragment(fr,addToStack);
    }




}
