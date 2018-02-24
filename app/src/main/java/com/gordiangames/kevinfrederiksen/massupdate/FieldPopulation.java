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
public class FieldPopulation {

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
    private HashMap<String, String> fieldTypeMap = new HashMap<String, String>();
    private HashMap<String, String> fieldOptionsTypeMap = new HashMap<String, String>();
    private HashMap<Integer, String> optionsMap = new HashMap<Integer, String>();
    private final static RestTemplate restTemplate = createRestTemplate();
    Activity activity;

    protected FieldPopulation(Activity activity){
        this.activity=activity;
    }

    protected void setFieldAdapter(String restUrl, String token, String entity){

        setRestUrl(restUrl);
        setToken(token);
        try{
        StandardMetaData metaData = restTemplate.getForObject(restUrl +
                        "meta/" + entity + "?fields=*&meta=full&BhRestToken=" + token,
                StandardMetaData.class);
        List<Field> fields = metaData.getFields();

        //fieldList, fieldTypeMap, and fieldOptionsTypeMap are set as new before setFieldList is called to allow for Address fields to populate
        //by recursion and still allow for a new list to generate should the user choose to select a different entity
        fieldList = new ArrayList<>();
        fieldTypeMap = new HashMap<String, String>();
        fieldOptionsTypeMap = new HashMap<String, String>();
        setFieldList(fields);

        //adds the first value as a hint
        fieldList.add(0, "Choose a field");

        //brute forces the state, country, and, for ClientContacts, desiredCategories and desiredSkills to populate as expected
        fieldTypeMap.put("state","");
        fieldTypeMap.put("countryID","");
        fieldOptionsTypeMap.put("state", "State");
        fieldOptionsTypeMap.put("countryId", "Country");
        if(entity.equals("ClientContact")){
            fieldTypeMap.put("desiredCategories","");fieldTypeMap.put("desiredSkills","");
            fieldOptionsTypeMap.put("desiredCategories","Category");fieldOptionsTypeMap.put("desiredSkills","Skill");
        }
        this.entity = entity;

        //allows for 400 and 500 errors without crashing the app
        } catch( HttpClientErrorException e){
            Toast.makeText
                    (activity.getApplicationContext(), "Oops! Something's wrong-o, Bob-o!", Toast.LENGTH_LONG)
                    .show();
        }

    }

    private void setFieldList(List<Field> fields){

        for(int i = 0; i < fields.size(); i++){
            try{
                //retrieves all client-facing, unhidden fields
                if(!(fields.get(i).getReadOnly())){
                    //calls method recursively to easily pull component fields of composite types
                    if(fields.get(i).getType().equals("COMPOSITE")){
                        setFieldList(fields.get(i).getFields());
                    }else{

                        fieldList.add(fields.get(i).getName());
                        //if the meta data shows that the user would need to select from values, and has a TO_MANY or TO_ONE association,
                        //stores that into fieldOptionsTypeMap to pull from when checking the options in the spinner2 listener
                        if(fields.get(i).getInputType().equals("SELECT")&&(fields.get(i).getType().equals("TO_MANY")||
                                fields.get(i).getType().equals("TO_ONE")))
                            fieldTypeMap.put(fields.get(i).getName(), fields.get(i).getType());
                            fieldOptionsTypeMap.put(fields.get(i).getName(), fields.get(i).getOptionsType());

                    }

                }

            }catch(NullPointerException e){}
        }
    }

    protected void setOptionsList(String optionType, String field){
        try{
        this.field = field;
        List<String> optionList = new ArrayList<>();
        HashMap<Integer, String> optionsMap = new HashMap<Integer, String>();
        optionList.add("Choose an option");

        //No class existed to pull options effectively in JSON format, so I pulled the data as a String and matched the pattern instead
        String options = null;
        Pattern p = Pattern.compile("value(\"\\:)(.*?)(\\,\")label(\"\\:\")(.*?)(\"\\})");
        Pattern space = Pattern.compile("\\s");
        int start = 0;
        boolean count = true;

        do{
            options = restTemplate.getForObject(restUrl + "/options/"+optionType+
                    "?start="+start+"&count=300&BhRestToken="+token, String.class);
            Matcher m = space.matcher(options);
            options = m.replaceAll("");
            System.out.println(options);
            m = p.matcher(options);
            while(m.find()){

                optionsMap.put(Integer.parseInt(m.group(2)), m.group(5));

            }

            if(!(optionsMap.size()%300==0)){
                count=false;
            }
            else start +=300;

        }while(count);

        this.optionsMap = new HashMap<Integer, String>();
        this.optionsMap = optionsMap;} catch( HttpClientErrorException e){
        Toast.makeText
                (activity.getApplicationContext(), "Oops! Something's wrong-o, Bob-o!", Toast.LENGTH_LONG)
                .show();
    }

    }

    protected HashMap<String, String> getFieldTypeMap(){
        return fieldTypeMap;
    }

    protected int getiValue(){
        return iValue;
    }

    protected HashMap<Integer, String> getOptionsMap(){
        return optionsMap;
    }

    protected HashMap<String, String> getFieldOptionsTypeMap(){
        return fieldOptionsTypeMap;
    }

    protected void setQuery(String query){
        this.query = query;
    }

    protected void setiValue(int iValue){
        this.iValue = iValue;
    }

    protected void setAppend(){
        append = !append;
    }

    protected void setValue(String value){
        this.value = value;
    }

    protected void setSelection(boolean b){
        selection = b;
    }

    protected List<String> getFieldList(){
        return fieldList;
    }

    protected String getQuery(){
        return query;
    }

    protected String getField(){
        return field;
    }

    protected void setField(String field){
        this.field = field;
    }

    protected boolean getSelection(){
        return selection;
    }

    private void setRestUrl(String url){

        restUrl = url;

    }

    private void setToken(String token){

        this.token = token;

    }

    protected String getRestUrl(){
        return restUrl;
    }

    protected String getToken(){
        return token;
    }

    protected boolean getAppend(){
        return append;
    }

    protected RestTemplate getRestTemplate(){
        return restTemplate;
    }

    protected String getValue(){
        return value;
    }

    protected String getEntity(){
        return entity;
    }

    private static RestTemplate createRestTemplate() {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter<Source>());
        messageConverters.add(new MappingJackson2HttpMessageConverter());

        return new RestTemplate(messageConverters);
    }

}
