package me.menext.menext;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


public class GlobalVar extends Application{
  public DefaultHttpClient httpClient;
  public SharedPreferences sharedpreferences;
  
  public void onCreate() {
    super.onCreate();

    httpClient = new DefaultHttpClient();
    sharedpreferences = getSharedPreferences("MeNextPreferences", Context.MODE_PRIVATE);
    String menext_domain = this.getString(R.string.menext_url);
    List<String> cookies = new ArrayList<String>();
    cookies.add("PHPSESSID");
    cookies.add("seriesId");
    cookies.add("token");
      for (int i = 0; i < cookies.size(); i++) {
        String cookie_name = cookies.get(i);
        String cookie_value = sharedpreferences.getString(cookie_name, "");
        Cookie cookie = new BasicClientCookie(cookie_name, cookie_value);
        ((BasicClientCookie) cookie).setDomain(menext_domain);
        httpClient.getCookieStore().addCookie(cookie);
      }
  }
}
