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
        new BytecodeInstrumentationInterpreter(p).load("/Users/juanmtamayo/Projects/pepe/tomcat6/apache-tomcat-6.0.30/1297931567797.dmp");
//        new JDTDependencyInterpreter(p).load("/Users/juanmtamayo/Projects/pepe/daytrader_proxy/build/geronimo-jetty6-minimal-2.1.4/1294945180579.jdbc.dmp");
        saveGraphs();
    }
    
    private void saveGraphs() throws FileNotFoundException {
        int index = 0;
        final String directory = "results_openbravo";
        File f = new File(directory);
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
