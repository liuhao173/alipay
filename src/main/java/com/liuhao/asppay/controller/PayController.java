package com.liuhao.asppay.controller;

import com.liuhao.asppay.model.Product;
import com.liuhao.asppay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

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
    public void pcPay(HttpServletResponse response, Product product) throws Exception {
        product.setPayWay((short)2);
        String result = payService.pay(product);
        response.getWriter().write(result);
    }

    @RequestMapping(value="pcPay1")
    public void pcPay1(HttpServletResponse response, Product product) throws Exception {
        product.setPayWay((short)2);
        String result = payService.unionPay(product);
        response.getWriter().write(result);
    }
}
