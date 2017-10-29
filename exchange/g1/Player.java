package exchange.g1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import exchange.sim.Offer;
import exchange.sim.Request;
import exchange.sim.Sock;
import exchange.sim.Transaction;

import exchange.g1.Blossom;

public class Player extends exchange.sim.Player {
    /*
        Inherited from exchange.sim.Player:
        Random random   -       Random number generator, if you need it

        Remark: you have to manually adjust the order of socks, to minimize the total embarrassment
                the score is calculated based on your returned list of getSocks(). Simulator will pair up socks 0-1, 2-3, 4-5, etc.
     */
    private int myFirstOffer, mySecondOffer, id, n;
    private int myFirstRequest, mySecondRequest, rankFirstRequest, rankSecondRequest;

    private Sock[] socks;

    @Override
    public void init(int id, int n, int p, int t, List<Sock> socks) {
        this.id = id;
        this.n = n;
        this.socks = (Sock[]) socks.toArray(new Sock[2 * n]);
        this.myFirstOffer = 0;
        this.mySecondOffer = 0;
    }

    @Override
    public Offer makeOffer(List<Request> lastRequests, List<Transaction> lastTransactions) {
        return this.isolatedSocks();
    }

    @Override
    public Request requestExchange(List<Offer> offers) {
        /*
            offers.get(i)                   -       Player i's offer
            For each offer:
            offer.getSock(rank = 1, 2)      -       get rank's offer
            offer.getFirst()                -       equivalent to offer.getSock(1)
            offer.getSecond()               -       equivalent to offer.getSock(2)

            Remark: For Request object, rank ranges between 1 and 2
         */
        
        myFirstRequest = -1;
        mySecondRequest = -1;

        Sock myFirstSock = socks[myFirstOffer]; // First sock we offer
        Sock mySecondSock = socks[mySecondOffer]; // Second sock we offer
        List<Sock> originalPairing = new ArrayList<Sock>(Arrays.asList(socks));
        socks = (Sock[]) getSocks().toArray(new Sock[2 * n]);
        double initialEmbarrassment = getTotalEmbarrassment(socks);
        double bestEmbarrassmentSoFar = initialEmbarrassment;        


        for (int i = 0; i < offers.size(); ++ i) {
            if (i == id) continue;
            
            if (offers.get(i).getFirst() != null)   {
                                
                // The list with one sock changed is sent to calculate the best possible embarrassment
                bestEmbarrassmentSoFar = getBestEmbarrassment(originalPairing,
                                                            bestEmbarrassmentSoFar, offers, 
                                                            i, false);
            }


            if (offers.get(i).getSecond() != null)   {

                // The list with one sock changed is sent to calculate the best possible embarrassment
                bestEmbarrassmentSoFar = getBestEmbarrassment(new ArrayList<Sock>(Arrays.asList(socks)), 
                                                                bestEmbarrassmentSoFar, offers, 
                                                                i, true);
            }   


        }

        socks = (Sock[]) originalPairing.toArray(new Sock[2 * n]);
        rankFirstRequest = (myFirstRequest != -1) ? rankFirstRequest : -1;
        rankSecondRequest = (mySecondRequest != -1) ? rankSecondRequest : -1;

        return new Request(myFirstRequest, rankFirstRequest, mySecondRequest, rankSecondRequest);
    }

    @Override
    public void completeTransaction(Transaction transaction) {
        /*
            transaction.getFirstID()        -       first player ID of the transaction
            transaction.getSecondID()       -       Similar as above
            transaction.getFirstRank()      -       Rank of the socks for first player
            transaction.getSecondRank()     -       Similar as above
            transaction.getFirstSock()      -       Sock offered by the first player
            transaction.getSecondSock()     -       Similar as above

            Remark: rank ranges between 1 and 2
         */
        int rank;
        Sock newSock;
        if (transaction.getFirstID() == id) {
            rank = transaction.getFirstRank();
            newSock = transaction.getSecondSock();
        } else {
            rank = transaction.getSecondRank();
            newSock = transaction.getFirstSock();
        }
        if (rank == 1) socks[myFirstOffer] = newSock;
        else socks[mySecondOffer] = newSock;
    }

    @Override
    public List<Sock> getSocks() {
        int[] match = new Blossom(getCostMatrix(), true).maxWeightMatching();
        List<Sock> result = new ArrayList<Sock>();
        for (int i=0; i<match.length; i++) {
            if (match[i] < i) continue;
            result.add(socks[i]);
            result.add(socks[match[i]]);
        }

        return result;
    }

    private float[][] getCostMatrix() {
        float[][] matrix = new float[2*n*(2*n-1)/2][3];
        int idx = 0;
        for (int i = 0; i < socks.length; i++) {
            for (int j=i+1; j< socks.length; j++) {
                matrix[idx] = new float[]{i, j, (float)(-socks[i].distance(socks[j]))};
                idx ++;
            }
        }
        return matrix;
    }
    
    // Finds the two socks whose nearest neighbor is the furthest
    public Offer isolatedSocks() {
        double firstLongest = 0;
        double secondLongest = 0;
        for (int i = 0; i < socks.length; i++) {
            double shortest = 500.0; // longest possible dist ~442
            for (int j = i + 1; j < socks.length; j++) {
                double dist = socks[i].distance(socks[j]);
                if (dist < shortest) {
                    shortest = dist;
                }
            }
            if (shortest >= firstLongest) {
                secondLongest = firstLongest;
                mySecondOffer = myFirstOffer;
                firstLongest = shortest;
                myFirstOffer = i;
            }
        }
        return new Offer(socks[myFirstOffer], socks[mySecondOffer]);
    }

    // Embarrasment calculation for current list of sockets
    private double getTotalEmbarrassment(Sock[] list) {

        double result = 0;
        for (int i = 0; i < list.length; i += 2)
            result += list[i].distance(list[i + 1]);
        return result;
    }

    // Switch our first and second offered sock for another's player sock and returns
    // new embarrassment. If the first sock was switched for second rank offered
    // sock, the boolean hast to be true.

    private double getBestEmbarrassment(List<Sock> originalPairing,
        double initialEmbarrassment, List<Offer> offers,
        int i, boolean isSecond)    {

        double bestEmbarrassmentSoFar = initialEmbarrassment;

        // Switch first sock with first or second on offer at player i to calculate new distance
        socks[myFirstOffer] = (isSecond == false) ? offers.get(i).getFirst() : offers.get(i).getSecond(); 
        
        // Get sock pairing    
        socks = (Sock[]) getSocks().toArray(new Sock[2 * n]);

        // Calculate new embarrassment
        double firstOfferExchEmb = getTotalEmbarrassment(socks);
        if(firstOfferExchEmb < bestEmbarrassmentSoFar) {
            myFirstRequest = i;
            rankFirstRequest = (isSecond == false) ? 1 : 2;
            bestEmbarrassmentSoFar = firstOfferExchEmb;
            mySecondRequest = -1;
        }

        for (int j = 0; j < offers.size(); j++) {
            if (j== id) continue;

            if ((j != i || isSecond == true) && offers.get(j).getFirst() != null)   {

                // Get original order
                socks = (Sock[]) originalPairing.toArray(new Sock[2 * n]);
                // Switch first sock with first on offer at player i to calculate new distance
                socks[myFirstOffer] = (isSecond == false) ? offers.get(i).getFirst() : offers.get(i).getSecond();   
                // Switch second sock to calculate new distance
                socks[mySecondOffer] = offers.get(j).getFirst();
                // Calculate new embarrassment                
                socks = (Sock[]) getSocks().toArray(new Sock[2 * n]);

                // Calculate new embarrassment
                double secondOfferExchEmb = getTotalEmbarrassment(socks);
                if(secondOfferExchEmb < bestEmbarrassmentSoFar) {
                    myFirstRequest = i;
                    rankFirstRequest = (isSecond == false) ? 1 : 2;
                    mySecondRequest = j;
                    rankSecondRequest = 1;
                    bestEmbarrassmentSoFar = secondOfferExchEmb;
                }
            }

            if((j != i || isSecond == false) && offers.get(j).getSecond() != null)   {
                
                // Get original order
                socks = (Sock[]) originalPairing.toArray(new Sock[2 * n]);
                // Switch first sock with first on offer at player i to calculate new distance
                socks[myFirstOffer] = (isSecond == false) ? offers.get(i).getFirst() : offers.get(i).getSecond();   
                // Switch second sock to calculate new distance
                socks[mySecondOffer] = offers.get(j).getSecond();
                // Calculate new embarrassment                
                socks = (Sock[]) getSocks().toArray(new Sock[2 * n]);

                // Calculate new embarrassment
                double secondOfferExchEmb = getTotalEmbarrassment(socks);
                if(secondOfferExchEmb < bestEmbarrassmentSoFar) {
                    myFirstRequest = i;
                    rankFirstRequest = (isSecond == false) ? 1 : 2;
                    mySecondRequest = j;
                    rankSecondRequest = 2;
                    bestEmbarrassmentSoFar = secondOfferExchEmb;
                }
            }
        }                    
        
        // Leave sock in the initial order
        socks = (Sock[]) originalPairing.toArray(new Sock[2 * n]);

        return bestEmbarrassmentSoFar;
    }    

}