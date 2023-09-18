package com.endeavour.tap4food.merchant.app.payload.response;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaytmResponse {
    @JsonProperty("head")
    private Head head;
    
    @JsonProperty("body")
    private Body body;

    // Getter and setter methods for 'head' and 'body'
    
    public static class Head {
        @JsonProperty("responseTimestamp")
        private String responseTimestamp;
        
        @JsonProperty("version")
        private String version;
        
        @JsonProperty("signature")
        private String signature;

        // Getter and setter methods for 'responseTimestamp', 'version', and 'signature'
    }

    public static class Body {
     
        
    	@JsonProperty("resultInfo")
        private ResultInfo resultInfo;

        @JsonProperty("txnToken")
        private String txnToken;

        @JsonProperty("isPromoCodeValid")
        private boolean isPromoCodeValid;

        @JsonProperty("authenticated")
        private boolean authenticated;
        // Getter and setter methods for 'resultInfo' and other body attributes
    }

    public static class ResultInfo {
        @JsonProperty("resultStatus")
        private String resultStatus;
        
        @JsonProperty("resultCode")
        private String resultCode;
        
        @JsonProperty("resultMsg")
        private String resultMsg;

        // Getter and setter methods for 'resultStatus', 'resultCode', and 'resultMsg'
    }
}
