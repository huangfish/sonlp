package com.sohu.mrd.sonlp.core

import java.io.{File, PrintWriter}

import com.sohu.mrd.sonlp.SoNLP
import com.sohu.mrd.sonlp.util.FileProcessor

/**
 * Created by yellowhuang on 2015/12/28.
 */
class MixClassification(featureDisPath: String, classDisPath: String, topicModelFile: String,
                        subFeatureDisPath: String, subClassDisPath: String) {

  val chModel = new BayesianClassification(featureDisPath, classDisPath, topicModelFile)
  val schModel = new BayesianClassification(subFeatureDisPath, subClassDisPath, topicModelFile)
  //  val simpleModel = new SimpleClassification(subFeatureDisPath, topicModelFile)


  def predict(title: String, content: String, keywordNum: Int): (String, Double) = {
    val keywords = SoNLP.keyword(title, content, num = keywordNum)
    if (keywords.length <= 4) {
      return (("-1", 0.0))
    }
    else {
      schModel.predict(keywords, keywordNum)
      //      val simple = simpleModel.predict(keywords, keywordNum, None)
      //      if (simple._2 > 1.0) {
      //        simple
      //      } else {
      //      val ch = chModel.predict(keywords, keywordNum)
      //      val simpleCh = schModel.predict(keywords, keywordNum, Some(ch._1))
      //      if (simpleCh._2 > 0.5) {
      //        simpleCh
      //      } else {
      //        (ch._1 + "_1", simpleCh._2)
      //      }
      //      ("-1",3)
      //      }
    }
  }


}

object MixClassification {

  def main(args: Array[String]) {
    if (args.length < 5) {
      println("Usage:<fdisPath> <cdisPath> <topicModelPath> <subfdisPath> <subcdisPath> <dataPath> <resultPath>")
      System.exit(1)
    }
    //    val model = new BayesianClassification("model/channel", "model/channel_num", "library/topic")
    //    val model = new BayesianClassification(args(0), args(1), args(2))
    val model = new MixClassification(args(0), args(1), args(2), args(3), args(4))
    val p = new PrintWriter(args(6), "utf-8")
    FileProcessor.processLine(new File(args(5)), line => {
      val ss = line.split("\t")
      //      val sb = new StringBuilder()
      val sch = model.predict(ss(12), ss(14), 10)
      println(sch)

      p.println(ss(0) + "\t" + ss(6) + "\t" + ss(7) + "\t" + sch._1 + "\t" + sch._1 + ":" + sch._2 + "\t" + ss(12))
      //      sch.foreach(i => {
      //        if (!sb.isEmpty) {
      //          sb.append(',')
      //        }
      //        sb.append(i._1 + ":" + i._2)
      //      })
      //      p.println(ss(0) + "\t" + ss(6) + "\t" + ss(7) + "\t" + (if (sch.length == 0) "-1" else sch(0)._1) + "\t" + sb.toString() + "\t" + ss(12))
    })
    p.close()
  }
}
