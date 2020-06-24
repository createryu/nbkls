package com.yuqiliu.dao.elasticsearch;

import com.yuqiliu.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author yuqiliu
 * @create 2020-06-04  14:29
 */

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {


}
