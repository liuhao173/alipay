package com.liuhao.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class Alipay3Controller {

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private AlipayProperties alipayProperties;

    @Autowired
    private WebSocket webSocket;

    public String index(){
        return "index";
    }

    @RequestMapping("/baidu")
    @ResponseBody
    public String baidu(HttpServletResponse response) throws Exception{
        return "https://www.baidu.com";
    }

    @RequestMapping("/preCreate")
    @ResponseBody
    public String preCreate(HttpServletResponse response) throws Exception{
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(alipayProperties.getReturnUrl());

        /**
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(OrderCodeFactory.getOrderCode(null));
        model.setSubject("打赏");
        model.setTotalAmount("0.01");
        request.setBizModel(model);
        */
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", OrderCodeFactory.getOrderCode(null));
        bizContent.put("total_amount", 0.01);
        bizContent.put("subject", "打赏");
        request.setBizContent(bizContent.toString());

        AlipayTradePrecreateResponse alipayTradePrecreateResponse = alipayClient.execute(request);
        if(alipayTradePrecreateResponse.isSuccess()){
            System.out.println("调用成功：" + JSONObject.toJSONString(alipayTradePrecreateResponse));
            //makeQRCode(alipayTradePrecreateResponse.getQrCode(), response.getOutputStream());
            return alipayTradePrecreateResponse.getQrCode();
        } else {
            System.out.println("调用失败");
            return "";
        }
    }

    public void makeQRCode(String content, OutputStream outputStream) throws Exception{
        Map<EncodeHintType,Object> map = new HashMap<>();
        map.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);
        map.put(EncodeHintType.MARGIN,2);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE,200,200,map);
        MatrixToImageWriter.writeToStream(bitMatrix,"jpeg",outputStream);
    }

    @RequestMapping("/cancel/{code}")
    public String cancel(@PathVariable("code") String code) throws Exception{
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        /**
        AlipayTradeCancelModel model = new AlipayTradeCancelModel();
        model.setOutTradeNo(code);
        request.setBizModel(model);
        */
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", code);
        request.setBizContent(bizContent.toString());
        AlipayTradeCancelResponse response = alipayClient.execute(request);
        return response.getBody();
    }

    @RequestMapping("/query/{code}")
    public String query(@PathVariable("code") String code) throws Exception{
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        /**
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(code);
        request.setBizModel(model);
        */
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", code);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        return response.getBody();
    }

    @RequestMapping("/notify")
    public void notify(HttpServletRequest request) throws Exception{
        System.out.println("订单支付成功后异步通知");
        if (check(request.getParameterMap())){
            System.out.println("异步通知：" + Instant.now() + " ,trade_status=" + request.getParameter("trade_status"));
            webSocket.sendMessage("true");
        }else {
            System.out.println("验签失败");
        }
    }

    @RequestMapping("/return")
    public String returnUrl(HttpServletRequest request) throws Exception{
        System.out.println("订单支付成功后同步返回地址");
        if (check(request.getParameterMap())){
            return "success";
        }else {
            return "false";
        }
    }

    private boolean check(Map<String,String[]> requestParams) throws Exception{  //对return、notify参数进行验签
        Map<String,String> params = new HashMap<>();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
            //System.out.println(name + " ==> " + valueStr);
        }
        return AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), alipayProperties.getSignType());
    }
}
