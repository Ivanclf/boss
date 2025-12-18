package com.boss.bosssearchservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosssearchservice.constants.JobApplyIndexConstant;
import com.boss.bosssearchservice.constants.JobIndexConstant;
import com.boss.bosssearchservice.service.SearchService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.boss.bosscommon.constant.JobPublishConstant.PUBLISHED;
import static com.boss.bosssearchservice.constants.ChatMessageIndexConstant.CHAT_MESSAGE_INDEX;
import static com.boss.bosssearchservice.constants.ChatMessageIndexConstant.CHAT_MESSAGE_SCRIPT;
import static com.boss.bosssearchservice.constants.JobApplyIndexConstant.JOB_APPLY_INDEX;
import static com.boss.bosssearchservice.constants.JobIndexConstant.JOB_INDEX;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final RestHighLevelClient client;

    public SearchServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.client = restHighLevelClient;
    }

    @PostConstruct
    public void createIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(JOB_INDEX);
        createIfNotExists(getIndexRequest, JobIndexConstant.JOB_INDEX, JobIndexConstant.JOB_SCRIPT);

        getIndexRequest = new GetIndexRequest(JOB_APPLY_INDEX);
        createIfNotExists(getIndexRequest, JobApplyIndexConstant.JOB_APPLY_INDEX, JobApplyIndexConstant.JOB_APPLY_SCRIPT);

        getIndexRequest = new GetIndexRequest(CHAT_MESSAGE_INDEX);
        createIfNotExists(getIndexRequest, CHAT_MESSAGE_INDEX, CHAT_MESSAGE_SCRIPT);
    }

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
            String keyword,
            LocalDateTime date,
            Integer pageNum,
            Integer pageSize) throws IOException {
        SearchRequest request = new SearchRequest(CHAT_MESSAGE_INDEX);

        BoolQueryBuilder bool = QueryBuilders.boolQuery();

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

    private void createIfNotExists(GetIndexRequest getIndexRequest, String chatMessageIndex, String chatMessageScript) throws IOException {
        if(!client.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(chatMessageIndex);
            createIndexRequest.settings(Settings.builder()
                    .put("analysis.analyzer.ik_max.type", "custom")
                    .put("analysis.analyzer.ik_max.tokenizer", "ik_max_word"));
            createIndexRequest.mapping(chatMessageScript, XContentType.JSON);

            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        }
    }
}
