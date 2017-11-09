package exchange.g1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import exchange.g1.Blossom;
import exchange.sim.Sock;
import exchange.sim.Multiset;

public class Tester {
    public static void main(String[] args) {
        int iterations = 10;
        double total = 0.0;
        double total2 = 0.0;
        for (int i = 0; i < iterations; i++) {
            total += testRuntime(1000);
            total2 += testAverageDistance(1000);
            System.out.println(i + "\t" + total / (i + 1) + "\t" + total2 / (i + 1));
        }
        System.out.println(total / iterations +  "\t" + total2 / iterations);
    }
    
    
    public static ArrayList<Sock> getSocks(int n) {
        ArrayList<Sock> socks = new ArrayList<Sock>();
        Random random = new Random();
        for (int i = 0; i < 2 * n; ++i) {
            Sock sock = new Sock(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            socks.add(sock);
        }
        return socks;
    }
    
    public static long testRuntime(int n) {
        ArrayList<Sock> socks = new ArrayList<Sock>();
        Random random = new Random();
        for (int i = 0; i < 2 * n; ++i) {
            Sock sock = new Sock(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            socks.add(sock);
        }
        long start = System.currentTimeMillis();
        hybridPair(socks, (int) 0.6 * n);
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
        hybridPair(socks, (int) 0.6 * n);
        double totalDistance = 0.0;
        for (int i = 0; i < 2 * n - 1; i += 2) {
            totalDistance += socks.get(i).distance(socks.get(i + 1));
        }
        return totalDistance / n;
    }
    
    public static void hybridPair(List<Sock> sockList, int numToBlossom) {
        ArrayList<Sock> pairedList = new ArrayList<Sock>();
        while (sockList.size() > numToBlossom) {
            double minDistance = 500.0;
            int minIndex1 = -1;
            int minIndex2 = -1;
            for (int i = 0; i < sockList.size(); i++) {
                for (int j = i + 1; j < sockList.size(); j++) {
                    double distance = sockList.get(i).distance(sockList.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        minIndex1 = i;
                        minIndex2 = j;
                    }
                }
            }
            pairedList.add(sockList.remove(minIndex2));
            pairedList.add(sockList.remove(minIndex1));
        }
        pair(sockList);
        sockList.addAll(pairedList);
    }
    
    public static void pair(List<Sock> socks) {
        Sock[] sockArray = socks.toArray(new Sock[socks.size()]);
        int[] match = new Blossom(getCostMatrix(sockArray), true).maxWeightMatching();
        socks.clear();
        for (int i = 0; i < match.length; i++) {
            if (match[i] < i) continue;
            socks.add(sockArray[i]);
            socks.add(sockArray[match[i]]);
        }
    }
    
    public static void segmentBlossomPair(ArrayList<Sock> sockList) {
        ArrayList<Sock> pairedList = new ArrayList<Sock>();
        int segmentSize = 100;
        for (int i = 0; i < sockList.size(); i += segmentSize) {
            List<Sock> subList = sockList.subList(i, i + segmentSize);
            pair(subList);
        }
    }
    
    public static void greedyPair(ArrayList<Sock> sockList) {
        ArrayList<Sock> pairedList = new ArrayList<Sock>();
        while (sockList.size() > 0) {
//            if (sockList.size() < 100) {
//                pair(sockList);
//                break;
//            }
            Sock toPair = sockList.remove(sockList.size() - 1);
            double minDistance = toPair.distance(sockList.get(0));
            int minIndex = 0;
            for (int i = 0; i < sockList.size(); i++) {
                Sock s = sockList.get(i);
                if (toPair.distance(s) < minDistance) {
                    minDistance = toPair.distance(s);
                    minIndex = i;
                }
            }
            pairedList.add(sockList.remove(minIndex));
            pairedList.add(toPair);
        }
        sockList.addAll(pairedList);
    }
    
    public static void greedyPair2(ArrayList<Sock> sockList) {
        ArrayList<Sock> pairedList = new ArrayList<Sock>();
        while (sockList.size() > 0) {
            double minDistance = 500.0;
            int minIndex1 = -1;
            int minIndex2 = -1;
            for (int i = 0; i < sockList.size(); i++) {
                for (int j = i + 1; j < sockList.size(); j++) {
                    double distance = sockList.get(i).distance(sockList.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        minIndex1 = i;
                        minIndex2 = j;
                    }
                }
            }
            pairedList.add(sockList.remove(minIndex2));
            pairedList.add(sockList.remove(minIndex1));
        }
        sockList.addAll(pairedList);
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
