package com.sohu.mrd.sonlp.sch

import scala.io.Source
import java.io.PrintWriter
import scala.collection.mutable
import java.io.File

object FileProcessor {
  def processFile(file: File, fun: File => Unit): Unit = {
    if (file.isFile())
      fun(file)
    else
      file.listFiles().foreach(fun)
  }

  def processLineString(file: String, fun: String => Unit): Unit = {
    processLine(new File(file), fun)
  }

  def processLineString(file: String, filter: File => Boolean, fun: String => Unit): Unit = {
    processLine(new File(file), filter, fun)
  }

  def processLine(file: File, fun: String => Unit): Unit = {
    if (file.isFile())
      Source.fromFile(file, "utf-8").getLines.foreach(line => fun(line))
    else
      file.listFiles().foreach(f => processLine(f, fun))
  }

  def processLine(file: File, filter: File => Boolean, fun: String => Unit): Unit = {
    if (!filter(file)) {
      if (file.isFile())
        Source.fromFile(file, "utf-8").getLines.foreach(line => fun(line))
      else
        file.listFiles().foreach(f => processLine(f, fun))
    }
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
