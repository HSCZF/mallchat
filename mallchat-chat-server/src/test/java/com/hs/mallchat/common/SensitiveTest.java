package com.hs.mallchat.common;

import com.hs.mallchat.common.common.algorithm.sensitiveWord.ac.ACFilter;
import com.hs.mallchat.common.common.algorithm.sensitiveWord.acpro.ACProFilter;
import com.hs.mallchat.common.common.algorithm.sensitiveWord.dfa.DFAFilter;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 15:12
 */
public class SensitiveTest {
    @Test
    public void DFAMulti() {
        List<String> sensitiveList = Arrays.asList("白痴", "你是白痴", "白痴吗");
        DFAFilter instance = DFAFilter.getInstance();
        instance.loadWord(sensitiveList);
        System.out.println(instance.filter("你是不是个白痴吗"));
    }


    @Test
    public void DFA() {
        List<String> sensitiveList = Arrays.asList("abcd", "abcbba", "adabca");
        DFAFilter instance = DFAFilter.getInstance();
        instance.loadWord(sensitiveList);
        System.out.println(instance.hasSensitiveWord("adabcd"));
    }


    /**
     * root
     * ├── a
     * │   ├── b
     * │   │   ├── c
     * │   │   │   ├── d
     * │   │   │   │   └── (EOW)
     * │   │   │   └── b
     * │   │   │       ├── b
     * │   │   │       │   ├── a
     * │   │   │       │   │   └── (EOW)
     * │   │   │       │   └── (EOW)
     * │   │   │       └── (EOW)
     * │   │   └── (EOW)
     * │   └── d
     * │       ├── a
     * │       │   ├── b
     * │       │   │   ├── c
     * │       │   │   │   └── a
     * │       │   │   │       └── (EOW)
     * │       │   │   └── (EOW)
     * │       │   └── (EOW)
     * │       └── (EOW)
     * └── (EOW)
     */
    @Test
    public void AC() {
        List<String> sensitiveList = Arrays.asList("abcd", "abcbba", "adabca");
        ACFilter instance = new ACFilter();
        instance.loadWord(sensitiveList);
        //System.out.println(instance.hasSensitiveWord("adabcd"));
        System.out.println(instance.filter("adabcd"));
    }

    @Test
    public void ACMulti() {
        List<String> sensitiveList = Arrays.asList("白痴", "你是白痴", "白痴吗");
        ACFilter instance = new ACFilter();
        instance.loadWord(sensitiveList);
        System.out.println(instance.filter("你是白痴吗"));
    }

    @Test
    public void ACPro() {
        List<String> sensitiveList = Arrays.asList("白痴", "你是白痴", "白痴吗");
        ACProFilter acProFilter = new ACProFilter();
        acProFilter.loadWord(sensitiveList);
        System.out.println(acProFilter.filter("你是白痴吗"));
    }

}
