package com.sohu.mrd.sonlp.sch

import java.io.{PrintWriter, File}

import collection.mutable

/**
  * Created by root on 16-4-18.
  */
object SchTrainModel {

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage:SchTrainModel <dataPath> <modelPath>")
    }

    val modelPath = if (args(1).endsWith("/")) args(1) else args(1) + "/"
    val dataPath = args(0)
    saveNum(modelPath + "channel_num", getNum(dataPath, f => false, ss => ss(1).substring(0, ss(1).length - 4) + "0000"))
    saveNum(modelPath + "sub_channel_num", getNum(dataPath, f => f.getAbsolutePath.contains("channel"), ss => ss(1)))
    saveMap(modelPath + "channel", getMap(dataPath, f => !(f.getAbsolutePath.contains("channel")), ss => ss(1).substring(0, ss(1).length - 4) + "0000"))
    saveMap(modelPath + "sub_channel", getMap(dataPath, f => (f.getAbsolutePath.contains("channel")), ss => ss(1)))
  }


  def saveNum(resultFile: String, fs: mutable.Map[String, Int]): Unit = {
    val p = new PrintWriter(resultFile, "utf-8")
    fs.toArray.sortBy(-_._2).foreach(kv => {
      p.println(kv._1 + "\t" + kv._2)
    })
    p.close
  }

  def getNum(dataPath: String, filter: File => Boolean, clsFun: Array[String] => String): mutable.Map[String, Int] = {

    val fs = new mutable.HashMap[String, Int]()
    FileProcessor.processLineString(dataPath, filter, line => {
      val ss = line.split("\001")
      if (ss.length == 4) {
        val cls = clsFun(ss)
        fs.put(cls, fs.getOrElse(cls, 0) + 1)
      } else {
        println(line)
      }
    }
    )
    fs
  }

  def saveMap(_resultPath: String, fs: mutable.Map[String, mutable.Map[String, Double]]): Unit = {
    val f = new File(_resultPath)
    if (f.exists()) {
      f.delete()
      f.mkdir()
    } else {
      f.mkdir()
    }
    val resultPath = if (_resultPath.endsWith("/")) _resultPath else _resultPath + "/"
    fs.foreach(sfs => {
      val p = new PrintWriter(resultPath + sfs._1, "utf-8")
      sfs._2.toArray.sortBy(-_._2).foreach(kv => {
        p.println(sfs._1 + "\t" + kv._1 + "\t" + kv._2)
      })
      p.close()
    })
  }

  def getMap(dataPath: String, filter: File => Boolean, clsFun: Array[String] => String): mutable.Map[String, mutable.Map[String, Double]] = {

    val fs = new mutable.HashMap[String, mutable.Map[String, Double]]()
    FileProcessor.processLineString(dataPath, filter, line => {
      val ss = line.split("\001")
      if (ss.length == 4) {
        val cls = clsFun(ss)
        val f = fs.getOrElseUpdate(cls, new mutable.HashMap[String, Double]())
        ss(3).split("\\s+").filter(_.trim != "").foreach(_kv => {
          val kv = _kv.split(":")
          f.put(kv(0), f.getOrElse(kv(0), 0.0) + kv(1).toDouble)
        })
      } else {
        println(line)
      }
    }
    )
    fs
  }
}
