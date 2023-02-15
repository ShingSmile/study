package com.study.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.Setmeal;
import com.study.dto.SetmealDto;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    void removeWithDish(List<Long> ids);
}
