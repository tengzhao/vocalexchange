package to52.utbm.com.vocalexchange;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class SendToServer  extends AsyncTask<String, String, String> {
    private Map<String, String> arguments;
    private String urlS;
    private String TAG = "debugSend";

    public SendToServer(Map<String, String> arguments, String url){
        this.arguments = arguments;
        this.urlS = url;
    }

    protected String doInBackground(String... args) {

        String response = null;
        try {
            response = post();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private String post() throws Exception {
        Log.i(TAG, "Sending post to " + urlS);
        URL url;
        url = new URL(urlS);
        URLConnection con = url.openConnection();
        con.setConnectTimeout(5 * 1000);
        HttpURLConnection http = (HttpURLConnection)con;
        try {
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String sj = "";
            if (arguments != null){
                for(Map.Entry<String,String> entry : arguments.entrySet())
                    sj += "&" + (URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                            + URLEncoder.encode(entry.getValue(), "UTF-8"));
                sj = sj.substring(1);
            }
            byte[] out = sj.getBytes(StandardCharsets.UTF_8);
            Log.i(TAG, sj);
            OutputStream os = http.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(sj);
            writer.flush();
            writer.close();
            os.close();

            http.connect();

            String sb = "";
            int HttpResult = http.getResponseCode();
            Log.i(TAG, "response code is "+HttpResult);
            if (HttpResult == HttpsURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb += line;
                }
                br.close();
                Log.i(TAG, "phpreturn"+sb);
                return sb;
            } else {
                Log.i(TAG, (http.getResponseMessage()));
            }
        }catch (Exception e){
            Log.i(TAG, e.toString());
        } finally {
            http.disconnect();
        }
        return null;
    }
}
