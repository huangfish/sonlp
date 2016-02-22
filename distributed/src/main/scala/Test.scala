import com.sohu.mrd.sonlp.common.{Article, SoNLP}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by huangyu on 16/2/18.
 */
object Test {

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setAppName("sonlp test"))
    //    HanLP.config(args(0))
    SoNLP.conf(args(0))
    //    HanLP.Config.sparkFiles().split(",").foreach { f => println(f); sc.addFile(f) }
    SoNLP.sparkFiles().split(",").foreach { f => println(f); sc.addFile(f) }
    sc.addFile(args(0))

    val content: String = "程序员(英文Programmer)是从事程序开发、维护的专业人员。" + "一般将程序员分为程序设计人员和程序编码人员，" + "但两者的界限并不非常清楚，特别是在中国。" + "软件从业人员分为初级程序员、高级程序员、系统" + "分析员和项目经理四大类。"
    sc.parallelize(List(content)).map(line => {
      //        HanLP.Config.enableDebug();
      //      HanLP.Config.sparkConf("hanlp.properties")
      //      SoNLP.conf("hanlp.properties")
      SoNLP.sparkConf("hanlp.properties")
      //      HanLP.segment(line).toArray(new Array[Term](0)).mkString(" ")
      //      SoNLP.keyword(new Article("", "", line, ""), 20).map(kv => kv.key + ":" + kv.weight).mkString("\t")
      SoNLP.topic(new Article("", "", line, ""), 10).map(kv => kv.key + ":" + kv.weight).mkString("\t")
      //      HanLP.extractKeyword(line, 20).toArray(new Array[String](0)).mkString(" ")
    }).collect().foreach(println)

    sc.stop()
  }
}
