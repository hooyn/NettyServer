package twim.netty.controller;

import org.springframework.stereotype.Controller;
import twim.netty.server.NettySocketServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Controller
public class NettyController {
    private NettySocketServer server;

    @PostConstruct
    private void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    System.out.println("start socket tcp");
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
        System.out.println("destroy socket");
    }
}
