package com.hkfs.fundamental.api.params;

import com.hkfs.fundamental.api.data.DataObjectBase;

/**
 * 参数基类
 * Created by zhoubing on 2016/4/20.
 */
public class BaseParameter extends DataObjectBase {
    private static final long serialVersionUID = 1L;

    /**
     * 客户端的IP地址
     */
    private String clientIp;

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
