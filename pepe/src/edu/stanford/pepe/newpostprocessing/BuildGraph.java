package edu.stanford.pepe.newpostprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class BuildGraph {

    final Program p = new Program();

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new BuildGraph().doStuff();
    }

    private void doStuff() throws IOException, ClassNotFoundException {
        new BytecodeInstrumentationInterpreter(p).load("/Users/juanmtamayo/Projects/pepe/dacapo/1288980829399.dmp");
        new JDTDependencyInterpreter(p).load("/Users/juanmtamayo/Projects/pepe/daytrader_proxy/build/geronimo-jetty6-minimal-2.1.4/1294945180579.jdbc.dmp");
        saveGraphs();
    }
    
    private void saveGraphs() throws FileNotFoundException {
        int index = 0;
        final String directory = "results2";
        File f = new File("results2");
        f.mkdirs();
        for (Operation o : p.getOperations()) {
            PrintWriter output = new PrintWriter(directory + File.separator + index + ".gv");
            index++;
            try {
                output.print(o.toGraph());
            } finally {
                output.close();
            }
        }
    }

}
