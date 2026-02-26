package com.senao.warehouse.database;

import com.senao.warehouse.database.BasicHelper;

public class ApiAuthHelper extends BasicHelper {
    private String username;
    private String password;
    private String token;
    private String company;
    private String authType;

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ApiAuthHelper() {
        super();
    }

    public ApiAuthHelper(String username, String password, String token, String company, String authType) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.company = company;
        this.authType = authType;
    }

    public ApiAuthHelper(String username, String password, String token, String company) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.company=company;
    }

    public ApiAuthHelper(String username, String password,String token ) {
        this.username = username;
        this.password = password;
        this.token = token;
    }

    @Override
    public String toString() {
        return "ApiAuthHelper{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
