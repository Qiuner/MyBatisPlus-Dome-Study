package com.itheima.mp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.itheima.mp.domain.po.Address;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.vo.AddressVO;
import com.itheima.mp.domain.vo.UserVO;
import com.itheima.mp.enums.UserStatus;
import com.itheima.mp.mapper.UserMapper;
import com.itheima.mp.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        if (user.getStatus() == UserStatus.FREEZE) {
            throw new RuntimeException("用户被冻结");
        }

        // 校验余额
        if (user.getBalance() < money) {
            throw new RuntimeException("用户余额不足") ;
        }
        // 4.扣减余额 update tb_user set balance = balance - ?
        int remainBalance = user.getBalance() - money;
        lambdaUpdate()
                .set(User::getBalance, remainBalance) // 更新余额
                .set(remainBalance == 0, User::getStatus, UserStatus.FREEZE) // 动态判断，是否更新status
                .eq(User::getId, userId)
                // 存在线程安全问题
                .eq(User::getBalance, user.getBalance()) // 乐观锁
                .update();
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

    @Override
    public UserVO queryUserAndAddressById(Long userId) {
        // 1.查询用户
        User user = getById(userId);
        if (user == null || user.getStatus()== UserStatus.FREEZE) {
            throw new RuntimeException("用户状态异常");
        }
        // 2.查询收货地址
        List<Address> addresses = Db.lambdaQuery(Address.class)
                .eq(Address::getUserId, userId)
                .list();
        // 3.处理vo
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        userVO.setAddresses(BeanUtil.copyToList(addresses, AddressVO.class));
        return userVO;
    }

    @Override
    public List<UserVO> queryUserAndAddressByIds(List<Long> ids) {
        // 1 查询用户
        List<User> users = listByIds(ids);
        if (CollUtil.isEmpty(users)) {
            return Collections.emptyList();
        }
        // 2 查询地址 获取用户集合
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        // 2.2 根据用户id查询地址
        List<Address> addresses = Db.lambdaQuery(Address.class).in(Address::getUserId, userIds).list();
        // 3 处理vo
        List<UserVO> userVOS = BeanUtil.copyToList(users, UserVO.class);
        userVOS.forEach(userVO -> {
            List<AddressVO> addressVOS = BeanUtil.copyToList(
                    addresses.stream()
                            .filter(address -> address.getUserId().equals(userVO.getId()))
                            .collect(Collectors.toList()),
                    AddressVO.class);
            userVO.setAddresses(addressVOS);
        });
        return userVOS;
    }
}