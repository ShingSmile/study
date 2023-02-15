package com.study.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.Dish;
import com.study.dto.DishDto;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);
}
