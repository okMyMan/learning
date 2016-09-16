package com.infinity.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.infinity.util.HttpUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthTest
 *
 * @author Alvin Xu
 * @date 2016/9/9
 */
public class AuthTest {
    private static String baseUrl = "http://localhost:8080/auth/";

    public static void main(String[] args) throws IOException {

        String generateUrl = baseUrl + "generateToken?id=dddddddd";
        String result = HttpUtil.requestPost(generateUrl, "", "UTF-8");

        JSONObject jsonObject = JSON.parseObject(result);
        String token = (String) jsonObject.get("data");


        String checkUrl = baseUrl + "checkToken?id=dddddddd&token=" + token;
        result = HttpUtil.requestPost(checkUrl, "", "UTF-8");

        jsonObject = JSON.parseObject(result);
        Integer status = (Integer) jsonObject.get("status");

        String invalidUrl = baseUrl + "invalidToken?token=" + token;
        String result2 = HttpUtil.requestPost(invalidUrl, "", "UTF-8");

        jsonObject = JSON.parseObject(result2);
        status = (Integer) jsonObject.get("status");

        result = HttpUtil.requestPost(checkUrl, "", "UTF-8");
        jsonObject = JSON.parseObject(result);
        status = (Integer) jsonObject.get("status");

//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("username", "cccc");
//        jsonObject.put("password", "aaaaaa");
//        jsonObject.put("xule", "cccc");

//        NameValuePair n0 = new BasicNameValuePair("username","62");
//        NameValuePair n1 = new BasicNameValuePair("password","newtest");
//        List<NameValuePair> names = new ArrayList<>();
//        names.add(n0);
//        names.add(n1);
//
//        HttpEntity httpEntity = new UrlEncodedFormEntity(names);
//        HttpPost post = new HttpPost(url);
//        post.setEntity(httpEntity);
//        HttpClient httpClient = HttpClients.createDefault();
//        HttpResponse response = httpClient.execute(post);


//        String response = HttpUtil.requestPost(baseUrl + "generateToken", jsonObject.toJSONString(), "UTF-8");

        System.out.println();
    }
}
