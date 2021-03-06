package com.sinotopia.demonstration.admin.dao;

import com.sinotopia.demonstration.admin.domain.AdminMenu;
import com.sinotopia.fundamental.database.PageDaoBase;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminMenuDao extends PageDaoBase<AdminMenu, Long> {
}
