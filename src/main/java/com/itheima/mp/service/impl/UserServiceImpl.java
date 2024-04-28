package com.itheima.mp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.itheima.mp.domain.dto.PageDTO;
import com.itheima.mp.domain.po.Address;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.query.PageQuery;
import com.itheima.mp.domain.query.UserQuery;
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



    @Override
    public PageDTO<UserVO> queryUsersPage(PageQuery query) {
        // 1.构建条件
        // 1.1.分页条件
        Page<User> page = Page.of(query.getPageNo(), query.getPageSize());
        // 1.2.排序条件
        if (query.getSortBy() != null) {
            page.addOrder(new OrderItem(query.getSortBy(), query.getIsAsc()));
        }else{
            // 默认按照更新时间排序
            page.addOrder(new OrderItem("update_time", false));
        }
        // 2.查询
        page(page);
        // 3.数据非空校验
        List<User> records = page.getRecords();
        if (records == null || records.size() <= 0) {
            // 无数据，返回空结果
            // return new PageDTO<>(page.getTotal(), page.getPages(), Collections.emptyList());
        }
        // 4.有数据，转换
        List<UserVO> list = BeanUtil.copyToList(records, UserVO.class);
        // 5.封装返回
        // return new PageDTO<UserVO>(page.getTotal(), page.getPages(), list);
        return  null;
    }
}