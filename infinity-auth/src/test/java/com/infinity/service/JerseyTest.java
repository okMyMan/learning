package com.infinity.service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
/**
 * JerseyTest
 *
 * @author Alvin Xu
 * @date 2016/9/20
 */
public class JerseyTest {
    private WebResource r = null;
    @Test
    public void insertUser(){
        r = Client.create().resource("http://localhost:8080/jersey/users");
        Form form = new Form();
        form.add("userId", "002");
        form.add("userName", "ZhaoHongXuan");
        form.add("userAge", 23);
        ClientResponse response = r.type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(ClientResponse.class, form);
        System.out.println(response.getStatus());
    }
    @Test
    public void findUser(){
        r = Client.create().resource("http://localhost:8081/jersey/api/users/002");
        String jsonRes = r.accept(MediaType.APPLICATION_XML).get(String.class);
        System.out.println(jsonRes);
    }
}
