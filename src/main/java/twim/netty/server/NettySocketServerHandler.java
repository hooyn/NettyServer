package twim.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable //안전하게 데이터를 처리하도록 하는 어노테이션
public class NettySocketServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg){
        // ByteBuf: 사용자 정의 버퍼 형식으로 확장할 수 있습니다.
        // 순차적인 두가지 포인트 변수를 제공하여 읽기 쓰기 전환 없이 사용가능합니다.
        // ChannelHandlerContext 는 다음 ChannelHandler에게 이벤트를 넘기거나
        // 동적으로 ChannelPipeline 을 변경할 수 있습니다.
        ByteBuf in = (ByteBuf) msg;
        String result = in.toString(CharsetUtil.UTF_8);

        System.out.println(result);
        ctx.write(in);

/*
        StringTokenizer st = new StringTokenizer(result, "}");
        int count = st.countTokens();

        for (int i = 0; i < count; i++) {
            System.out.println(st.nextToken()+"}");
        }
*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
