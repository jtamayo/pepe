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

package org.apache.geronimo.samples.daytrader.ejb3;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.apache.geronimo.samples.daytrader.TradeServices;
import org.apache.geronimo.samples.daytrader.direct.TradeDirect;
import org.apache.geronimo.samples.daytrader.util.Log;
import org.apache.geronimo.samples.daytrader.util.MDBStats;
import org.apache.geronimo.samples.daytrader.util.TimerStat;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@MessageDriven(activationConfig =  {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "TradeBrokerQueue"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "NonDurable")
    })
public class DTBroker3MDB implements MessageListener {
    private MDBStats mdbStats;
    private int statInterval = 100;
    
    @EJB
    private TradeSLSBLocal tradeSLSB;
    
    @Resource
    public MessageDrivenContext mdc;
    
    /** Creates a new instance of DTBroker3MDB */
    public DTBroker3MDB() {
         if (Log.doTrace()) Log.trace("DTBroker3MDB:DTBroker3MDB()");
         if ( statInterval <= 0 ) statInterval = 100;
    mdbStats = MDBStats.getInstance();
    }
    
    public void onMessage(Message message) {
        try {
            if (Log.doTrace())
                Log.trace("TradeBroker:onMessage -- received message -->"
                        + ((TextMessage)message).getText() + "command-->"
                        +   message.getStringProperty("command") + "<--");
            
            if (message.getJMSRedelivered()) {
                Log.log("DTBroker3MDB: The following JMS message was redelivered due to a rollback:\n"+ ((TextMessage)message).getText() );
                //Order has been cancelled -- ignore returned messages
                return;
            }
            String command = message.getStringProperty("command");
            if (command==null) {
                Log.debug("DTBroker3MDB:onMessage -- received message with null command. Message-->"+message);
                return;
            }
            if (command.equalsIgnoreCase("neworder")) {
                /* Get the Order ID and complete the Order */
                Integer orderID = new Integer(message.getIntProperty("orderID"));
                boolean twoPhase = message.getBooleanProperty("twoPhase");
                boolean direct = message.getBooleanProperty("direct");
                long publishTime = message.getLongProperty("publishTime");
                long receiveTime = System.currentTimeMillis();
                
                
                TradeServices trade = null;
                
                try {
                    trade = getTrade(direct);
                    
                    if (Log.doTrace())
                        Log.trace("DTBroker3MDB:onMessage - completing order " + orderID + " twoPhase=" +twoPhase + " direct="+direct);
                    
                    trade.completeOrder(orderID, twoPhase);
                    
                    TimerStat currentStats = mdbStats.addTiming("DTBroker3MDB:neworder", publishTime, receiveTime );
                    
                    if ( (currentStats.getCount() % statInterval) == 0) {
                        Log.log(new java.util.Date()+ "\nDTBroker3MDB: processed 100 stock trading orders. " +
                                "\nCurrent NewOrder Message Statistics\n\tTotal NewOrders process = " + currentStats.getCount() +
                                "\n\tTime to receive messages (in seconds):" +
                                "\n\t\tmin: " +currentStats.getMinSecs()+
                                "\n\t\tmax: " +currentStats.getMaxSecs()+
                                "\n\t\tavg: " +currentStats.getAvgSecs()+
                                "\n\n\n\tThe current order being processed is:\n\t"+((TextMessage)message).getText());
                    }
                } catch (Exception e) {
                    Log.error("DTBroker3MDB:onMessage Exception completing order: " + orderID + "\n",  e);
                    mdc.setRollbackOnly();
                    /* UPDATE - order is cancelled in trade if an error is caught
                                try
                                {
                                        trade.cancelOrder(orderID, twoPhase);
                                }
                                catch (Exception e2)
                                {
                                        Log.error("order cancel failed", e);
                                }*/
                }
            } else if (command.equalsIgnoreCase("ping")) {
                if (Log.doTrace())
                    Log.trace("DTBroker3MDB:onMessage  received test command -- message: " + ((TextMessage)message).getText());
                
                long publishTime = message.getLongProperty("publishTime");
                long receiveTime = System.currentTimeMillis();
                
                TimerStat currentStats = mdbStats.addTiming("DTBroker3MDB:ping", publishTime, receiveTime );
                
                if ( (currentStats.getCount() % statInterval) == 0) {
                    Log.log(new java.util.Date()+ "\nDTBroker3MDB: received 100 ping messages. " +
                            "\nCurrent Ping Message Statistics\n\tTotal ping message count = " + currentStats.getCount() +
                            "\n\tTime to receive messages (in seconds):" +
                            "\n\t\tmin: " +currentStats.getMinSecs()+
                            "\n\t\tmax: " +currentStats.getMaxSecs()+
                            "\n\t\tavg: " +currentStats.getAvgSecs()+
                            "\n\n\n\tThe current message is:\n\t"+((TextMessage)message).getText());
                }
            } else
                Log.error("DTBroker3MDB:onMessage - unknown message request command-->" + command + "<-- message=" + ((TextMessage)message).getText());
        } catch (Throwable t) {
            //JMS onMessage should handle all exceptions
            Log.error("DTBroker3MDB: Error rolling back transaction", t);
            mdc.setRollbackOnly();
        }
    }
    
    private TradeServices getTrade(boolean direct) throws Exception{
        TradeServices trade;
        if (direct)
            trade = new TradeDirect();
        else
            trade = tradeSLSB;
        
        return trade;
    }
    
}
