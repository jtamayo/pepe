package edu.stanford.pepe.jdt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps track of all transactions executed, and writes the whole thing to disk
 * when the JVM shuts down.
 * 
 * @author jtamayo
 */
public class ResultLogger {
    /*
     * Ok, in what format am I going to log this? I need: 1. A transaction ID,
     * to group all queries 2. All queries for a given transaction a. The stack
     * trace for the query b. The order, because that's how you identify them in
     * the fingerprints c. Whether it is a read or a write d. The fingerprints
     * themselves
     */
    public static final Logger logger = Logger.getLogger("edu.stanford.pepe.jdbc");
    
    static {
        logger.setLevel(Level.INFO);
    }

    // In principle, only one thread should be accessing this
    private static final List<Transaction> transactions = new ArrayList<Transaction>();

    static {
        logger.info("Adding shutdown hook to save the log on shutdown");
        System.out.println("Adding shutdown hook to save the log on shutdown");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                toDisk();
            }
        });
    }

    synchronized protected static void toDisk() {
        logger.info("Writing transaction log to disk");
        System.out.println("Writing transaction log to disk");
        File f = new File(System.currentTimeMillis() + ".jdbc.dmp");
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            try {
                oos.writeObject(transactions);
            } finally {
                oos.close();
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Couldn't write log to disk", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Couldn't write log to disk", e);
        }
    }

    synchronized public static void log(long transactionId, List<StackTraceElement[]> queries, ArrayList<Boolean> isWrite,
            Fingerprint[][] fingerprints) {
        System.out.println("Logging transaction");
        transactions.add(new Transaction(transactionId, queries, isWrite, fingerprints));
    }

}
