/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.samples.daytrader;

public class TradeWSServicesProxy implements org.apache.geronimo.samples.daytrader.TradeWSServices {
  private String _endpoint = null;
  private org.apache.geronimo.samples.daytrader.TradeWSServices __tradeWSServices = null;
  
  public TradeWSServicesProxy() {
    _initTradeWSServicesProxy();
  }
  
  private void _initTradeWSServicesProxy() {
    try{
      javax.naming.InitialContext ctx = new javax.naming.InitialContext();
      __tradeWSServices = ((org.apache.geronimo.samples.daytrader.Trade)ctx.lookup("java:comp/env/service/Trade")).getTradeWSServices();
    }
    catch (javax.naming.NamingException namingException) {}
    catch (javax.xml.rpc.ServiceException serviceException) {}
}


public String getEndpoint() {
  return _endpoint;
}

public void setEndpoint(String endpoint) {
  _endpoint = endpoint;
  if (__tradeWSServices != null)
    ((javax.xml.rpc.Stub)__tradeWSServices)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
  
}

public org.apache.geronimo.samples.daytrader.TradeWSServices getTradeWSServices() {
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices;
}

public org.apache.geronimo.samples.daytrader.MarketSummaryDataBeanWS getMarketSummary() throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.getMarketSummary();
}

public org.apache.geronimo.samples.daytrader.OrderDataBean buy(java.lang.String userID, java.lang.String symbol, double quantity, int orderProcessingMode) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.buy(userID, symbol, quantity, orderProcessingMode);
}

public org.apache.geronimo.samples.daytrader.OrderDataBean sell(java.lang.String userID, java.lang.Integer holdingID, int orderProcessingMode) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.sell(userID, holdingID, orderProcessingMode);
}

public void queueOrder(java.lang.Integer orderID, boolean twoPhase) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  __tradeWSServices.queueOrder(orderID, twoPhase);
}

public org.apache.geronimo.samples.daytrader.OrderDataBean completeOrder(java.lang.Integer orderID, boolean twoPhase) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.completeOrder(orderID, twoPhase);
}

public void cancelOrder(java.lang.Integer orderID, boolean twoPhase) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  __tradeWSServices.cancelOrder(orderID, twoPhase);
}

public void orderCompleted(java.lang.String userID, java.lang.Integer orderID) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  __tradeWSServices.orderCompleted(userID, orderID);
}

public org.apache.geronimo.samples.daytrader.OrderDataBean[] getOrders(java.lang.String userID) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.getOrders(userID);
}

public org.apache.geronimo.samples.daytrader.OrderDataBean[] getClosedOrders(java.lang.String userID) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.getClosedOrders(userID);
}

public org.apache.geronimo.samples.daytrader.QuoteDataBean createQuote(java.lang.String symbol, java.lang.String companyName, java.math.BigDecimal price) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.createQuote(symbol, companyName, price);
}

public org.apache.geronimo.samples.daytrader.QuoteDataBean getQuote(java.lang.String symbol) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.getQuote(symbol);
}

public org.apache.geronimo.samples.daytrader.QuoteDataBean[] getAllQuotes() throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.getAllQuotes();
}

public org.apache.geronimo.samples.daytrader.QuoteDataBean updateQuotePriceVolume(java.lang.String symbol, java.math.BigDecimal newPrice, double sharesTraded) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.updateQuotePriceVolume(symbol, newPrice, sharesTraded);
}

public org.apache.geronimo.samples.daytrader.HoldingDataBean[] getHoldings(java.lang.String userID) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.getHoldings(userID);
}

public org.apache.geronimo.samples.daytrader.HoldingDataBean getHolding(java.lang.Integer holdingID) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.getHolding(holdingID);
}

public org.apache.geronimo.samples.daytrader.AccountDataBean getAccountData(java.lang.String userID) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.getAccountData(userID);
}

public org.apache.geronimo.samples.daytrader.AccountProfileDataBean getAccountProfileData(java.lang.String userID) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.getAccountProfileData(userID);
}

public org.apache.geronimo.samples.daytrader.AccountProfileDataBean updateAccountProfile(org.apache.geronimo.samples.daytrader.AccountProfileDataBean profileData) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.updateAccountProfile(profileData);
}

public org.apache.geronimo.samples.daytrader.AccountDataBean login(java.lang.String userID, java.lang.String password) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.login(userID, password);
}

public boolean logout(java.lang.String userID) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.logout(userID);
}

public org.apache.geronimo.samples.daytrader.AccountDataBean register(java.lang.String userID, java.lang.String password, java.lang.String fullname, java.lang.String address, java.lang.String email, java.lang.String creditcard, java.math.BigDecimal openBalance) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.register(userID, password, fullname, address, email, creditcard, openBalance);
}

public org.apache.geronimo.samples.daytrader.RunStatsDataBean resetTrade(boolean deleteAll) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.resetTrade(deleteAll);
}

public boolean runDaCapoTrade(java.lang.String size, int threads, boolean soap) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.runDaCapoTrade(size, threads, soap);
}

public boolean initializeDaCapo(java.lang.String size) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.initializeDaCapo(size);
}

public boolean resetDaCapo(java.lang.String size, int threads) throws java.rmi.RemoteException{
  if (__tradeWSServices == null)
    _initTradeWSServicesProxy();
  return __tradeWSServices.resetDaCapo(size, threads);
}
}
