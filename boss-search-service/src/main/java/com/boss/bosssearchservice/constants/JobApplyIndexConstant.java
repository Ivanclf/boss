package com.boss.bosssearchservice.constants;

public class JobApplyIndexConstant {
    public static final String JOB_APPLY_INDEX = "job_apply_index";

    public static final String JOB_APPLY_SCRIPT = """
            {
              "properties": {
                "applyId": { "type": "long", "index": false },
                "candidateUid": { "type": "long", "index": false },
                "candidateName": {
                  "type": "text",
                  "analyzer": "ik_max_word"
                },
                "candidatePhone": { "type": "keyword" },
            
                "hrUid": { "type": "long", "index": false },
                "jobUid": { "type": "long", "index": false },
            
                "jobTitle": {
                  "type": "text",
                  "analyzer": "ik_max_word"
                },
                "jobCity": { "type": "keyword" },
            
                "salaryMin": { "type": "integer" },
                "salaryMax": { "type": "integer" },
            
                "status": { "type": "integer" },
                "applyTime": { "type": "date" },
            
                "tags": { "type": "keyword", "index": false }
              }
            }
            """;
}
