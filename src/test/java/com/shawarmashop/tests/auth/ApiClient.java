package com.shawarmashop.tests.auth;

import com.shawarmashop.tests.rest.clients.AuthClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApiClient {

    private static final ApiClient ANONYMOUS = new ApiClient(null, null);

    private final String fixedToken;
    private final Credentials credentials;

    private String cachedToken;

    public static ApiClient asUser(Credentials credentials) {
        return new ApiClient(null, credentials);
    }

    public static ApiClient withToken(String token) {
        return new ApiClient(token, null);
    }

    public static ApiClient anonymous() {
        return ANONYMOUS;
    }

    public synchronized String tokenOrNull() {
        if (fixedToken != null) {
            return fixedToken;
        }

        if (credentials == null) {
            return null;
        }

        if (cachedToken == null) {
            cachedToken = login();
        }

        return cachedToken;
    }

    private String login() {
        String token = anonymous()
                .auth()
                .login(credentials.getUsername(), credentials.getPassword())
                .expect(200)
                .getToken();

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("POST /auth/login вернул пустой JWT для пользователя " + credentials.getUsername());
        }

        return token;
    }

    public AuthClient auth(){
        return new AuthClient(this);
    }
}
