package org.pentaho.di.core.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.pentaho.di.i18n.BaseMessages;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class PlatformUtil {
    public static Properties properties = new  Properties();

    public static String httpGet(String repUrl){
        String messages="OK";
        URL url;
        try {
            url = new URL(repUrl);
            URLConnection conn = url.openConnection() ;
            InputStream in = conn.getInputStream();
            String encoding = conn.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(in, encoding);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            messages="Exception";
            e.printStackTrace();
        }
        return messages;
    }
    public static String httpPost(String repUrl,List<NameValuePair> params) {
        URL url;
        CloseableHttpClient client=null;
        try {
            client= HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(repUrl);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            CloseableHttpResponse response = client.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            if (status!=200)
            {
                throw new Exception("post request failed"+repUrl);
            }
            int length =  (int)response.getEntity().getContentLength();
            byte[] body = new byte[length];
            response.getEntity().getContent().read(body,0,length);
            return new String(body);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
            client.close();}catch(Exception e){}
        }
        return null;
    }



    public static void resetLoginPassword(String originalPassword,String newPassword) throws Exception{
        //String url=properties.get("SERVER_URL")+"/modifypwd?userId="+properties.get("USER_ID")+"&originalPassword="+originalPassword+"&newPassword="+newPassword  ;
        String url=properties.get("SERVER_URL")+"/modifypwd";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if(properties.get("USER_ID")==null)
            return;
        params.add(new BasicNameValuePair("userId", properties.get("USER_ID").toString()));
        params.add(new BasicNameValuePair("originalPassword", originalPassword));
        params.add(new BasicNameValuePair("newPassword", newPassword));
        String body =  httpPost(url,params);
        JSONObject obj = (JSONObject) JSONValue.parse(body) ;
        String status = obj.get("code").toString();
        if(!status.equals("200")){
            throw new Exception("Can not reset password:"+ status);
        }
    }

}
