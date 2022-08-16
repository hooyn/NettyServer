package twim.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettySocketServer {
    private int port;

    public NettySocketServer(int port) {
        this.port = port;
    }

    public void run(){
        // boss 그룹은 연결을 담당하는 스레드들로 '동시'에 처리가능한 접속요청과 관련이 있습니다.
        // boss 그룹에 핸들러를 추가해서 병목을 만들지 않는 이상
        // 1개의 boss 스레드만으로도 충분히 많은 접속 요청을 처리할 수 있습니다.
        // boss 스레드는 클라이언트의 연결을 수락하는 부모 스레드 -> 매개변수로 지정한 스레드 개수에 맞춰서 일 처리
        // workerGroup은 worker쓰레드가 10개라면 100명의 클라이언트가 동시 접속 했을 때
        // worker스레드 하나당 10명의 클라이언트를 처리합니다.
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup();



        // 네티의 부트스트랩은 네티가 작동할 때 기본적으로 설정해야하는 클래스 입니다.
        // 부트스트랩을 사용하므로써 네티 소켓의 모드나 스레드 등을 쉽게 설정할 수 있습니다.
        // 또한 이벤트 핸들러도 부트스트랩에서 설정해야합니다.

        // ServerBootstrap은 두가지로 나뉘는데 하나는 서버 애플리케이션을 위한 것이고,
        // 다른 하나는 클라이언트를 위한 부트스트랩입니다. 둘은 유사하긴 하지만 분명
        // 다른 부분이 있습니다.
        ServerBootstrap bootstrap = new ServerBootstrap();

        ServerBootstrap group = bootstrap.group(bossGroup, workerGroup);
        // group()메서드는 EventLoopGroup을 설정하는 역할입니다.
        // EventLoopGroup은 스레드의 그룹이라고 생각해도 됩니다.
        // group()은 인자로 parentGroup과 childGroup을 받습니다.
        // group()은 인자로 받은 스레드들로 스레드 그룹을 초기화 해줍니다.
        // parentGroup은 부모 스레드로써 클라이언트 요청을 수락하는 역할을 하고
        // childGroup은 자식 스레드로써 IO와 이벤트 처리를 담당합니다.

        ServerBootstrap channel = group.channel(NioServerSocketChannel.class);
        // channel() 메서드는 소켓의 입출력 모드를 설정하는 역할을 합니다.
        // NioServerSocketChannel클래스는 논블로킹 모드로 채널을 만든다는 의미입니다.

        // 소켓 관련 시스템 콜에 대하여 네트워크 시스템이 즉시 처리할 수 없는 경우라도
        // 시스템콜이 바로 리턴되어 응용 프로그램이 block되지 않게 하는 소켓 모드입니다.
        // 통신 상대가 여럿이거나 여러가지 작업을 병행하려면
        // nonblocking 또는 비동기 모드를 사용해야 합니다.
        // non-blcking 모드를 사용하는 경우에는 일반적으로
        // 어떤 시스템 콜이 성공적으로 실행될때까지 계속 루프를
        // 돌면서 확인하는 방법(폴링)을 사용합니다.

        ServerBootstrap bootstrap_childHandler = channel.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                //pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(80));
                pipeline.addLast(new NettySocketServerHandler());
            }
        });
        // childHandler() 메서드는 소켓 채널로 송수신 되는 데이터를 가공하는 역할을 합니다.
        // ChannelInitializer 객체의 initChannel 메서드를 구현해서 파이프라인 객체를 만들고
        // 핸들러를 파이프라인에 추가했습니다.

        bootstrap_childHandler
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        // option() 메서드는 서버 소켓의 옵션을 설정할 수 있고,
        // childOption() 메서드는 서버에 접속한 클라이언트 소켓에 대한 옵션을 설정합니다.
        // TCP_NODELAY : 데이터 송수신에 네이글 알고리즘 비활 성화 여부 지정
        // SO_KEEPALIVE : 운영체제에서 지정된 시간에 한번씩 keepalive 패킷을 상대 방에게 전송
        // SO_BACKLOG : 동시에 수용 가능한 소켓 연결 요청수

        try{
            // 서버를 비동기 식으로 바인딩 한다. sync() 는 바인딩이 완료되기를 대기한다.
            // ChannelFuture 는 작업이 완료되면 그 결과에 접근 할 수 있게 해주는
            // 자리 표시자 역활을 하는 인터페이스이다.
            // 아래 코드는 부트스트랩 시동장치에 포트번호를 부여한다고 이해하였습니다.
            ChannelFuture f = bootstrap.bind(port).sync();

            // 채널의 CloseFuture를 얻고 완료 될때 까지 현재 스레드를 블로킹한다.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
