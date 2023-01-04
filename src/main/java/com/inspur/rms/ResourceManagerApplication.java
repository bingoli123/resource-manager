package com.inspur.rms;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.dtflys.forest.springboot.annotation.ForestScan;
import com.inspur.ivideo.common.entity.consulentity.configyml.ConfigProperty;
import com.inspur.ivideo.common.entity.consulentity.configyml.DomainServiceMap2;
import com.inspur.ivideo.common.utils.ConsulConfigUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

/**
 * @author lidongbin
 */
@SpringBootApplication
@MapperScan(value = {"com.inspur.rms.dao", "com.inspur.ivideo.rabbit.producer.mapper"})
@EnableAsync
@ComponentScan(value = {"com.inspur.*", "com.inspur.ivideo.rabbit.*"})
@EnableScheduling
@EnableDiscoveryClient
@ForestScan(basePackages = {"com.inspur.rms.api"})
@ServletComponentScan
public class ResourceManagerApplication {
    public static String serviceUid = "";
    public static DomainServiceMap2 DOMAIN_SERVICE_MAP = null;

    public static void main(String[] args) throws IOException {
        //1、读取 /opt/components/resource-manager/configs/config.yml文件获取consul相关配置
        //2、生成application.yml文件
        String projectName = "resource-manager";
        System.out.println("start config consul...build config");
        String configUrl = "/opt/components/" + projectName + "/configs/config.yml";
        String applicationUrl = "/opt/components/" + projectName + "/configs";
        //获取config配置文件
        ConfigProperty configYml = ConsulConfigUtils.readBootstrapProperty(configUrl);
        serviceUid = configYml.getServiceUid();
        DomainServiceMap2 domainServiceMap = ConsulConfigUtils.buildYmlProperty2(configYml, applicationUrl, projectName);
        DOMAIN_SERVICE_MAP = domainServiceMap;
        System.out.println("end config consul...build config");
        SpringApplication.run(ResourceManagerApplication.class, args);
    }


    /**
     * 乐观锁配置
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    /*解决文件名中含有":\\"等特殊字符时，接口400的问题
     * Tomcat的新版本中增加了一个新特性，就是严格按照 RFC 3986规范进行访问解析，而 RFC 3986规范定义了Url中只允许包含英文字母（a-zA-Z）、数字（0-9）、-_.~4个特殊字符
     * 以及所有保留字符(RFC3986中指定了以下字符为保留字符：! * ’ ( ) ; : @ & = + $ , / ? # [ ])。*/
    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
        // 修改内置的 tomcat 容器配置
        TomcatServletWebServerFactory tomcatServlet = new TomcatServletWebServerFactory();
        tomcatServlet.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> connector.setProperty("relaxedQueryChars", "%=;[]{}"));
        //tomcatServlet.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> connector.setProperty("relaxedPathChars", "%=;[]{}"));
        return tomcatServlet;
    }
}
