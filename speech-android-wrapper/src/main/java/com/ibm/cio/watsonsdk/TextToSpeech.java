package com.ibm.cio.watsonsdk;

import android.util.Log;

import com.ibm.cio.util.TTSUtility;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

/**Speech Recognition Class for SDK functions
 * @author Viney Ugave (vaugave@us.ibm.com)
 *
 */
public class TextToSpeech {
    protected static final String TAG = "TextToSpeech";

    private TTSUtility ttsUtility;
    private String username;
    private String password;
    private URI hostURL;
    private TokenProvider tokenProvider = null;
    private String voice;

    /**Speech Recognition Shared Instance
     *
     */
    private static TextToSpeech _instance = null;

    public static TextToSpeech sharedInstance(){
        if(_instance == null){
            synchronized(SpeechToText.class){
                _instance = new TextToSpeech();
            }
        }
        return _instance;
    }

    /**
     * Init the shared instance with the context when VAD is being used
     * @param uri
     */
    public void initWithContext(URI uri){
        this.setHostURL(uri);
    }

    public void synthesize(String ttsString) {
        Log.i(TAG, "synthesize called: " + this.hostURL.toString()+"/v1/synthesize");
        String[] Arguments = { this.hostURL.toString()+"/v1/synthesize",
                this.username, this.password,
                this.voice, ttsString,
                this.tokenProvider.getToken()};
        try {
            ttsUtility = new TTSUtility();
            ttsUtility.setCodec(TTSUtility.CODEC_WAV);
            ttsUtility.tts(Arguments);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildAuthenticationHeader(HttpGet httpGet) {

        // use token based authentication if possible, otherwise Basic Authentication will be used
        if (this.tokenProvider != null) {
            Log.d(TAG, "using token based authentication");
            httpGet.setHeader("X-Watson-Authorization-Token",this.tokenProvider.getToken());
        } else {
            Log.d(TAG, "using basic authentication");
            httpGet.setHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(this.username, this.password), "UTF-8",false));
        }
    }

    public JSONObject getVoices() {

        JSONObject object = null;

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(this.hostURL+"/v1/voices");
            Log.d(TAG,"url: " + this.hostURL+"/v1/voices");
            this.buildAuthenticationHeader(httpGet);
            httpGet.setHeader("accept", "application/json");
            HttpResponse executed = httpClient.execute(httpGet);
            InputStream is = executed.getEntity().getContent();

            // get the JSON object containing the models from the InputStream
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            object = new JSONObject(responseStrBuilder.toString());
            Log.d(TAG, object.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    /**
     * Set credentials
     * @param username
     * @param password
     */
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
    /**
     * Get host URL
     * @return
     */
    public URI getHostURL() {
        return hostURL;
    }
    /**
     * Set host URL
     * @param hostURL
     */
    public void setHostURL(URI hostURL) {
        this.hostURL = hostURL;
    }
    /**
     * Set token provider (for token based authentication)
     */
    public void setTokenProvider(TokenProvider tokenProvider) { this.tokenProvider = tokenProvider; }

    /**
     * Set TTS voice
     */
    public void setVoice(String voice) {
        this.voice = voice;
    }
}
