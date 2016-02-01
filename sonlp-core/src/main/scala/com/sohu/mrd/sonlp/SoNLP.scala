package com.sohu.mrd.sonlp

import com.sohu.mrd.sonlp.core.util.Filter
import com.sohu.mrd.sonlp.core.{Article, StringWeight, TFKeyword}
import org.ansj.domain.Term
import org.ansj.splitWord.analysis.ToAnalysis

import scala.io.Source

/**
 * Created by huangyu on 15/12/20.
 */
object SoNLP {

  val keywordExtractor = new TFKeyword

  def keyword(article: Article, num: Int): Array[StringWeight] = {
    keywordExtractor.keyword(article, num)
  }

  def segment(str: String): Array[Term] = {

    val words = ToAnalysis.parse(str)
    words.toArray(new Array[Term](words.size()))
  }

  def keyword(title: String, content: String, media: String = "", num: Int = 10): Array[StringWeight] = {
    val plainTitle = Filter.killTags(title)
    val plainContent = Filter.killFrom(Filter.killTags(content))
    keyword(new Article(plainTitle, "", plainContent, media), num)

  }

  def main(args: Array[String]): Unit = {
    Source.fromFile("data/news_data_bak", "utf-8").getLines().foreach(line => {
      val ss = line.split("\t")
      println(ss(0) + "\t" + ss(1) + "\t" + keyword(ss(12), ss(14), num = 20).map(kv => kv.key + ":" + kv.weigth).mkString(","))
    })

  }


}
