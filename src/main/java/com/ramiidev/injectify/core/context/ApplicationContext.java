package com.ramiidev.injectify.core.context;

import com.ramiidev.injectify.core.beans.BeanDefinitionRegistry;
import com.ramiidev.injectify.core.beans.BeanFactory;
import com.ramiidev.injectify.core.beans.lifecycle.DisposableBean;

public interface ApplicationContext extends BeanFactory, BeanDefinitionRegistry, DisposableBean {
    void refresh();
    String[] getBeanNamesForType(Class<?> type);
    void registerSingleton(Class<?> type, Object singletonObject);
}
