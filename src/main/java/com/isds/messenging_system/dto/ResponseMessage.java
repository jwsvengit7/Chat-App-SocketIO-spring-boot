package com.isds.messenging_system.dto;

import lombok.Data;

@Data
public class ResponseMessage {
    private String content;

    public ResponseMessage() {
    }

    public ResponseMessage(String content) {
        this.content = content;
    }
}