package twim.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

import java.util.StringTokenizer;

@ChannelHandler.Sharable //안전하게 데이터를 처리하도록 하는 어노테이션
public class NettySocketServerHandler extends ChannelInboundHandlerAdapter {

    private String message = "";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){

        // ByteBuf: 사용자 정의 버퍼 형식으로 확장할 수 있습니다.
        // 순차적인 두가지 포인트 변수를 제공하여 읽기 쓰기 전환 없이 사용가능합니다.
        // ChannelHandlerContext 는 다음 ChannelHandler에게 이벤트를 넘기거나
        // 동적으로 ChannelPipeline 을 변경할 수 있습니다.

        // 들어오는 데이터를 받아서 message에 이어 붙입니다.
        message += (String) msg;
    }

    //데이터 다음으로 넘어가서 출력되는 문제 해결 참고
    //https://groups.google.com/g/netty-ko/c/IcRU-Qoaw7w

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
        // 모든 데이터를 읽었을 때 message를 파싱하여 데이터를 빼냅니다.
        StringTokenizer st = new StringTokenizer(message, "}");
        int count = st.countTokens();

        for (int i = 0; i < count; i++) {
            System.out.println("Data: " + st.nextToken() + "}");
        }

        ctx.writeAndFlush(message);

        //모든 데이터를 출력한 후 message 초기화 해줍니다.
        message = "";
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
