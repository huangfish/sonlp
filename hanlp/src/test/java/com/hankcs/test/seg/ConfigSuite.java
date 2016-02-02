package com.hankcs.test.seg;

import com.hankcs.hanlp.HanLP;

import java.io.FileNotFoundException;

/**
 * Created by yellowhuang on 2016/2/2.
 */
public class ConfigSuite {

    public static void main(String[] args) throws FileNotFoundException {
//        System.out.println(HanLP.Config.BiGramDictionaryPath);
//        System.out.println(HanLP.Config.CustomDictionaryPath[0]);
        HanLP.config("src/conf/hanlp.properties");
        System.out.println(HanLP.Config.BiGramDictionaryPath);
        System.out.println(HanLP.Config.CustomDictionaryPath[0]);
    }
}
