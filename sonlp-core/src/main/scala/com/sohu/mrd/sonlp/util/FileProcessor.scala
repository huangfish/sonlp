package com.sohu.mrd.sonlp.util

import java.io.File

import scala.io.Source

/**
 * Created by huangyu on 15/12/20.
 */
object FileProcessor {

  def processFile(file: File, fun: File => Unit): Unit = {
    if (file.isFile())
      fun(file)
    else
      file.listFiles().foreach(fun)
  }

  def processLine(file: File, fun: String => Unit): Unit = {
    if (file.isFile())
      Source.fromFile(file, "utf-8").getLines.foreach(line => fun(line))
    else
      file.listFiles().foreach(f => processLine(f, fun))
  }

  def processFileLine(file: File, fileFun: File => Unit, lineFun: String => Unit): Unit = {
    if (file.isFile()) {
      Source.fromFile(file, "utf-8").getLines.foreach(line => lineFun(line))
      fileFun(file)
    }
    else
      file.listFiles().foreach(f => processFileLine(f, fileFun, lineFun))
  }

}
