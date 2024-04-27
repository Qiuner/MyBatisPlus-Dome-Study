package com.itheima.mp.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.po.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testInsert() {
        User user = new User();
        // user.setId(5L);
        user.setUsername("adsfadafdaaffd123f");
        user.setPassword("123");
        user.setPhone("18688990011");
        user.setBalance(200);
        // user.setInfo("{\"age\": 24, \"intro\": \"英文老师\", \"gender\": \"female\"}");
        user.setInfo(UserInfo.of(24,"英文老师","female"));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Test
    void testSelectById() {
        User user = userMapper.selectById(5L);
        System.out.println("user = " + user);
    }


    @Test
    void testQueryByIds() {
        List<User> users = userMapper.selectBatchIds(List.of(1L, 2L, 3L, 4L));
        users.forEach(System.out::println);
    }

    @Test
    void testUpdateById() {
        User user = new User();
        user.setId(5L);
        user.setBalance(20000);
        userMapper.updateById(user);
    }

    @Test
    void testDeleteUser() {
        userMapper.deleteById(5L);
    }

    @Test
    void testQueryWrapper() {
        QueryWrapper<User> wrapper = new QueryWrapper<User>()
                .select("id", "username", "info", "balance")
                        .like("username","o")
                                .ge("balance",1000);
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(System.out::println);
    }

    @Test
    void testUpdateByQueryWrapper() {
    //    要更新的数据
        User user = new User();
        user.setBalance(20000);
    //     更新条件
        QueryWrapper<User> wrapper = new QueryWrapper<User>()
               .eq("username", "jack");
        userMapper.update(user, wrapper);
    }
    @Test
    void testUpdateWrapper() {
        List<Long> ids = List.of(1L, 2L, 4L);
        // 1.生成SQL
        UpdateWrapper<User> wrapper = new UpdateWrapper<User>()
                .setSql("balance = balance - 200") // SET balance = balance - 200
                .in("id", ids); // WHERE id in (1, 2, 4)
        // 2.更新，注意第一个参数可以给null，也就是不填更新字段和数据，
        // 而是基于UpdateWrapper中的setSQL来更新
        userMapper.update(null, wrapper);
    }

    @Test
    void testLambdaQueryWrapper() {
        // 1.构建条件 WHERE username LIKE "%o%" AND balance >= 1000
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .select(User::getId, User::getUsername, User::getInfo, User::getBalance)
                .like(User::getUsername, "o")
                .ge(User::getBalance, 1000);
        // 2.查询
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(System.out::println);
    }

    @Test
    void testCustomWrapper() {
        // 1.准备自定义查询条件
        List<Long> ids = List.of(1L, 2L, 4L);
        QueryWrapper<User> wrapper = new QueryWrapper<User>().in("id", ids);

        // 2.调用mapper的自定义方法，直接传递Wrapper
        userMapper.deductBalanceByIds(200, wrapper);
    }
}