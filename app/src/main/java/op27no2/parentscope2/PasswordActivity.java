package op27no2.parentscope2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class PasswordActivity extends Fragment {

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
        View view = inflater.inflate(R.layout.password_activity, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();


        prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();


        editText = (EditText) view.findViewById(R.id.edit_text);

        enterButton = (Button) view.findViewById(R.id.proceed);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pw = prefs.getString("Password","");
                if(pw.equals(editText.getText().toString())){
                    MonitoredActivity frag = new MonitoredActivity();
                    showOtherFragment(frag, false);

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService( Context.INPUT_METHOD_SERVICE );
                    View f = getActivity().getCurrentFocus();
                    if( null != f && null != f.getWindowToken() && EditText.class.isAssignableFrom( f.getClass() ) )
                        imm.hideSoftInputFromWindow( f.getWindowToken(), 0 );
                    else {
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }
                }


            }
        });


        return view;
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
        fc.replaceFragment(fr, addToStack);
    }


}
