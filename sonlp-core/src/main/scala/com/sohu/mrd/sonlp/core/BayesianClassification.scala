package com.sohu.mrd.sonlp.core

import java.io.{File, PrintWriter}

import com.sohu.mrd.sonlp.SoNLP
import com.sohu.mrd.sonlp.util.FileProcessor
import org.apache.commons.lang.StringUtils

import scala.collection.mutable

/**
 * Created by huangyu on 15/12/27.
 */
class BayesianClassification(featureDisPath: String, classDisPath: String, topicModelFile: String) {

  val featureDis: mutable.Map[String, mutable.Map[String, Double]] = new mutable.HashMap[String, mutable.Map[String, Double]]()
  val clsDis: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  val featureSum: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  val topicModel = readTopicModel(topicModelFile)
  val sum = featureSum.values.sum

  initParameter()

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

    FileProcessor.processLine(new File(featureDisPath), line => {
      val ss = line.split("\t")
      val wd = featureDis.getOrElseUpdate(ss(1), new mutable.HashMap[String, Double]())
      wd.put(ss(0), ss(2).toDouble)
      featureSum.put(ss(1), featureSum.getOrElse(ss(1), 0.0) + ss(2).toDouble)
    })

    FileProcessor.processLine(new File(classDisPath), line => {
      val ss = line.split("\t")
      clsDis.put(ss(0), ss(1).toDouble)
    })
  }

  val dataNum = clsDis.values.sum
  clsDis.keys.foreach(k => clsDis(k) = clsDis(k) / dataNum)

  val clsSum = new mutable.HashMap[String, Double]()
  featureDis.foreach(fdis => {
    fdis._2.foreach(kv => clsSum.put(kv._1, clsSum.getOrElse(kv._1, 0.0) + kv._2))
  })

  val featureKlDis = new mutable.HashMap[String, Double]()
  featureDis.foreach(fdis => {
    var kl = 0.0
    fdis._2.foreach(kv => {
      val numerator = kv._2 / featureSum(fdis._1)
      kl += numerator * Math.log(numerator / clsDis(kv._1))
    })
    featureKlDis.put(fdis._1, kl)
  })

  //p(w,c)/p(w)/p(c),norm
  featureDis.foreach(fdis => {
    val f = fdis._1
    val dis = fdis._2
    dis.keys.foreach(cls => {
      //      dis.put(cls, Math.pow(dis(cls) / featureSum(f) / (clsDis(cls) * dataNum), 0.5))
      //hc
      dis.put(cls, dis(cls) / featureSum(f) / clsDis(cls))
      //      dis.put(cls, dis(cls) * dis(cls) / featureSum(f) / (clsDis(cls)))
      //      dis.put(cls, dis(cls) * dis(cls) / featureSum(f) / (clsSum(cls)))
      //      dis.put(cls, dis(cls) * dis(cls) / featureSum(f) / (clsDis(cls)) * dataNum)
    })
    //norm
    val totalScore = dis.values.sum
    dis.keys.foreach(cls => dis.put(cls, dis(cls) / totalScore))
  })

  val parameter: mutable.Map[String, mutable.Map[String, Double]] = new mutable.HashMap[String, mutable.Map[String, Double]]()
  val (startAllScore, endAllScore, startKlScore, endKlScore) = (2.0, 5.0, 0.1, 0.8)
  featureDis.foreach(fdis => {
    val f = fdis._1
    val dis = fdis._2
    var sectionRatio = 1.0
    val allScore = featureSum(f)
    val klScore = 2.0 / (1.0 + math.exp(-featureKlDis(f))) - 1.0
    var isAdd = true
    if (allScore < startAllScore) {
      isAdd = false
    }
    if (isAdd && allScore < endAllScore) {
      sectionRatio *= allScore / endAllScore
    }
    if (isAdd && klScore < startKlScore) {
      isAdd = false
    }
    if (isAdd && klScore < endKlScore) {
      sectionRatio *= klScore / endKlScore
    }
    if (isAdd) {
      clsDis.keys.foreach(cls => dis.put(cls, dis.getOrElse(cls, 0.0) + 0.05))
      dis.keys.foreach(cls => dis.put(cls, Math.log(dis(cls)) * sectionRatio))
      parameter.put(f, dis)
    }
  })
  clsDis.keys.foreach(cls => clsDis.put(cls, Math.log(0.01 + clsDis(cls))))


  //  clsDis.keys.foreach(cls => clsDis.put(cls, Math.log(clsDis(cls))))


  def predict(title: String, content: String, keywordNum: Int, num: Int): Array[(String, Double)] = {

    predict(SoNLP.keyword(title, content, num = keywordNum), keywordNum, num)
  }

  def predict(keywords: Array[StringWeight], keywordNum: Int, num: Int, chs: Set[String]): Array[(String, Double)] = {
    val c = predict(keywords, keywordNum, Int.MaxValue)
    c.filter(kv => chs.contains(kv._1.substring(0, kv._1.length - 4) + "0000")).take(num)
  }

  def predict(keywords: Array[StringWeight], keywordNum: Int, chs: Set[String]): (String, Double) = {
    val cls = predict(keywords, keywordNum, 1, chs)
    if (cls.length == 0) (("-1", 0.0)) else cls(0)
  }


  def predict(keywords: Array[StringWeight], keywordNum: Int, num: Int): Array[(String, Double)] = {
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
            feature.put(t.toString, feature.getOrElse(t.toString, 0.0) + kw.weigth * scale)

          })
        }
      })
      val predictCls = clsDis.clone()
      feature.foreach(fw => {
        if (parameter.contains(fw._1)) {
          parameter(fw._1).foreach(cw => {
            predictCls.put(cw._1, cw._2 * fw._2 + predictCls.getOrElse(cw._1, 0.0))
          })
        }
      })

      predictCls.toArray.sortBy(-_._2).take(num)
    } else {
      Array.empty[(String, Double)]
    }

  }

  def predict(title: String, content: String, keywordNum: Int): (String, Double) = {
    val cls = predict(title, content, keywordNum, 1)
    if (cls.length == 0) (("-1", 0.0)) else cls(0)
  }

  def predict(keywords: Array[StringWeight], keywordNum: Int): (String, Double) = {
    val cls = predict(keywords, keywordNum, 1)
    if (cls.length == 0) (("-1", 0.0)) else cls(0)
  }

}

object BayesianClassification {
  def main(args: Array[String]) {
    if (args.length < 5) {
      println("Usage:<fdisPath> <cdisPath> <topicModelPath> <dataPath> <resultPath>")
      System.exit(1)
    }
    //    val model = new BayesianClassification("model/channel", "model/channel_num", "library/topic")
    val model = new BayesianClassification(args(0), args(1), args(2))
    val p = new PrintWriter(args(4), "utf-8")
    FileProcessor.processLine(new File(args(3)), line => {
      val ss = line.split("\t")
      val sb = new StringBuilder()
      val sch = model.predict(ss(12), ss(14), 10, 3)
      sch.foreach(i => {
        if (!sb.isEmpty) {
          sb.append(',')
        }
        sb.append(i._1 + ":" + i._2)
      })
      p.println(ss(0) + "\t" + ss(6) + "\t" + ss(7) + "\t" + (if (sch.length == 0) "-1" else sch(0)._1) + "\t" + sb.toString() + "\t" + ss(12))
    })
    p.close()
  }
}