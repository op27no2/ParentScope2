package op27no2.parentscope;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;


public class SettingsActivity extends Fragment {
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Spinner recordSpinner;
    private Spinner qualitySpinner;
    private Spinner storedSpinner;
    private Spinner totalSpinner;
    private Spinner freqSpinner;
    private Spinner lengthSpinner;
    private SwitchButton deleteSwitch;
    private SwitchButton micSwitch;

    private LinearLayout adminLayout;
    private LinearLayout monitoredLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.settings_activity, container, false);
        prefs = MyApplication.getAppContext().getSharedPreferences(
                "PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

       // recordSpinner = (Spinner) view.findViewById(R.id.record_spinner);
        qualitySpinner = (Spinner) view.findViewById(R.id.quality_spinner);
        storedSpinner = (Spinner) view.findViewById(R.id.stored_spinner);
        totalSpinner = (Spinner) view.findViewById(R.id.total_spinner);
        freqSpinner = (Spinner) view.findViewById(R.id.freq_spinner);
        lengthSpinner = (Spinner) view.findViewById(R.id.length_spinner);
        deleteSwitch = view.findViewById(R.id.delete_button);
        micSwitch = view.findViewById(R.id.mic_button);
        adminLayout = view.findViewById(R.id.admin_layout);
        monitoredLayout = view.findViewById(R.id.monitored_layout);

        if(prefs.getString("type","").equals("monitored")){
            adminLayout.setVisibility(View.GONE);
            monitoredLayout.setVisibility(View.VISIBLE);
        }else if(prefs.getString("type","").equals("admin")){
            adminLayout.setVisibility(View.VISIBLE);
            monitoredLayout.setVisibility(View.GONE);
        }

     /*   ArrayList<String> mItems = new ArrayList<String>();
        mItems.add("Video");
        mItems.add("Images");
        ArrayAdapter<String> deviceArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems);
        deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recordSpinner.setAdapter(deviceArrayAdapter);

        recordSpinner.setSelection(prefs.getInt("record_setting",0));
        recordSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edt.putInt("record_setting",i);
                edt.commit();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });*/

        ArrayList<String> mItems2 = new ArrayList<String>();
        mItems2.add("Ultra High - (Video Files Will Be Large)");
        mItems2.add("High");
        mItems2.add("Medium");
        mItems2.add("Low");
        mItems2.add("Periodic Screenshots");
        ArrayAdapter<String> deviceArrayAdapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems2);
        deviceArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(deviceArrayAdapter2);

        qualitySpinner.setSelection(prefs.getInt("quality_setting",0));
        qualitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edt.putInt("quality_setting",i);
                edt.commit();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        ArrayList<String> mItems3 = new ArrayList<String>();
        mItems3.add("No Limit");
        mItems3.add("20");
        mItems3.add("10");
        mItems3.add("5");
        ArrayAdapter<String> deviceArrayAdapter3 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems3);
        deviceArrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storedSpinner.setAdapter(deviceArrayAdapter3);

        storedSpinner.setSelection(prefs.getInt("stored_setting",0));
        storedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edt.putInt("stored_setting",i);
                edt.commit();

            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });


        ArrayList<String> mItems4 = new ArrayList<String>();
        mItems4.add("No Limit");
        mItems4.add("100");
        mItems4.add("50");
        mItems4.add("40");
        mItems4.add("30");
        mItems4.add("20");
        ArrayAdapter<String> deviceArrayAdapter4 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems4);
        deviceArrayAdapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        totalSpinner.setAdapter(deviceArrayAdapter4);

        totalSpinner.setSelection(prefs.getInt("total_setting",0));
        totalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edt.putInt("total_setting",i);
                edt.commit();


            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        ArrayList<String> mItems5 = new ArrayList<String>();
        mItems5.add("All Events");
        mItems5.add("50%");
        mItems5.add("25%");
        mItems5.add("15%");
        mItems5.add("10%");
        mItems5.add("5%");
        mItems5.add("1%");
        ArrayAdapter<String> deviceArrayAdapter5 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems5);
        deviceArrayAdapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        freqSpinner.setAdapter(deviceArrayAdapter5);

        freqSpinner.setSelection(prefs.getInt("freq_setting",0));
        freqSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edt.putInt("freq_setting",i);
                edt.commit();

            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        ArrayList<String> mItems6 = new ArrayList<String>();
        mItems6.add("5 minutess");
        mItems6.add("2 minutess");
        mItems6.add("60 seconds");
        mItems6.add("30 seconds");
        mItems6.add("15 seconds%");
        mItems6.add("10 seconds");
        mItems6.add("5 seconds");
        ArrayAdapter<String> deviceArrayAdapter6 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems6);
        deviceArrayAdapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lengthSpinner.setAdapter(deviceArrayAdapter6);

        lengthSpinner.setSelection(prefs.getInt("length_setting",0));
        lengthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                edt.putInt("length_setting",i);
                edt.commit();

            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        deleteSwitch.setChecked(prefs.getBoolean("delete_setting",false));
        deleteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edt.putBoolean("delete_setting",b);
                edt.commit();

            }
        });
        micSwitch.setChecked(prefs.getBoolean("mic_setting",false));
        micSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edt.putBoolean("mic_setting",b);
                edt.commit();
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




}
