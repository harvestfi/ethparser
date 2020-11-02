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
        ConfigurableApplicationContext context = SpringApplication.run(AppVaultDownloader.class, args);
        UniswapLpDownloader downloader = context.getBean(UniswapLpDownloader.class);
//        DefaultBlockParameterName from = DefaultBlockParameterName.EARLIEST;
//        DefaultBlockParameterName from = DefaultBlockParameter.valueOf(new BigInteger(""));

        int step = 1000;
        for (int blockNum = 10765094; blockNum < 11178000; blockNum += step) {
            DefaultBlockParameter from = DefaultBlockParameter.valueOf(new BigInteger(blockNum + ""));
            DefaultBlockParameter to = DefaultBlockParameter.valueOf(new BigInteger((blockNum + step) + ""));
            downloader.load(from, to);
        }

        context.close();
    }
}
