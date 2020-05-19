package com.yuqiliu.dao;

import com.yuqiliu.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author yuqiliu
 * @create 2020-05-18  23:04
 */

@Mapper
@Repository
public interface LoginTicketMapper {


    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);


    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);


    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);
}
