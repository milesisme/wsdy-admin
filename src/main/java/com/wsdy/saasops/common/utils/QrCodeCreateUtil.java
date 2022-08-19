package com.wsdy.saasops.common.utils;

import java.awt.image.BufferedImage;
import java.io.*;

import com.wsdy.saasops.common.exception.R200Exception;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.util.HashMap;

import javax.imageio.ImageIO;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * 二维码生成和读的工具类
 */
public class QrCodeCreateUtil {

    /**
     * 生成包含字符串信息的二维码图片
     */
    public static BufferedImage createQrCode(int width, int height, String content) {
        HashMap hints = new HashMap(4);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //设置纠错率，分为L、M、H三个等级，等级越高，纠错率越高，但存储的信息越少
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        //设置一下边距，默认是5
        hints.put(EncodeHintType.MARGIN, 0);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            throw new R200Exception("生成二维码失败");
        }
    }

    /**
     * 读二维码并输出携带的信息
     */
    public static String readQrCode(InputStream inputStream) {
        try {
            //从输入流中获取字符串信息
            BufferedImage image = ImageIO.read(inputStream);
            //将图像转换为二进制位图源
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            QRCodeReader reader = new QRCodeReader();
            Result result = reader.decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 测试代码
     */
    public static void main(String[] args) {
        //设置二维码像素
        int width = 150;
        int height = 150;
        //要生成什么格式的二维码
        String format = "png";
        //二维码当中要存储什么信息
        String content = "http://www.baidu.com";
        HashMap hints = new HashMap(4);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //设置纠错率，分为L、M、H三个等级，等级越高，纠错率越高，但存储的信息越少
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        //设置一下边距，默认是5
        hints.put(EncodeHintType.MARGIN, 0);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ImageIO.write(image, "png", new File("D://3333333.png"));
            System.out.println(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}