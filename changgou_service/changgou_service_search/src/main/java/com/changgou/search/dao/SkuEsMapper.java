package com.changgou.search.dao;

import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.search.dao
 * @date 2020-2-8
 */
@Repository
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {
}
