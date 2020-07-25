package com.trobot.gkashdemo.model;

import androidx.annotation.NonNull;

public class PaymentMethod {

    private String cid;
    private String method;
    private String remarks;
    private String token;

    public PaymentMethod(String cid, String method, String remarks, String token) {
        this.cid = cid;
        this.method = method;
        this.remarks = remarks;
        this.token = token;
    }

    @NonNull
    @Override
    public String toString() {
        String response = "{cid: " + this.cid + ", method: " + this.method + ", remarks: " + this.remarks + ", token: " + this.token + "}";
        return response;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
