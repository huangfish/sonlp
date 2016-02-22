package com.sohu.mrd.sonlp.common

/**
 * Created by huangyu on 16/2/2.
 */
abstract class Segment {

  def seg(text: String): Array[Term]
}
