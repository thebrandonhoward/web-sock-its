package com.thebrandonhoward.websockits.infrastructure.api.websocket.config;

import com.thebrandonhoward.websockits.infrastructure.api.websocket.client.GraphQLWebSocketClient;
import com.thebrandonhoward.websockits.infrastructure.api.websocket.interceptors.GQLClientInterceptor;
import com.thebrandonhoward.websockits.infrastructure.api.websocket.interceptors.GraphQLClientInterceptor;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.WebSocketGraphQlClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class WebSocketClientConfig {
    private final GraphQLClientInterceptor graphQLClientInterceptor;
    private final GQLClientInterceptor gqlClientInterceptor;

    @Bean
    public WebSocketClient webSocketClient(ProxyConfig proxyConfig) {
        // Proxy settings
        String proxyHost = "your-proxy-host";
        int proxyPort = 8080;
        String username = "user2";
        String password = "password";
        String authString = username + ":" + password;
        String base64Auth = Base64.getEncoder().encodeToString(authString.getBytes());
        String authorizationHeader = "Basic " + base64Auth;
        String url = "ws://localhost:8080/graphql";

        // Create the Reactor Netty HttpClient with a ConnectionProvider and Proxy
        HttpClient httpClient = HttpClient.create(ConnectionProvider.create("custom"))
//                .proxy(proxy -> proxy
//                        .type(ProxyProvider.Proxy.HTTP)
//                        .host(proxyHost)
//                        .port(proxyPort))  // Proxy configuration
                .headers(headers -> {
                            headers.add("Authorization", authorizationHeader);
                            //headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
                            //headers.add("Sec-WebSocket-Protocol","graphql-transport-ws");
                        }
                )  // Custom header
                .responseTimeout(java.time.Duration.ofSeconds(30));// Response timeout

        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient(httpClient);

        return client;
    }

    @Bean
    public WebSocketGraphQlClient webSocketGraphQlClient(ProxyConfig proxyConfig) {
        // Proxy settings
        String proxyHost = "your-proxy-host";
        int proxyPort = 8080;
        String username = "user2";
        String password = "password";
        String authString = username + ":" + password;
        String base64Auth = Base64.getEncoder().encodeToString(authString.getBytes());
        String authorizationHeader = "Basic " + base64Auth;
        String url = "ws://localhost:8080/graphql";

        // Create the Reactor Netty HttpClient with a ConnectionProvider and Proxy
        HttpClient httpClient = HttpClient.create(ConnectionProvider.create("custom"))
//                .proxy(proxy -> proxy
//                        .type(ProxyProvider.Proxy.HTTP)
//                        .host(proxyHost)
//                        .port(proxyPort))  // Proxy configuration
                .headers(headers -> {
                            headers.add("Authorization", authorizationHeader);
                            //headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
                            //headers.add("Sec-WebSocket-Protocol","graphql-transport-ws");
                        }
                )  // Custom header
                .responseTimeout(java.time.Duration.ofSeconds(30));// Response timeout

        WebSocketClient client = new ReactorNettyWebSocketClient(httpClient);

        WebSocketGraphQlClient graphQlClient = WebSocketGraphQlClient.builder(url, client)
                .keepAlive(Duration.ofSeconds(15))
                .headers((headers) ->
                        headers.setBasicAuth(username, password))
                .interceptors(interceptors -> {
                    interceptors.add(graphQLClientInterceptor);
                    interceptors.add(gqlClientInterceptor);
                })
                .build();

        return graphQlClient;
    }

    @Bean
    public ProxyConfig proxyConfig() {
        // Load proxy configuration from properties or environment variables
        ProxyConfig config = new ProxyConfig();
        config.setEnabled(false); // Set to true to enable proxy
        config.setType("HTTP"); // Or "SOCKS"
        config.setHost("your.proxy.host");
        config.setPort(8080);
        config.setUsername("your_proxy_username"); // Optional
        config.setPassword("your_proxy_password"); // Optional
        return config;
    }

    public static class ProxyConfig {
        private boolean enabled;
        private String type;
        private String host;
        private int port;
        private String username;
        private String password;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}