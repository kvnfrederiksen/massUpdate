package com.gordiangames.kevinfrederiksen.massupdate;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.Toast;
import com.bullhornsdk.data.model.response.crud.CreateResponse;
import com.bullhornsdk.data.model.response.crud.DeleteResponse;
import com.bullhornsdk.data.model.response.crud.UpdateResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kevin Frederiksen on 2/3/2018.
 */
public class RestLogic extends AsyncTask<FieldPopulation, Void, Void> {//start class

    Pattern nonAppendPattern = Pattern.compile("id(\"\\:)(.*?)([\\}\\,])");
    Pattern space = Pattern.compile("\\s");
    Matcher m;
    Activity activity;
    MyApplication app;

    public RestLogic(Activity activity, MyApplication app) {//start constructor

        this.activity = activity;
        this.app = app;

    }//end constructor

    protected Void doInBackground(FieldPopulation... fp){//start doInBackground

        init(fp[0]);
        return null;

    }//end doInBackground

    protected void init(FieldPopulation fp) {//start init

        RestTemplate restTemplate = fp.getRestTemplate();
        String query = fp.getQuery();
        String restUrl = fp.getRestUrl();
        String token = fp.getToken();
        String field = fp.getField();
        String entity = fp.getEntity();
        String value = fp.getValue();
        int iValue = 0;
        int start = 0;
        int progress = 0;
        int total = 0;
        boolean count = true;
        boolean append = fp.getAppend();
        boolean toMany = toManyCheck(fp, field);
        boolean toOne = toOneCheck(fp,field);
        DataRepository dataRepository = app.getDataRepository();

        //assigns iValue a non-zero value, should this be necessary for the call
        if(fp.getSelection()&&fp.getiValue()!=0) {//start if

            iValue = fp.getiValue();

        }//end if

        String url = restUrl + "search/" + entity + "?fields=id";

        //if the user wants to add onto the value that is already there and the field isn't a TO_MANY, or if the exact opposite is true,
        //the response will need to include the current value of the field
        if((append&&!toMany)||(!append&&toMany)) {//start if

            url +=","+field;

        }//end if

        String response = "";

        while (count) {//start while1

            url += "&start=" + start + "&count=500&query=" + query + "&BhRestToken=" + token;

            //once again, there wasn't a generalized class to use, so I had to pull the response as a String for pattern matching
            try {//start try

                response = restTemplate.getForObject(url, String.class);

            }//end try
            catch( HttpClientErrorException e){//start catch

                activity.runOnUiThread(new Runnable() {//start runOnUiThread

                    @Override
                    public void run(){//start run

                        Toast.makeText
                                (activity.getApplicationContext(), "Oops! Something's wrong-o, Bob-o!", Toast.LENGTH_LONG)
                                .show();

                    }//end run

                });//end runOnUiThread

            }//end catch

            m = space.matcher(response);
            response = m.replaceAll("");

            //looks for the total number of records that will be parsed, so the user has an idea
            Pattern totalPattern = Pattern.compile("al(\"\\:)(.*?)(\\,\")");
            m = totalPattern.matcher(response);

            while(m.find()) {//start while2

                total = Integer.parseInt(m.group(2));
                int i = total;
                if(progress < 500) {//start if1

                    activity.runOnUiThread(new Runnable() {//start runOnUiThread

                        @Override
                        public void run() {//start run

                            dataRepository.updateText("0/" + i);

                        }//end run

                    });//end runOnUiThread

                    break;

                }//end if1

            }//end while2

            //update to a non-selection field that doesn't add on to the current value
            if(!append&&!toMany&&!toOne)
                count = regularNonAppend(response, restUrl, token, field, value, entity, restTemplate, progress, total, dataRepository);
            //update to a non-selection field that does add on to the current value
            else if(append&&!toMany)
                count = regularAppend(response, restUrl, token, field, value, entity, restTemplate, progress, total, dataRepository);
            //update to a TO_MANY field that the user would like to add on to existing values
            else if(append&&toMany)
                count = appendToMany(response, restUrl, token, field, iValue, entity, restTemplate, progress, total, dataRepository);
                //update to a TO_MANY field that the user would not like to add on to existing values
            else if(!append&&toMany)
                count = nonAppendToMany(response, restUrl, token, field, iValue, entity, restTemplate, progress, total, dataRepository);
                //update to a TO_ONE field
            else if(toOne)
                count = toOne(response, restUrl, token, field, iValue, entity, restTemplate, progress, total, dataRepository);

            //sends the background processes back through the loop if more than 500 records match the search criteria
            if(count){ start += 500; progress+=500;}

        }

        //performs these actions upon completion
        MediaPlayer lol= MediaPlayer.create(this.activity,R.raw.lol);
        activity.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                Toast.makeText
                        (activity.getApplicationContext(), "Done!", Toast.LENGTH_SHORT)
                        .show();
            }
        });
        lol.start();
    }

    private boolean regularNonAppend(String response, String restUrl, String token, String field, String value, String entity,
                                     RestTemplate restTemplate, int progress, int total, DataRepository dataRepository){

        int recordCount = 0;
        m = nonAppendPattern.matcher(response);
        String json = "{\""+field + "\":" + value + "}";

        while(m.find()){

            try{restTemplate.postForObject(restUrl + "entity/" + entity + "/" + m.group(2) + "?BhRestToken="+ token, json,
                    UpdateResponse.class, "");
        } catch( HttpClientErrorException e){
            activity.runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    Toast.makeText
                            (activity.getApplicationContext(), "Nope " + m.group(2), Toast.LENGTH_LONG)
                            .show();
                }
            });}
            recordCount++;
            progress++;
            int i = progress;
            activity.runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    dataRepository.updateText(i + "/" + total);
                }
            });
        }
        return recordCount==500;
    }

    private boolean regularAppend(String response, String restUrl, String token, String field, String value, String entity,
                                  RestTemplate restTemplate, int progress, int total, DataRepository dataRepository){

        int recordCount = 0;
        Pattern appendPattern = Pattern.compile("id(\"\\:)(.*?)(\\,\")" + field + "(\"\\:)(.*?)([\"])([\\}\\,])");
        m = appendPattern.matcher(response);
        String json;

        while(m.find()){
                json = "{\"" + field + "\":" + m.group(5) + value + "\"}";
            try{
            restTemplate.postForObject(restUrl + "entity/" + entity + "/" + m.group(2) + "?BhRestToken="+ token, json,
                    UpdateResponse.class, "");}
            catch( HttpClientErrorException e){
                activity.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText
                                (activity.getApplicationContext(), "Nope " + m.group(2), Toast.LENGTH_LONG)
                                .show();
                    }
                });}
            recordCount++;
            progress++;
            int i = progress;
            activity.runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    dataRepository.updateText(i + "/" + total);
                }
            });

        }


        return recordCount==500;
    }

    private boolean appendToMany(String response, String restUrl, String token, String field, int iValue, String entity,
                                 RestTemplate restTemplate, int progress, int total, DataRepository dataRepository){

        int recordCount = 0;

        m = nonAppendPattern.matcher(response);

        while(m.find()){
            try{
            restTemplate.exchange(restUrl + "entity/" + entity + "/" + m.group(2) + "/" + field + "/" + iValue + "?BhRestToken=" + token,
                    HttpMethod.PUT, null, CreateResponse.class,
                    "");}
            catch( HttpClientErrorException e){
                activity.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText
                                (activity.getApplicationContext(), "Nope " + m.group(2), Toast.LENGTH_LONG)
                                .show();
                    }
                });}

            recordCount++;
            progress++;
            int i = progress;
            activity.runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    dataRepository.updateText(i + "/" + total);
                }
            });


        }

        return recordCount==500;
    }

    private boolean nonAppendToMany(String response, String restUrl, String token, String field, int iValue, String entity,
                                    RestTemplate restTemplate, int progress, int total, DataRepository dataRepository){

        int recordCount = 0;
        String record = "";
        String deleteValues = "";
        boolean firstThru = true;
        Pattern idPattern = Pattern.compile("id(\"\\:)(.*?)(\\,\")(.*?)(\")");

        m = idPattern.matcher(response);

        //Loops through the current values for each record, disassociates them all, and then updates the field with the selected value
        while(m.find()){
            if(firstThru){
                record = m.group(2);
                firstThru = false;
            }
            else{

                if(m.group(4).equals(field)){
                    String j = record;
                    try{
                    if(!deleteValues.equals(""))
                    restTemplate.exchange(restUrl + "entity/" + entity + "/" + record + "/" + field + "/" + deleteValues + "?BhRestToken=" + token, HttpMethod.DELETE, null,
                            DeleteResponse.class, "");

                    restTemplate.exchange(restUrl + "entity/" + entity + "/" + record + "/" + field + "/" + iValue + "?BhRestToken=" + token, HttpMethod.PUT, null, CreateResponse.class,
                            "");}
                    catch( HttpClientErrorException e){
                        activity.runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                Toast.makeText
                                        (activity.getApplicationContext(), "Nope " + j, Toast.LENGTH_LONG)
                                        .show();
                            }
                        });}
                    recordCount++;
                    progress++;
                    int i = progress;
                    activity.runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            dataRepository.updateText(i + "/" + total);
                        }
                    });
                    record = m.group(2);
                    deleteValues = "";
                } else deleteValues += m.group(2) + ",";

            }

        }

        return recordCount==500;
    }

    private boolean toOne(String response, String restUrl, String token, String field, int iValue, String entity,
                          RestTemplate restTemplate, int progress, int total, DataRepository dataRepository){

        int recordCount = 0;
        m = nonAppendPattern.matcher(response);
        String json = "{\""+field + "\":{\"id\":" + iValue + "}}";
        if ((field.equals("state"))||(field.equals("countryID")))
            json = "{\"address\":{\""+field+"\":"+iValue+"}}";

        while(m.find()){
            try{
            restTemplate.postForObject(restUrl + "entity/" + entity + "/" + m.group(2) + "?BhRestToken="+ token, json, UpdateResponse.class, "");}
            catch( HttpClientErrorException e){
                activity.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText
                                (activity.getApplicationContext(), "Nope " + m.group(2), Toast.LENGTH_LONG)
                                .show();
                    }
                });}
            recordCount++;
            progress++;
            int i = progress;
            activity.runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    dataRepository.updateText(i + "/" + total);
                }
            });

        }

        return recordCount==500;
    }

    private boolean toManyCheck(FieldPopulation fp, String field){

        try{
            return fp.getFieldTypeMap().containsKey(field)&fp.getFieldTypeMap().get(field).equals("TO_MANY");
        }catch(NullPointerException e){
            return false;
        }

    }

    private boolean toOneCheck(FieldPopulation fp, String field){

        try{
            if((field.equals("state")||field.equals("countryID")))
                return true;
            return fp.getFieldTypeMap().containsKey(field)&fp.getFieldTypeMap().get(field).equals("TO_ONE");
        }catch(NullPointerException e){
            return false;
        }

    }

}
