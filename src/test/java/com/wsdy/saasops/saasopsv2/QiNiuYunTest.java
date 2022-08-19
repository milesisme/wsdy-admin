package com.wsdy.saasops.saasopsv2;

import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.wsdy.saasops.common.utils.QiNiuYunUtil;

import javax.imageio.ImageIO;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QiNiuYunTest {
	@Autowired
	private QiNiuYunUtil qiNiuYunUtil;
	// 查询七牛云上所有的文件名
	@Test
	public void test002() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		List<String> list = qiNiuYunUtil.bucketFileList();
		for (String str : list) {
			System.out.println(str);
		}
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	// 删除对应的文件
	@Test
	public void test003() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.deleteFile("FtK-7CiGqM_8O04zMXadXTsB6VFg");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	// 通过字节数组与文件名的形式上传
	@Test
	public void test004() throws Exception {
		Long start = System.currentTimeMillis();
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		File file = new File("C:\\Users\\Gray\\Desktop\\123.jpg");
		InputStream in = new FileInputStream(file);
		byte[] data = qiNiuYunUtil.readInputStreamAsByteArray(in);
		String fileName = file.getName();
		String uploadFile = qiNiuYunUtil.uploadFile(data, fileName);
		System.out.println(uploadFile);// FmTG8HZNI5gBU8Aqpoudz-0w6ruw
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<"+(System.currentTimeMillis()-start));

	}

	@Test
	public void uploadMultiple() throws Exception {
		Long start = System.currentTimeMillis();
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		String path = "C:\\Users\\Staff\\Desktop\\icon\\";
		String[] files = new String[]{"456.jpg"};
		for (int i = 0; i < files.length; i++){
			File file = new File(path+ files[i]);
			InputStream in = new FileInputStream(file);
			byte[] data = qiNiuYunUtil.readInputStreamAsByteArray(in);
			String fileName = file.getName();
			String uploadFile = qiNiuYunUtil.uploadFile(data, fileName);
			System.out.println(uploadFile);// FmTG8HZNI5gBU8Aqpoudz-0w6ruw
		}


		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<"+(System.currentTimeMillis()-start));

	}


	@Test
	public void test00477() throws Exception {
		//设置二维码像素
		int width = 300;
		int height = 300;
		//要生成什么格式的二维码
		String format = "png";
		//二维码当中要存储什么信息
		String content = "http://www.baidu.com";
		HashMap hints = new HashMap();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		//设置纠错率，分为L、M、H三个等级，等级越高，纠错率越高，但存储的信息越少
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
		//设置一下边距，默认是5
		hints.put(EncodeHintType.MARGIN, 0);
		try {
			BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
			BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			String uploadFile = qiNiuYunUtil.uploadFile(baos.toByteArray(), "test111111.jpg");
			System.out.println(uploadFile);// FmTG8HZNI5gBU8Aqpoudz-0w6ruw

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	// 通过本地文件上传,传入文件路径
	@Test
	public void test005() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		String uploadFile = qiNiuYunUtil.uploadFile("D:\\sdy会员\\photo_2021-09-06_17-09-51.jpg");
		System.out.println(uploadFile);
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	// 通过本地文件上传，传入文件夹路径
	@Test
	public void test0005() {
		try {
		    long startTime=System.currentTimeMillis();
			System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
			String path = "F:\\pic";
			File fileInt = new File(path);
			String resultPath = "D:\\result.txt";
			File fileOut = new File(resultPath);
			FileWriter fw = new FileWriter(fileOut);
			BufferedWriter bfw = new BufferedWriter(fw);
			String[] filesName = fileInt.list();
			for (String fileName : filesName) {
				String uploadFile = qiNiuYunUtil.uploadFile(path + "\\" + fileName);
//				uploadFile = domainOfBucket + uploadFile;
				System.out.println(uploadFile);
				bfw.write("insert into tableName column values('" + uploadFile + "'); -- " + fileName);
				bfw.newLine();
				bfw.flush();
			}
			bfw.close();
			System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
            long endTime=System.currentTimeMillis();
            System.out.println("---------------------->"+(endTime-startTime));
		} catch (Exception e) {
			System.out.println("--------------");
		}

	}
	@Test
	public void test00051() {
		try {
			System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
			String path = "D:\\backBankImg";
			File fileInt = new File(path);
			String resultPath = "D:\\backBankImg.sql";
			File fileOut = new File(resultPath);
			FileWriter fw = new FileWriter(fileOut);
			BufferedWriter bfw = new BufferedWriter(fw);
			String[] filesName = fileInt.list();
			for (String fileName : filesName) {
				String uploadFile = qiNiuYunUtil.uploadFile(path + "\\" + fileName);
//				uploadFile = "http://img-ybh.oduosa.com/" + uploadFile;
				System.out.println(uploadFile);
				bfw.write("update t_bs_bank set backBankImg ='" + uploadFile + "' where bankName='"+fileName+"'; -- " + fileName);
				bfw.newLine();
				bfw.flush();
			}
			bfw.close();
			System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
		} catch (Exception e) {
			System.out.println("--------------");
		}

	}

	// 通过文件名下载文件
	@Test
	public void test006() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.downLoadFile("FlHhseBeqAIG-JbeDUbEvrNTRokp", "D:\\");
		System.out.println("OK");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	/*@Test
	public void test007() {
		try {
			String url = "jdbc:mysql//192.168.5.30:8066";
			String username = "root";
			String password = "myCat_dev";
			String table = "tableName";
			String column1 = "id";
			String column2 = "picPcPath";

			File file = new File("");
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, username, password);
			String sql = "select * from " + table;
			FileWriter fw = new FileWriter(file);
			BufferedWriter bfw = new BufferedWriter(fw);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				int id = resultSet.getInt(column1);
				String picPath = resultSet.getString("column2");
				InputStream inputStream = fastDFSUtil.downloadFile("", picPath);
				byte[] data = qiNiuYunUtil.readInputStreamAsByteArray(inputStream);
				String uploadFile = qiNiuYunUtil.uploadFile(data, picPath);
				bfw.write("update " + table + " set " + column2 + "=" + uploadFile + " where " + column1 + "=" + id
						+ ";");
			}
			bfw.close();
		} catch (Exception e) {

		}
	}*/

	/*
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */
	// opr_adv
	@Test
	public void replace001() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"opr_adv", "id", "picPcPath", "F:\\a001\\opr_adv__picPcPath.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void replace002() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"opr_adv", "id", "picMbPath", "F:\\a001\\opr_adv__picMbPath.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	// opr_act_activity
	@Test
	public void replace0031() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"opr_act_activity", "id", "pcLogoUrl", "F:\\a001\\opr_act_activity__pcLogoUrl.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void replace0032() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"opr_act_activity", "id", "mbLogoUrl", "F:\\a001\\opr_act_activity__mbLogoUrl.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
	@Test
	public void replace0033() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"opr_act_activity", "id", "pcRemoteFileName", "F:\\a001\\opr_act_activity__pcRemoteFileName.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
	@Test
	public void replace0034() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"opr_act_activity", "id", "mbRemoteFileName", "F:\\a001\\opr_act_activity__mbRemoteFileName.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
	// set_basic_set_sys_setting
	// 单独手动替换
	//logoPath: http://img-ybh.oduosa.com/Fl9KcWZuqrbc1Y7QlsRueNN3zzuK
	//titlePath: http://img-ybh.oduosa.com/Fv-j3g1apBWra0f8V3h2544jPcPI

	// t_opr_adv
	@Test
	public void replace005() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"t_opr_adv", "id", "picPcPath", "F:\\a001\\t_opr_adv__picPcPath.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void replace006() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"t_opr_adv", "id", "picMbPath", "F:\\a001\\t_opr_adv__picMbPath.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	// t_op_pay
	@Test
	public void replace009() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"t_op_pay", "id", "mBankLog", "F:\\a001\\t_op_pay__mBankLog.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	// t_game_logo
	@Test
	public void replace012() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"t_game_logo", "id", "picUrl", "F:\\a001\\t_game_logo__picUrl.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void replace013() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"t_game_logo", "id", "mbPicUrl", "F:\\a001\\t_game_logo__mbPicUrl.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	// t_gm_game
	@Test
	public void replace014() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"t_gm_game", "id", "logo", "F:\\a001\\t_gm_game__logo.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void replace015() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"t_gm_game", "id", "logo2", "F:\\a001\\t_gm_game__logo2.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	// t_bs_bank
	@Test
	public void test0016() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		qiNiuYunUtil.replaceTablePath("jdbc:mysql://202.61.86.162:8066/saasops_a001", "root", "Dashujushiyindanmie",
				"t_bs_bank", "id", "bankLog", "F:\\a001\\t_bs_bank__bankLog.sql");
		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

}
