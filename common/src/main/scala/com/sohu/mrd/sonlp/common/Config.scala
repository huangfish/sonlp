package com.sohu.mrd.sonlp.common

import java.io.FileInputStream
import java.util.Properties

import com.hankcs.hanlp.HanLP

/**
 * Created by huangyu on 16/2/19.
 */
object Config {

  var root: String = null
  var sameWord: String = null
  var stopWord: String = null
  var whiteWord: String = null
  var topicWord: String = null
  var idfDict: String = null
  //  var confProp: Properties = null

  def sparkFiles(): String = {
    HanLP.Config.sparkFiles() + "," + sameWord + "," +
      stopWord + "," + whiteWord + "," + idfDict + "," + topicWord
  }

  def sparkConf(confFile: String): Unit = {

    def name(path: String): String = path.substring(path.lastIndexOf("/") + 1, path.length())

    HanLP.Config.sparkConf(confFile)
    val p = new Properties()
    p.load(new FileInputStream(confFile))
    //    confProp = p
    root = p.getProperty("root")
    root = if (root.endsWith("/")) root else root + "/"
    sameWord = name(p.getProperty("sameWord", root + "keyword/same_word"))
    stopWord = name(p.getProperty("stopWord", root + "keyword/stop_word"))
    whiteWord = name(p.getProperty("whiteWord", root + "keyword/white_word"))
    idfDict = name(p.getProperty("idfDict", root + "keyword/idf_dict"))
    topicWord = name(p.getProperty("topicWord", root + "topic/topic_word"))
  }

  def conf(confFile: String): Unit = {
    HanLP.config(confFile)
    val p = new Properties();
    p.load(new FileInputStream(confFile))
    //    confProp = p
    root = p.getProperty("root")
    root = if (root.endsWith("/")) root else root + "/"
    sameWord = p.getProperty("sameWord", root + "keyword/same_word")
    stopWord = p.getProperty("stopWord", root + "keyword/stop_word")
    whiteWord = p.getProperty("whiteWord", root + "keyword/white_word")
    idfDict = p.getProperty("idfDict", root + "keyword/idf_dict")
    topicWord = p.getProperty("topicWord", root + "topic/topic_word")

  }

}
