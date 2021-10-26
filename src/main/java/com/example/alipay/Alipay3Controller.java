package com.example.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCancelModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping("/precreate")
    public void precreate(HttpServletResponse response) throws Exception{  //商户预创建支付订单，生成二维码
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo("666");                   //商户订单号
        model.setProductCode("FACE_TO_FACE_PAYMENT"); //销售产品码
        model.setSubject("海贼王");                    //订单标题
        model.setTotalAmount("100");                  //订单总金额

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);

        AlipayTradePrecreateResponse alipayTradePrecreateResponse = alipayClient.execute(request);
        String content=alipayTradePrecreateResponse.getQrCode();

        makeQRCode(content,response.getOutputStream());
    }

    public void makeQRCode(String content, OutputStream outputStream) throws Exception{  //生成二维码
        Map<EncodeHintType,Object> map = new HashMap<>();
        map.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);
        map.put(EncodeHintType.MARGIN,2);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE,200,200,map);
        MatrixToImageWriter.writeToStream(bitMatrix,"jpeg",outputStream);
    }

    @RequestMapping("/cancel/{code}")
    public String cancel(@PathVariable("code") String code) throws Exception{  //取消订单，支付超时、支付结果未知是可撤销，超过24小时不可撤销
        AlipayTradeCancelModel model = new AlipayTradeCancelModel();
        model.setOutTradeNo(code);

        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        request.setBizModel(model);

        AlipayTradeCancelResponse response = alipayClient.execute(request);
        return response.getBody();
    }

    @RequestMapping("/notify")
    public void notify(HttpServletRequest request) throws Exception{   //trade_success状态下异步通知接口
        if (check(request.getParameterMap())){
            System.out.println(request.getParameter("trade_status"));
            System.out.println("异步通知 "+ Instant.now());
        }else {
            System.out.println("验签失败");
        }
    }

    @RequestMapping("/return")
    public String returnUrl(HttpServletRequest request) throws Exception{  //订单支付成功后同步返回地址
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
            System.out.println(name + " ==> " + valueStr);
        }

        return AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), alipayProperties.getSignType()); //调用SDK验证签名
    }
}
