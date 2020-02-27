# justauth-spring-boot-starter-demo

> 此 demo 主要演示 Spring Boot 如何使用 justauth-spring-boot-starter 集成 JustAuth

## 快速开始

### 1. 基础配置

- 引用依赖

```xml
<dependency>
  <groupId>com.xkcoding.justauth</groupId>
  <artifactId>justauth-spring-boot-starter</artifactId>
  <version>1.3.2</version>
</dependency>
```

- 添加配置，在 `application.yml` 中添加配置配置信息

```yaml
justauth:
  enabled: true
  type:
    QQ:
      client-id: 10**********6
      client-secret: 1f7d08**********5b7**********29e
      redirect-uri: http://oauth.xkcoding.com/demo/oauth/qq/callback
  cache:
    type: default
```

- 然后就开始玩耍吧~

```java
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

    @GetMapping("/login/{type}")
    public void login(@PathVariable String type, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = factory.get(type);
        response.sendRedirect(authRequest.authorize(AuthStateUtils.createState()));
    }

    @RequestMapping("/{type}/callback")
    public AuthResponse login(@PathVariable String type, AuthCallback callback) {
        AuthRequest authRequest = factory.get(type);
        AuthResponse response = authRequest.login(callback);
        log.info("【response】= {}", JSONUtil.toJsonStr(response));
        return response;
    }

}
```

### 2. 缓存配置

> starter 内置了2种缓存实现，一种是上面的默认实现，另一种是基于 Redis 的缓存实现。
>
> 当然了，你也可以自定义实现你自己的缓存。

#### 2.1. 默认缓存实现

在配置文件配置如下内容即可

```yaml
justauth:
  cache:
    type: default
```

#### 2.2. Redis 缓存实现

1.添加 Redis 相关依赖

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- 对象池，使用redis时必须引入 -->
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-pool2</artifactId>
</dependency>
```

2.配置文件配置如下内容即可

```yaml
justauth:
  cache:
    type: redis
    # 缓存前缀，目前只对redis缓存生效，默认 JUSTAUTH::STATE::
    prefix: ''
    # 超时时长，目前只对redis缓存生效，默认3分钟
    timeout: 1h
spring:
  redis:
    host: localhost
    # 连接超时时间（记得添加单位，Duration）
    timeout: 10000ms
    # Redis默认情况下有16个分片，这里配置具体使用的分片
    # database: 0
    lettuce:
      pool:
        # 连接池最大连接数（使用负值表示没有限制） 默认 8
        max-active: 8
        # 连接池最大阻塞等待时间（使用负值表示没有限制） 默认 -1
        max-wait: -1ms
        # 连接池中的最大空闲连接 默认 8
        max-idle: 8
        # 连接池中的最小空闲连接 默认 0
        min-idle: 0
```

#### 2.3. 自定义缓存实现

1.配置文件配置如下内容

```yaml
justauth:
  cache:
    type: custom
```

2.自定义缓存实现 `AuthStateCache` 接口

```java
/**
 * <p>
 * 自定义缓存实现
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/8/31 12:53
 */
public class MyAuthStateCache implements AuthStateCache {
    /**
     * 存入缓存
     *
     * @param key   缓存key
     * @param value 缓存内容
     */
    @Override
    public void cache(String key, String value) {
        // TODO: 自定义存入缓存
    }

    /**
     * 存入缓存
     *
     * @param key     缓存key
     * @param value   缓存内容
     * @param timeout 指定缓存过期时间（毫秒）
     */
    @Override
    public void cache(String key, String value, long timeout) {
        // TODO: 自定义存入缓存
    }

    /**
     * 获取缓存内容
     *
     * @param key 缓存key
     * @return 缓存内容
     */
    @Override
    public String get(String key) {
        // TODO: 自定义获取缓存内容
        return null;
    }

    /**
     * 是否存在key，如果对应key的value值已过期，也返回false
     *
     * @param key 缓存key
     * @return true：存在key，并且value没过期；false：key不存在或者已过期
     */
    @Override
    public boolean containsKey(String key) {
        // TODO: 自定义判断key是否存在
        return false;
    }
}
```

3.自动装配 `JustAuthConfig`

```java
/**
 * <p>
 * 自定义缓存装配
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/8/31 12:29
 */
@Configuration
public class AuthStateConfiguration {
    @Bean
    public AuthStateCache authStateCache() {
        return new MyAuthStateCache();
    }
}
```

### 3. 自定义第三方平台配置

1.创建自定义的平台枚举类

```java
/**
 * <p>
 * 扩展的自定义 source
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/10/9 14:14
 */
public enum ExtendSource implements AuthSource {

    /**
     * 测试
     */
    TEST {
        /**
         * 授权的api
         *
         * @return url
         */
        @Override
        public String authorize() {
            return "http://authorize";
        }

        /**
         * 获取accessToken的api
         *
         * @return url
         */
        @Override
        public String accessToken() {
            return "http://accessToken";
        }

        /**
         * 获取用户信息的api
         *
         * @return url
         */
        @Override
        public String userInfo() {
            return null;
        }

        /**
         * 取消授权的api
         *
         * @return url
         */
        @Override
        public String revoke() {
            return null;
        }

        /**
         * 刷新授权的api
         *
         * @return url
         */
        @Override
        public String refresh() {
            return null;
        }
    }
}
```

2.创建自定义的请求处理

```java
/**
 * <p>
 * 测试用自定义扩展的第三方request
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/10/9 14:19
 */
public class ExtendTestRequest extends AuthDefaultRequest {

    public ExtendTestRequest(AuthConfig config) {
        super(config, ExtendSource.TEST);
    }

    public ExtendTestRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, ExtendSource.TEST, authStateCache);
    }

    /**
     * 获取access token
     *
     * @param authCallback 授权成功后的回调参数
     * @return token
     * @see AuthDefaultRequest#authorize()
     * @see AuthDefaultRequest#authorize(String)
     */
    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        return AuthToken.builder().openId("openId").expireIn(1000).idToken("idToken").scope("scope").refreshToken("refreshToken").accessToken("accessToken").code("code").build();
    }

    /**
     * 使用token换取用户信息
     *
     * @param authToken token信息
     * @return 用户信息
     * @see AuthDefaultRequest#getAccessToken(AuthCallback)
     */
    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        return AuthUser.builder().username("test").nickname("test").gender(AuthUserGender.MALE).token(authToken).source(this.source.toString()).build();
    }

    /**
     * 撤销授权
     *
     * @param authToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public AuthResponse revoke(AuthToken authToken) {
        return AuthResponse.builder().code(AuthResponseStatus.SUCCESS.getCode()).msg(AuthResponseStatus.SUCCESS.getMsg()).build();
    }

    /**
     * 刷新access token （续期）
     *
     * @param authToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public AuthResponse refresh(AuthToken authToken) {
        return AuthResponse.builder().code(AuthResponseStatus.SUCCESS.getCode()).data(AuthToken.builder().openId("openId").expireIn(1000).idToken("idToken").scope("scope").refreshToken("refreshToken").accessToken("accessToken").code("code").build()).build();
    }
}
```

3.在配置文件配置相关信息

```yaml
justauth:
  enabled: true
  extend:
    enum-class: com.xkcoding.justauthspringbootstarterdemo.extend.ExtendSource
    config:
      TEST:
        request-class: com.xkcoding.justauthspringbootstarterdemo.extend.ExtendTestRequest
        client-id: xxxxxx
        client-secret: xxxxxxxx
        redirect-uri: http://oauth.xkcoding.com/demo/oauth/test/callback
```

## 附录

### 1. 基础配置

`justauth` 配置列表

| 属性名             | 类型                                                         | 默认值 | 可选项     | 描述              |
| ------------------ | ------------------------------------------------------------ | ------ | ---------- | ----------------- |
| `justauth.enabled` | `boolean`                                                    | true   | true/false | 是否启用 JustAuth |
| `justauth.type`    | `java.util.Map<me.zhyd.oauth.config.AuthSource,me.zhyd.oauth.config.AuthConfig>` | 无     |            | JustAuth 配置     |
| `justauth.cache`   | `com.xkcoding.justauth.properties.CacheProperties`           |        |            | JustAuth缓存配置  |

`justauth.type` 配置列表

| 属性名                      | 描述                                                         |
| --------------------------- | ------------------------------------------------------------ |
| `justauth.type.keys`        | `justauth.type` 是 `Map` 格式的，key 的取值请参考 [`AuthSource`](https://github.com/zhangyd-c/JustAuth/blob/master/src/main/java/me/zhyd/oauth/config/AuthSource.java) |
| `justauth.type.keys.values` | `justauth.type` 是 `Map` 格式的，value 的取值请参考 [`AuthConfig`](https://github.com/zhangyd-c/JustAuth/blob/master/src/main/java/me/zhyd/oauth/config/AuthConfig.java) |

`justauth.cache` 配置列表

| 属性名                   | 类型                                                         | 默认值            | 可选项               | 描述                                                         |
| ------------------------ | ------------------------------------------------------------ | ----------------- | -------------------- | ------------------------------------------------------------ |
| `justauth.cache.type`    | `com.xkcoding.justauth.properties.CacheProperties.CacheType` | default           | default/redis/custom | 缓存类型，default使用JustAuth默认的缓存实现，redis使用默认的redis缓存实现，custom用户自定义缓存实现 |
| `justauth.cache.prefix`  | `string`                                                     | JUSTAUTH::STATE:: |                      | 缓存前缀，目前只对redis缓存生效，默认 JUSTAUTH::STATE::      |
| `justauth.cache.timeout` | `java.time.Duration`                                         | 3分钟             |                      | 超时时长，目前只对redis缓存生效，默认3分钟                   |

`justauth.extend` 配置列表

| 属性名                       | 类型                                         | 默认值 | 可选项 | 描述         |
| ---------------------------- | -------------------------------------------- | ------ | ------ | ------------ |
| `justauth.extend.enum-class` | `Class<? extends AuthSource>`                | 无     |        | 枚举类全路径 |
| `justauth.extend.config`     | `java.util.Map<String, ExtendRequestConfig>` | 无     |        | 对应配置信息 |

 `justauth.extend.config` 配置列表

| 属性名                          | 类型                                                         | 默认值 | 可选项 | 描述                                                         |
| ------------------------------- | ------------------------------------------------------------ | ------ | ------ | ------------------------------------------------------------ |
| `justauth.extend.config.keys`   | `java.lang.String`                                           | 无     |        | key 必须在 `justauth.extend.enum-class` 配置的枚举类中声明   |
| `justauth.extend.config.values` | `com.xkcoding.justauth.autoconfigure.ExtendProperties.ExtendRequestConfig` | 无     |        | value 就是 `AuthConfig` 的子类，增加了一个 `request-class` 属性配置请求的全类名，具体参考类[`ExtendProperties.ExtendRequestConfig`](https://github.com/justauth/justauth-spring-boot-starter/blob/master/src/main/java/com/xkcoding/justauth/autoconfigure/ExtendProperties.java#L49-L54) |

### 2. SNAPSHOT版本

![https://img.shields.io/badge/snapshots-1.4.0--SNAPSHOT-green](https://img.shields.io/badge/snapshots-1.4.0--SNAPSHOT-green)如果需要体验快照版本，可以在你的 `pom.xml`进行如下配置：

```xml
<repositories>
    <!--阿里云私服-->
    <repository>
      <id>aliyun</id>
      <name>aliyun</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public</url>
    </repository>
    <!--中央仓库-->
    <repository>
      <id>oss</id>
      <name>oss</name>
      <url>http://oss.sonatype.org/content/repositories/snapshots</url>
      <releases>
        <enabled>true</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
</repositories>
```
