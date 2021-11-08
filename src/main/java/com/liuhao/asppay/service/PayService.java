package com.liuhao.asppay.service;

import com.liuhao.asppay.model.Product;

public interface PayService {
    /**
     * 银联支付
     * @param product
     * @return  String
     */
    String unionPay(Product product) throws Exception;

    String pay(Product product) throws Exception;

}
