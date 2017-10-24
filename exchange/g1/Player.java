package exchange.g1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import exchange.sim.Offer;
import exchange.sim.Request;
import exchange.sim.Sock;
import exchange.sim.Transaction;

public class Player extends exchange.sim.Player {
    /*
        Inherited from exchange.sim.Player:
        Random random   -       Random number generator, if you need it

        Remark: you have to manually adjust the order of socks, to minimize the total embarrassment
                the score is calculated based on your returned list of getSocks(). Simulator will pair up socks 0-1, 2-3, 4-5, etc.
     */
    private int myFirstOffer, mySecondOffer, id;
    private Sock[] socks;

    @Override
    public void init(int id, int n, int p, int t, List<Sock> socks) {
        this.id = id;
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
			offers.get(i)			-		Player i's offer
			For each offer:
			offer.getSock(rank = 1, 2)		-		get rank's offer
			offer.getFirst()				-		equivalent to offer.getSock(1)
			offer.getSecond()				-		equivalent to offer.getSock(2)

			Remark: For Request object, rank ranges between 1 and 2
		 */

		List<Integer> availableOffers = new ArrayList<>();
		for (int i = 0; i < offers.size(); ++ i) {
		    if (i == id) continue;

		    // Encoding the offer information into integer: id * 2 + rank - 1
            if (offers.get(i).getFirst() != null)
                availableOffers.add(i * 2);
            if (offers.get(i).getSecond() != null)
                availableOffers.add(i * 2 + 1);
        }

        int test = random.nextInt(3);
        if (test == 0 || availableOffers.size() == 0) {
            // In Request object, id == -1 means no request.
            return new Request(-1, -1, -1, -1);
        } else if (test == 1 || availableOffers.size() == 1) {
            // Making random requests
            int k = availableOffers.get(random.nextInt(availableOffers.size()));
            return new Request(k / 2, k % 2 + 1, -1, -1);
        } else {
            int k1 = availableOffers.get(random.nextInt(availableOffers.size()));
            int k2 = availableOffers.get(random.nextInt(availableOffers.size()));
            while (k1 == k2)
                k2 = availableOffers.get(random.nextInt(availableOffers.size()));
            return new Request(k1 / 2, k1 % 2 + 1, k2 / 2, k2 % 2 + 1);
        }
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
        return Arrays.asList(socks);
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
}
