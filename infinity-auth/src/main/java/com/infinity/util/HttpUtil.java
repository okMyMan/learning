package com.infinity.util;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * HttpUtil
 *
 * @author Alvin Xu
 * @date 2016/9/9
 */
public class HttpUtil {
    private static HttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // 最大连接数增加到200
        connectionManager.setDefaultMaxPerRoute(20);// 每个路由基础的连接增加到20
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
    }

    /**
     * GET方式提交数据
     *
     * @param url
     *            待请求的URL
     * @param encode
     *            编码
     * @return 响应结果为json字符串
     * @throws IOException
     */
    public static String requestGet(String url, String encode) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        return EntityUtils.toString(response.getEntity(), encode);

    }

    /**
     * POST方式提交数据
     *
     * @param url
     *            待请求的URL
     * @param encode
     *            编码
     * @return 响应结果为json字符串
     * @throws IOException
     */
    public static String requestPost(String url, String encode) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        HttpResponse httpResponse = httpClient.execute(httpPost);
        return EntityUtils.toString(httpResponse.getEntity(), encode);
    }

    /**
     * POST方式提交数据
     * @param url  待请求的URL
     * @param jsonParams    需要提交的数据（json格式）
     * @param encode     编码
     * @return 响应结果为json字符串
     * @throws IOException
     */
    public static String requestPost(String url, String jsonParams, String encode) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json;charset=utf-8");
        httpPost.setEntity(new StringEntity(jsonParams, encode));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        return EntityUtils.toString(httpResponse.getEntity(), encode);
    }

    /**
     * POST方式提交数据
     *
     * @param url
     *            待请求的URL
     * @param entity
     *            An entity that can be sent or received with an HTTP message
     * @return 响应结果为json字符串
     * @throws IOException
     */
    public static String requestPost(String url, HttpEntity entity) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        HttpResponse httpResponse = httpClient.execute(httpPost);
        return EntityUtils.toString(httpResponse.getEntity(), ContentType.get(entity).getCharset());
    }

    /**
     * POST方式提交数据
     * @param url 待请求的URL
     * @param entity    An entity that can be sent or received with an HTTP message
     * @return 响应结果为json字符串
     * @throws IOException
     */
    public static byte[] requestPostForByte(String url, HttpEntity entity) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        HttpResponse httpResponse = httpClient.execute(httpPost);
        return EntityUtils.toByteArray(httpResponse.getEntity());
    }

    /**
     * @param url
     * @param paramData	请求参数k/v对
     * @param encode
     * @return
     * @throws IOException
     */
    public static String requestPost(String url, Map<String, String> paramData, String encode) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(buildParams(paramData, encode));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        return EntityUtils.toString(httpResponse.getEntity(), encode);
    }

    /**
     * @param url
     * @param encode
     * @param headerParams 设置请求表头参数
     * @return
     * @throws IOException
     */
    public static String requestPost(String url, String encode, Map<String, String> headerParams) throws IOException {
        HttpPost httpPost = (HttpPost) buildHttpRequest(url, headerParams, "POST");
        HttpResponse httpResponse = httpClient.execute(httpPost);
        return EntityUtils.toString(httpResponse.getEntity(), encode);
    }

    private static HttpRequestBase buildHttpRequest(String url, Map<String, String> headerParams, String type) {
        HttpRequestBase httpRequest = null;
        if (type == "POST") {
            httpRequest = new HttpPost(url);
        } else {
            httpRequest = new HttpGet(url);
        }
        for (String param : headerParams.keySet()) {
            httpRequest.setHeader(param, headerParams.get(param));
        }
        return httpRequest;
    }

    /**键值对的方式传递
     * @param paramData
     * @throws UnsupportedEncodingException
     */
    private static UrlEncodedFormEntity buildParams(Map<String, String> paramData, String encode)
            throws UnsupportedEncodingException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (String key : paramData.keySet()) {
            params.add(new BasicNameValuePair(key, paramData.get(key)));
        }
        return new UrlEncodedFormEntity(params, encode);
    }

    /**
     * @param url
     * @param paramData	请求参数k/v对
     * @param encode
     * @return
     * @throws IOException
     */
    public static String requestGet(String url, Map<String, String> paramData, String encode) throws IOException {
        url = builderUrl(url, paramData);
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        return EntityUtils.toString(httpResponse.getEntity(), encode);
    }
    public static String requestGetByObject(String url, Map<String, Object> paramData, String encode) throws IOException {
        url = builderUrl2ByObject(url, paramData);
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        return EntityUtils.toString(httpResponse.getEntity(), encode);
    }

    private static String builderUrl(String url, Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder urlTemp = new StringBuilder(url);
        int i = 0;
        for (String key : params.keySet()) {
            // 任何 URL 实例只要遵守 RFC 2396 就可以转化为 URI。但是，有些未严格遵守该规则的 URL 将无法转化为 URI。
            if (i == 0) {
                urlTemp.append("?").append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8"));
            } else {
                urlTemp.append("&").append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8"));
            }
            i++;
        }
        return String.valueOf(urlTemp);
    }
    private static String builderUrl2ByObject(String url, Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder urlTemp = new StringBuilder(url).append("?");
        for (String key : params.keySet()) {
            // 任何 URL 实例只要遵守 RFC 2396 就可以转化为 URI。但是，有些未严格遵守该规则的 URL 将无法转化为 URI。
            if (params.get(key) != null)
                urlTemp.append("&").append(key).append("=").append(URLEncoder.encode(params.get(key).toString(), "UTF-8"));
        }
        return String.valueOf(urlTemp);
    }
}
