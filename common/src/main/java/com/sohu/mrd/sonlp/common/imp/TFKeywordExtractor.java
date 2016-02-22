package com.sohu.mrd.sonlp.common.imp;

import com.sohu.mrd.sonlp.common.*;

import java.util.*;

/**
 * Created by huangyu on 16/2/19.
 */
public class TFKeywordExtractor extends KeywordExtractor {

    public TFKeywordExtractor(Segment segment) {
        super(segment);
    }

    public static final KeywordExtractor keywordExtractor =
            new TFKeywordExtractor(ViterbiSegment.segment());

    public static StringWeight[] keyword_(Article article, int num) {
        return keywordExtractor.keyword(article, num);
    }

    public static StringWeight[] keyword_(Article article) {
        return keywordExtractor.keyword(article);
    }

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
        Term[] _parse = segment.seg(content);
        Term[] parse = new Term[_parse.length];
        for (int i = 0; i < parse.length; i++) {
            parse[i] = new Term(_parse[i].word().toLowerCase(), _parse[i].nature(), _parse[i].offset());
        }
        for (Term term : parse) {
            String word = term.word();
            if (!shouldInclude(term)) {
                continue;
            }
//            boolean flag = false;
//            if (Library.whitemap.containsKey(word)) {
//                flag = true;
//            }

//            if (word.equals(article.media())) {
//                continue;
//            }
//            double weight = getWeight(term, content.length(), article.title().length(), flag);
//            if (weight == 0) {
//                continue;
//            }
            double weight = getWeight(term, content.length(), article.title().length());
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

    private double getWeight(Term term, int length, int titleLength) {
        double weight = 0;
        if (titleLength > term.offset()) {
            return 20;
        }
        double position = (term.offset() + 0.0) / length;
        weight += (10 - 10 * position);
        return weight;
    }


    //calculate word weight function
    private double getWeight(Term term, int length, int titleLength, boolean flag) {
        if (term.word().trim().length() < 2) {
            return 0;
        }
        if (Library.stopmap.containsKey(term.word())) {
            return 0;
        }
        String pos = term.nature();
        if (!(pos.startsWith("n") || "j".equals(pos) || "en".equals(pos) || "userDefine".equals(pos) || flag == true)) {
            return 0;
        }
        double weight = 0;
        if (titleLength > term.offset()) {
            return 20;
        }
        double position = (term.offset() + 0.0) / length;
        weight += (10 - 10 * position);
        return weight;
    }
}
