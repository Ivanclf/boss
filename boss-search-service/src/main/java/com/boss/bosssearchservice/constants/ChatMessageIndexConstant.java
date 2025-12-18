package com.boss.bosssearchservice.constants;

public class ChatMessageIndexConstant {
    public static final String CHAT_MESSAGE_INDEX = "chat_message_index";

    public static final String CHAT_MESSAGE_SCRIPT = """
            {
              "properties": {
                "messageId": { "type": "long", "index": false },
                "fromUid": { "type": "long", "index": false },
                "toUid": { "type": "long", "index": false },
                "jobUid": { "type": "long", "index": false },
            
                "context": {
                  "type": "text",
                  "analyzer": "ik_max_word"
                },
            
                "createTime": { "type": "date" }
              }
            }
            """;
}
