package twim.netty;

import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) {
        String input = "0036A9901FFFFFFFFFFFFFFFA{0:0}0040A9914FFFFFFFFFFFFFFFB{0:0,1:1}0044A9914FFFFFFFFFFFFFFFC{0:0,1:1,2:2}";
        StringTokenizer st = new StringTokenizer(input, "}");

        int count = st.countTokens();
        for (int i = 0; i < count; i++) {
            System.out.println(st.nextToken());
        }
    }
}
