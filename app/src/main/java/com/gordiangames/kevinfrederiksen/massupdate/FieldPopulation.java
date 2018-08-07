package com.gordiangames.kevinfrederiksen.massupdate;

import android.app.Activity;
import android.widget.Toast;

import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;

/**
 * Created by Kevin Frederiksen on 2/2/2018.
 *
 * Class that is responsible for the population of fields and potential values that will be used in the update
 */
public class FieldPopulation {//start class

    private String restUrl;
    private String token;
    private String entity;
    private String field;
    private String query;
    private String value = "";
    private int iValue = 0;
    private boolean append = false;
    private boolean selection = false;
    private List<String> fieldList = new ArrayList<>();
    private HashMap<String, String> fieldTypeMap = new HashMap<>();
    private HashMap<String, String> fieldOptionsTypeMap = new HashMap<>();
    private HashMap<Integer, String> optionsMap = new HashMap<>();
    private final static RestTemplate restTemplate = createRestTemplate();
    Activity activity;

    protected FieldPopulation(Activity activity) {//start constructor

        this.activity = activity;

    }//end constructor

    protected void setFieldAdapter(String restUrl, String token, String entity) {//start setFieldAdapter

        setRestUrl(restUrl);
        setToken(token);

        try {//start try

            StandardMetaData metaData = restTemplate.getForObject(restUrl + "meta/" + entity + "?fields=*&meta=full&BhRestToken=" + token,
                    StandardMetaData.class);
            List<Field> fields = metaData.getFields();

            //fieldList, fieldTypeMap, and fieldOptionsTypeMap are set as new before setFieldList is called to allow for Address fields to
            //populate by recursion and still allow for a new list to generate should the user choose to select a different entity
            fieldList = new ArrayList<>();
            fieldTypeMap = new HashMap<>();
            fieldOptionsTypeMap = new HashMap<>();
            setFieldList(fields);

            //adds the first value as a hint
            fieldList.add(0, "Choose a field");

            //Assigns currently selected entity to the appropriate variable
            setEntity(entity);

        }//end try

        //allows for 400 and 500 errors without crashing the app
        catch(HttpClientErrorException e) {//start catch

            Toast.makeText(activity.getApplicationContext(), "Oops! Something's wrong-o, Bob-o!", Toast.LENGTH_LONG).show();

        }//end catch

    }//end setFieldAdapter

    private void setFieldList(List<Field> fields) {//start setFieldList

        for(int i = 0; i < fields.size(); i++) {//start for

            try {//start try

                //retrieves all client-facing, unhidden fields
                if(!(fields.get(i).getReadOnly())) {//start if1

                    //calls method recursively to easily pull component fields of composite types
                    if(fields.get(i).getType().equals("COMPOSITE")) {//start if2

                        setFieldList(fields.get(i).getFields());

                    }//end if2

                    else {//start else2

                        fieldList.add(fields.get(i).getName());

                        //if the meta data shows that the user would need to select from values, and has a TO_MANY or TO_ONE association,
                        //stores that into fieldOptionsTypeMap to pull from when checking the options in the spinner2 listener, and sets the
                        //fieldTypeMap to store the type for later calls
                        if(!(fields.get(i).getOptionsType().equals(null))) {//start if3

                            fieldTypeMap.put(fields.get(i).getName(), fields.get(i).getType());
                            fieldOptionsTypeMap.put(fields.get(i).getName(), fields.get(i).getOptionsType());

                        }//end if3

                    }//end else2

                }//end if1

            }//end try

            catch(NullPointerException e) {//start catch

            }//end catch

        }//end for

    }//end setFieldList

    protected void setOptionsList(String optionType, String field) {//start setOptionsList

        try {//start try

            setField(field);
            List<String> optionList = new ArrayList<>();
            HashMap<Integer, String> optionsMap = new HashMap<>();
            optionList.add("Choose an option");

            //No class existed to pull options effectively in JSON format, so I pulled the data as a String and matched the pattern instead
            String options = null;
            Pattern p = Pattern.compile("value(\"\\:)(.*?)(\\,\")label(\"\\:\")(.*?)(\"\\})");
            Pattern space = Pattern.compile("\\s");
            int start = 0;
            boolean count = true;

            do {//start do/while

                options = restTemplate.getForObject(restUrl + "/options/"+optionType+ "?start="+start+"&count=300&BhRestToken="+token,
                        String.class);
                Matcher m = space.matcher(options);
                options = m.replaceAll("");
                System.out.println(options);
                m = p.matcher(options);

                while(m.find()) {//start while2

                    optionsMap.put(Integer.parseInt(m.group(2)), m.group(5));

                }//end while2

                if(!(optionsMap.size()%300==0)) {//start if2

                    count=false;

                }//end if2

                else {//start else2

                    start += 300;

                }//end else2

            } while(count); //end do/while

            this.optionsMap = new HashMap<>();
            this.optionsMap = optionsMap;

        }//end try

        catch(HttpClientErrorException e) {//start catch

        Toast.makeText(activity.getApplicationContext(), "Oops! Something's wrong-o, Bob-o!", Toast.LENGTH_LONG).show();

        }//end catch

    }//end setOptionsList

    protected String getRestUrl() {//start getRestUrl

        return restUrl;

    }//end getRestUrl

    private void setRestUrl(String restUrl) {//start setRestUrl

        this.restUrl = restUrl;

    }//end setRestUrl

    protected String getToken() {//start getToken

        return token;

    }//end getToken

    private void setToken(String token) {//start setToken

        this.token = token;

    }//end setToken

    protected String getEntity() {//start getEntity

        return entity;

    }//end getEntity

    private void setEntity(String entity) {//start setEntity

        this.entity = entity;

    }//end set Entity

    protected String getField() {//start getField

        return field;

    }//end getField

    protected void setField(String field) {//start setField

        this.field = field;

    }//end setField

    protected String getQuery() {//start getQuery

        return query;

    }//end getQuery

    protected void setQuery(String query) {//start setQuery

        this.query = query;

    }//end setQuery

    protected String getValue() {//start getValue

        return value;

    }//end getValue

    protected void setValue(String value) {//start setValue

        this.value = value;

    }//end setValue

    protected int getiValue() {//start getiValue

        return iValue;

    }//end getiValue

    protected void setiValue(int iValue) {//start setiValue

        this.iValue = iValue;

    }//end setiValue

    protected boolean getAppend() {//start getAppend

        return append;

    }//end getAppend

    protected void setAppend() {//start setAppend

        append = !append;

    }//end setAppend

    protected boolean getSelection() {//start getSelection

        return selection;

    }//end getSelection

    protected void setSelection(boolean selection) {//start setSelection

        this.selection = selection;

    }//end setSelection

    protected HashMap<String, String> getFieldTypeMap() {//start getFieldTypeMap

        return fieldTypeMap;

    }//end getFieldTypeMap

    protected HashMap<Integer, String> getOptionsMap() {//start getOptionsMap

        return optionsMap;

    }//end getOptionsMap

    protected HashMap<String, String> getFieldOptionsTypeMap() {//start getFieldOptionsTypeMap

        return fieldOptionsTypeMap;

    }//end getFieldOptionsTypeMap

    protected List<String> getFieldList() {//start getFieldList

        return fieldList;

    }//end getFieldList

    protected RestTemplate getRestTemplate() {//start getRestTemplate

        return restTemplate;

    }//end getRestTemplate

    private static RestTemplate createRestTemplate() {//start createRestTemplate

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter<Source>());
        messageConverters.add(new MappingJackson2HttpMessageConverter());

        return new RestTemplate(messageConverters);

    }//end createRestTemplate

}//end class
