package com.gordiangames.kevinfrederiksen.massupdate;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.graphics.Color;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Kevin Frederiksen on 2/2/2018.
 *
 * Houses UI behaviors. Implements LifecycleRegistryOwner to allow for LiveData to be transferred from background to UI processes
 */

public class MainActivity extends AppCompatActivity implements LifecycleRegistryOwner {//start class

    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter2;
    final private FieldPopulation fp = new FieldPopulation(this);
    DataRepository dataRepository;
    TextView textView;

    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {//start getLifecycle

        return lifecycleRegistry;

    }//end getLifecycle

    //General method for adapting Spinner lists
    protected ArrayAdapter<String> adapt(List<String> list) {//start adapt

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, list){//start adapter constructor

            @Override
            public boolean isEnabled(int position) {//start isEnabled

                return position != 0;

            }//end isEnabled

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {//start getDropDownView

                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                if (position == 0) {//start if

                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);

                }//end if
                else {//start else

                    tv.setTextColor(Color.BLACK);

                }//end else

                return view;

            }//end getDropDownView

        };//end adapter constructor

        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        return spinnerArrayAdapter;

    }//end adapt

    @Override
    protected void onCreate(Bundle savedInstanceState) {//start onCreate

        super.onCreate(savedInstanceState);

        MyApplication app = (MyApplication)getApplicationContext();
        dataRepository = app.getDataRepository();
        setContentView(R.layout.activity_main);

        //lines 106 and 108 allow for FieldPopulation class to operate non-asynchronously
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        //Sets observer to perform update to a text view so the user can see progress as records are updated
        dataRepository.getMyData().observe(this, new Observer<String>() {//start Observer constructor

            public void onChanged(String myObject) {//start onChanged

                textView.setText(myObject);

            }//end onChanged

        });//end Observer constructor

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        final Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
        final TextView query = (TextView) findViewById(R.id.editText);
        final TextView token = (TextView) findViewById(R.id.editText2);
        final TextView restUrl = (TextView) findViewById(R.id.editText3);
        final TextView value = (TextView) findViewById(R.id.editText4);
        final CheckBox append = (CheckBox) findViewById(R.id.checkBox);
        final Button button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.progress);


        String[] entities = {"Choose an entity", "Appointment", "Candidate", "ClientContact", "ClientCorporation", "JobOrder", "JobSubmission",
                "Lead", "Note", "Opportunity", "Placement", "Sendout", "Task"};


        final List<String> entityList = new ArrayList<>(Arrays.asList(entities));
        List<String> spinner2List = new ArrayList<>();
        spinner2List.add("Choose a field");
        adapter2 = adapt(spinner2List);
        final List<String> listViewPlaceholder = new ArrayList<>();
        listViewPlaceholder.add("");

        spinner.setAdapter(adapt(entityList));
        spinner2.setAdapter(adapter2);
        spinner3.setAdapter(adapt(listViewPlaceholder));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//start spinner1 listenter

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {//start onItemSelected

                String selectedItemText = (String) parent.getItemAtPosition(position);

                // First item is disabled and used for hint
                // If user change the default selection
                if (position > 0) {//start if

                    //Sends to FieldPopulation class to retrieve and store field metadata
                    fp.setFieldAdapter(restUrl.getEditableText().toString(), token.getEditableText().toString(),
                            selectedItemText);

                    //Clears and re-sets values into the second drop-down, allowing for a live change of available fields
                    adapter2.clear();
                    adapter2.notifyDataSetChanged();
                    adapter2 = adapt(fp.getFieldList());
                    adapter2.notifyDataSetChanged();
                    spinner2.setAdapter(adapter2);

                }//end if

            }//end onItemSelected

            @Override
            public void onNothingSelected(AdapterView<?> parent) {//start onNothingSelected

            }//end onNothingSelected

        });//end spinner1 listener

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//start spinner2 listener

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {//start onItemSelected

                String selectedItemText = (String) parent.getItemAtPosition(position);

                // First item is disabled and used for hint
                // If user change the default selection
                if (position > 0) {//start if1

                    //fieldTypeMap stores fields that users would have to select values from in their Bullhorn. This checks to see if the
                    //current selected item is listed there, and populates a list of those options accordingly
                        if(fp.getFieldTypeMap().containsKey(selectedItemText)){//start if2

                            fp.setOptionsList(fp.getFieldOptionsTypeMap().get(selectedItemText),selectedItemText);
                            fp.setSelection(true);

                        }//end if2

                        else {//start else2

                            fp.setSelection(false);

                        }//end else2

                        fp.setField(selectedItemText);

                }//end if1

            }//end onItemSelected

            @Override
            public void onNothingSelected(AdapterView<?> parent) {//start onNothingSelected

            }//end onNothingSelected

        });//end spinner2 listener

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//start spinner3 listener
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {//start onItemSelected

                // First item is disabled and it is used for hint
                // If user change the default selection
                if (position > 0){//start if

                    //sets the value of the option to update the selected field with
                    fp.setiValue(Integer.parseInt(spinner3.getItemAtPosition(position).toString()));

                }//end if

            }//end onItemSelected

            @Override
            public void onNothingSelected(AdapterView<?> parent) {//start onNothingSelected

            }//end onNothingSelected

        });//end spinner3 listener

        append.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {//start append listener

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {//start onCheckedChanged

                //Calls method to switch the variable to the opposite of what it was before the change
                fp.setAppend();

            }//end onCheckedChanged

        });//end append listener

        value.addTextChangedListener(new TextWatcher() {//start value listener

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {//start beforeTextChanged

            }//end beforeTextChanged

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {//start onTextChanged

            }//end onTextChanged

            @Override
            public void afterTextChanged(Editable editable) {//start afterTextChanged

                //Checks to see if the user entered the correct ID number for the value, should the field require such input.
                //If so, it sets the iValue for updating. If not, a list of ID numbers are retrieved and displayed in spinner3 for the
                //user to choose from.
                if(fp.getSelection()) {//start if1

                    try {//start try

                        if (fp.getOptionsMap().containsKey(Integer.parseInt(value.getEditableText().toString()))){//start if2

                            fp.setiValue(Integer.parseInt(value.getEditableText().toString()));

                        }//end if2

                    }//end try
                    catch (NumberFormatException e) {//start catch

                        initList(getKeysByValue(fp.getOptionsMap(), editable.toString()), spinner3);
                        adapter.notifyDataSetChanged();
                        spinner3.setVisibility(View.VISIBLE);

                    }//end catch

                }//end if1

            }//end afterTextChanged

        });//end value listener

        button.setOnClickListener((View v) -> {//start button listner

            textView.setVisibility(View.VISIBLE);
            textView.setText("0/0");

            //Creates a new thread so the processes in RestLogic can run asynchonously, but still communicate with the UI
            Thread thread = new Thread(new Runnable() {//start Runnable constructor

                @Override
                public void run() {//start run

                    try {//start try

                        Thread.sleep(1000);

                    }//end try
                    catch (InterruptedException e) {//start catch

                        e.printStackTrace();

                    }//end catch

                    Looper.prepare();

                    //Checks to make sure the call isn't set to add onto a value that exists in a TO_ONE field
                    if(fp.getAppend()&&fp.getFieldTypeMap().containsKey(fp.getField())&& fp.getFieldTypeMap().get(fp.getField()).equals("TO_ONE")) {//start if1

                        Toast.makeText(getApplicationContext(), "TO_ONE fields cannot have multiple values!", Toast.LENGTH_SHORT).show();

                    }//end if1
                    else {//start else1

                        if(fp.getValue().equals("")){//start if2

                            fp.setValue(value.getEditableText().toString());

                        }//end if2

                        fp.setQuery(query.getEditableText().toString());
                        RestLogic rl = new RestLogic(MainActivity.this,app);
                        rl.execute(fp);

                    }//end else1

                }//end run

            });//end Runnable constructor

            thread.start();

        });//end button listener

    }//end onCreate

    //retrieves a list of keys (in this case, ID numbers for option values)
    private static <T,E> Set<T> getKeysByValue(Map<T, E> map, E value) {//start getKeysByValue

        Set<T> keys = new HashSet<T>();

        for (Map.Entry<T, E> entry : map.entrySet()) {//start for

            if (Objects.equals(value, entry.getValue())) {//start if

                keys.add(entry.getKey());

            }//end if

        }//end for

        return keys;

    }//end getKeysByValue

    //resets the values the user can choose from in spinner3
    private void initList(Set<Integer> keys, Spinner spinner3) {//start initList

        ArrayList<Integer> keysList = new ArrayList<Integer>();
        keysList.addAll(keys);
        ArrayList<String> keyList = new ArrayList<String>();
        keyList.add("Choose an option");

        for(int i = 0; i < keys.size(); i++) {//start for

            keyList.add(""+keysList.get(i));

        }//end for

        adapter=adapt(keyList);
        spinner3.setAdapter(adapter);

    }//end initList

}//end class
