package org.pentaho.di.core.util;

import org.apache.commons.io.IOUtils;
import org.pentaho.di.i18n.BaseMessages;
import java.util.Properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class PlatformUtil {
    public static Properties properties = new  Properties();

    public static String getResponse(String repUrl){
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

    public static void resetLoginPassword(String originalPassword,String newPassword) throws Exception{
        String url=properties.get("SERVER_URL")+"/modifypwd?userId="+properties.get("USER_ID")+"&originalPassword="+originalPassword+"&newPassword="+newPassword  ;
        String body =  getResponse(url);

        JSONObject obj = (JSONObject) JSONValue.parse(body) ;
        String status = obj.get("status").toString();
        if(status!="OK"){
            throw new Exception("Can not reset password:"+ status);
        }
    }

}
