package com.sohu.mrd.sonlp.sch

import java.io.{File, PrintWriter}

import com.sohu.mrd.sonlp.SoNLP
import com.sohu.mrd.sonlp.util.FileProcessor
import org.apache.commons.lang.StringUtils

import scala.collection.mutable

/**
 * Created by yellowhuang on 2015/12/25.
 */
object GenerateData {

  def readTopicModel(modelFile: String): mutable.Map[String, mutable.Set[Int]] = {
    val topicModel = new mutable.HashMap[String, mutable.Set[Int]]()
    FileProcessor.processLine(new File(modelFile), line => {
      val ss = line.split("\t")
      val topics = topicModel.getOrElseUpdate(ss(0), new mutable.HashSet[Int]())
      topics += ss(1).toInt
    })
    topicModel
  }

  def readWordIndex(fileName: String): mutable.Map[String, Int] = {
    val wordIndex = new mutable.HashMap[String, Int]()
    FileProcessor.processLine(new File(fileName), line => {
      val ss = line.split("\t")
      if (!wordIndex.contains(ss(0))) {
        wordIndex.put(ss(0), wordIndex.size)
      }
    })
    wordIndex
  }

  def generateSampleLine(line: String, topicModel: mutable.Map[String, mutable.Set[Int]],
                         keywordNum: Int, topicScale: Boolean): String = {
    val ss = line.split("\001")
    val sb = new StringBuilder()
    sb.append(ss(1) + "\001" + ss(2))
    val keywords = SoNLP.keyword(ss(2), ss(3), num = keywordNum)
    val scale = Math.pow(keywords.length.toDouble / keywordNum, 0.5)
    val feature: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
    keywords.foreach(kw => {
      if (!StringUtils.isNumeric(kw.key)) {
        feature.put(kw.key.replaceAll(":", "").replaceAll("\\s+", ""), kw.weigth * scale)
      }
      if (topicModel.contains(kw.key)) {
        topicModel(kw.key).foreach(t => {
          if (topicScale) {
            feature.put(t.toString, feature.getOrElse(t.toString, 0.0) + kw.weigth * scale / topicModel(kw.key).size)
          } else {
            feature.put(t.toString, feature.getOrElse(t.toString, 0.0) + kw.weigth * scale)
          }
        })
      }
    })
    var count = 0
    feature.foreach(fw => {
      val sp = if (count == 0) "\001" else " "
      sb.append(sp + fw._1 + ":" + "%3.3f".format(fw._2))
      count += 1
    })
    sb.toString()

  }

  def generateSample(inputPath: String, _outputPath: String, topicFile: String,
                     keywordNum: Int, scaleTopic: Boolean = false): Unit = {
    val topicModel = readTopicModel(topicFile)
    val outputPath = if (_outputPath.endsWith("/")) _outputPath else _outputPath + "/"
    val ps = new mutable.HashMap[String, PrintWriter]()
    FileProcessor.processLine(new File(inputPath), line => {
      val ss = line.split("\001")
      val p = ps.getOrElseUpdate(ss(1), new PrintWriter(outputPath + ss(1)))
      try {
        p.println(ss(0) + "\001" + generateSampleLine(line, topicModel, keywordNum, scaleTopic))
      } catch {
        case e: Throwable =>
          println(line)
      }
    })
    ps.values.foreach(_.close())

  }

  def main(args: Array[String]): Unit = {
    if (args.length < 5) {
      println("GenerateSample <inputPath> <outputFile> <topicFile> <keywordNum> <scaleTopic>")
      System.exit(1)
    }
    generateSample(args(0), args(1), args(2), args(3).toInt, args(4).toBoolean)
  }

}
