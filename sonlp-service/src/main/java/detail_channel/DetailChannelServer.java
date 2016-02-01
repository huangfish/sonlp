package detail_channel;

import org.apache.log4j.Logger;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;

/**
 * Created by yellowhuang on 2015/12/28.
 */
public class DetailChannelServer {

    private static Logger logger = Logger.getLogger(DetailChannelImpl.class);

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("java -cp detail_channel_server.jar detail_channel.DetailChannelService " +
                    "<modelPath> <kNum(10)> <port(7912)>");
            System.exit(1);
        }
        TServerSocket serverTransport = null;
        TServer server = null;

        try {
            DetailChannelImpl imp = new DetailChannelImpl(args[0], Integer.parseInt(args[1]));
            logger.info("Test:" + imp.getDetailChannel("小伙销售盗版国外教材 获刑3年6个月并处罚金100万元\001原题：小伙盗版出售高价国外教材两年获利百万被判刑——来源：中国网江苏-中青在线　　导读：1988年出生的湖州小伙郑某，在杭州读完大学后一直没有正当工作，但从2012年起的短短两年时间里，郑某突然间暴富，让身边的同学、朋友很是羡慕."));
            serverTransport = new TServerSocket(Integer.parseInt(args[2]));
            TBinaryProtocol.Factory proFactory = new TBinaryProtocol.Factory();
            TProcessor processor = new DetailChannel.Processor(imp);
            TThreadPoolServer.Args rpcArgs = new TThreadPoolServer.Args(serverTransport);
            rpcArgs.processor(processor);
            rpcArgs.protocolFactory(proFactory);
            server = new TThreadPoolServer(rpcArgs);
            server.serve();

        } catch (Exception e) {
            logger.error("Detail channel server exception.", e);
            server.stop();
            serverTransport.close();

        }
    }
}
