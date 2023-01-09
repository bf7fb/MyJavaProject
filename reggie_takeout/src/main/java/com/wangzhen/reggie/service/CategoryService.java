package com.wangzhen.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangzhen.reggie.pojo.Category;

/**
 * @author wz
 * @ClassName CategoryService
 * @date 2023/1/6 16:23
 * @Description TODO
 */
public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
