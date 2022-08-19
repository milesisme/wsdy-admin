package com.wsdy.saasops.saasopsv2.mybatis;


import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/**
 * @Description: mybatis-plus 连接mysql生成增删改查代码
 */
public class MySqlplusGenerator {
    public static void main(String[] args) {

        String moduleName = scanner("输入all 不区分路径生成，否则按照模块包名生成");//模块名可根据实际情况是否需要
        //指定表生成，不指定则全部生成
        //sc.setInclude("sys_parameter");
        String tables = scanner("输入all 全库生成 ，输入表名单个或多个，多个英文逗号分割");

        AutoGenerator ag = new AutoGenerator();
        //全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");//获取当前项目的路径
        projectPath = "C:";
        String projectName = "wsdy";
        gc.setOutputDir(projectPath + "/" + projectName + "/src/main/java");
        //gc.setSwagger2(true);
        gc.setBaseColumnList(true);
        gc.setBaseResultMap(true);
        gc.setIdType(IdType.AUTO);  //自增
        gc.setDateType(DateType.ONLY_DATE);
        gc.setServiceName("%sService");
        gc.setServiceImplName("%sServiceImpl");
//        gc.setControllerName("%sController");
        gc.setMapperName("%sMapper");
        gc.setXmlName("%sMapper");
        ag.setGlobalConfig(gc);

        //数据源配置
        DataSourceConfig ds = new DataSourceConfig();
        ds.setUrl("jdbc:mysql://10.0.0.166:3306/saasops_a001?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true");
        ds.setDriverName("com.mysql.cj.jdbc.Driver");
        ds.setUsername("admin");
        ds.setPassword("fHtEkmD6NRetAX5C");
        ag.setDataSource(ds);
        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.wsdy.saasops.modules");

        if (StringUtils.isNotBlank(moduleName) && !moduleName.equals("all")) {
            //配置路径
            pc.setEntity("entity");
            pc.setMapper("mapper." + moduleName);
            pc.setService("service." + moduleName);
            pc.setServiceImpl("service." + moduleName + ".impl");
//            pc.setController("controller." + moduleName);
            pc.setXml("xml." + moduleName);
        }
        ag.setPackageInfo(pc);

        //策略配置
        StrategyConfig sc = new StrategyConfig();

        if (StringUtils.isNotBlank(tables) && !tables.equals("all")) {
            sc.setInclude(tables.split(","));
        }

        sc.setNaming(NamingStrategy.underline_to_camel);
        sc.setColumnNaming(NamingStrategy.underline_to_camel);
        sc.setEntityLombokModel(true);
        sc.setRestControllerStyle(true);
//		sc.setSuperControllerClass("com.fl.play.base.BaseController");
        sc.setControllerMappingHyphenStyle(true);
        ag.setStrategy(sc);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setController(null);
        ag.setTemplate(templateConfig);

        //执行生成文件
        ag.execute();
    }

    /**
     * <p>
     * 读取控制台内容,用于自己输入要生成的模块(生成后以文件夹形式)和表名
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append(tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }
}

