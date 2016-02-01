package com.sohu.mrd.sonlp.core

import java.io.{File, PrintWriter}

import com.sohu.mrd.sonlp.SoNLP
import com.sohu.mrd.sonlp.util.FileProcessor
import org.apache.commons.lang.StringUtils

import scala.collection.mutable

/**
 * Created by huangyu on 15/12/20.
 */
class SimpleClassification(_parameterPath: String, topicModelPath: String) {

  //  val words: mutable.Set[String] = new mutable.HashSet[String]()
  //  val parameterPath = if (_parameterPath.endsWith("/")) _parameterPath else _parameterPath + "/"
  val wordDistribute: mutable.Map[String, mutable.Map[String, Double]] = new mutable.HashMap[String, mutable.Map[String, Double]]()
  //  val classDistribute: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  val wordSum: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  val wordMax: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  initParameter()
  //  val classNum = classDistribute.keys.size
  val wordNum = wordDistribute.keys.size
  smooth()
  normalize()

  val topicModel = readTopicModel(topicModelPath)

  private[this] def readTopicModel(modelFile: String): mutable.Map[String, mutable.Set[Int]] = {
    val topicModel = new mutable.HashMap[String, mutable.Set[Int]]()
    FileProcessor.processLine(new File(modelFile), line => {
      val ss = line.split("\t")
      val topics = topicModel.getOrElseUpdate(ss(0), new mutable.HashSet[Int]())
      topics += ss(1).toInt
    })
    topicModel
  }

  private[this] def initParameter(): Unit = {

    //    FileProcessor.processLine(new File(parameterPath + "word"), line => {
    //      val ss = line.split("\t")
    //      if (ss(1).toDouble >= 10) {
    //        words.add(ss(0))
    //      }
    //    })

    FileProcessor.processLine(new File(/*parameterPath + "word_dis"*/ _parameterPath), line => {
      val ss = line.split("\t")
      //      if (words.contains(ss(1))) {
      val wd = wordDistribute.getOrElseUpdate(ss(1), new mutable.HashMap[String, Double]())
      wd.put(ss(0), ss(2).toDouble)
      wordSum.put(ss(0), wordSum.getOrElse(ss(0), 0.0) + ss(2).toDouble)
      wordMax.put(ss(0), Math.max(wordMax.getOrElse(ss(0), 0.0), ss(2).toDouble))
      //      }
    })
    //    FileProcessor.processLine(new File(parameterPath + "class_dis"), line => {
    //      val ss = line.split("\t")
    //      classDistribute.put(ss(0), ss(1).toDouble)
    //    })
  }


  private[this] def normalize(): Unit = {
    //    wordDistribute.foreach(kv => {
    //      val dis = kv._2
    //      dis.keys.foreach(sch => {
    //        dis.put(sch, (dis.getOrElse(sch, 0.0)) / (wordSum(sch)))
    //      })
    //    })


    wordDistribute.foreach(kv => {
      val dis = kv._2
      dis.keys.foreach(sch => {
        dis.put(sch, (dis.getOrElse(sch, 0.0)) / (wordMax(sch)))
      })
    })

    //    wordDistribute.foreach(kv => {
    //      val dis = kv._2
    //      val sum = dis.values.sum
    //      dis.keys.foreach(sch => {
    //        dis.put(sch, dis(sch) / sum)
    //      })
    //    })

  }

  private def getWeigth(word: String, sch: String): Double = {
    //    wordDistribute.get(word).map(_.getOrElse(sch, 1.0 / (wordSum(sch) + wordNum))).
    //      getOrElse(1.0 / (wordSum(sch) + wordNum))
    wordDistribute.get(word).map(_.getOrElse(sch, 0.0)).getOrElse(0.0)
  }

  private[this] def smooth(): Unit = {
  }


  def predict(title: String, content: String, keywordNum: Int,ch:Option[String]): (String, Double) = {
    val cls = predict(title, content, keywordNum, 1,ch)
    if (cls.length == 0) (("-1", 0.0)) else cls(0)
  }

  def predict(keywords: Array[StringWeight], keywordNum: Int, ch: Option[String] ): (String, Double) = {

    val cls = predict(keywords, keywordNum, 1, ch)
    if (cls.length == 0) (("-1", 0.0)) else cls(0)
  }

  def predict(title: String, content: String, keywordNum: Int, num: Int, channel: Option[String] ): Array[(String, Double)] = {
    predict(SoNLP.keyword(title, content, num = keywordNum), keywordNum, num, channel)
  }

  def predict(keywords: Array[StringWeight], keywordNum: Int, num: Int, channel: Option[String] ): Array[(String, Double)] = {
    //    val keywords = SoNLP.keyword(title, content, num = keywordNum)
    if (keywords.length >= 4) {
      val scale = Math.pow(keywords.length.toDouble / keywordNum, 0.5)
      val feature: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
      keywords.foreach(kw => {
        if (!StringUtils.isNumeric(kw.key)) {
          feature.put(kw.key.replaceAll(":", "").replaceAll("\\s+", ""), kw.weigth * scale)
        }
        if (topicModel.contains(kw.key)) {
          topicModel(kw.key).foreach(t => {
            feature.put(t.toString, feature.getOrElse(t.toString, 0.0) + 1.0)

          })
        }
      })
      val schWeight = new mutable.HashMap[String, Double]()
      feature.foreach(kv => {
        wordDistribute.get(kv._1).foreach(_.foreach(schw => {
          schWeight.put(schw._1, schWeight.getOrElse(schw._1, 0.0) + kv._2 * schw._2)

        }))
      })
      schWeight.toArray.filter(kv => channel.map(ch => (kv._1.substring(0, kv._1.length - 4) + "0000" == ch)).getOrElse(true)).
        sortBy(-_._2).take(num)
    } else {
      Array.empty[(String, Double)]
    }
  }

  def save(resultFile: String): Unit = {
    val p = new PrintWriter(resultFile, "utf-8")
    wordDistribute.foreach(wordChDis => {
      val word = wordChDis._1
      val dis = wordChDis._2
      dis.foreach(sw => {
        p.println(sw._1 + "\t" + word + "\t" + sw._2)
      })
    })
    p.close()
  }

}

