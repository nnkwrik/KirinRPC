package io.github.nnkwrik.kirinrpc.springboot.config.consumer;

import io.github.nnkwrik.kirinrpc.springboot.annotation.KirinConsumeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * 对 @KirinConsumeService 进行自动装载。
 * 类似 @Autowired 的功能,但实际装载的是proxy
 *
 * @author nnkwrik
 * @date 19/05/23 10:42
 */
@Component
public class KirinAutowiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, PriorityOrdered {

    private final Log logger = LogFactory.getLog(getClass());

    private final ConcurrentMap<String, ConsumerInjectionMetadata> injectionMetadataCache =
            new ConcurrentHashMap<>(256);

    private final ConcurrentMap<String, KirinConsumerBean<?>> consumerBeansCache =
            new ConcurrentHashMap<>();


    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        //找到带有 @KirinConsumeService 的属性和方法
        InjectionMetadata metadata = findConsumerMetadata(beanName, bean.getClass(), pvs);
        try {
            //往带有 @KirinConsumeService 注解的属性或方法中注入bean
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @KirinConsumeService dependencies failed", ex);
        }
        return pvs;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findConsumerMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    private InjectionMetadata findConsumerMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        ConsumerInjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        //为这个类构建ConsumerInjectionMetadata，包含需要注入的属性和方法的信息
                        metadata = buildConsumerMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName() +
                                "] for consumer metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    private ConsumerInjectionMetadata buildConsumerMetadata(final Class<?> beanClass) {
        //遍历类中的属性，把带有 @KirinConsumeService 的属性构建为ConsumerFieldElement
        Collection<ConsumerFieldElement> fieldElements = findFieldConsumerMetadata(beanClass);
        //遍历类中的方法, 、、
        Collection<ConsumerMethodElement> methodElements = findMethodConsumerMetadata(beanClass);
        return new ConsumerInjectionMetadata(beanClass, fieldElements, methodElements);

    }

    private List<ConsumerFieldElement> findFieldConsumerMetadata(final Class<?> beanClass) {

        final List<ConsumerFieldElement> elements = new LinkedList<>();

        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

                KirinConsumeService comsumer = getAnnotation(field, KirinConsumeService.class);

                if (comsumer != null) {

                    if (Modifier.isStatic(field.getModifiers())) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("@KirinConsumeService annotation is not supported on static fields: " + field);
                        }
                        return;
                    }

                    elements.add(new ConsumerFieldElement(field, comsumer));
                }

            }
        });

        return elements;

    }

    private List<ConsumerMethodElement> findMethodConsumerMetadata(final Class<?> beanClass) {

        final List<ConsumerMethodElement> elements = new LinkedList<>();

        ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {

                Method bridgedMethod = findBridgedMethod(method);

                if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }

                KirinConsumeService consumer = findAnnotation(bridgedMethod, KirinConsumeService.class);

                if (consumer != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("@KirinConsumeService annotation is not supported on static methods: " + method);
                        }
                        return;
                    }
                    if (method.getParameterTypes().length == 0) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("@KirinConsumeService  annotation should only be used on methods with parameters: " +
                                    method);
                        }
                    }
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
                    elements.add(new ConsumerMethodElement(method, pd, consumer));
                }
            }
        });

        return elements;

    }

    private static class ConsumerInjectionMetadata extends InjectionMetadata {

        private final Collection<ConsumerFieldElement> fieldElements;

        private final Collection<ConsumerMethodElement> methodElements;


        public ConsumerInjectionMetadata(Class<?> targetClass, Collection<ConsumerFieldElement> fieldElements,
                                         Collection<ConsumerMethodElement> methodElements) {
            super(targetClass, combine(fieldElements, methodElements));
            this.fieldElements = fieldElements;
            this.methodElements = methodElements;
        }

        private static <T> Collection<T> combine(Collection<? extends T>... elements) {
            List<T> allElements = new ArrayList<T>();
            for (Collection<? extends T> e : elements) {
                allElements.addAll(e);
            }
            return allElements;
        }

        public Collection<ConsumerFieldElement> getFieldElements() {
            return fieldElements;
        }

        public Collection<ConsumerMethodElement> getMethodElements() {
            return methodElements;
        }
    }

    private class ConsumerFieldElement extends InjectionMetadata.InjectedElement {

        private final Field field;

        private final KirinConsumeService consumeServiceAnnotation;

        private volatile KirinConsumerBean<?> consumerBean;

        protected ConsumerFieldElement(Field field, KirinConsumeService consumeServiceAnnotation) {
            super(field, null);
            this.field = field;
            this.consumeServiceAnnotation = consumeServiceAnnotation;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<?> consumerClass = field.getType();

            consumerBean = buildConsumerBean(consumeServiceAnnotation, consumerClass);

            ReflectionUtils.makeAccessible(field);

            field.set(bean, consumerBean.getObject());

        }

    }

    private class ConsumerMethodElement extends InjectionMetadata.InjectedElement {

        private final Method method;

        private final KirinConsumeService consumeServiceAnnotation;

        private volatile KirinConsumerBean<?> consumerBean;

        protected ConsumerMethodElement(Method method, PropertyDescriptor pd, KirinConsumeService consumeServiceAnnotation) {
            super(method, pd);
            this.method = method;
            this.consumeServiceAnnotation = consumeServiceAnnotation;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<?> consumerClass = pd.getPropertyType();

            consumerBean = buildConsumerBean(consumeServiceAnnotation, consumerClass);

            ReflectionUtils.makeAccessible(method);

            method.invoke(bean, consumerBean.getObject());

        }

    }

    private KirinConsumerBean<?> buildConsumerBean(KirinConsumeService consumeServiceAnnotation, Class<?> consumerClass) throws Exception {

        String consumerBeanCacheKey = consumeServiceAnnotation.group() + "/" + consumerClass.getName();

        KirinConsumerBean<?> consumerBean = consumerBeansCache.get(consumerBeanCacheKey);

        if (consumerBean == null) {

            consumerBean = new KirinConsumerBean<>(consumerClass, consumeServiceAnnotation);

            consumerBeansCache.putIfAbsent(consumerBeanCacheKey, consumerBean);
        }

        return consumerBean;

    }
}
