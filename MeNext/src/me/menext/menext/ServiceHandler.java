package me.menext.menext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ServiceHandler {

    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
    public ServiceHandler() {

    }

    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * */
    public String makeServiceCall(Activity activity, String site_name, int method) {
        return this.makeServiceCall(activity, site_name, method, null);
    }

    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public String makeServiceCall(Activity activity, String site_name, int method,
      List<NameValuePair> params) {
      DefaultHttpClient httpClient = ((GlobalVar) activity.getApplication()).httpClient;
      SharedPreferences sharedpreferences = ((GlobalVar) activity.getApplication()).sharedpreferences;
      String url = activity.getString(R.string.menext_handler_url);
      try {
            // http client
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                // adding post params
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }

                httpResponse = httpClient.execute(httpPost);

            } else if (method == GET) {
                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params , "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url);

                httpResponse = httpClient.execute(httpGet);
            }
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);
            Editor editor = sharedpreferences.edit();
            List<Cookie> cookies = httpClient.getCookieStore().getCookies();
            if (!cookies.isEmpty()) {
              for (int i = 0; i < cookies.size(); i++) {
                editor.putString(cookies.get(i).getName(), cookies.get(i).getValue());
              }
            }
            editor.commit();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;

    }
}