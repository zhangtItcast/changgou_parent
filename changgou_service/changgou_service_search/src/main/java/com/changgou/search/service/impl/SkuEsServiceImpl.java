package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuEsService;
import entity.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.search.service.impl
 * @date 2020-2-8
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class SkuEsServiceImpl implements SkuEsService {
    @Autowired
    private SkuEsMapper skuEsMapper;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Override
    public void importSku() {
        //1、调用商品微服务获取商品列表
        Result<List<Sku>> listResult = skuFeign.findByStatus("1");//只查询正常状态的商品
        System.out.println("本次将要导入的数据总数为：" + listResult.getData().size());
        //把List<Sku>转成List<SkuInfo>
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(listResult.getData()), SkuInfo.class);
        //把Sku中的spec属性变成specMap结构
        for (SkuInfo skuInfo : skuInfoList) {
            Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //2、通过SpringDataEs完成批量导入
        skuEsMapper.saveAll(skuInfoList);
    }

    @Override
    public Map search(Map<String, String> searchMap) {
        Map map = new HashMap();
        //1、创建查询条件构建器-builder = new NativeSearchQueryBuilder()
        NativeSearchQueryBuilder builder = builderBasicQuery(searchMap);
        //2、根据查询条件查询商品列表
        searchList(builder, map);

        //3、根据查询条件分组(聚合)查询商品分类列表
        //searchCategoryList(builder, map);
        //4、根据查询条件分组(聚合)查询商品品牌列表
        //searchBrandList(builder, map);
        //5、根据查询条件分组(聚合)查询规格列表
        //searchSpecMap(builder, map);

        //优化代码，把聚合整合在一起，完成一次搜索，得到多个分组结果
        searchGroup(builder, map);

        return map;
    }

    /**
     * 根据查询条件构建器分组(聚合)查询商品分类、品牌与规格列表
     * @param builder 查询条件
     * @param map 结果集
     */
    private void searchGroup(NativeSearchQueryBuilder builder, Map map) {
        //--1添加分类聚合条件
            //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_category").field("categoryName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //--2添加品牌聚合条件
            //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder brandAggregationBuilder = AggregationBuilders.terms("group_brand").field("brandName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(brandAggregationBuilder);
        //--3添加规格聚合条件
            //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder specAggregationBuilder = AggregationBuilders.terms("group_spec").field("spec.keyword").size(1000000);
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(specAggregationBuilder);

        //只发起一次查询请求
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();

        //--1提取分类数据
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms categoryTerms = aggregations.get("group_category");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> categoryList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : categoryTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            categoryList.add(bucket.getKeyAsString());
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("categoryList", categoryList);

        //--2提取品牌数据
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms brandTerms = aggregations.get("group_brand");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> brandList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : brandTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            brandList.add(bucket.getKeyAsString());
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("brandList", brandList);

        //--3提取规格数据
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms specTerms = aggregations.get("group_spec");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> specList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : specTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            specList.add(bucket.getKeyAsString());
        }
        //把spec的json列表，转换成Map<String,Set<String>>
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Map<String,String> tempMap = null;
        //循环解析数据
        //{"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"}
        for (String spec : specList) {
            //把spec的json串转成map
            tempMap = JSON.parseObject(spec, Map.class);
            //电视音响效果、电视屏幕尺寸、尺码....
            for (String key : tempMap.keySet()) {
                Set<String> values = specMap.get(key);
                //如果当前规格没有任何选项列表
                if (values == null || values.size() < 1) {
                    values = new HashSet<String>();
                }
                //小影院,在set集合中会自动去掉重复内容
                values.add(tempMap.get(key));
                //把新set放入specMap
                specMap.put(key, values);
            }
        }
        //8.返回规格数据列表-map.put("categoryList", categoryList)
        map.put("specMap", specMap);
    }

    /**
     * 根据查询条件构建器分组(聚合)查询规格列表
     * @param builder 查询条件
     * @param map 结果集
     */
    private void searchSpecMap(NativeSearchQueryBuilder builder, Map map) {
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_spec").field("spec.keyword").size(1000000);
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms specTerms = aggregations.get("group_spec");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> specList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : specTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            specList.add(bucket.getKeyAsString());
        }

        //把spec的json列表，转换成Map<String,Set<String>>
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Map<String,String> tempMap = null;
        //循环解析数据
        //{"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"}
        for (String spec : specList) {
            //把spec的json串转成map
            tempMap = JSON.parseObject(spec, Map.class);
            //电视音响效果、电视屏幕尺寸、尺码....
            for (String key : tempMap.keySet()) {
                Set<String> values = specMap.get(key);
                //如果当前规格没有任何选项列表
                if (values == null || values.size() < 1) {
                    values = new HashSet<String>();
                }
                //小影院,在set集合中会自动去掉重复内容
                values.add(tempMap.get(key));
                //把新set放入specMap
                specMap.put(key, values);
            }
        }
        //8.返回规格数据列表-map.put("categoryList", categoryList)
        map.put("specMap", specMap);
    }

    /**
     * 根据查询条件构建器分组(聚合)查询品牌列表
     * @param builder 查询条件
     * @param map 结果集
     */
    private void searchBrandList(NativeSearchQueryBuilder builder, Map map) {
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_brand").field("brandName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms brandTerms = aggregations.get("group_brand");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> brandList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : brandTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            brandList.add(bucket.getKeyAsString());
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("brandList", brandList);
    }

    /**
     * 根据查询条件构建器分组(聚合)查询商品分类列表
     * @param builder 查询条件
     * @param map 结果集
     */
    private void searchCategoryList(NativeSearchQueryBuilder builder, Map map) {
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_category").field("categoryName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms categoryTerms = aggregations.get("group_category");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> categoryList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : categoryTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            categoryList.add(bucket.getKeyAsString());
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("categoryList", categoryList);
    }

    /**
     * 根据查询条件构建器，查询商品列表
     * @param builder 查询条件
     * @param map 结果集
     */
    private void searchList(NativeSearchQueryBuilder builder, Map map) {
        //3、获取NativeSearchQuery搜索条件对象-builder.build()
        /*NativeSearchQuery query = builder.build();
        //4.查询数据-esTemplate.queryForPage(条件对象,搜索结果对象)
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(query, SkuInfo.class);*/

        //h1.配置高亮查询信息-hField = new HighlightBuilder.Field()
        //h1.1:设置高亮域名-在构造函数中设置
        HighlightBuilder.Field hField = new HighlightBuilder.Field("name");
        //h1.2：设置高亮前缀-hField.preTags
        hField.preTags("<em style='color:red;'>");
        //h1.3：设置高亮后缀-hField.postTags
        hField.postTags("</em>");
        //h1.4：设置碎片大小-hField.fragmentSize
        hField.fragmentSize(100);
        //h1.5：追加高亮查询信息-builder.withHighlightFields()
        builder.withHighlightFields(hField);

        //h2.高亮数据读取-AggregatedPage<SkuInfo> page = esTemplate.queryForPage(query, SkuInfo.class, new SearchResultMapper(){})
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(builder.build(), SkuInfo.class, new SearchResultMapper() {
            //h2.1实现mapResults(查询到的结果,数据列表的类型,分页选项)方法
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //h2.2 先定义一组查询结果列表-List<T> list = new ArrayList<T>()
                List<T> list = new ArrayList<T>();
                //h2.3 遍历查询到的所有高亮数据-response.getHits().for
                for (SearchHit hit : response.getHits()) {
                    //h2.3.1 先获取当次结果的原始数据(无高亮)-hit.getSourceAsString()
                    String skuJson = hit.getSourceAsString();
                    //h2.3.2 把json串转换为SkuInfo对象-skuInfo = JSON.parseObject()
                    SkuInfo skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                    //h2.3.3 获取name域的高亮数据-nameHighlight = hit.getHighlightFields().get("name")
                    HighlightField nameHighlight = hit.getHighlightFields().get("name");
                    //h2.3.4 如果高亮数据不为空-读取高亮数据
                    if (nameHighlight != null) {
                        //h2.3.4.1 定义一个StringBuffer用于存储高亮碎片-buffer = new StringBuffer()
                        StringBuffer buffer = new StringBuffer();
                        //h2.3.4.2 循环组装高亮碎片数据- nameHighlight.getFragments().for(追加数据)
                        for (Text fragment : nameHighlight.getFragments()) {
                            buffer.append(fragment);
                        }
                        //h2.3.4.3 将非高亮数据替换成高亮数据-skuInfo.setName()
                        skuInfo.setName(buffer.toString());
                    }
                    //h2.3.5 将替换了高亮数据的对象封装到List中-list.add((T) esItem)
                    list.add((T) skuInfo);
                }
                //h2.4 返回当前方法所需要参数-new AggregatedPageImpl<T>(数据列表，分页选项,总记录数)
                //h2.4 参考new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits())
                return new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits());
            }
        });
        //5、包装结果并返回
        map.put("rows", page.getContent());
        map.put("total", page.getTotalElements());
        map.put("totalPages", page.getTotalPages());

        //返回分页信息
        int pageNum = page.getPageable().getPageNumber() + 1;  //当前页
        map.put("pageNum", pageNum);
        int pageSize = page.getPageable().getPageSize();//每页查询的条数
        map.put("pageSize", pageSize);
    }

    /**
     * 根据用户传入的查询条件组装查询条件构建器
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder builderBasicQuery(Map<String, String> searchMap) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //2、组装查询条件
        if (searchMap != null) {
            //多域匹配搜索构建器
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            //2.1 关键字查询
            if (StringUtils.isNotBlank(searchMap.get("keywords"))) {
                //2.1关键字搜索-builder.withQuery(QueryBuilders.matchQuery(域名，内容))
                //builder.withQuery(QueryBuilders.matchQuery("name", searchMap.get("keywords")));
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")));
            }
            //2.2 分类过滤查询
            if (StringUtils.isNotBlank(searchMap.get("category"))) {
                //词条搜索
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
            }
            //2.3 品牌过滤查询
            if (StringUtils.isNotBlank(searchMap.get("brand"))) {
                //词条搜索
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            //2.4 规格过滤查询
            //spec_网络制式:移动4G,spec_显示屏尺寸:4.0
            for (String key : searchMap.keySet()) {
                //如果是规格参数
                if (key.startsWith("spec_")) {
                    //计算域名称：specMap.像素
                    String fieldName = "specMap." + key.substring(5) + ".keyword";
                    boolQueryBuilder.must(QueryBuilders.termQuery(fieldName, searchMap.get(key)));
                }
            }
            //2.5 价格过滤
            //0-500 500-100 .... 3000
            if (StringUtils.isNotBlank(searchMap.get("price"))) {
                String[] prices = searchMap.get("price").split("-");
                //范围匹配构建器
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
                //price >= 0,3000
                rangeQueryBuilder.gte(prices[0]);
                //价格区间 0 <= price <= 500
                if (prices.length > 1) {
                    //price <= 500
                    rangeQueryBuilder.lte(prices[1]);
                }
                boolQueryBuilder.must(rangeQueryBuilder);
            }
            builder.withQuery(boolQueryBuilder);

            //当前页码-由于of(当前页是0开始的，所以这里要转换一下)
            Integer pageNum = searchMap.get("pageNum") == null ? 0 : new Integer(searchMap.get("pageNum")) - 1;
            //每页查询的条数
            Integer pageSize = 10;
            //排序的域
            String sortField = searchMap.get("sortField") == null ? "" : searchMap.get("sortField");
            //排序的方式
            String sortRule = searchMap.get("sortRule") == null ? "asc" : searchMap.get("sortRule");

            //2.6 分页查询-1、无排序查询
            //当前页码-由于of(当前页是0开始的，所以这里要转换一下)
            //构建分页条件-PageRequest.of(当前页码[从0开始]，查询条数)
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            //添加分页参数
            builder.withPageable(pageable);

            //2.6 分页查询-2-设置排序参数
            /*if (sortField.length() > 0) {
                //Sort(排序方式，排序的域)
                Sort sort = new Sort(Sort.Direction.valueOf(sortRule.toUpperCase()),sortField);
                //构建分页条件-PageRequest.of(当前页码[从0开始]，查询条数)
                Pageable pageable = PageRequest.of(pageNum, pageSize,sort);
                //添加分页参数
                builder.withPageable(pageable);
            }else{  //不设置排序条件
                Pageable pageable = PageRequest.of(pageNum, pageSize);
                //添加分页参数
                builder.withPageable(pageable);
            }*/

            //2.7 排序查询
            if(sortField.length() > 1) {
                builder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule.toUpperCase())));
            }
        }
        return builder;
    }
}
