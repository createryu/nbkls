package com.yuqiliu.service;

import com.yuqiliu.dao.UserMapper;
import com.yuqiliu.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yuqiliu
 * @create 2020-05-13  0:40
 */

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id)
    {
        return userMapper.selectById(id);
    }


}
