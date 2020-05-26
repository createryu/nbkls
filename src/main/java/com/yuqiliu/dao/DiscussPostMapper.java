package com.yuqiliu.dao;

import com.yuqiliu.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuqiliu
 * @create 2020-05-12  12:43
 */

@Repository
@Mapper
public interface DiscussPostMapper {

    //设计的精妙之处在于带复用的功能
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    //设计的精妙之处在于带复用的功能
    int selectDiscussPostRows(@Param("userId") int userId);


    int insertDiscussPost(DiscussPost discussPost);


    DiscussPost selectDiscussPostById(int id);


    int updateCommentCount(int id,int commentCount);
}
