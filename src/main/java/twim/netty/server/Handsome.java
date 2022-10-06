package twim.netty.server;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Handsome extends Thread{
    private static String msg;

    public void run(){

    }

    public Handsome(String msg) {
        this.msg = msg;
    }

    public void addMsg(String msg){
        this.msg += msg;
    }
}
