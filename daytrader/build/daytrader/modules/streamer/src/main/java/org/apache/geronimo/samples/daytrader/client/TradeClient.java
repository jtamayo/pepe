/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.samples.daytrader.client;

import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.ejb.EJB;

import org.apache.geronimo.samples.daytrader.QuoteDataBean;
import org.apache.geronimo.samples.daytrader.ejb3.TradeSLSBRemote;

public class TradeClient {

	public static final int DEFAULT_UPDATE_INTERVAL = 2;
	public static final int DEFAULT_MAX_PER_SECOND = 10;

	// Various components
	private TradeQuoteAuditStats auditStats;
    private TradeClientGUI gui;

	private static TradeClient tradeClient;

	// EJB values
	private InitialContext initial;
    private boolean waitForMain = false;
    
    @EJB
    private static TradeSLSBRemote tradeSLSBRemote;

	// Updater thread
    private final Timer timer = new Timer();
    private TimerTask updater;
    private int updateInterval = DEFAULT_UPDATE_INTERVAL;

	public static void main(String[] args) {
		try	{
			TradeClient streamer  = new TradeClient();
            if (args.length > 0) {
                if (args[0].equals("-waitForMain")) {
                    streamer.waitForMain = true;
                } else {
                    System.out.println("Usage TradeClient [-waitForMain]");
                    System.exit(1);
                }
            }
			tradeClient = streamer;
			streamer.startClient();
        }
		catch (Exception e)	{
			System.err.println("Caught an unexpected exception!");
			e.printStackTrace();
		}
	}

	public static TradeClient getTradeClient() {
		return tradeClient;
	}

	private void startClient() throws Exception { 
		auditStats = new TradeQuoteAuditStats();
		setupEJB();
        TradeClientMessageListener listener = new TradeClientMessageListener(this);
		listener.subscribe();
		resetStatsFromServer();
		gui = new TradeClientGUI(this);
		gui.show();
        
        // Added the "waitForMain" flag to disable/enable the workaround below        
        if (this.waitForMain) {
            // Geronimo client terminates JVM process when Main completes (not sure why)
            // even though client GUI is still active. For now, force Main to remain alive
            // until GUI is closed.
            
            while (gui.isVisible())
                Thread.sleep(5000);
        }
        
    }

	public TradeQuoteAuditStats getAuditStats() {
		return auditStats;
	}

	public void reset() throws Exception {
		resetStatsFromServer();
	}

	public void resetStatsFromServer() throws Exception {
		auditStats.clearStats();
		Collection<QuoteDataBean> quotes = tradeSLSBRemote.getAllQuotes();

		for (Iterator it = quotes.iterator(); it.hasNext(); ) {
			QuoteDataBean bean = (QuoteDataBean)it.next();
			auditStats.updateSymbol(bean.getSymbol(), bean.getCompanyName(), bean.getPrice(), bean.getOpen(), bean.getLow(), bean.getHigh(), bean.getVolume(), System.currentTimeMillis(), bean.getPrice(), bean.getVolume());
		}
	}

	public void updateStatusMessage(String message) {
		gui.updateStatusMessage(message);
	}

	public InitialContext getInitialContext() {
		return initial;
	}

	public void setupEJB() throws Exception {
		initial = new InitialContext();
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
        if (updater != null) {
            updater.cancel();
        }
        updater = new TimerTask() {
            public void run() {
                auditStats.fireTableDataChanged();
                System.out.println("Updating");
            }
        };
        timer.scheduleAtFixedRate(updater, (long)updateInterval*1000, (long)updateInterval*1000);
    }

	public void closeClient() {
        System.exit(1);
	}

}
