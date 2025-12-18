package com.boss.bosssearchservice.constants;

public class JobIndexConstant {
    public static final String JOB_INDEX = "job_index";

    public static final String JOB_SCRIPT = """
            {
                      "properties": {
                        "uid": { "type": "long", "index": false },
                        "hrUid": { "type": "long", "index": false },
                        "title": {
                          "type": "text",
                          "analyzer": "ik_max_word"
                        },
                        "description": {
                          "type": "text",
                          "analyzer": "ik_max_word"
                        },
                        "requirement": {
                          "type": "text",
                          "analyzer": "ik_max_word"
                        },
                        "city": { "type": "keyword" },
                        "salaryMin": { "type": "integer" },
                        "salaryMax": { "type": "integer" },
                        "status": { "type": "byte" },
                        "publishTime": { "type": "date", "index": false },
                        "tags": { "type": "keyword", "index": false }
                      }
            }
            """;
}
