package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.common.utils.QiNiuYunUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QiNiuYunBannerTest {
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;

    @Test
    public void test0001() {
        try {
            System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
            String path = "D:\\pic";
            File fileInt = new File(path);
            String resultPath = "D:\\result.txt";
            File fileOut = new File(resultPath);
            FileWriter fw = new FileWriter(fileOut);
            BufferedWriter bfw = new BufferedWriter(fw);
            String[] filesName = fileInt.list();
            for (String fileName : filesName) {
                //String uploadFile="qweqwe";
                String uploadFile = qiNiuYunUtil.uploadFile(path + "\\" + fileName);
                //uploadFile = "http://img-ybh.oduosa.com/" + uploadFile;
                System.out.println(uploadFile);
                bfw.write("insert into t_opt_adv_banner (evebNum,clientShow,advType,picPcPath,picMbPath) values("+fileName.substring(0,2)+",2,1,'" + uploadFile + "','"+uploadFile+"'); /* " + fileName+"*/");
                bfw.newLine();
                bfw.flush();
            }
            bfw.close();
            System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } catch (Exception e) {
            System.out.println("--------------");
        }

    }

    @Test
    public void test0002() {
        try {
            System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");

            int tampNum[] = {1,2,3,4,5,6,7,8,9,10,11,12};
            String resultPath = "D:\\result.txt";
            File fileOut = new File(resultPath);
            FileWriter fw = new FileWriter(fileOut);
            BufferedWriter bfw = new BufferedWriter(fw);
            for (int num:  tampNum) {
                String path = "D:\\pic\\"+num;
                File fileInt = new File(path);

                bfw.write("/**??????  "+num+"**/");
                bfw.newLine();

                String[] filesName = fileInt.list();
                for (String fileName : filesName) {
                    //String uploadFile="qweqwe";
                    String uploadFile = qiNiuYunUtil.uploadFile(path + "\\" + fileName);
                    //uploadFile = "http://img-ybh.oduosa.com/" + uploadFile;
                    System.out.println(uploadFile);
                    Integer type=0;
                    String typeName = fileName.substring(0,2);
                    switch (typeName){
                        case "??????":
                            type=2;
                            break;
                        case "??????":
                            type=3;
                            break;
                        case "??????":
                            type=4;
                            break;
                        case "??????":
                            type=5;
                            break;
                        case "??????":
                            type=6;
                            break;
                    }
                    bfw.write("insert into t_opt_adv_banner (evebNum,clientShow,advType,picPcPath,picMbPath) values("+num+",0,"+type+",'" + uploadFile + "',''); /* " + fileName+"*/");
                    bfw.newLine();
                    bfw.flush();
                }

            }
            bfw.close();
            System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } catch (Exception e) {
            System.out.println("--------------");
        }

    }
}
