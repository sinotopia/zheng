package com.sinotopia.fundamental.dubbo.test.anotation;

import com.sinotopia.fundamental.dubbo.test.loader.SinotopiaDubboApplicationContextLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

/**
 * <p>输入注释</p>
 *
 * @Author dzr
 * @Date 2016/6/6
 */
@ContextConfiguration(loader = SinotopiaDubboApplicationContextLoader.class)
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SinotopiaDubboApplicationConfiguration {

    /**
     * The context configuration classes.
     * @see org.springframework.test.context.ContextConfiguration#classes()
     * @return the context configuration classes
     */
    @AliasFor("classes")
    Class<?>[] value() default {};

    /**
     * The context configuration locations.
     * @see org.springframework.test.context.ContextConfiguration#locations()
     * @return the context configuration locations
     */
    @AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
    String[] locations() default {"classpath*:spring/applicationContext-*.xml","classpath*:spring/dubbo-*.xml","classpath*:applicationContext-*.xml"};

    /**
     * The context configuration classes.
     * @see org.springframework.test.context.ContextConfiguration#classes()
     * @return the context configuration classes
     */
    @AliasFor("value")
    Class<?>[] classes() default {};

    /**
     * The context configuration initializers.
     * @see org.springframework.test.context.ContextConfiguration#initializers()
     * @return the context configuration initializers
     */
    @AliasFor(annotation = ContextConfiguration.class, attribute = "initializers")
    Class<? extends ApplicationContextInitializer<? extends ConfigurableApplicationContext>>[] initializers() default {};

    /**
     * Should context locations be inherited.
     * @see org.springframework.test.context.ContextConfiguration#inheritLocations()
     * @return {@code true} if context locations should be inherited
     */
    @AliasFor(annotation = ContextConfiguration.class, attribute = "inheritLocations")
    boolean inheritLocations() default true;

    /**
     * Should initializers be inherited.
     * @see org.springframework.test.context.ContextConfiguration#inheritInitializers()
     * @return {@code true} if context initializers should be inherited
     */
    @AliasFor(annotation = ContextConfiguration.class, attribute = "inheritInitializers")
    boolean inheritInitializers() default true;

    /**
     * The name of the context hierarchy level.
     * @see org.springframework.test.context.ContextConfiguration#name()
     * @return the name of the context hierarchy level
     */
    @AliasFor(annotation = ContextConfiguration.class, attribute = "name")
    String name() default "";
}
