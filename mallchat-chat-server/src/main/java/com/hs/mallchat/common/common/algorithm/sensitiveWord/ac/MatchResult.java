package com.hs.mallchat.common.common.algorithm.sensitiveWord.ac;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/12 - 16:00
 */

@Getter
@Setter
@AllArgsConstructor
public class MatchResult {

    private int startIndex;

    private int endIndex;

    @Override
    public String toString() {
        return "MatchResult{" +
                "startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
    }
}
