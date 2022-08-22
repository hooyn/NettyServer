package twim.netty.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.List;

@ChannelHandler.Sharable
@Slf4j
public class StringEncoderHoyun extends MessageToMessageEncoder<CharSequence> {

    private final Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public StringEncoderHoyun() {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public StringEncoderHoyun(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
        if (msg.length() == 0) {
            return;
        }

        int totalLength = msg.toString().length();
        int cnt = 0;
        while(cnt<totalLength){
            if(msg.toString().equals("SERVER:READY")){
                log.warn("Data: " + msg.toString());
                break;
            }

            int msgLength = Integer.parseInt(msg.toString().substring(cnt+2, cnt+4));
            String data = msg.toString().substring(cnt+4, cnt+msgLength);
            log.warn("Data: " + data);
            cnt += msgLength;

            out.add(data);
        }
    }
}
