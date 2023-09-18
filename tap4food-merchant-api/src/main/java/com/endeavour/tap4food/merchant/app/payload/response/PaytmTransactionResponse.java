package com.endeavour.tap4food.merchant.app.payload.response;

import com.endeavour.tap4food.merchant.app.payload.response.PaytmResponse.ResultInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaytmTransactionResponse {
    @JsonProperty("head")
    private Head head;

    @JsonProperty("body")
    private Body body;

    // getters and setters

    // Inner classes for the "head" and "body" objects
    public static class Head {
        @JsonProperty("responseTimestamp")
        private String responseTimestamp;

        @JsonProperty("version")
        private String version;

        @JsonProperty("clientId")
        private String clientId;

        @JsonProperty("signature")
        private String signature;

        // getters and setters
    }

    public static class Body {
        @JsonProperty("resultInfo")
        private ResultInfo resultInfo;

        @JsonProperty("txnId")
        private String txnId;

        @JsonProperty("bankTxnId")
        private String bankTxnId;

        @JsonProperty("orderId")
        private String orderId;

        @JsonProperty("txnAmount")
        private String txnAmount;

        @JsonProperty("txnType")
        private String txnType;

        @JsonProperty("gatewayName")
        private String gatewayName;

        @JsonProperty("bankName")
        private String bankName;

        @JsonProperty("mid")
        private String mid;

        @JsonProperty("paymentMode")
        private String paymentMode;

        @JsonProperty("refundAmt")
        private String refundAmt;

        @JsonProperty("txnDate")
        private String txnDate;

        @JsonProperty("authRefId")
        private String authRefId;

        // getters and setters
    }
}
