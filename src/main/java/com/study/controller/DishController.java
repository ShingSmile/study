package com.study.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.Category;
import com.study.common.Dish;
import com.study.common.Result;
import com.study.dto.DishDto;
import com.study.service.CategoryService;
import com.study.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
//    @Autowired
//    private DishFlavorService dishFlavorService;

    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        log.info("接收到的数据为：{}",dishDto);
        dishService.saveWithFlavor(dishDto);
        return Result.success("菜品添加成功！");
    }

    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //这个就是我们到时候返回的结果
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝，这里只需要拷贝一下查询到的条目数
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //获取原records数据
        List<Dish> records = pageInfo.getRecords();

        //遍历每一条records数据
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //将数据赋给dishDto对象
            BeanUtils.copyProperties(item, dishDto);
            //然后获取一下dish对象的category_id属性
            Long categoryId = item.getCategoryId();  //分类id
            //根据这个属性，获取到Category对象（这里需要用@Autowired注入一个CategoryService对象）
            Category category = categoryService.getById(categoryId);
            //随后获取Category对象的name属性，也就是菜品分类名称
            String categoryName = category.getName();
            //最后将菜品分类名称赋给dishDto对象就好了
            dishDto.setCategoryName(categoryName);
            //结果返回一个dishDto对象
            return dishDto;
            //并将dishDto对象封装成一个集合，作为我们的最终结果
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return Result.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public Result<DishDto> getByIdWithFlavor(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        log.info("查询到的数据为：{}", dishDto);
        return Result.success(dishDto);
    }

    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info("接收到的数据为：{}", dishDto);
        dishService.updateWithFlavor(dishDto);
        return Result.success("修改菜品成功");
    }

    @GetMapping("/list")
    public Result<List<Dish>> get(Dish dish) {
        //条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //根据传进来的categoryId查询
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //只查询状态为1的菜品（启售菜品）
        queryWrapper.eq(Dish::getStatus, 1);
        //简单排下序，其实也没啥太大作用
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //获取查询到的结果作为返回值
        List<Dish> list = dishService.list(queryWrapper);
        return Result.success(list);
    }

}
