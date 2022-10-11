package twim.netty.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ChannelHandler.Sharable //안전하게 데이터를 처리하도록 하는 어노테이션
public class NettySocketServerHandler extends ChannelInboundHandlerAdapter {

    private String message = "";
    public static List<Long> banList = new ArrayList<Long>();

    /**
     * ByteBuf: 사용자 정의 버퍼 형식으로 확장할 수 있습니다.
     * 순차적인 두가지 포인트 변수를 제공하여 읽기 쓰기 전환 없이 사용가능합니다.
     * ChannelHandlerContext 는 다음 ChannelHandler 에게 이벤트를 넘기거나
     * 동적으로 ChannelPipeline 을 변경할 수 있습니다.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){

        String data = (String) msg;

        // 들어오는 데이터를 받아서 message 에 이어 붙입니다.
        message += data;
        printMessage();
    }

    private void printMessage() {
        try{
            // message 의 길이가 4개 이상 즉, 데이터의 길이를 얻어올 수 있다면 진행
            if(message.length()>4){

                // 데이터의 길이를 가져와서 msgLength 에 저장
                int msgLength = Integer.parseInt(message.substring(2, 4));

                if(message.length()>=msgLength){

                    // 데이터의 길이가 된다면 message 에 있는 데이터 모두 출력
                    while(message.length()>=msgLength){

                        // message 를 출력
                        String output = message.substring(4, msgLength);

                        if(!banList.contains(Thread.currentThread().getId()))
                            log.info(output + " " + Thread.currentThread().getId());

                        // 출력 데이터 제거
                        message = message.substring(msgLength);

                        // message 가 없거나, message 길이가 작아서 데이터의 길이를 얻지 못할 경우 break
                        if(message.length() == 0 || message.length() < 4)
                            break;

                        // message 를 통해 데이터 길이를 측정할 수 있다면 측정
                        msgLength = Integer.parseInt(message.substring(2, 4));
                    }
                }
            }
        } catch (NumberFormatException e){
            log.error("올바른 데이터를 입력해주세요.");
            message = "";
        }
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
        // Buffer 에 앞에서 지정한 30byte 의 데이터가 들어오면 실행
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
