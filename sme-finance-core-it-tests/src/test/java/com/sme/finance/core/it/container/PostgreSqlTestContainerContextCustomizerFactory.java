package com.sme.finance.core.it.container;

import com.sme.finance.core.it.annotation.EmbeddedSQL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

@Slf4j
public class PostgreSqlTestContainerContextCustomizerFactory implements ContextCustomizerFactory {

    private static SqlTestContainer prodTestContainer;

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        return (context, mergedConfig) -> {
            final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

            TestPropertyValues testValues = TestPropertyValues.empty();
            if (null != AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedSQL.class)) {
                log.debug("detected the EmbeddedSQL annotation on class {}", testClass.getName());
                log.info("Warming up the sql database");

                if (prodTestContainer == null) {
                    prodTestContainer = beanFactory.createBean(PostgreSqlTestContainer.class);
                    beanFactory.registerSingleton(PostgreSqlTestContainer.class.getName(), prodTestContainer);
                }

                testValues = testValues.and("spring.datasource.url=" + prodTestContainer.getTestContainer().getJdbcUrl() + "");
                testValues = testValues.and("spring.datasource.username=" + prodTestContainer.getTestContainer().getUsername());
                testValues = testValues.and("spring.datasource.password=" + prodTestContainer.getTestContainer().getPassword());
            }

            testValues.applyTo(context);
        };
    }
}
