package twim.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.StringTokenizer;

@ChannelHandler.Sharable //안전하게 데이터를 처리하도록 하는 어노테이션
public class NettySocketServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg){
        String readMessage = ((ByteBuf) msg).toString(Charset.forName("UTF8"));
        // ByteBuf: 사용자 정의 버퍼 형식으로 확장할 수 있습니다.
        // 순차적인 두가지 포인트 변수를 제공하여 읽기 쓰기 전환 없이 사용가능합니다.
        System.out.println(readMessage);

        ctx.channel().write(msg);
        ctx.close();
        // ChannelHandlerContext 는 다음 ChannelHandler에게 이벤트를 넘기거나
        // 동적으로 ChannelPipeline을 변경할 수 있습니다.

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
