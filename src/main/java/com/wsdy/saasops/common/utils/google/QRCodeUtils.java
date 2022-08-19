package com.wsdy.saasops.common.utils.google;

import com.wsdy.saasops.common.exception.R200Exception;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 */
public class QRCodeUtils {
    private static final int width = 200;//二维码宽度(默认)
    private static final int height = 200;//二维码高度(默认)
    private static final String format = "png";//二维码文件格式
    private static final Map<EncodeHintType, Object> hints = new HashMap();//二维码参数

    static {
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");//字符编码
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//容错等级 H为最高
        hints.put(EncodeHintType.MARGIN, 2);//边距
    }

    /**
     * 返回一个 BufferedImage 对象
     *
     * @param content 二维码内容
     */
    public static BufferedImage toBufferedImage(String content) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * 返回一个 BufferedImage 对象
     *
     * @param content 二维码内容
     * @param width   宽
     * @param height  高
     */
    public static BufferedImage toBufferedImage(String content, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            throw new R200Exception("二维码转base64出错");
        }
    }


    /**
     * 将二维码图片输出到一个流中
     *
     * @param content 二维码内容
     */
    public static String writeToBase64String(String content) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream os = new ByteArrayOutputStream();//新建流。
            ImageIO.write(image, format, os);
            byte[] b = os.toByteArray();//从流中获取数据数组。
            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(b);
        } catch (Exception e) {
            throw new R200Exception("二维码转base64出错");
        }
    }


}
