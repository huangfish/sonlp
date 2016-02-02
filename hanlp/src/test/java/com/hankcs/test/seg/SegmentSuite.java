package com.hankcs.test.seg;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;

/**
 * Created by yellowhuang on 2016/2/2.
 */
public class SegmentSuite {

    public static void segSuite() throws Exception {
//        HanLP.Config.enableDebug();
        HanLP.config("src/conf/hanlp.properties");
        Segment segment = HanLP.newSegment();
        System.out.println(segment.seg(
                "并有望在那与1993年就结识的友人重聚。"
        ));
    }

    public static void main(String[] args) throws Exception {
        segSuite();
    }
}
