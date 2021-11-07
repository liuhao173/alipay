package com.liuhao.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class AlipayConfig {

    @Resource
    private AlipayProperties alipayProperties;

    @Bean
    public AlipayClient initAlipayClient(){
        return new DefaultAlipayClient(alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(), alipayProperties.getAppPrivateKey(),
                alipayProperties.getFormat(), alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(), alipayProperties.getSignType());
    }

}
