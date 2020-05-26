package com.yuqiliu.dao;

import com.yuqiliu.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuqiliu
 * @create 2020-05-22  22:28
 */


@Mapper
@Repository
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

}
