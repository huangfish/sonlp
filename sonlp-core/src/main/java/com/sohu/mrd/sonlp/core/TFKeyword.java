package com.sohu.mrd.sonlp.core;


import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.*;

/**
 * Created by huangyu on 15/12/20.
 */
public class TFKeyword extends KeywordExtractor {

    @Override
    public StringWeight[] keyword(Article article, int num) {
        List<Keyword> keywords = computeKeyword(article, num);
        double sum = 0;
        for (Keyword keyword : keywords) {
            sum += keyword.getScore();
        }
        StringWeight[] kws = new StringWeight[keywords.size()];
        for (int i = 0; i < kws.length; i++) {
            kws[i] = new StringWeight(keywords.get(i).name, keywords.get(i).getScore() / sum * 10);
        }
        return kws;
    }

    private List<Keyword> computeKeyword(Article article, int num) {
        Map<String, Keyword> keywordMap = new HashMap<String, Keyword>();
        String content = article.title() + "\t" + article.content();
        List<Term> parse = ToAnalysis.parse(content);
        for (Term term : parse) {

            String word = term.getName();
            Boolean flag = false;
            if (Library.whitemap.containsKey(term.getName())) {
                flag = true;
            }
            if (word.equals(article.media())) {
                continue;
            }
            double weight = getWeight(term, content.length(), article.title().length(), flag);
            if (weight == 0) {
                continue;
            }
            if (Library.sameword.containsKey(word)) {
                word = Library.sameword.get(word);
            }

            Keyword keyword = keywordMap.get(word);
            if (keyword == null) {
                double idf = 12.0;
                Double d = 0.0;
                if ((d = Library.idfmap.get(word)) != null) {
                    idf = d;
                    if (d > 12.0) {
                        idf = 12;
                    }
                }
                keyword = new Keyword(word, idf, weight);
                keywordMap.put(word, keyword);
            } else {
                keyword.updateWeight(1);
            }
        }
        TreeSet<Keyword> treeSet = new TreeSet<Keyword>(keywordMap.values());
        ArrayList<Keyword> arrayList = new ArrayList<Keyword>(treeSet);
        if (treeSet.size() < num) {
            return arrayList;
        } else {
            return arrayList.subList(0, num);
        }
    }

    //calculate word weight function
    private double getWeight(Term term, int length, int titleLength, Boolean flag) {
        if (term.getName().trim().length() < 2) {
            return 0;
        }
        if (Library.stopmap.containsKey(term.getName())) {
            return 0;
        }
        String pos = term.getNatrue().natureStr;
        if (!(pos.startsWith("n") || "j".equals(pos) || "en".equals(pos) || "userDefine".equals(pos) || flag == true)) {
            return 0;
        }
        double weight = 0;
        if (titleLength > term.getOffe()) {
            return 20;
        }
        double position = (term.getOffe() + 0.0) / length;
        weight += (10 - 10 * position);
        return weight;
    }
}
