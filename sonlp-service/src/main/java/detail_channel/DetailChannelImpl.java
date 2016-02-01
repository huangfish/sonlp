package detail_channel;

import com.sohu.mrd.sonlp.core.SchClassification;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import scala.Tuple2;

/**
 * Created by yellowhuang on 2015/12/28.
 */
public class DetailChannelImpl implements DetailChannel.Iface {

    private static Logger logger = Logger.getLogger(DetailChannelImpl.class);

    private final SchClassification model;
    private final int keywordNum;


    public DetailChannelImpl(String _modelPath, int keywordNum) {
        this.keywordNum = keywordNum;
        String modelPath = _modelPath.endsWith("/") ? _modelPath : _modelPath + "/";
        model = new SchClassification(modelPath + "sub_channel", modelPath + "sub_channel_num",
                modelPath + "channel", modelPath + "channel_num", modelPath + "topic_model", true);
    }

    @Override
    public String getDetailChannel(String para) throws TException {
        String[] fields = para.split("\001", 3);
        String re;
//        System.out.println(para);
//        System.out.println(fields.length);
        try {
            if (fields.length <= 0) {
                re = "-1";
            } else {
                Tuple2<String, Object>[] schWeight = model.predict(fields[0], fields.length >= 2 ? fields[1] : "", keywordNum, Integer.MAX_VALUE);

                if (schWeight == null || schWeight.length == 0 || schWeight[0]._1() == "-1") {
                    re = "-1";
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < schWeight.length; i++) {
                        if (i > 0) {
                            sb.append(',');
                        }
                        sb.append(schWeight[i]._1() + ":" + schWeight[i]._2());
                    }
                    re = sb.toString();
                }

            }


        } catch (Exception e) {
            re="-1";
            logger.error("Sub chanenl exception",e);
        }
        logger.info(para + SystemUtils.LINE_SEPARATOR + re);
        return re;

    }
}
