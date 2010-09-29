<%--
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
--%>

dojo.provide("dojotrader.widget.Context");
dojo.require("dojo.collections.*");
dojo.require("dojo.lang.timing.Timer");

dojotrader.widget.Context = function(userID){
	this._user = userID;
	this._quotesCache = new dojo.collections.Dictionary();
	this._quotesToUpdate = 0;
	this._updateTimer = null;
	
	
	this.startUpdateTimer = function(interval) {
		this._updateTimer = new dojo.lang.timing.Timer(interval);
		this._updateTimer.onTick = dojo.lang.hitch(this,this._updateQuotesCache);
		this._updateTimer.start();
	};
	
	this.stopUpdateTimer = function() {
		this._updateTimer.stop();
		this._updateTimer = null;
	};
	
	this._addQuoteToCache = function(quote) {
		this._quotesCache.add(quote.symbol, quote);
		//this.onQuoteAddComplete(quote);
	};
	
	this.getQuoteFromCache = function(symbol) {
		//alert("getQuoteFromCache");
		value = null;
		if (this._quotesCache.contains(symbol)){
			value = this._quotesCache.entry(symbol).value;
		} else {
			this._getQuoteFromServer(symbol);
		}
		
		return value;
	};
	
	this._getQuoteFromServer = function(symbol) {
		//alert("_getQuoteFromServer");
		dojo.io.bind({
    		method:  "GET",
    		//url: "/proxy/SoapProxy/getQuote?p1=" + symbol.value + "&format=json",
       		url: "/daytraderProxy/doProxy/getQuote?p1=" + symbol,
       		mimetype: "text/json",
    		load: dojo.lang.hitch(this, this._handleQuote),
    		error: dojo.lang.hitch(this, this._handleError),
    		useCache: false,
            preventCache: true
  		});
  	};
  	
  	this._getQuoteFromServerForUpdate = function(symbol) {
		//alert("_getQuoteFromServer");
		dojo.io.bind({
    		method:  "GET",
    		//url: "/proxy/SoapProxy/getQuote?p1=" + symbol.value + "&format=json",
       		url: "/daytraderProxy/doProxy/getQuote?p1=" + symbol,
       		mimetype: "text/json",
    		load: dojo.lang.hitch(this, this._handleQuoteUpdate),
    		error: dojo.lang.hitch(this, this._handleError),
    		useCache: false,
            preventCache: true
  		});
  	};
  	
  	this._handleQuote = function(type, data, evt) {
  		//alert("_handleQuote");
  		this._addQuoteToCache(data.getQuoteReturn);
  		this.onQuoteAddComplete(data.getQuoteReturn);
  		dojo.event.topic.publish("/quotes", {action: "add", quote: data.getQuoteReturn});
  	};
  	
  	this._handleQuoteUpdate = function(type, data, evt) {
  		//alert("_handleQuote");
  		newQuote = data.getQuoteReturn;
  		
  		// check for quote equality - only update if they are different
  		oldQuote = this._quotesCache.entry(newQuote.symbol).value;
  		if (oldQuote.price != newQuote.price || oldQuote.change != newQuote.change) {
  			this._addQuoteToCache(newQuote);
  			dojo.event.topic.publish("/quotes", {action: "update", quote: newQuote});
  		}
  		
  		this._quotesToUpdate++;
 			
 		if (this._quotesCache.count == this._quotesToUpdate) {
 			//alert("Refresh Complete: " + this._quotesCache.count + " - " + this._quotesToUpdate);
 			this.onQuotesUpdateComplete();
 		}
  	};
  	
  	this._updateQuotesCache = function() {
  		this._quotesToUpdate = 0;
			
		keys = this._quotesCache.getKeyList();
		for (idx = 0; idx < keys.length; idx++) {
			this._getQuoteFromServerForUpdate(keys[idx]);
		}
  	};
  	
  	this.onQuotesUpdateComplete = function() {};
  	
  	this.onQuoteAddComplete = function() {}
};
