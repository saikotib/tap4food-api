package com.endeavour.tap4food.user.app.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties("paytm.payment.sandbox")
public class PaytmDetailPojo {

	private String merchantId;

	private String merchantKey;

	private String channelId;

	private String website;

	private String industryTypeId;

	private String paytmUrl;

	private Map<String, String> details;
}
