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

public class PasswordActivity extends AppCompatActivity {

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
        setContentView(R.layout.password_activity);
        prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();


        editText = (EditText) findViewById(R.id.edit_text);

        enterButton = (Button) findViewById(R.id.proceed);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pw = prefs.getString("Password","");
                if(pw.equals(editText.getText().toString())){
                    Intent btintent = null;
                    btintent = new Intent(PasswordActivity.this, MonitoredActivity.class);
                    //TODO add back
                    //btintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(btintent);
                }


            }
        });

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
