package com.sohu.mrd.sonlp.core

/**
 * Created by huangyu on 15/12/20.
 */
abstract class KeywordExtractor {

  def keyword(article: Article, num: Int): Array[StringWeight]

}
