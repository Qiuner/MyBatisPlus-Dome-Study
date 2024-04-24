package com.itheima.mp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.mapper.UserMapper;
import com.itheima.mp.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements IUserService {
    @Override
    public void deductBalance(Long userId, Integer money) {
        // 查询用户
        User user = getById(userId);
        // 校验用户状态
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == 2) {
            throw new RuntimeException("用户被冻结");
        }

        // 校验余额
        if (user.getBalance() < money) {
            throw new RuntimeException("用户余额不足") ;
        }
        // 扣减余额
        baseMapper.deductBalance(userId, money);
        // 更新用户
        updateById(user);
    }

    @Override
    public List<User> queryUsers(String name, Integer status, Integer minBalance, Integer maxBalance) {

        return   lambdaQuery()
                .like(name != null, User::getUsername, name)
                .eq(status != null, User::getStatus, status)
                .ge(minBalance != null, User::getBalance, minBalance)
                .le(maxBalance != null, User::getBalance, maxBalance)
                .list();
    }
}