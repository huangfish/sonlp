package com.sohu.mrd.sonlp.common

import com.sohu.mrd.sonlp.common.imp.TFKeywordExtractor
import com.sohu.mrd.sonlp.common.util.Filter

import collection.mutable
import scala.io.Source

/**
 * Created by huangyu on 16/2/22.
 */
class TopicComputer(
                     val topicWord: mutable.Map[String, mutable.Map[Int, Double]],
                     val keywordExtractor: KeywordExtractor
                     ) {

  val DEFAULT_KEYWORDS_NUM = 30

  def topic(article: Article, num: Int): Array[IntWeight] = {
    val plainArticle = new Article(Filter.killTags(article.title),
      Filter.killFrom(Filter.killTags(article.summary)),
      Filter.killFrom(Filter.killTags(article.content)),
      article.media
    )
    val _keywords = keywordExtractor.keyword(plainArticle, DEFAULT_KEYWORDS_NUM)
    val scale = _keywords.length.toDouble / DEFAULT_KEYWORDS_NUM
    val keywords = _keywords.map(kw => new StringWeight(kw.key, kw.weight * scale))
    val topics = new mutable.HashMap[Int, Double]()
    keywords.foreach(kw => {
      topicWord.getOrElse(kw.key, mutable.Map.empty).foreach(tw => {
        topics.put(tw._1, topics.getOrElse(tw._1, 0.0) + tw._2 * kw.weight)
      })
    })
    topics.map(tw => new IntWeight(tw._1, tw._2)).toArray.sortBy(-_.weight).take(num)
  }

}


object TopicComputer {

  val topicComputer = TopicComputer()

  def topic(article: Article, num: Int): Array[IntWeight] = {
    topicComputer.topic(article, num)
  }

  def readTopicWord(modelFile: String): mutable.Map[String, mutable.Map[Int, Double]] = {
    val topicWord = new mutable.HashMap[String, mutable.Map[Int, Double]]()

    Source.fromFile(modelFile, "utf-8").getLines().filter(_.trim != "").foreach(line => {
      val ss = line.split("\t")
      val topics = topicWord.getOrElseUpdate(ss(0), new mutable.HashMap[Int, Double]())
      topics.put(ss(1).toInt, topics.getOrElse(ss(1).toInt, 0.0) + ss(2).toDouble)
    })
    topicWord
  }

  def apply(topicWord: mutable.Map[String, mutable.Map[Int, Double]]): TopicComputer = {
    new TopicComputer(topicWord, TFKeywordExtractor.keywordExtractor)
  }

  def apply(modelFile: String): TopicComputer = {
    new TopicComputer(readTopicWord(modelFile), TFKeywordExtractor.keywordExtractor)
  }

  def apply(): TopicComputer = {
    new TopicComputer(readTopicWord(Config.topicWord), TFKeywordExtractor.keywordExtractor)
  }

  def main(args: Array[String]): Unit = {
    TopicComputer.readTopicWord("./hanlp_library/topic/topic_word")
    //    TopicComputer("hanlp_library/topic/topic_word")
  }
}

//class VarCountWeight(var count: Int, var weight: Double)
