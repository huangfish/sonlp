package com.sohu.mrd.sonlp.core

import java.io.{File, PrintWriter}

import com.sohu.mrd.sonlp.SoNLP
import com.sohu.mrd.sonlp.util.FileProcessor

import scala.collection.mutable

/**
 * Created by huangyu on 15/12/20.
 */
class NewsClassification(_parameterPath: String) {

  val words: mutable.Set[String] = new mutable.HashSet[String]()
  val parameterPath = if (_parameterPath.endsWith("/")) _parameterPath else _parameterPath + "/"
  val wordDistribute: mutable.Map[String, mutable.Map[String, Double]] = new mutable.HashMap[String, mutable.Map[String, Double]]()
  //  val classDistribute: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  val wordSum: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  val wordMax: mutable.Map[String, Double] = new mutable.HashMap[String, Double]()
  initParameter()
  //  val classNum = classDistribute.keys.size
  val wordNum = wordDistribute.keys.size
  smooth()
  normalize()

  private[this] def initParameter(): Unit = {

    FileProcessor.processLine(new File(parameterPath + "word"), line => {
      val ss = line.split("\t")
      if (ss(1).toDouble >= 10) {
        words.add(ss(0))
      }
    })

    FileProcessor.processLine(new File(parameterPath + "word_dis"), line => {
      val ss = line.split("\t")
      if (words.contains(ss(1))) {
        val wd = wordDistribute.getOrElseUpdate(ss(1), new mutable.HashMap[String, Double]())
        wd.put(ss(0), ss(2).toDouble)
        wordSum.put(ss(0), wordSum.getOrElse(ss(0), 0.0) + ss(2).toDouble)
        wordMax.put(ss(0), Math.max(wordMax.getOrElse(ss(0), 0.0), ss(2).toDouble))
      }
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

  def predict(keywords: Array[StringWeight]): Array[(String, Double)] = {
    val schWeight = new mutable.HashMap[String, Double]()
    keywords.foreach(kv => {
      wordDistribute.get(kv.key).foreach(_.foreach(schw => {
        schWeight.put(schw._1, schWeight.getOrElse(schw._1, 0.0) + kv.weigth * schw._2)

      }))
    })
    schWeight.toArray.sortBy(-_._2).take(8)
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

object NewsClassification {
  def main(args: Array[String]): Unit = {
    val nc = new NewsClassification("data/csh/")
    //    nc.save("result/word_dis")
    //    println(nc.wordSum("20200"))
    //    println(nc.wordSum("90300"))
    //    println(nc.wordSum("10100"))
    //    println(nc.wordSum("50100"))
    //    println(nc.wordDistribute("相机")("71300"))
    //    println(nc.getWeigth("挺住", "71400"))
    //    println(nc.getWeigth("挺住", "20200"))
    //    println(nc.getWeigth("南京", "120100"))
    //    println(nc.getWeigth("南京", "50100"))

    //    val pf = new PrintWriter("result/t")
    val pfs = new mutable.HashMap[String, PrintWriter]()
    FileProcessor.processLine(new File("data/20151223"), line => {
      val ss = line.split("\t")
      //      val pf = pfs.getOrElseUpdate(ss(7), new PrintWriter("result/predict/" + ss(7)))
      val kws = SoNLP.keyword(ss(12), ss(14), num = 10)
      val p = nc.predict(kws)
      val name = if (p(0)._2 > 0.9) p(0)._1 else "unkown"
      val pf = pfs.getOrElseUpdate(name, new PrintWriter("result/predict/" + name))

      kws.foreach(kw => pf.print(kw.key + ":" + kw.weigth + ","))
      pf.println
      pf.println(ss(12) + "\t" + ss(7))
      p.foreach(kv => pf.print(kv._1 + ":" + kv._2 + ","))
      pf.println
      pf.println("##############")
    })
    pfs.values.foreach(_.close())

  }
}