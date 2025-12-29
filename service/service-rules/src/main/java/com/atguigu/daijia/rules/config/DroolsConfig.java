package com.atguigu.daijia.rules.config;


import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {
    // private static final String RULES_CUSTOMER_RULES_DRL = "rules/FeeRule.drl";
    @Bean
    public KieContainer kieContainer() {
        org.kie.api.KieServices kieServices = org.kie.api.KieServices.Factory.get();

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // 修改这一部分 - 加载具体的规则文件：
        // 1. 加载费用规则
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/FeeRule.drl"));
        // 2. 加载奖励规则  
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/RewardRule.drl"));
        // 3. 加载分账规则
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/ProfitsharingRule.drl"));
        // kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_CUSTOMER_RULES_DRL));

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        return kieContainer;
    }
}
