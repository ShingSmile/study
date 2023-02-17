package com.study.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.BaseContext;
import com.study.common.Result;
import com.study.common.ShoppingCart;
import com.study.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("shoppingCart={}", shoppingCart);
        //获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        //设置当前用户id
        shoppingCart.setUserId(currentId);
        //获取当前菜品id
        Long dishId = shoppingCart.getDishId();
        //条件构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //判断添加的是菜品还是套餐
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //查询当前菜品或者套餐是否在购物车中
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne != null) {
            //如果已存在就在当前的数量上加1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //如果不存在，则还需设置一下创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            //如果不存在，则添加到购物车，数量默认为1
            shoppingCartService.save(shoppingCart);
            //这里是为了统一结果，最后都返回cartServiceOne会比较方便
            cartServiceOne = shoppingCart;
        }
        return Result.success(cartServiceOne);
    }

    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = BaseContext.getCurrentId();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        return Result.success(shoppingCarts);
    }

    @DeleteMapping("/clean")
    public Result<String> clean() {
        //条件构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        queryWrapper.eq(userId != null, ShoppingCart::getUserId, userId);
        //删除当前用户id的所有购物车数据
        shoppingCartService.remove(queryWrapper);
        return Result.success("成功清空购物车");
    }
}