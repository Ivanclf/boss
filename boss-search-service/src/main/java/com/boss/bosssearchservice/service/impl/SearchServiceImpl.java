package com.boss.bosssearchservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosssearchservice.service.SearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.boss.bosscommon.constant.JobPublishConstant.PUBLISHED;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;
import static com.boss.bosssearchservice.constants.ChatMessageIndexConstant.CHAT_MESSAGE_INDEX;
import static com.boss.bosssearchservice.constants.JobApplyIndexConstant.JOB_APPLY_INDEX;
import static com.boss.bosssearchservice.constants.JobIndexConstant.JOB_INDEX;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final RestHighLevelClient client;

    public SearchServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.client = restHighLevelClient;
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public List<JobElasticsearchDTO> searchJob(
            String keyword,
            String city,
            Integer salaryMin,
            Integer salaryMax,
            Integer pageNum,
            Integer pageSize
    ) throws IOException {
        SearchRequest request = new SearchRequest(JOB_INDEX);
        BoolQueryBuilder bool = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("status", PUBLISHED));

        if(StringUtils.hasText(keyword)) {
            bool.must(QueryBuilders.multiMatchQuery(
                    keyword,
                    "title",
                    "description",
                    "requirement"
            ));
        }

        if(StringUtils.hasText(city)) {
            bool.filter(QueryBuilders.termQuery("city", city));
        }

        if(salaryMin != null || salaryMax != null) {
            RangeQueryBuilder range = QueryBuilders.rangeQuery("salaryMax");
            if(salaryMin != null) {
                range.gte(salaryMin);
            }
            if(salaryMax != null) {
                range.lte(salaryMax);
            }
            bool.filter(range);
        }

        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(bool)
                .from((pageNum - 1) * pageSize)
                .size(pageSize);

        request.source(source);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        return Arrays.stream(response.getHits().getHits())
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), JobElasticsearchDTO.class))
                .toList();
    }

    @Override
    public List<JobApplyElasticsearchDTO> searchJobApply(
            String token,
            String keyword,
            String jobCity,
            Integer salaryMin,
            Integer salaryMax,
            Integer status,
            LocalDateTime date,
            Integer pageNum,
            Integer pageSize) throws IOException {
        SearchRequest request = new SearchRequest(JOB_APPLY_INDEX);

        BoolQueryBuilder bool = QueryBuilders.boolQuery();

        Long uid = (Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");
        if(uid == null) {
            throw new ClientAbortException("用户未登录");
        }
        bool.filter(QueryBuilders.termQuery("hrUid", uid));

        if(StringUtils.hasText(keyword)) {
            bool.must(QueryBuilders.multiMatchQuery(
                    keyword,
                    "candidateName",
                    "candidatePhone",
                    "jobTitle"
            ));
        }

        if(StringUtils.hasText(jobCity)) {
            bool.must(QueryBuilders.termQuery("jobCity", jobCity));
        }

        if(salaryMin != null || salaryMax != null) {
            RangeQueryBuilder range = QueryBuilders.rangeQuery("salaryMax");
            if(salaryMin != null) {
                range.gte(salaryMin);
            }
            if(salaryMax != null) {
                range.lte(salaryMax);
            }
            bool.filter(range);
        }

        if(status != null && status > 0 && status < 7) {
            bool.filter(QueryBuilders.termQuery("status", status));
        }

        if(date != null) {
            RangeQueryBuilder rangeTime = QueryBuilders.rangeQuery("applyTime");
            rangeTime.gte(date);
            bool.filter(rangeTime);
        }

        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(bool)
                .from((pageNum - 1) * pageSize)
                .size(pageSize);

        request.source(source);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        return Arrays.stream(response.getHits().getHits())
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), JobApplyElasticsearchDTO.class))
                .toList();
    }

    @Override
    public List<ChatMessageElasticsearchDTO> searchChatMessage(
            String token,
            String keyword,
            LocalDateTime date,
            Integer pageNum,
            Integer pageSize) throws IOException {
        SearchRequest request = new SearchRequest(CHAT_MESSAGE_INDEX);

        BoolQueryBuilder bool = QueryBuilders.boolQuery();

        Long uid = (Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");
        if(uid == null) {
            throw new ClientAbortException("用户未登录");
        }
        bool.filter(QueryBuilders.multiMatchQuery(
                uid,
                "fromUid",
                "toUid"
        ));

        if(StringUtils.hasText(keyword)) {
            bool.must(QueryBuilders.multiMatchQuery(
                    keyword,
                    "context"
            ));
        }

        if(date != null) {
            RangeQueryBuilder rangeTime = QueryBuilders.rangeQuery("createTime");
            rangeTime.gte(date);
            bool.filter(rangeTime);
        }

        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(bool)
                .from((pageNum - 1) * pageSize)
                .size(pageSize);

        request.source(source);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        return Arrays.stream(response.getHits().getHits())
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), ChatMessageElasticsearchDTO.class))
                .toList();
    }

}
