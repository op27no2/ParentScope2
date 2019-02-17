package op27no2.parentscope;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SelectActivity extends AppCompatActivity {

    private Button adminButton;
    private Button monitoredButton;
    private Button enterButton;
    private EditText editText;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private LinearLayout passwordLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_activity);
        prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        passwordLayout = (LinearLayout) findViewById(R.id.password_layout);
        passwordLayout.setVisibility(View.GONE);
        editText = (EditText) findViewById(R.id.edit_text);

        checkLoginType();

        //Admin
        adminButton = (Button) findViewById(R.id.admin);
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent btintent = null;
                btintent = new Intent(SelectActivity.this, AdminActivity.class);
                //TODO add back
                //btintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(btintent);

                edt.putString("type","admin");
                edt.commit();
            }
        });

        //Show Monitored
        monitoredButton = (Button) findViewById(R.id.monitor);
        monitoredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordLayout.setVisibility(View.VISIBLE);
            }
        });

        //Monitored
        enterButton = (Button) findViewById(R.id.enter);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edt.putString("Password", editText.getText().toString());
                edt.commit();

                Intent btintent = null;
                btintent = new Intent(SelectActivity.this, MonitoredActivity.class);
                //TODO add back
                //btintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(btintent);

                edt.putString("type","monitored");
                edt.commit();

            }
        });

    }


    public void checkLoginType(){
        String type = prefs.getString("type","");
        System.out.println("type = "+type);
        Intent btintent = null;
        Intent btintent2 = null;
        if(type.equals("monitored")){
            btintent = new Intent(SelectActivity.this, PasswordActivity.class);
            //TODO add back
            //btintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(btintent);
        }else if(type.equals("admin")){
            btintent2 = new Intent(SelectActivity.this, AdminActivity.class);
            //TODO add back
            //btintent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(btintent2);
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





}
