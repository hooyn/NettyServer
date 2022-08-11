package twim.netty.client;

import twim.netty.client.NettySocketClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        new NettySocketClient("Connect", "127.0.0.1", 4564).run();
    }
}
