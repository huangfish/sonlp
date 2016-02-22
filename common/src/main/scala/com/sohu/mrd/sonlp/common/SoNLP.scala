package com.sohu.mrd.sonlp.common

import com.sohu.mrd.sonlp.common.imp.{TFKeywordExtractor, ViterbiSegment}
import com.sohu.mrd.sonlp.common.util.Filter

/**
 * Created by huangyu on 16/2/20.
 */
object SoNLP {

  def sparkFiles(): String = Config.sparkFiles()

  def conf(confFile: String): Unit = Config.conf(confFile)

  def sparkConf(confFile: String): Unit = Config.sparkConf(confFile)

  def seg(text: String): Array[Term] = ViterbiSegment.seg(text)

  def keyword(article: Article, num: Int): Array[StringWeight] = {
    val plainArticle = new Article(Filter.killTags(article.title),
      Filter.killFrom(Filter.killTags(article.summary)),
      Filter.killFrom(Filter.killTags(article.content)),
      article.media
    )
    TFKeywordExtractor.keyword_(plainArticle, num)
  }

  def keyword(article: Article): Array[StringWeight] = {
    val plainArticle = new Article(Filter.killTags(article.title),
      Filter.killFrom(Filter.killTags(article.summary)),
      Filter.killFrom(Filter.killTags(article.content)),
      article.media
    )
    TFKeywordExtractor.keyword_(plainArticle)
  }

  def topic(article: Article, num: Int): Array[IntWeight] = {
    TopicComputer.topic(article, num)
  }

  def main(args: Array[String]): Unit = {
    SoNLP.conf("src/conf/hanlp.properties")
    val content: String = "程序员(英文Programmer)是从事程序开发、维护的专业人员。" + "一般将程序员分为程序设计人员和程序编码人员，" + "但两者的界限并不非常清楚，特别是在中国。" + "软件从业人员分为初级程序员、高级程序员、系统" + "分析员和项目经理四大类。"
    println(SoNLP.keyword(new Article("", "", content, ""), 10).map(kv => kv.key + ":" + kv.weight).mkString("\t"))
  }

}
