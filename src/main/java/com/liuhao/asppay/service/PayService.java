package com.liuhao.asppay.service;

import com.liuhao.asppay.model.Product;

import java.io.UnsupportedEncodingException;

public interface PayService {
    /**
     * 银联支付
     * @param product
     * @return  String
     */
    String unionPay(Product product);

    String pay(Product product) throws UnsupportedEncodingException, Exception;

}
