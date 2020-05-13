package com.yuqiliu.dao;

import com.yuqiliu.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author yuqiliu
 * @create 2020-05-13  0:36
 */

@Repository
@Mapper
public interface UserMapper {

    User selectById(int id);
}