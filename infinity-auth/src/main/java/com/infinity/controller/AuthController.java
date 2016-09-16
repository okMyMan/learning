package com.infinity.controller;

import com.infinity.entity.TokenEntity;
import com.infinity.entity.response.AuthResponse;
import com.infinity.service.AuthCheckService;
import com.infinity.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by alvin.xu on 2016/9/8.
 */
@Controller
@RequestMapping(value = "auth")
public class AuthController {
    @Autowired
    private AuthCheckService authCheckService;

    @RequestMapping(value = "generateToken")
    @ResponseBody
    public AuthResponse generateToken(HttpServletRequest request) {  //@RequestBody  @RequestParam
//        String authorization=request.getHeader("Authorization");
        String id = request.getParameter("id");
        return authCheckService.generateToken(id);
    }

    @RequestMapping(value = "checkToken")
    @ResponseBody
    public AuthResponse checkToken(HttpServletRequest request) {
        String id = request.getParameter("id");
        String token = request.getParameter("token");
        return authCheckService.checkToken(id, token);
    }

    @RequestMapping(value = "invalidToken")
    @ResponseBody
    public AuthResponse invalidToken(HttpServletRequest request) {
        String token = request.getParameter("token");
        return authCheckService.invalidToken(token);
    }

}
