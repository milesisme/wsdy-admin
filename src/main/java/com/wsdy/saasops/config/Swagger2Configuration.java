package com.wsdy.saasops.config;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;



@Configuration
@EnableSwagger2
@EnableAutoConfiguration
//@Profile({"dev", "local"})
@EnableSwaggerBootstrapUI
public class Swagger2Configuration {

    @Value("${swagger.scope}")
    private String basePackage;

    @Value("${swagger.profile}")
    private String profile;

    @Bean
    public Docket buildDocket() {
        if("dev".equals(this.profile) || "test".equals(this.profile)){
            return new Docket(DocumentationType.SWAGGER_2).enable(true)
                    .groupName(profile)
                    .apiInfo(buildApiInf())
                    .select()
                    .apis(RequestHandlerSelectors.basePackage(basePackage))//要扫描的API(Controller)基础包
                    .paths(PathSelectors.any())
                    .build();
        }
        return new Docket(DocumentationType.SWAGGER_2).enable(false)
                .groupName(profile)
                .apiInfo(buildApiInf())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))//要扫描的API(Controller)基础包
                .paths(PathSelectors.any())
                .build();

    }

    //com.eveb.saasops.api.modules.user

    /*
     public Docket buildDocket() {
     return new Docket(DocumentationType.SWAGGER_2)
              .groupName("dev")
          .apiInfo(buildApiInf())
          .select()
          .apis(RequestHandlerSelectors.basePackage("com.eveb.saasops.api.modules"))//要扫描的API(Controller)基础包
          .paths(PathSelectors.any())
          .build();
    }*/
    private ApiInfo buildApiInf() {
        return new ApiInfoBuilder()
                .title("sdy saasops API文档")
                .contact("sdy")
                .version("1.0")
                .build();
    }
}
