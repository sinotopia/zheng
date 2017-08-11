package com.sinotopia.cms.rpc.api;

import com.sinotopia.common.base.BaseService;
import com.sinotopia.cms.dao.model.CmsArticle;
import com.sinotopia.cms.dao.model.CmsArticleExample;

import java.util.List;

/**
* CmsArticleService接口
* Created by shuzheng on 2017/4/5.
*/
public interface CmsArticleService extends BaseService<CmsArticle, CmsArticleExample> {

    // 根据类目获取文章列表
    List<CmsArticle> selectCmsArticlesByCategoryId(Integer categoryId, Integer offset, Integer limit);

    // 根据类目获取文章数量
    long countByCategoryId(Integer categoryId);

    // 根据标签获取文章列表
    List<CmsArticle> selectCmsArticlesByTagId(Integer tagId, Integer offset, Integer limit);

    // 根据标签获取文章数量
    long countByTagId(Integer tagId);

}