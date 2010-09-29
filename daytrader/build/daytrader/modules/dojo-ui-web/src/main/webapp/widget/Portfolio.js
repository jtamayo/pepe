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

dojo.provide("dojotrader.widget.Portfolio");

dojo.require("dojo.widget.*");
//TODO - can this be removed
dojo.require("dojo.widget.Tooltip");
dojo.require("dojo.storage.*");
dojo.require("dojo.collections.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojotrader.widget.DaytraderProgressBar");
dojo.require("dojotrader.widget.BaseDaytraderPane");

dojo.widget.defineWidget(
	"dojotrader.widget.Portfolio", 
	[dojo.widget.HtmlWidget, dojotrader.widget.BaseDaytraderPane], 
	{
		templatePath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlPortfolio.html"),
		widgetType: "Portfolio",

		label: "Portfolio",
		
		// backing cache for quote/holding data
		_holdingCache: null,
		_numUpdates: 0,
		_numSells: 0,
		_quoteCache: null,
		_toolTips: null,
		
		_updateType: "full",
		
		_refreshButton: null,
		_changeRefreshComboBox: null,
		_numQuotes: 0,
		_timer: null,
		_progressBar: null,
		
	
		postCreate: function() {
			dojotrader.widget.Portfolio.superclass.postCreate.call(this);
			
			dojo.event.topic.subscribe("/portfolio", this, "handleExternalEvents");
			
			this._holdingCache = new dojo.collections.Dictionary();
			this._quoteCache = new dojo.collections.Dictionary();
			this._toolTips = new dojo.collections.Dictionary();
		},
		
		handleExternalEvents: function(args) {
			if (args.event == "updateHoldings") {
				this._updateType = "partial";
				this.getHoldings();
			} else if (args.event == "getHoldings") {
				//alert("getHoldings");
				this._updateType = "full";
				this.resetCaches();
				this.getHoldings();
			}
		},
		
		fillInTemplate: function(args, frag) {
			this._refreshButton = dojo.widget.createWidget("Button", {caption: "Refresh Now"}, this.buttonNode);
			dojo.event.connect(this._refreshButton, "onClick", this, "refreshQuotes");
			
			this._progressBar = dojo.widget.createWidget("dojotrader:DaytraderProgressBar", {cycle: true}, this.progressBar);
			this._progressBar.onComplete = dojo.lang.hitch(this, this.refreshQuotes);
		
			if (this.debug) {
				var ref = dojo.widget.createWidget("Button", {caption: "Get Holdings"}, this.holdingsButtonNode);
				dojo.event.connect(ref, "onClick", this, "getHoldings");
				this.pfDebug.style.display = "";
			}
			
			ref = dojo.widget.createWidget("Button", {caption: "Sell Holdings"}, this.sellButtonNode);
			dojo.event.connect(ref, "onClick", this, "sellSelectedHoldings");
		},
		
		changeTimerSettings: function() {
			var value = this.refreshSelectBox.value;
			this._progressBar.stop();

			if (value == "manual") {
				this._progressBar.reset();	
			} else {
				this._progressBar.setPeriod(+(value) * 1000);
				this._progressBar.start();
			}
		},
		
		// ------------------------------------------------
		// Population (initial and adding) to holdings cache and table
		// ------------------------------------------------
		
		getHoldings: function() {
			var uid = dojo.storage.get("uid");
			if (uid == null || uid == "") {
				alert("Unable to find uid in storage, using uid:0");
				uid = "uid:0";
			}
			
			dojo.io.bind({
   			 	method:  "GET",
    			//url: "/proxy/SoapProxy/getHoldings?p1=" + uid + "&format=json",
      			url: "/daytraderProxy/doProxy/getHoldings?p1=" + uid,
      			mimetype: "text/json",
    			load: dojo.lang.hitch(this,this.handleGetHoldings),
    			error: dojo.lang.hitch(this,this.handleError),
    			useCache: false,
                preventCache: true
  			});
		},
		
		handleGetHoldings: function(type, data, evt) {
			this.populateHoldingsCache(data);
		},
		
		populateHoldingsCache: function(data) {
			if (data.getHoldingsReturn.HoldingDataBean) {
				for (idx=0; idx < data.getHoldingsReturn.HoldingDataBean.length; idx++) {
					// add holding to the holding cache if it does not exist
					var holding = data.getHoldingsReturn.HoldingDataBean[idx];
					
					if (!this._holdingCache.contains(holding.holdingID)) {
						this._numUpdates++;
						this._holdingCache.add(holding.holdingID, holding);
						this.getQuote(holding.quoteID);
					}					
				}
			}		
		},
		
		getQuote: function (quoteID) {		
			dojo.io.bind({
    			method:  "GET",
    			//url: "/proxy/SoapProxy/getQuote?p1=" + quote + "&format=json",
       			url: "/daytraderProxy/doProxy/getQuote?p1=" + quoteID,
       			mimetype: "text/json",
    			load: dojo.lang.hitch(this, this.handleQuote),
    			error: dojo.lang.hitch(this, this.handleError),
    			useCache: false,
                preventCache: true
  			});
		},
		
		handleQuote: function (type, data, event) {
			// add quote to the cache (does it really matter if i protect the data??)
			if (!this._quoteCache.contains(data.getQuoteReturn.symbol)) {
				this._quoteCache.add(data.getQuoteReturn.symbol, data.getQuoteReturn);
 			}
 			this._numUpdates--;
 			
 			if (this._numUpdates == 0) {
 				// holding/quote retrieval has completed
 				// now go off and build the DOM
 					
				if (this.pfHoldingsDisplay.style.display == "none") 
					this.pfHoldingsDisplay.style.display = "";
					
				this.addToHoldingsTable();
				this.replaceTextNode(this.msLastUpdated, this.createShortTimeStampStr());

 				//alert("Done: " + this._holdingCache.count + " - " + this._quoteCache.count);
 			}
		},
		
		addToHoldingsTable: function() {
			var list = this._holdingCache.getKeyList();
			for (idx=0; idx < list.length; idx++) {
				var holding = this._holdingCache.entry(list[idx]).value;
			
				// only add holding if it isn't in the table already
				if (this.pfHoldingsTable.innerHTML.indexOf(holding.holdingID + "-row") < 0) {
					//row = this.pfHoldingsTable.insertRow(this.pfHoldingsTable.rows.length);
					row = this.pfHoldingsTable.insertRow(1); 				
  					this.addHoldingToTable(holding, row);
				}
			}
			
			//this.redoTableColorScheme(this.pfHoldingsTable, 1, 1);
			this.addHoldingStatsToTable();
		},
		
		addHoldingStatsToTable: function() {
			var stats = this.calculateHoldingStats();
			
			if (this._holdingCache.count + 2 == this.pfHoldingsTable.rows.length) {
				// replace the stats with updated information
				//alert("table already exists - updating stats");
				
				row = this.pfHoldingsTable.rows[this.pfHoldingsTable.rows.length - 1];
  				this.replaceTextNode(row.cells[2], this.addCommas("$" + stats.purchase.toFixed(2)));
				this.replaceTextNode(row.cells[3], this.addCommas("$" + stats.current.toFixed(2)));
				if (row.cells[4].firstChild)
					row.cells[4].removeChild(row.cells[4].firstChild);
	  			this.addValueWithArrow(row.cells[4], stats.gain, this.addCommas("$" + stats.gain.toFixed(2)));
			} else {
				alert("i messed up!");
			}	
			
			// finally update the account summary with the holdings value
  			dojo.event.topic.publish("/accountSummary", {event: "updateHoldingsValue", value: stats.current});
		},
		
		calculateGain: function(holding, quote){
			purchase = +(holding.purchasePrice);
			current = +(quote.price);
			quantity = +(holding.quantity);
			
			return quantity * (current - purchase);
		},
		
		calculateHoldingStats: function() {
			var totalPurchase = 0;
			var totalCurrent = 0;
			var totalGain = 0;
			
			var list = this._holdingCache.getKeyList();
			for (idx=0; idx < list.length; idx++) {
				var holding = this._holdingCache.entry(list[idx]).value;
				quote = this._quoteCache.entry(holding.quoteID).value;
		
				totalPurchase += +(holding.quantity) * +(holding.purchasePrice);
  				totalCurrent += +(holding.quantity) * +(quote.price);
  			}
  			totalGain = totalCurrent - totalPurchase;
		
			return {purchase: totalPurchase, current: totalCurrent, gain: totalGain};
		},
		
		addHoldingToTable: function(holding, row) {
			row.id = holding.holdingID + "-row";
			
			if (this.pfHoldingsTable.rows.length % 2 == 0)
				row.className = "row-even";
			else
				row.className = "row-odd";
				
			cell = row.insertCell(0);			
			//TODO - Stan this is the attempt to add Tooltip
			tooltipText = "HoldingID: " + holding.holdingID + " PurchaseDate: " + holding.purchaseDate;
			cell.innerHTML = "<input type=\"checkbox\" name=\"holdings-chkbox\" id=\"" + holding.holdingID + "\" dojoType=\"Checkbox\" />" 
									+ "<span></span>";
			span = cell.childNodes[1];
			tooltip = dojo.widget.createWidget("ToolTip", {id: "tip-"+holding.holdingID, connectId: holding.holdingID + "-row", toggle: "explode", caption: tooltipText}, span);
			this._toolTips.add(holding.holdingID, tooltip);
		
			cell = row.insertCell(1);
			this.appendTextNode(cell, holding.quoteID);
				
			cell = row.insertCell(2);
			this.appendTextNode(cell, holding.quantity);
	
			cell = row.insertCell(3);
			this.appendTextNode(cell, this.addCommas("$" + holding.purchasePrice));
				
			// handle the current quote price and calculate gain/loss
			quote = this._quoteCache.entry(holding.quoteID).value;
	
			cell = row.insertCell(4);
			this.appendTextNode(cell, this.addCommas("$" + quote.price));

			cell = row.insertCell(5);
	  		gain = this.calculateGain(holding, quote);
	  		this.addValueWithArrow(cell, gain, this.addCommas("$" + gain.toFixed(2)));
		},
		
 		
 		// ------------------------------------------------
		// Sell holdings
		// ------------------------------------------------

		sellSelectedHoldings: function() {
			// figure out which holdings were selected
			var checkboxes = document.getElementsByName("holdings-chkbox");
			var uid = dojo.storage.get("uid");
			if (uid == null || uid == "") {
				alert("Unable to find uid in storage, using uid:0");
				uid = "uid:0";
			}
	
			for (idx=0; idx < checkboxes.length; idx++) {
				if (checkboxes[idx].checked) {
					// use numSells variable to "batch" requests 
					this._numSells++;
				
					dojo.io.bind({
    					method:  "GET",
    					//url: "/proxy/SoapProxy/sell?p1=" + uid + "&p2=" + checkboxes[idx].id + "&p3=0&format=json",
      					url: "/daytraderProxy/doProxy/sell?p1=" + uid + "&p2=" + checkboxes[idx].id + "&p3=0",
      					mimetype: "text/json",
    					load: dojo.lang.hitch(this, this.handleSellHolding),
    					error: dojo.lang.hitch(this, this.handleError),
    					useCache: false,
                		preventCache: true,
                		holdingid: checkboxes[idx].id
  					});
				}
			}
		},

		handleSellHolding: function(type, data, event, xhr) {
			var order = data.sellReturn;
			var message = "Sell order completed (OrderId: " + order.orderID + " - $" + order.price + ")"
			//alert("HoldingID: " + xhr.holdingid);
	
			//addMessage(message);
			//displayStatusMessage(message);
			dojo.event.topic.publish("/messages", {event: "addMessage", message: message});
			
			// remove holding from cache
			this._holdingCache.remove(xhr.holdingid);
			
			// remove the tooltip
			tooltip = this._toolTips.entry(xhr.holdingid).value;
			tooltip.destroy();
			
			// remove row from table
			rows = this.pfHoldingsTable.rows;
			for (idx = 1; idx < rows.length - 1; idx++) {
				row = rows[idx];
				holdingID = row.id.substring(0,row.id.indexOf("-"));
				if (holdingID == xhr.holdingid)
					this.pfHoldingsTable.deleteRow(idx);
			}
			
			this._numSells--;
			
			// don't refresh holdings table until all sells are completed
			if (this._numSells == 0) {
				//this.resetCaches();
				//this.getHoldings();
				this.addHoldingStatsToTable();
				this.redoTableColorScheme(this.pfHoldingsTable, 1, 1);
				this.replaceTextNode(this.msLastUpdated, this.createShortTimeStampStr());
			}
		},
		
		// ------------------------------------------------
		// Update current quote prices
		// ------------------------------------------------
		
		refreshQuotes: function () {
			this._numQuotes = 0;
			
			keys = this._quoteCache.getKeyList();
			//alert("Refresh Quotes: " + keys);
			for (idx = 0; idx < keys.length; idx++) {
				//alert(keys[idx]);
				this.updateQuote(keys[idx]);
			}
			
			this.replaceTextNode(this.msLastUpdated, this.createShortTimeStampStr());
		},
		
		updateQuote: function (symbol) {	
				dojo.io.bind({
    				method:  "GET",
    				//url: "/proxy/SoapProxy/getQuote?p1=" + symbol.value + "&format=json",
       				url: "/daytraderProxy/doProxy/getQuote?p1=" + symbol,
       				mimetype: "text/json",
    				load: dojo.lang.hitch(this, this.handleUpdateQuote),
    				error: dojo.lang.hitch(this, this.handleError),
    				useCache: false,
                	preventCache: true
  				});
		},
		
		handleUpdateQuote: function (type, data, evt) {	
			//alert("handleQuoteUpate");			
			newQuote = data.getQuoteReturn;
			oldQuote = this._quoteCache.entry(newQuote.symbol).value;
			
			// replace quote in the quote cache
			if (oldQuote.price != newQuote.price || oldQuote.change != newQuote.change) {
				//alert("replaced item in cache");
				this._quoteCache.add(newQuote.symbol, newQuote);
				this.updateQuoteRow(newQuote);
 			}
 			
 			this._numQuotes++;
 			
 			if (this._quoteCache.count == this._numQuotes) {
 				//alert("Refresh Complete: " + this._quoteCache.count + " - " + this._numQuotes);
 				//this.addHoldingStatsToTable();
 			}
 		},
 		
 		updateQuoteRow: function (quote) {
 			//alert("updateQuoteRow");
 			rows = this.pfHoldingsTable.rows;
		
			for (idx=1; idx < rows.length; idx++) {
				//alert(rows[idx].cells[1].innerHTML);
				row = rows[idx];
				if (row.cells[1].innerHTML == quote.symbol) {
					dojo.lfx.html.highlight(row,"red",2000,5).play();	
					this.replaceTextNode(row.cells[4], this.addCommas("$" + quote.price));
					
					holdingID = row.id.substring(0,row.id.indexOf("-"));
					holding = this._holdingCache.entry(holdingID).value;
					gain = this.calculateGain(holding, quote);
					row.cells[5].removeChild(row.cells[5].firstChild);
	  				this.addValueWithArrow(row.cells[5], gain, this.addCommas("$" + gain.toFixed(2)));				
				}
			}
			this.addHoldingStatsToTable();
 		},
		
		resetCaches: function () {
			//alert("resetCaches");
			this._holdingCache.clear();
			//this._quoteCache.clear();
			//this._numUpdates = 0;
			
			var list = this._toolTips.getKeyList();
			for (idx=0; idx < list.length; idx++) {
				tooltip = this._toolTips.entry(list[idx]).value;
				tooltip.destroy();
			}
			this._toolTips.clear();
			
			var count = this.pfHoldingsTable.rows.length - 1;
			for (idx=1; idx < count; idx++) {
				this.pfHoldingsTable.deleteRow(1);
			}
			
			this.addHoldingStatsToTable();
			this.resetHoldingsPane();
		},

		resetHoldingsPane: function () {
			// this method is used to cleanup the holdings pane when a user logs out
			var display = this.pfHoldingsDisplay;
			if (display.style.display == "")
				display.style.display = "none";
		}
	}
);
