package com.sinotopia.fundamental.config;

import com.baidu.disconf.client.addons.properties.PropertiesReloadedEvent;
import com.baidu.disconf.client.addons.properties.ReloadingPropertyPlaceholderConfigurer;

/**
 * 重写百度的PropertyPlaceholderConfigurer，添加FundamentalConfigProvider重新加载配置
 */
public class BaiduPropertyPlaceholderConfigurer extends ReloadingPropertyPlaceholderConfigurer {
    @Override
    public void propertiesReloaded(PropertiesReloadedEvent event) {
        super.propertiesReloaded(event);
        FundamentalConfigProvider.reload();
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        FundamentalConfigProvider.reload();
    }
}
