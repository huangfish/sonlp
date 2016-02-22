package com.sohu.mrd.sonlp.common

/**
 * Created by huangyu on 16/2/20.
 */
object KeywordSuite {

  def main(args: Array[String]): Unit = {
    SoNLP.conf("src/conf/hanlp.properties")
    val testCase = Array[String]("2月19日上午,从救援指挥部了解到,金华浦江失联三小孩在浦江檀溪镇长山村找到,目前已送到当地卫生院,精神状态良好。据了解,失联的三个孩子走失近72小时,当日上午10:30左右,三个孩子在大山深处一个山村里被找到,随后救护车立即赶往现场,发现孩子们的地方距离失踪点约15分钟车程。图片来源:浙江在线从2月16日的上午10点,到今天(19日)上午的10点半,整整三天三夜!经过各方救援人员的不懈努力,孩子终于找到了!指挥部传出消息说三个孩子都活着,在长村附近山上的山洞被发现,很多村民奔跑向村口希望看到孩子回来的一刻。图为2月19日,被找到的失联小孩在吃面包。据了解,失踪的三个孩子被找到后都已进食,状态良好!救护队员进入檀溪镇唐山村后,在小溪边见到了三个孩子。据第一时间发现孩子的救护人员称,3个孩子都呆呆地坐在村边。其中的一个女孩,在见到救援队员时就哭了,喊着要妈妈。图为2月19日,找到失联小孩现场。最先找到孩子的,是参加搜救的浦江岩头镇民兵应急小分队。据其中一个孩子说,他们三个人是迷路了,几天没有进食,渴的时候就喝点溪水。现在,在医护人员的照料下,最大的孩子穿上了棉鞋和羽绒衣,被抬上了担架,另外两个孩子都在救援队员怀里,三个进食后生命体征平稳。图为2月19日,救援人员抱着小孩。金华浦江的三个孩子是在16日上午10点左右离开家外出玩耍,此后失联。相关的搜救工作立即展开,浙江几乎全省动员,出动救援队伍59支4000多人次。昨天(18日),这场搜救仍然在继续,搜救队员们几乎将70平方公里范围内的山地仔细找了一遍,可一直没有线索。图为2月19日,救护车将孩子们送往浦江县人民医院接受检查。浙江各地救援人员陆续到达建光村进行搜救,浙江省公安厅也首次出动警用直升机参加搜救。按照指挥部的要求,这次救援要“不放弃任何希望,动用一切力量,采取一切手段,不放过任何线索”。截至2月18日下午5点,已发动省内专业救援队伍59支共4050人次,搜寻面积达70余平方公里。图为2月19日,孩子们在浦江县人民医院接受检查。孩子们被找到是今天(19)中午的10点半,已经达到了72小时黄金救援期的极限。三个孩子在吃了点食物后立即被送往浦江县人民医院,孩子们的精神状态看上去还不错。图为2月19日,孩子们在浦江县人民医院接受检查。自2月16日3名孩童失联后,搜救消息牵动着浦江县内外乃至全国人民的心。由于孩子们失踪的地点附近山势陡峭,山沟、山谷、悬崖等容易成为陆路救援人员的搜寻盲点,因此,运用直升飞机搜寻,能够让我们更直观、清楚地对这些特殊的地段进行勘察。从17日上午接到通知开始,省警务航空队全体队员马不停蹄,从杭州萧山机场驾驶直升机赶到。一面是直升机的高空观察,一面还有“蛙人”在水中的全力搜寻。18日上午9时许,应浦江县公安局请求、前来帮助进行水下搜索的金华潜水队也赶到上河村,在村中一处蓄水池进行搜救。60来岁的金华“蛙人”老王扎进刺骨的池水中搜寻近3小时。此外,省内各地的救援人员一次次跋山涉水、不放过任何有可能发现孩子们踪迹的报案线索……这一切,只是为了能够更及时地追寻失联儿童的下落,给期盼他们回家的亲人们一个交待。")
    println(SoNLP.keyword(new Article("", "", testCase(0), "")).map(kv => kv.key + ":" + kv.weight).mkString("\t"))
  }

}
