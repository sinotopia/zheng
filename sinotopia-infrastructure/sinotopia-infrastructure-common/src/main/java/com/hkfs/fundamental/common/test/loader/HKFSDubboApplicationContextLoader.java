package com.hkfs.fundamental.common.test.loader;

import com.hkfs.fundamental.common.resolver.AppNameResolver;
import com.hkfs.fundamental.common.resolver.DefaultAppNameResolver;
import com.hkfs.fundamental.common.utils.StrUtils;
import com.hkfs.fundamental.config.FundamentalConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.web.ServletContextApplicationContextInitializer;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringBootMockServletContext;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.test.context.support.AnnotationConfigContextLoaderUtils;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * <p>
 *     不要在location里面配下面这些路径
 *     <ul>classpath*:spring/applicationContext-*.xml</ul>
 *     <ul>classpath*:spring/spring/dubbo-*.xml</ul>
 *     <ul>classpath*:classpath*:applicationContext-*.xml</ul>
 * </p>
 *
 * @Author dzr
 * @Date 2016/6/6
 */
public class HKFSDubboApplicationContextLoader extends AbstractContextLoader {

    private final static Logger LOGGER = LoggerFactory.getLogger(HKFSDubboApplicationContextLoader.class);

    private static final String APP_SPRING_EXCLUDE_XML_KEY = "app.spring.exclude.xml";
    private static final String APP_DUBBO_API_XML_TPL_KEY = "app.dubbo.api.xml.tpl";
    private static final String APP_NAME_RESOLVER_CLASS_KEY = "app.name.resovler.class";

    private static AppNameResolver appNameResolver = new DefaultAppNameResolver();
    private static String APP_DUBBO_API_XML_TPL = "applicationContext-{}-api.xml";

    private static String[] excludeLoadXmlArr = null;
    private static String appName = null;
    private static String apiXml = null;
    @Override
    public ApplicationContext loadContext(final MergedContextConfiguration config)
            throws Exception {
        assertValidAnnotations(config.getTestClass());
        SpringApplication application = new SpringApplication();
        application.setMainApplicationClass(config.getTestClass());
        application.setSources(getSources(config));
        ConfigurableEnvironment environment = new StandardEnvironment();
        if (!ObjectUtils.isEmpty(config.getActiveProfiles())) {
            setActiveProfiles(environment, config.getActiveProfiles());
        }
        Map<String, Object> properties = getEnvironmentProperties(config);
        addProperties(environment, properties);
        application.setEnvironment(environment);
        List<ApplicationContextInitializer<?>> initializers = getInitializers(config,
                application);
        if (config instanceof WebMergedContextConfiguration) {
            new WebConfigurer().configure(config, application, initializers);
        }
        else {
            application.setWebEnvironment(false);
        }
        application.setInitializers(initializers);
        ConfigurableApplicationContext applicationContext = application.run();
        return applicationContext;
    }

    private void assertValidAnnotations(Class<?> testClass) {
        boolean hasWebAppConfiguration = AnnotationUtils.findAnnotation(testClass,
                WebAppConfiguration.class) != null;
        boolean hasWebIntegrationTest = AnnotationUtils.findAnnotation(testClass,
                WebIntegrationTest.class) != null;
        if (hasWebAppConfiguration && hasWebIntegrationTest) {
            throw new IllegalStateException("@WebIntegrationTest and "
                    + "@WebAppConfiguration cannot be used together");
        }
    }

    private Set<Object> getSources(MergedContextConfiguration mergedConfig) {
        Set<Object> sources = new LinkedHashSet<Object>();
        try{
            LOGGER.info("dubbo test begin start ……");

            //如果配置文件中自定一些不需要加载的xml文件
            String excludeLoadXml = FundamentalConfigProvider.getString(APP_SPRING_EXCLUDE_XML_KEY);
            if(!StrUtils.isEmpty(excludeLoadXml)){
                excludeLoadXmlArr = excludeLoadXml.split(",");
                LOGGER.info("current dubbo excludeXml:{}",excludeLoadXmlArr);
            }
            //获取api模板
            String dubboApiXmlTpl = FundamentalConfigProvider.getString(APP_DUBBO_API_XML_TPL_KEY);
            if(!StrUtils.isEmpty(dubboApiXmlTpl)){
                APP_DUBBO_API_XML_TPL = dubboApiXmlTpl;
            }

            //获取app解析名称
            String appNameResolverClass = FundamentalConfigProvider.getString(APP_NAME_RESOLVER_CLASS_KEY);
            if(!StrUtils.isEmpty(appNameResolverClass)){
                Class<?> appNameResolverClz = ClassUtils.forName(appNameResolverClass, ClassUtils.getDefaultClassLoader());
                appNameResolver = (AppNameResolver) appNameResolverClz.newInstance();
            }
            LOGGER.info("current dubbo app name resovler:{}",appNameResolver.getClass().getName());
            LOGGER.info("current dubbo api xml tpl:{}",APP_DUBBO_API_XML_TPL);

            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

            //1.加载spring目录下面的dubbo-*.xml,获取appname
            Resource[]	dubboResources = resourcePatternResolver.getResources("classpath*:spring/dubbo-*.xml");
            if(dubboResources.length > 1){
                throw new IllegalArgumentException("dubbo service must define only one dubbo-*.xml,but find " + dubboResources.length + " in this dubbo!");
            }
            for(Resource resource : dubboResources) {
                String resourceName = resource.getFilename();
                LOGGER.info("Under classPath of {} ,the xml:{} is added!","classpath*:spring/dubbo-*.xml",resourceName);
                sources.add(resource);
                appName = getAppName(resourceName);
            }
            if(!StrUtils.isEmpty(appName)) {
                LOGGER.info("current test dubbo appName:{}", appName);
                apiXml = APP_DUBBO_API_XML_TPL.replace("{}", appName);
                LOGGER.info("current test dubbo apiXml:{}", apiXml);
            }
            //2.处理自定义的
            for(String location : mergedConfig.getLocations()){
                Resource[] resources = resourcePatternResolver.getResources(location);
                for(Resource resource : resources){
                    if(!isMatch(resource)) {
                        boolean flag = sources.add(resource);
                        if(flag) {
                            LOGGER.info("Under classPath of {} ,the xml:{} is added!", location, resource.getFilename());
                        }else{
                            LOGGER.info("Under classPath of {} ,the xml:{} is load!", location, resource.getFilename());
                        }
                    }
                }
            }
        }catch (Exception e){
            LOGGER.error("init sources happen error:",e);
        }

//        sources.addAll(Arrays.asList(mergedConfig.getLocations()));
        sources.addAll(Arrays.asList(mergedConfig.getClasses()));
        Assert.state(!sources.isEmpty(), "No configuration classes "
                + "or locations found in @SpringApplicationConfiguration. "
                + "For default configuration detection to work you need "
                + "Spring 4.0.3 or better (found " + SpringVersion.getVersion() + ").");
        return sources;
    }

    private void setActiveProfiles(ConfigurableEnvironment environment,
                                   String[] profiles) {
        EnvironmentTestUtils.addEnvironment(environment, "spring.profiles.active="
                + StringUtils.arrayToCommaDelimitedString(profiles));
    }

    protected Map<String, Object> getEnvironmentProperties(
            MergedContextConfiguration config) {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        // JMX bean names will clash if the same bean is used in multiple contexts
        disableJmx(properties);
        properties.putAll(TestPropertySourceUtils
                .convertInlinedPropertiesToMap(config.getPropertySourceProperties()));
        if (!TestAnnotations.isIntegrationTest(config)) {
            properties.putAll(getDefaultEnvironmentProperties());
        }
        return properties;
    }

    private void disableJmx(Map<String, Object> properties) {
        properties.put("spring.jmx.enabled", "false");
    }

    private Map<String, String> getDefaultEnvironmentProperties() {
        return Collections.singletonMap("server.port", "-1");
    }

    private void addProperties(ConfigurableEnvironment environment,
                               Map<String, Object> properties) {
        // @IntegrationTest properties go before external configuration and after system
        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MapPropertySource("integrationTest", properties));
    }

    private List<ApplicationContextInitializer<?>> getInitializers(
            MergedContextConfiguration mergedConfig, SpringApplication application) {
        List<ApplicationContextInitializer<?>> initializers = new ArrayList<ApplicationContextInitializer<?>>();
        initializers.add(new PropertySourceLocationsInitializer(
                mergedConfig.getPropertySourceLocations()));
        initializers.addAll(application.getInitializers());
        for (Class<? extends ApplicationContextInitializer<?>> initializerClass : mergedConfig
                .getContextInitializerClasses()) {
            initializers.add(BeanUtils.instantiate(initializerClass));
        }
        return initializers;
    }

    @Override
    public void processContextConfiguration(
            ContextConfigurationAttributes configAttributes) {
        super.processContextConfiguration(configAttributes);
        if (!configAttributes.hasResources()) {
            Class<?>[] defaultConfigClasses =  AnnotationConfigContextLoaderUtils
                    .detectDefaultConfigurationClasses(
                    configAttributes.getDeclaringClass());
            configAttributes.setClasses(defaultConfigClasses);
        }
    }


    @Override
    public ApplicationContext loadContext(String... locations) throws Exception {
        throw new UnsupportedOperationException("SpringApplicationContextLoader "
                + "does not support the loadContext(String...) method");
    }

    @Override
    protected String[] getResourceSuffixes() {
        return new String[] { "-context.xml", "Context.groovy" };
    }

    @Override
    protected String getResourceSuffix() {
        throw new IllegalStateException();
    }

    /**
     * Inner class to configure {@link WebMergedContextConfiguration}.
     */
    private static class WebConfigurer {

        private static final Class<GenericWebApplicationContext> WEB_CONTEXT_CLASS = GenericWebApplicationContext.class;

        void configure(MergedContextConfiguration configuration,
                       SpringApplication application,
                       List<ApplicationContextInitializer<?>> initializers) {
            if (!TestAnnotations.isIntegrationTest(configuration)) {
                WebMergedContextConfiguration webConfiguration = (WebMergedContextConfiguration) configuration;
                addMockServletContext(initializers, webConfiguration);
                application.setApplicationContextClass(WEB_CONTEXT_CLASS);
            }
        }

        private void addMockServletContext(
                List<ApplicationContextInitializer<?>> initializers,
                WebMergedContextConfiguration webConfiguration) {
            SpringBootMockServletContext servletContext = new SpringBootMockServletContext(
                    webConfiguration.getResourceBasePath());
            initializers.add(0,
                    new ServletContextApplicationContextInitializer(servletContext));
        }

    }

    /**
     * {@link ApplicationContextInitializer} to setup test property source locations.
     */
    private static class PropertySourceLocationsInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private final String[] propertySourceLocations;

        PropertySourceLocationsInitializer(String[] propertySourceLocations) {
            this.propertySourceLocations = propertySourceLocations;
        }

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addPropertiesFilesToEnvironment(applicationContext,
                    this.propertySourceLocations);
        }

    }

    private static class TestAnnotations {

        public static boolean isIntegrationTest(
                MergedContextConfiguration configuration) {
            return (hasAnnotation(configuration, IntegrationTest.class)
                    || hasAnnotation(configuration, WebIntegrationTest.class));
        }

        private static boolean hasAnnotation(MergedContextConfiguration configuration,
                                             Class<? extends Annotation> annotation) {
            return (AnnotationUtils.findAnnotation(configuration.getTestClass(),
                    annotation) != null);
        }

    }

    /**
     * 获取应用的名称
     * @param fileName
     * @return
     */
    private static String getAppName(String fileName){
        if( appNameResolver != null){
            return appNameResolver.resolver(fileName);
        }
        return null;
    }
    /**
     * 校验是否匹配
     * @param resource
     * @return
     */
    private static boolean isMatch(Resource resource){
        String xmlName = resource.getFilename();
        LOGGER.info("check test xml:{}",resource.getFilename());
        if(!StrUtils.isEmpty(apiXml)){
            if(xmlName.endsWith(apiXml)){
                LOGGER.info("current test xml name:{} matched with api xml:{} will be excluded !", xmlName,apiXml);
                return true;
            }
        }
        if(excludeLoadXmlArr != null){
            for(String excludeXml : excludeLoadXmlArr){
                if(xmlName.endsWith(excludeXml)){
                    LOGGER.info("current test xml name:{} matched with exclude xml:{} will be excluded !", xmlName,excludeXml);
                    return true;
                }
            }
        }
        return false;
    }
}
