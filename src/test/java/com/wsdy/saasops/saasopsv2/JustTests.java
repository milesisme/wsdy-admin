package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.analysis.dto.DepositOrBetDailyDto;
import com.wsdy.saasops.modules.analysis.mapper.AnalysisMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class JustTests {

    @Test
    public void myTest2(){
        Integer a1 = 1;
        Integer a2 = 1;
        Integer a3 = 0;
        Integer a4 = 0;

        System.out.println(a1 | a2);
        System.out.println(a1 | a3);
        System.out.println(a3 | a4);
    }

    @Test
    public void myTest1(){
        String startTime = "2022-04-28 15:58:58";
        String endTime = "2022-04-30 15:58:58";

        LocalDate sp = LocalDate.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDate ep = LocalDate.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime start = LocalDateTime.of(sp, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(ep, LocalTime.MAX);

        LocalDateTime endBegin = LocalDateTime.of(ep, LocalTime.MIN);
        LocalDateTime tmpTime = null;
        long step = 0l;
        do{
            // 每次增加一天，获取当天首充的用户
            tmpTime = start.plusDays(step);
            System.out.println(tmpTime.compareTo(endBegin));
            String tStr = tmpTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String eStr = tmpTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            System.out.println("tmpTime="+tStr);
            System.out.println("endBegin="+eStr);
            // 获取当前时间的起始和结束时间
            LocalDate theDate = LocalDate.parse(tStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime theDateStart = LocalDateTime.of(theDate, LocalTime.MIN);
            LocalDateTime theDateEnd = LocalDateTime.of(theDate, LocalTime.MAX);
            System.out.println("theDateStart="+theDateStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.println("theDateEnd="+theDateEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            step++;
        }while (tmpTime.compareTo(endBegin)<0);

        System.out.println("==================================================");

        System.out.println("start="+start);
        System.out.println("end="+end);
    }
}
