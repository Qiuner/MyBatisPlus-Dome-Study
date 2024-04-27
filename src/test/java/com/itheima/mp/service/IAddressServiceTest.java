package com.itheima.mp.service;

import com.itheima.mp.domain.po.Address;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class IAddressServiceTest {

    @Autowired
    private IAddressService addressService;
    @Test
    public void testDeleteByLogic() {
        // 删除
        addressService.removeById(59L);
    }
    @Test
    public void testQuery() {
        List<Address> list = addressService.list();
        list.forEach(System.out::println);
    }
}
