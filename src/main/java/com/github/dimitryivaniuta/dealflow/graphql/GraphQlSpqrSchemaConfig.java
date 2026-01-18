package com.github.dimitryivaniuta.dealflow.graphql;

import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ClassUtils;

@Configuration
public class GraphQlSpqrSchemaConfig {

    @Bean(name = "graphQLSchema") // REQUIRED name for SPQR starter controller
    @Primary
    public GraphQLSchema graphQLSchema(ConfigurableListableBeanFactory beanFactory) {
        GraphQLSchemaGenerator generator = new GraphQLSchemaGenerator()
                .withBasePackages("com.github.dimitryivaniuta.dealflow");

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            if (beanFactory.findAnnotationOnBean(beanName, GraphQLApi.class) == null) {
                continue;
            }

            Object bean = beanFactory.getBean(beanName);

            // robust target type for proxied beans
            Class<?> targetType = ClassUtils.getUserClass(AopUtils.getTargetClass(bean));
            generator.withOperationsFromSingleton(bean, targetType);
        }

        return generator.generate();
    }
}
