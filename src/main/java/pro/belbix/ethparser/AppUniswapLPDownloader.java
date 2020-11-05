package pro.belbix.ethparser;

import java.math.BigInteger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.web3j.protocol.core.DefaultBlockParameter;
import pro.belbix.ethparser.web3.uniswap.UniswapLpDownloader;

@SpringBootApplication
public class AppUniswapLPDownloader {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AppUniswapLPDownloader.class, args);
        UniswapLpDownloader downloader = context.getBean(UniswapLpDownloader.class);
//        DefaultBlockParameterName from = DefaultBlockParameterName.EARLIEST;
//        DefaultBlockParameterName from = DefaultBlockParameter.valueOf(new BigInteger(""));

        int step = 1000;
        for (int blockNum = 11171716; blockNum < 11197000; blockNum += step) {
            DefaultBlockParameter from = DefaultBlockParameter.valueOf(new BigInteger(blockNum + ""));
            DefaultBlockParameter to = DefaultBlockParameter.valueOf(new BigInteger((blockNum + step) + ""));
            downloader.load(from, to);
        }

        context.close();
    }
}
