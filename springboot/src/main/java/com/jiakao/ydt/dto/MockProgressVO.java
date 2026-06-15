package com.jiakao.ydt.dto;

import lombok.Data;

@Data
public class MockProgressVO {
    private int wrongCount;
    private int maxWrong;
    private boolean failed;
}

