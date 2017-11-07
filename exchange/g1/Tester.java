package exchange.g1;

import java.util.ArrayList;
import java.util.Random;

import exchange.g1.Blossom;
import exchange.sim.Sock;
import exchange.sim.Multiset;

public class Tester {
    public static void main(String[] args) {
        for (int n = 10; n <= 1000; n += 10) {
            double totalAverageDistance = 0.0;
            long totalRuntime = 0;
            for (int i = 0; i < 10; i++) {
                totalAverageDistance += testAverageDistance(n);
                totalRuntime += testRuntime(n);
            }
            double averageAverageDistance = totalAverageDistance / 10;
            double averageRuntime = ((double)totalRuntime) / 10;
            System.out.println(n + "\t" + averageAverageDistance + "\t" + averageRuntime);
        }
    }
    
    public static long testRuntime(int n) {
        ArrayList<Sock> socks = new ArrayList<Sock>();
        Random random = new Random();
        for (int i = 0; i < 2 * n; ++i) {
            Sock sock = new Sock(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            socks.add(sock);
        }
        long start = System.currentTimeMillis();
        pair(socks);
        long elapsed = System.currentTimeMillis() - start;
        return elapsed;
    }
    
    public static double testAverageDistance(int n) {
        ArrayList<Sock> socks = new ArrayList<Sock>();
        Random random = new Random();
        for (int i = 0; i < 2 * n; ++i) {
            Sock sock = new Sock(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            socks.add(sock);
        }
        pair(socks);
        double totalDistance = 0.0;
        for (int i = 0; i < 2 * n - 1; i += 2) {
            totalDistance += socks.get(i).distance(socks.get(i + 1));
        }
        return totalDistance / n;
    }
    
    public static void pair(ArrayList<Sock> socks) {
        Sock[] sockArray = socks.toArray(new Sock[socks.size()]);
        int[] match = new Blossom(getCostMatrix(sockArray), true).maxWeightMatching();
        socks.clear();
        for (int i = 0; i < match.length; i++) {
            if (match[i] < i) continue;
            socks.add(sockArray[i]);
            socks.add(sockArray[match[i]]);
        }
    }
    
    private static float[][] getCostMatrix(Sock[] sockArray) {
        float[][] matrix = new float[sockArray.length * (sockArray.length - 1)/2][3];
        int idx = 0;
        for (int i = 0; i < sockArray.length; i++) {
            for (int j=i+1; j< sockArray.length; j++) {
                matrix[idx] = new float[]{i, j, (float)(-sockArray[i].distance(sockArray[j]))};
                idx ++;
            }
        }
        return matrix;
    }
}
