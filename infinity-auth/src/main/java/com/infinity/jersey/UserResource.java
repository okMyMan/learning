package com.infinity.jersey;

import com.infinity.User;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserResource
 *
 * @author Alvin Xu
 * @date 2016/9/20
 */


//@Path("/users") 将 UserResource 暴露为一个Rest服务，
//@POST 将HTTP方法映射到资源的 让POST方法变成创建方法。
//@Consumes：声明该方法使用 HTML FORM即表单输入。
//@FormParam：注入该方法的 HTML 属性确定的表单输入。
//@Response.created(uri).build()： 构建新的 URI 用于新创建的User（/users/{id}）并设置响应代码（201/created）。您可以使用 http://localhost:8081/jersey/api/users/ 访问新用户。
//@Produces：限定响应内容的 MIME 类型。MIME类型有很多种，XML和JSON是常用的两种
//@Context： 使用该注释注入上下文对象，比如 Request、Response、UriInfo、ServletContext 等。


public class UserResource {
    private final static ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap();
    @Context
    UriInfo uriInfo;

    /**
     * 增加用户
     *
     * @param userId
     * @param userName
     * @param userAge
     * @param servletResponse
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void newUser(
            @FormParam("userId") String userId,
            @FormParam("userName") String userName,
            @FormParam("userAge") int userAge,
            @Context HttpServletResponse servletResponse
    ) throws IOException {
        User user = new User(userId, userName, userAge);
        userMap.put(userId, user);
        URI uri = uriInfo.getAbsolutePathBuilder().path(userId).build();
        Response.created(uri).build();
    }
}
