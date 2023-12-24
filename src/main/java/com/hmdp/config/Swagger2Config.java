package com.hmdp.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author Husp
 * @Date 2023/12/22 21:58
 */
@Configuration
@EnableSwagger2    //开启 Swagger2
@EnableKnife4j     //开启 knife4j，可以不写
//@Import(BeanValidatorPluginsConfiguration.class)
public class Swagger2Config extends WebMvcConfigurationSupport {

    /**
     * 显示swagger-ui.html文档展示页
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * swagger2的配置文件，这里可以配置swagger2的一些基本的内容，比如扫描的包等等
     * @return
     */
    @Bean(value = "defaultApi2")
    public Docket defaultApi2(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.hmdp.controller"))
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
//                .securitySchemes(Collections.singletonList(securityScheme()))
//                .securityContexts(securityContexts())
//                .globalOperationParameters(setHeaderToken());
    }

    /**
     * OAuth2配置
     * 需要增加swagger授权回调地址
     * @return
     */
//    @Bean
//    SecurityScheme securityScheme() {
//        return new ApiKey(X_ACCESS_TOKEN, X_ACCESS_TOKEN, "header");
//    }

    /**
     * JWT token
     * @return
     */
//    private List<Parameter> setHeaderToken() {
//        ParameterBuilder tokenPar = new ParameterBuilder();
//        List<Parameter> pars = new ArrayList<>();
//        tokenPar.name(X_ACCESS_TOKEN).description("token").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
//        pars.add(tokenPar.build());
//        return pars;
//    }

    /**
     * 新增 securityContexts 保持登录状态
     * @return
     */
//    private List<SecurityContext> securityContexts() {
//        return new ArrayList(
//                Collections.singleton(SecurityContext.builder()
//                .securityReferences(defaultAuth())
//                .forPaths(PathSelectors.regex("^(?!auth).*$"))
//                .build())
//        );
//    }

//    private List<SecurityReference> defaultAuth() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//        return new ArrayList(
//                Collections.singleton(new SecurityReference(X_ACCESS_TOKEN, authorizationScopes)));
//    }

    /**
     * api文档的详细信息函数,注意这里的注解引用的是哪个
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("hmdpAPI接口文档")
                .version("v1.0.1")
                .description("hmdpC端产品项目API接口")
                .contact(new Contact("后端产品开发项目组", "www.baidu.com", "shunpeng_hu@126.com"))
                .license("The Apache License, Version 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .build();
    }

}
