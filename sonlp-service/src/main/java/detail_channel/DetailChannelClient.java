package detail_channel;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * Created by yellowhuang on 2015/12/28.
 */
public class DetailChannelClient {
    public static void main(String[] args) throws TException {
        TTransport transport = new TSocket("localhost", 7912);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        DetailChannel.Client client = new DetailChannel.Client(protocol);
        System.out.println(client.getDetailChannel("小伙销售盗版国外教材 获刑3年6个月并处罚金100万元\001不过，郑某的暴富神话很快破灭。昨天来自杭州市拱墅区检察院的消息说，由该院提起公诉的首起涉外侵犯著作权案近日一审判决，作为主犯的郑某被判处有期徒刑3年6个月并处罚金100万元。<br><image_0></image_0><br>　　他的暴富“秘笈”：盗版出售高价国外教材<br>　　法院一审判其有期徒刑3年6个月并处罚金100万元<br>　　1988年出生的湖州小伙郑某，在杭州读完大学后一直没有正当工作，但从2012年起的短短两年时间里，郑某突然间暴富，让身边的同学、朋友很是羡慕。<br>　　不过，郑某的暴富神话很快破灭。昨天来自杭州市拱墅区检察院的消息说，由该院提起公诉的首起涉外侵犯著作权案近日一审判决，作为主犯的郑某被判处有期徒刑3年6个月并处罚金100万元。<br>　　原来，郑某从事的“生意”，竟是盗版国外教材从中牟取暴利，他还把有海外硕士学历的堂姐及一些同学朋友都拉下了水。<br>　　盗版复制原价3万的美国教材<br>　　以十分之一的价格出售<br>　　美国注册会计师资格证在全球具有权威性，针对该考试全球有多种教材，其中美国某公司的软件和书籍市场销量较好。这家公司的正版软件和书籍在美国的销售价格大约为3393美元，在中国的售价则约为3.5万元人民币。<br>　　最初让郑某接触到该公司软件和书籍的是其堂姐。郑某的堂姐是一位“海归”硕士，在国外念书时通过了美国注册会计师资格考试，她向郑某推荐了这家美国公司的软件和书籍。<br>　　当时，刚刚大学毕业的郑某在了解该书籍、软件后发现了意外的“商机”——因为该公司的正版培训软件和教材口碑较好，但价格居高不下，郑某就此动起了歪脑筋。<br>　　2012年2月，在杭州的郑某购买了该公司的正版软件和书籍后，联系了一家小型图文社复印该正版书籍，并通过其堂姐联系了网络黑客，破解了正版软件。<br>　　之后，姐弟俩注册了一家网店，在网店上以低于正版市场价格10倍之多(2000至4000元不等)的低价吸引客户，销售所复印的书"));
    }
}
