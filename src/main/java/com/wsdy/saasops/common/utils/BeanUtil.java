package com.wsdy.saasops.common.utils;

import com.github.pagehelper.Page;
import java.util.List;



public class BeanUtil {

    public static PageUtils toPagedResult(List datas) {
        PageUtils result = new PageUtils();
        if (datas instanceof Page) {
            Page page = (Page) datas;
            result.setCurrPage(page.getPageNum());
            result.setPageSize(page.getPageSize());
            result.setList(page.getResult());
            result.setTotalCount(page.getTotal());
            result.setTotalPage(page.getPages());
        }
        else {
            result.setCurrPage(1);
            result.setPageSize(datas.size());
            result.setList(datas);
            result.setTotalCount(datas.size());
        }

        return result;
    }

}
