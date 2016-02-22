package com.sohu.mrd.sonlp.common;

import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;

/**
 * Created by huangyu on 16/2/20.
 */
public abstract class KeywordExtractor {

    public final static int DEFUALT_KEYWORD_NUM = 10;
    protected final Segment segment;

    public KeywordExtractor(Segment segment) {
        this.segment = segment;
    }
    //  def shouldInclude(term: Term): Boolean = {
    //
    //    if (term.nature == null) false
    //    else if (Library.whitemap.containsKey(term.word)) true
    //    else if (term.word.trim.length < 2) false
    //    else if (Library.stopmap.containsKey(term.word)) false
    //    else if (!(term.nature.startsWith("n") || term.nature == "j" || "en" == term.nature)) false
    //    else true
    //
    //  }

    /**
     * 是否应当将这个term纳入计算，词性属于名词、动词、副词、形容词
     *
     * @param term
     * @return 是否应当
     */
    protected boolean shouldInclude(Term term) {
        // 除掉停用词
        if (term.nature() == null) return false;
        String nature = term.nature();
        char firstChar = nature.charAt(0);
        switch (firstChar) {
            case 'm':
            case 'b':
            case 'c':
            case 'e':
            case 'o':
            case 'p':
            case 'q':
            case 'u':
            case 'y':
            case 'z':
            case 'r':
            case 'w': {
                return false;
            }
            default: {
                if (Library.whitemap.containsKey(term.word())
                        || term.word().trim().length() > 1
                        && !CoreStopWordDictionary.contains(term.word())
                        && !Library.stopmap.containsKey(term.word())
                        ) {
                    return true;
                }
            }
            break;
        }

        return false;
    }

    public abstract StringWeight[] keyword(Article article, int num);

    public StringWeight[] keyword(Article article) {
        return keyword(article, DEFUALT_KEYWORD_NUM);
    }
}
