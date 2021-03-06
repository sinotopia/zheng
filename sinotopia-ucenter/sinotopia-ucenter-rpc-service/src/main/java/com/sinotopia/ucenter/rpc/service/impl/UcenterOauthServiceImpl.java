package com.sinotopia.ucenter.rpc.service.impl;

import com.sinotopia.common.annotation.BaseService;
import com.sinotopia.common.base.BaseServiceImpl;
import com.sinotopia.ucenter.dao.mapper.UcenterOauthMapper;
import com.sinotopia.ucenter.dao.model.UcenterOauth;
import com.sinotopia.ucenter.dao.model.UcenterOauthExample;
import com.sinotopia.ucenter.rpc.api.UcenterOauthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UcenterOauthService实现
 * Created by shuzheng on 2017/4/27.
 */
@Service
@Transactional
@BaseService
public class UcenterOauthServiceImpl extends BaseServiceImpl<UcenterOauthMapper, UcenterOauth, UcenterOauthExample> implements UcenterOauthService {

    private static Logger _log = LoggerFactory.getLogger(UcenterOauthServiceImpl.class);

    @Autowired
    UcenterOauthMapper ucenterOauthMapper;

}