package com.jerry.request_core.utils.reflect;


import androidx.annotation.NonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * 组合注解 对JDK的原生注解机制做一个增强，支持类似Spring的组合注解。<br>
 * 核心实现使用了递归获取指定元素上的注解以及注解的注解，以实现复合注解的获取。
 *
 * @author Succy,Looly
 * @since 4.0.9
 **/

public class CombinationAnnotationElement implements AnnotatedElement, Serializable {
    private static final long serialVersionUID = 1L;

    @SafeVarargs
    public static <T> HashSet<T> newHashSet(T... ts) {
        return set(false, ts);
    }

    @SafeVarargs
    public static <T> HashSet<T> set(boolean isSorted, T... ts) {
        if (null == ts) {
            return isSorted ? new LinkedHashSet<>() : new HashSet<>();
        }
        int initialCapacity = Math.max((int) (ts.length / .75f) + 1, 16);
        final HashSet<T> set = isSorted ? new LinkedHashSet<>(initialCapacity) : new HashSet<>(initialCapacity);
        Collections.addAll(set, ts);
        return set;
    }

    /** 元注解 */
    private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = newHashSet(
            Target.class,
            Retention.class,
            Inherited.class,
            Documented.class,
            SuppressWarnings.class,
            Override.class,
            Deprecated.class,
            kotlin.annotation.Retention.class,
            kotlin.annotation.Target.class,
            kotlin.annotation.Repeatable.class,
            kotlin.annotation.MustBeDocumented.class,
            kotlin.Metadata.class
    );

    /** 注解类型与注解对象对应表 */
    private Map<Class<? extends Annotation>, Annotation> annotationMap;
    /** 直接注解类型与注解对象对应表 */
    private Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap;

    /**
     * 构造
     *
     * @param element 需要解析注解的元素：可以是Class、Method、Field、Constructor、ReflectPermission
     */
    public CombinationAnnotationElement(AnnotatedElement element) {
        init(element);
    }

    @Override
    public boolean isAnnotationPresent(@NonNull Class<? extends Annotation> annotationClass) {
        return annotationMap.containsKey(annotationClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(@NonNull Class<T> annotationClass) {
        Annotation annotation = annotationMap.get(annotationClass);
        return (annotation == null) ? null : (T) annotation;
    }

    @NonNull
    @Override
    public Annotation[] getAnnotations() {
        final Collection<Annotation> annotations = this.annotationMap.values();
        return annotations.toArray(new Annotation[0]);
    }

    @NonNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        final Collection<Annotation> annotations = this.declaredAnnotationMap.values();
        return annotations.toArray(new Annotation[0]);
    }

    /**
     * 初始化
     *
     * @param element 元素
     */
    private void init(AnnotatedElement element) {
        final Annotation[] declaredAnnotations = element.getDeclaredAnnotations();
        this.declaredAnnotationMap = new HashMap<>();
        parseDeclared(declaredAnnotations);

        final Annotation[] annotations = element.getAnnotations();
        if(Arrays.equals(declaredAnnotations, annotations)) {
            this.annotationMap = this.declaredAnnotationMap;
        }else {
            this.annotationMap = new HashMap<>();
            parse(annotations);
        }
    }

    /**
     * 进行递归解析注解，直到全部都是元注解为止
     *
     * @param annotations Class, Method, Field等
     */
    private void parseDeclared(Annotation[] annotations) {
        Class<? extends Annotation> annotationType;
        // 直接注解
        for (Annotation annotation : annotations) {
            annotationType = annotation.annotationType();
            if (!META_ANNOTATIONS.contains(annotationType)) {
                declaredAnnotationMap.put(annotationType, annotation);
                parseDeclared(annotationType.getDeclaredAnnotations());
            }
        }
    }

    /**
     * 进行递归解析注解，直到全部都是元注解为止
     *
     * @param annotations Class, Method, Field等
     */
    private void parse(Annotation[] annotations) {
        Class<? extends Annotation> annotationType;
        for (Annotation annotation : annotations) {
            annotationType = annotation.annotationType();
            if (!META_ANNOTATIONS.contains(annotationType)) {
                annotationMap.put(annotationType, annotation);
                parse(annotationType.getAnnotations());
            }
        }
    }
}