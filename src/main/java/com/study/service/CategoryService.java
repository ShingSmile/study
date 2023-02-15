package com.study.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
