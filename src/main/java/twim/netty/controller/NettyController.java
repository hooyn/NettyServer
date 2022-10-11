package twim.netty.controller;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import twim.netty.server.NettySocketServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Controller
@Slf4j
public class NettyController {
    private NettySocketServer server;

    @PostConstruct
    private void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    log.info("Start Socket TCP { Port: 5050 }");
                    server = new NettySocketServer(5050);
                    server.run();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @PreDestroy
    private void destroy(){
        log.info("Destroy Socket TCP { Port: 5050 }");
    }
}
