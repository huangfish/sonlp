package com.hankcs.test.seg;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;

import java.io.FileNotFoundException;

/**
 * Created by yellowhuang on 2016/2/2.
 */
public class DicSuite {
    public static void main(String[] args) throws FileNotFoundException {
        HanLP.config("src/conf/hanlp.properties");
        // 动态增加
        CustomDictionary.add("攻城狮 dfdfd");
        System.out.println(CustomDictionary.get("攻城狮 dfdfd"));
        System.out.println(CustomDictionary.get("俄塔社"));


//        System.out.println(HanLP.Config.BiGramDictionaryPath);
//        System.out.println(HanLP.Config.CustomDictionaryPath[0]);
    }
}
