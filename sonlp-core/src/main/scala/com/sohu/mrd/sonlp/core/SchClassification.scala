package com.sohu.mrd.sonlp.core

import java.io.{File, PrintWriter}

import com.sohu.mrd.sonlp.SoNLP
import com.sohu.mrd.sonlp.util.FileProcessor
import org.apache.commons.lang.StringUtils

import scala.collection.mutable

/**
 * Created by huangyu on 15/12/27.
 */
class SchClassification(schFeatureDisPath: String, schDisPath: String,
                        chFeatureDisPath: String, chDisPath: String,
                        topicModelFile: String, topicScale: Boolean = true) {

  //key:feature,value:sch->weight
  val schFeatureDis: mutable.Map[String, mutable.Map[String, Double]] = new mutable.HashMap[String, mutable.Map[String, Double]]()
  //key:feature,value:ch->weight
  val chFeatureDis: mutable.Map[String, mutable.Map[String, Double]] = new mutable.HashMap[String, mutable.Map[String, Double]]()
  //key:sch,value:weight
  val schDis: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  //key:ch,value:weight
  val chDis: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  //key:feature,value:sch weight
  val schFeatureSum: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  //key:feature,value:ch weight
  val chFeatureSum: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  val topicModel = readTopicModel(topicModelFile)
  //  val schSum = schFeatureSum.values.sum
  //  val chSum = chFeatureSum.values.sum

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

    FileProcessor.processLine(new File(schFeatureDisPath), line => {
      val ss = line.split("\t")
      val wd = schFeatureDis.getOrElseUpdate(ss(1), new mutable.HashMap[String, Double]())
      wd.put(ss(0), ss(2).toDouble)
      schFeatureSum.put(ss(1), schFeatureSum.getOrElse(ss(1), 0.0) + ss(2).toDouble)
    })

    FileProcessor.processLine(new File(schDisPath), line => {
      val ss = line.split("\t")
      schDis.put(ss(0), ss(1).toDouble)
    })

    FileProcessor.processLine(new File(chFeatureDisPath), line => {
      val ss = line.split("\t")
      val wd = chFeatureDis.getOrElseUpdate(ss(1), new mutable.HashMap[String, Double]())
      wd.put(ss(0), ss(2).toDouble)
      chFeatureSum.put(ss(1), chFeatureSum.getOrElse(ss(1), 0.0) + ss(2).toDouble)
    })

    FileProcessor.processLine(new File(chDisPath), line => {
      val ss = line.split("\t")
      chDis.put(ss(0), ss(1).toDouble)
    })
  }

  val schDataNum = schDis.values.sum
  val chDataNum = chDis.values.sum
  schDis.keys.foreach(k => schDis(k) = schDis(k) / schDataNum)
  chDis.keys.foreach(k => chDis(k) = chDis(k) / chDataNum)

  //  val clsSum = new mutable.HashMap[String, Double]()
  //  schFeatureDis.foreach(fdis => {
  //    fdis._2.foreach(kv => clsSum.put(kv._1, clsSum.getOrElse(kv._1, 0.0) + kv._2))
  //  })

  val schFeatureKlDis = new mutable.HashMap[String, Double]()
  schFeatureDis.foreach(fdis => {
    var kl = 0.0
    fdis._2.foreach(kv => {
      val numerator = kv._2 / schFeatureSum(fdis._1)
      kl += numerator * Math.log(numerator / schDis(kv._1))
    })
    schFeatureKlDis.put(fdis._1, kl)
  })

  //p(c|w)/p(c),norm
  schFeatureDis.foreach(fdis => {
    val f = fdis._1
    val dis = fdis._2
    dis.keys.foreach(cls => {
      //      dis.put(cls, Math.pow(dis(cls) / featureSum(f) / (clsDis(cls) * dataNum), 0.5))
      //hc
      dis.put(cls, dis(cls) / schFeatureSum(f) / schDis(cls))
      //      dis.put(cls, dis(cls) * dis(cls) / featureSum(f) / (clsDis(cls)))
      //      dis.put(cls, dis(cls) * dis(cls) / featureSum(f) / (clsSum(cls)))
      //      dis.put(cls, dis(cls) * dis(cls) / featureSum(f) / (clsDis(cls)) * dataNum)
    })
    //norm
    val totalScore = dis.values.sum
    dis.keys.foreach(cls => dis.put(cls, dis(cls) / totalScore))
  })
  //p(c|w)/p(c),norm
  chFeatureDis.foreach(fdis => {
    val f = fdis._1
    val dis = fdis._2
    dis.keys.foreach(cls => {
      dis.put(cls, dis(cls) / chFeatureSum(f) / chDis(cls))
    })
    //norm
    val totalScore = dis.values.sum
    dis.keys.foreach(cls => dis.put(cls, dis(cls) / totalScore))
  })

  //key:feature,value:sch->weight
  val parameter: mutable.Map[String, mutable.Map[String, Double]] = new mutable.HashMap[String, mutable.Map[String, Double]]()
  val (startAllScore, endAllScore, startKlScore, endKlScore) = (2.0, 5.0, 0.1, 0.8)
  schFeatureDis.foreach(fdis => {
    val f = fdis._1
    val dis = fdis._2
    var sectionRatio = 1.0
    val allScore = schFeatureSum(f)
    val klScore = 2.0 / (1.0 + math.exp(-schFeatureKlDis(f))) - 1.0
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
      //smooth
      schDis.keys.foreach(cls => dis.put(cls, dis.getOrElse(cls, 0.0) * 0.84 + 0.1 *
        chFeatureDis.get(f).map(_.getOrElse(cls.substring(0, cls.length - 4) + "0000", 0.0)).getOrElse(0.0) +
        0.06 * schDis(cls)))

      //      schDis.keys.foreach(cls => dis.put(cls, dis.getOrElse(cls, 0.0) * 0.7 + 0.2 *
      //        chFeatureDis.get(f).map(_.getOrElse(cls.substring(0, cls.length - 4) + "0000", 0.0)).getOrElse(0.0) +
      //        0.1 * schDis(cls)))
      dis.keys.foreach(cls => dis.put(cls, Math.log(dis(cls)) * sectionRatio))
      parameter.put(f, dis)
    }
  })
  schDis.keys.foreach(cls => schDis.put(cls, Math.log(schDis(cls))))


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


  def generateFeature(keywords: Array[StringWeight], keywordNum: Int): mutable.Map[String, Double] = {
    //    if (keywords.length <= 4) {
    //      mutable.Map.empty[String, Double]
    //    } else {

    def wordSizeScale(size: Int): Double = {
      //      if (size <= 3) 1.0 else Math.pow(0.95, (size - 3))
      if (size == 0) 1.0 else Math.pow(0.9, size - 1)
      //      1.0
      //      1.0
      //      1.0 / size
      //      1.0 / Math.pow(size, 0.5)
      //      1.0 / size
      //      if (size <= 2) 1.0 else Math.pow(1.0 / (size - 2), 0.5)
    }
    val scale = Math.pow(keywords.length.toDouble / keywordNum, 0.5)
    val feature: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
    keywords.foreach(kw => {
      if (!StringUtils.isNumeric(kw.key)) {
        feature.put(kw.key.replaceAll(":", "").replaceAll("\\s+", ""), kw.weigth * scale)
      }
      if (topicModel.contains(kw.key)) {
        topicModel(kw.key).foreach(t => {
          if (topicScale) {
            //            feature.put(t.toString, feature.getOrElse(t.toString, 0.0) + kw.weigth * scale / Math.log1p(topicModel(kw.key).size))
            //            feature.put(t.toString, feature.getOrElse(t.toString, 0.0) + kw.weigth * scale / Math.log1p(topicModel(kw.key).size))
            feature.put(t.toString, feature.getOrElse(t.toString, 0.0) + kw.weigth * scale * wordSizeScale(topicModel(kw.key).size))
          } else {
            feature.put(t.toString, feature.getOrElse(t.toString, 0.0) + kw.weigth * scale)
          }

        })
      }
    })
    feature
    //    }
  }

  def generateFeature(title: String, content: String, keywordNum: Int): mutable.Map[String, Double] = {
    generateFeature(SoNLP.keyword(title, content, num = keywordNum), keywordNum)
  }

  def getWeight(feature: mutable.Map[String, Double]): mutable.Map[String, mutable.Map[String, Double]] = {
    val usedParameter = new mutable.HashMap[String, mutable.Map[String, Double]]()
    feature.foreach(fw => {
      if (parameter.contains(fw._1)) {
        usedParameter.put(fw._1, parameter(fw._1))
      }
    })
    usedParameter
  }


  def predict(keywords: Array[StringWeight], keywordNum: Int, num: Int): Array[(String, Double)] = {
    //    val keywords = SoNLP.keyword(title, content, num = keywordNum)
    if (keywords.length >= 4) {
      //      val scale = Math.pow(keywords.length.toDouble / keywordNum, 0.5)
      //      val feature: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
      //      keywords.foreach(kw => {
      //        if (!StringUtils.isNumeric(kw.key)) {
      //          feature.put(kw.key.replaceAll(":", "").replaceAll("\\s+", ""), kw.weigth * scale)
      //        }
      //        if (topicModel.contains(kw.key)) {
      //          topicModel(kw.key).foreach(t => {
      //            feature.put(t.toString, feature.getOrElse(t.toString, 0.0) + kw.weigth * scale)
      //
      //          })
      //        }
      //      })
      val feature = generateFeature(keywords, keywordNum)
      val predictCls = schDis.clone()
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

object SchClassification {

  def apply(_basePath: String): SchClassification = {
    val basePath = if (_basePath.endsWith("/")) _basePath else _basePath + "/"
    new SchClassification(basePath + "sub_channel", basePath + "sub_channel_num", basePath + "channel",
      basePath + "channel_num", basePath + "topic_model")
  }

  def main(args: Array[String]) {
    if (args.length < 7) {
      println("Usage:<schFeatureDisPath> <schDisPath> <chFeatureDisPath> <chDisPath> <topicModelPath> <dataPath> <resultPath>")
      System.exit(1)
    }
    //109178490
    //109140121
    //109178584
    //704906
    //109138534
    //109138823
    //    val model = new BayesianClassification("model/channel", "model/channel_num", "library/topic")
    val model = new SchClassification(args(0), args(1), args(2), args(3), args(4), true)
    val p = new PrintWriter(args(6), "utf-8")
    FileProcessor.processLine(new File(args(5)), line => {
      val ss = line.split("\t")
      val sb = new StringBuilder()
      val sch = model.predict(ss(12), ss(14), 20, 3)
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