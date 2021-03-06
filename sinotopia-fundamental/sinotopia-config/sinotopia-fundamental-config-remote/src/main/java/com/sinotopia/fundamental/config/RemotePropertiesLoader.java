package com.sinotopia.fundamental.config;

import com.baidu.disconf.client.common.model.DisconfCenterFile;
import com.baidu.disconf.client.store.inner.DisconfCenterStore;

import java.util.Map;
import java.util.Properties;

/**
 * 远程配置加载器
 */
public class RemotePropertiesLoader implements PropertiesLoader {
    @Override
    public void load(Properties properties) {
        Map<String, DisconfCenterFile> fileMap = DisconfCenterStore.getInstance().getConfFileMap();
        if (fileMap == null || fileMap.values().size() == 0) {
            return;
        }

        for (DisconfCenterFile disconfCenterFile : fileMap.values()) {
            if (disconfCenterFile.getFilePath() != null && disconfCenterFile.getFilePath().length() > 0) {
                String configPath = FilePropertiesLoader.FILE_PREFIX + disconfCenterFile.getFilePath();
                PropertiesLoader propertiesLoader = new FilePropertiesLoader(configPath,
                        com.sinotopia.fundamental.config.FundamentalConfigProvider.getCharset());
                propertiesLoader.load(properties);
            }
        }
    }
}
