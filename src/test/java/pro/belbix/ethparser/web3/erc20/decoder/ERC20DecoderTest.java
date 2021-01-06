package pro.belbix.ethparser.web3.erc20.decoder;

import static org.junit.Assert.*;

import org.junit.Test;

public class ERC20DecoderTest {
    private ERC20Decoder decoder = new ERC20Decoder();

    @Test
    public void decodeMethodNameTest() {
        assertEquals("", decoder.decodeMethodName("0xd9627aa4"));
    }
}
