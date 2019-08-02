package com.xkcoding.justauthspringbootstarterdemo;

import cn.hutool.json.JSONUtil;
import com.xkcoding.justauth.AuthRequestFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 测试 Controller
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019-07-22 11:17
 */
@Slf4j
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TestController {
    private final AuthRequestFactory factory;

    @GetMapping
    public List<String> list() {
        return factory.oauthList();
    }

    @GetMapping("/login/qq")
    public void login(HttpServletResponse response) throws IOException {
        AuthRequest authRequest = factory.get(AuthSource.QQ);
        response.sendRedirect(authRequest.authorize(AuthStateUtils.createState()));
    }

    @RequestMapping("/qq/callback")
    public AuthResponse login(AuthCallback callback) {
        AuthRequest authRequest = factory.get(AuthSource.QQ);
        AuthResponse response = authRequest.login(callback);
        log.info("【response】= {}", JSONUtil.toJsonStr(response));
        return response;
    }
}
