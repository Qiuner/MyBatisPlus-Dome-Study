package com.itheima.mp.controller;

import cn.hutool.core.bean.BeanUtil;
import com.itheima.mp.domain.dto.UserFormDTO;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.query.UserQuery;
import com.itheima.mp.domain.vo.UserVO;
import com.itheima.mp.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户管理接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("users")
public class UserController {

    private final IUserService userService;

    @PostMapping
    @ApiOperation("新增用户")
    public void saveUser(@RequestBody UserFormDTO userFormDTO){
        // 1.转换DTO为PO
        User user = BeanUtil.copyProperties(userFormDTO, User.class);
        // 2.新增
        userService.save(user);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public void removeUserById(@PathVariable("id") Long userId){
        userService.removeById(userId);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询用户")
    public UserVO queryUserById(@PathVariable("id") Long userId){
        // 1.查询用户
        return userService.queryUserAndAddressById(userId);
    }

    @GetMapping
    @ApiOperation("根据id集合查询用户")
    public List<UserVO> queryUserByIds(@RequestParam("ids") List<Long> ids) {
        // 1.查询用户
        return userService.queryUserAndAddressByIds(ids);

    }

    @ApiOperation("扣减用户余额接口")
    @PutMapping("/{id}/deduction/{money}")
    public void deductBalance(
         @ApiParam("用户id") @PathVariable("id") Long userId,
         @ApiParam("扣减的金额")   @PathVariable("money") Integer money){
        userService.deductBalance(userId, money);
    }

    @GetMapping("/list")
    @ApiOperation("根据对象查询用户")
    public List<UserVO> queryUsers(UserQuery query){
        //查询用户po
        List<User> users = userService.queryUsers(
                query.getName(),query.getStatus(),
                query.getMinBalance(),query.getMaxBalance());
        //处理vo
        return BeanUtil.copyToList(users, UserVO.class);
    }
}
