package com.liuhao.asppay.controller;

import com.liuhao.asppay.model.Product;
import com.liuhao.asppay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayController {
    
    @Autowired
    private PayService payService;
    
    /**
     * 电脑支付
     * @param product
     * @return
     */
    @RequestMapping(value="pcPay")
    public String pcPay(Product product) {
        product.setPayWay((short)2);
        //return payService.unionPay(product);
        return payService.pay(product);
    }
}
