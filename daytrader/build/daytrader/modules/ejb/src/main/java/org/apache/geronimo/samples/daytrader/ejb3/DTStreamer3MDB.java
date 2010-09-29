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
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.apache.geronimo.samples.daytrader.util.Log;
import org.apache.geronimo.samples.daytrader.util.MDBStats;
import org.apache.geronimo.samples.daytrader.util.TimerStat;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@MessageDriven(activationConfig =  {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "TradeStreamerTopic"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "NonDurable")
    })
public class DTStreamer3MDB implements MessageListener {
    
    private MDBStats mdbStats;
    private int statInterval = 100;
    
    @Resource
    public MessageDrivenContext mdc;
    
    /** Creates a new instance of TradeSteamerMDB */
    public DTStreamer3MDB() {
        if (Log.doTrace()) Log.trace("DTStreamer3MDB:DTStreamer3MDB()");
        if ( statInterval <= 0 ) statInterval = 100;
        mdbStats = MDBStats.getInstance();
    }
    
    public void onMessage(Message message) {
        try {
            if (Log.doTrace())
                Log.trace("DTStreamer3MDB:onMessage -- received message -->"
                        + ((TextMessage)message).getText() + "command-->"
                        +	message.getStringProperty("command") + "<--");
            String command = message.getStringProperty("command");
            if (command==null) {
                Log.debug("DTStreamer3MDB:onMessage -- received message with null command. Message-->"+message);
                return;
            }
            if (command.equalsIgnoreCase("updateQuote")) {
                if (Log.doTrace())
                    Log.trace("DTStreamer3MDB:onMessage -- received message -->"
                            + ((TextMessage)message).getText()
                            + "\n\t symbol = " + message.getStringProperty("symbol")
                            + "\n\t current price =" + message.getStringProperty("price")
                            + "\n\t old price =" + message.getStringProperty("oldPrice")
                            );
                long publishTime = message.getLongProperty("publishTime");
                long receiveTime = System.currentTimeMillis();
                
                TimerStat currentStats = mdbStats.addTiming("DTStreamer3MDB:udpateQuote", publishTime, receiveTime );
                
                if ( (currentStats.getCount() % statInterval) == 0) {
                    Log.log(new java.util.Date()+ "\nDTStreamer3MDB: 100 Trade stock prices updated:  " +
                            "\nCurrent Statistics\n\tTotal update Quote Price message count = " + currentStats.getCount() +
                            "\n\tTime to receive stock update alerts messages (in seconds):" +
                            "\n\t\tmin: " +currentStats.getMinSecs()+
                            "\n\t\tmax: " +currentStats.getMaxSecs()+
                            "\n\t\tavg: " +currentStats.getAvgSecs()+
                            "\n\n\n\tThe current price update is:\n\t"+((TextMessage)message).getText()) ;
                }
            } else if (command.equalsIgnoreCase("ping")) {
                if (Log.doTrace())
                    Log.trace("DTStreamer3MDB:onMessage  received ping command -- message: " + ((TextMessage)message).getText());
                
                long publishTime = message.getLongProperty("publishTime");
                long receiveTime = System.currentTimeMillis();
                
                TimerStat currentStats = mdbStats.addTiming("DTStreamer3MDB:ping", publishTime, receiveTime );
                
                if ( (currentStats.getCount() % statInterval) == 0) {
                    Log.log(new java.util.Date()+ "\nDTStreamer3MDB: received 100 ping messages. " +
                            "\nCurrent Ping Message Statistics\n\tTotal ping message count = " + currentStats.getCount() +
                            "\n\tTime to receive messages (in seconds):" +
                            "\n\t\tmin: " +currentStats.getMinSecs()+
                            "\n\t\tmax: " +currentStats.getMaxSecs()+
                            "\n\t\tavg: " +currentStats.getAvgSecs()+
                            "\n\n\n\tThe current message is:\n\t"+((TextMessage)message).getText());
                }
            } else
                Log.error("DTStreamer3MDB:onMessage - unknown message request command-->" + command + "<-- message=" + ((TextMessage)message).getText());
        } catch (Throwable t) {
            //JMS onMessage should handle all exceptions
            Log.error("DTStreamer3MDB: Exception", t);
            //UPDATE - Not rolling back for now -- so error messages are not redelivered
            //mdc.setRollbackOnly();
        }
    }
    
}
