package com.xkcoding.justauthspringbootstarterdemo;

import cn.hutool.core.lang.Dict;
import com.xkcoding.justauth.AuthRequestFactory;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * 测试 Controller
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019-07-22 11:17
 */
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TestController {
    private final AuthRequestFactory factory;

    @GetMapping("/login/qq")
    public void login(HttpServletResponse response) throws IOException {
        AuthRequest authRequest = factory.get(AuthSource.QQ, Dict.create().set("id", "xkcoding"));
        response.sendRedirect(authRequest.authorize());
    }

    @RequestMapping("/qq/callback")
    public AuthResponse login(AuthCallback callback) {
        // 第一种方式获取request
        //body => {"id":"xkcoding"}
        Map body = AuthState.getBody(AuthSource.QQ, AuthState.get(AuthSource.QQ), Map.class);
        AuthRequest authRequest1 = factory.get(AuthSource.QQ, body);
        // 第二种方式获取request，和第一种结果是一样的
        AuthRequest authRequest2 = factory.get(AuthSource.QQ);

        AuthResponse response = authRequest2.login(callback);
        // 移除校验通过的state
        AuthState.delete(AuthSource.QQ);
        return response;
    }
}
