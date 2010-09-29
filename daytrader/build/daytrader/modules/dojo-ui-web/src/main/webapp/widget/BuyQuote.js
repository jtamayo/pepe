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

dojo.provide("dojotrader.widget.BuyQuote");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.ValidationTextbox");
dojo.require("dojo.collections.*");
dojo.require("dojo.storage.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojotrader.widget.DaytraderProgressBar");
dojo.require("dojotrader.widget.BaseDaytraderPane");


dojo.widget.defineWidget(
	"dojotrader.widget.BuyQuote", 
	[dojo.widget.HtmlWidget, dojotrader.widget.BaseDaytraderPane], 
	{
		templatePath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlBuyQuote.html"),
		widgetType: "BuyQuote",
		
		label: "Buy Stock",
		historySize: 6,
		
		_symbolTextBox: null,
		_numBuys: 0,
		
		_refreshButton: null,
		_changeRefreshComboBox: null,
		_numQuotes: 0,
		_timer: null,
		_progressBar: null,
		
		_quoteCache: null,
			
		postCreate: function() {
			dojotrader.widget.BuyQuote.superclass.postCreate.call(this);
			
			dojo.event.topic.subscribe("/buyQuote", this, "handleExternalEvents");
			
			this._quoteCache = new dojo.collections.Dictionary();
		},
		
		handleExternalEvents: function(args) {
			if (args.event == "clearQuotes")
				this.clearQuotes();
		},
		
		fillInTemplate: function(args, frag) {
			this._refreshButton = dojo.widget.createWidget("Button", {caption: "Refresh Now"}, this.buttonNode);
			dojo.event.connect(this._refreshButton, "onClick", this, "refreshQuotes");
			
			this._progressBar = dojo.widget.createWidget("dojotrader:DaytraderProgressBar", {cycle: true}, this.progressBar);
			this._progressBar.onComplete = dojo.lang.hitch(this, this.refreshQuotes);
		
			var ref = dojo.widget.createWidget("Button", {caption: "Get Quote"}, this.quoteButtonNode);
			dojo.event.connect(ref, "onClick", this, "getQuoteFromInput");
			
			ref = dojo.widget.createWidget("Button", {caption: "Buy Quote"}, this.buyButtonNode);
			dojo.event.connect(ref, "onClick", this, "buySelectedQuotes");
			
			this._symbolTextBox = dojo.widget.createWidget("ValidationTextBox", {type: "text", validation: "false", size: "5"}, this.textBoxNode);
		},
		
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
		
		
		
		changeTimerSettings: function() {
			//var value = this._changeRefreshComboBox.getValue();
			var value = this.refreshSelectBox.value;
			this._progressBar.stop();

			if (value == "manual") {
				this._progressBar.reset();	
			} else {
				this._progressBar.setPeriod(+(value) * 1000);
				this._progressBar.start();
			}
		},

		getQuoteFromInput: function () {
			var symbol = this._symbolTextBox.getValue();
	
			// determine if the symbol is already in the list
			var table = this.bqQuoteTable;
			var inTable = table.innerHTML.indexOf(">" + symbol + "<");
	
			if (symbol != "" && inTable == -1) {		
				dojo.io.bind({
    				method:  "GET",
    				//url: "/proxy/SoapProxy/getQuote?p1=" + symbol.value + "&format=json",
       				url: "/daytraderProxy/doProxy/getQuote?p1=" + symbol,
       				mimetype: "text/json",
    				load: dojo.lang.hitch(this, this.handleQuote),
    				error: dojo.lang.hitch(this, this.handleError),
    				useCache: false,
                	preventCache: true
  				});
  			}
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
 				//this.replaceTextNode(this.msLastUpdated, this.createShortTimeStampStr());
 			}
 		},
 		
 		updateQuoteRow: function (quote) {
 			//alert("updateQuoteRow");
 			rows = this.bqQuoteTable.rows;
		
			for (idx=1; idx < rows.length; idx++) {
				//alert(rows[idx].cells[1].innerHTML);
				if (rows[idx].cells[1].innerHTML == quote.symbol) {
					row = rows[idx];
					
					this.replaceTextNode(row.cells[2], this.addCommas("$" + quote.price));
					this.replaceTextNode(row.cells[3], quote.change);
					this.replaceTextNode(row.cells[4], quote.volume);
					dojo.lfx.html.highlight(row,"red",2000,5).play();
				}
			} 		
 		},
 		
 		updateQuotesTable: function () {
 			//keys = this._quoteCache.getKeyList();
 			rows = this.bqQuoteTable.rows;

			for (idx = 1; idx < rows.length; idx++) {
				alert(rows[idx].cells[1].firstChild.nodeValue);
				quote = this._quoteCache.entry(rows[idx].cells[1].firstChild.nodeValue).value;
				alert(quote);
				row = rows[idx];
				//alert(rows[idx].cells[1].firstChild.nodeValue);
				//this.updateQuote(keys[idx]);
				
				this.replaceTextNode(row.cells[2], this.addCommas("$" + quote.price));
				this.replaceTextNode(row.cells[3], quote.change);
				this.replaceTextNode(row.cells[4], quote.volume);
			}
 		},

		handleQuote: function (type, data, evt) {
			// unhide the table
			if (this.bqQuotesDisplay.style.display == "none")
				this.bqQuotesDisplay.style.display = "";
			
			this.replaceTextNode(this.msLastUpdated, this.createShortTimeStampStr());
				
			// add quote to the quote cache
			if (!this._quoteCache.contains(data.getQuoteReturn.symbol)) {
				this._quoteCache.add(data.getQuoteReturn.symbol, data.getQuoteReturn);
 			}
	
			// add the information to the table
			var row = this.bqQuoteTable.insertRow(1);
			cell = row.insertCell(0);
			cell.innerHTML = "<input size=\"5\" id=\"" + data.getQuoteReturn.symbol + "\" name=\"buyStk-amount\" dojoType=\"ValidationTextBox\" type=\"text\" required=\"false\" />";
	
			cell = row.insertCell(1);
			this.appendTextNode(cell, data.getQuoteReturn.symbol);
	
			cell = row.insertCell(2);
			this.appendTextNode(cell, this.addCommas("$" + data.getQuoteReturn.price));
	
			cell = row.insertCell(3);
			this.appendTextNode(cell, data.getQuoteReturn.change);
	
			cell = row.insertCell(4);
			this.appendTextNode(cell, data.getQuoteReturn.volume);

			// handle the table style and history
			if (this.bqQuoteTable.rows.length == 2) {
				row.className = "row-even";
			} else {
				var prev = this.bqQuoteTable.rows[2];
				if (prev.className == "row-even")
					row.className = "row-odd";
				else
					row.className = "row-even";
			}

			if (this.bqQuoteTable.rows.length > this.historySize + 1) {
				this.bqQuoteTable.deleteRow(this.bqQuoteTable.rows.length - 1);
			}	
		},

		buySelectedQuotes: function () {
			// figure out which holdings were selected
			var textboxes = document.getElementsByName("buyStk-amount");
			var uid = dojo.storage.get("uid");
			if (uid == null || uid == "") {
				alert("Unable to find uid in storage, using uid:0");
				uid = "uid:0";
			}
	
			// getCompletionDate in soap proxy causing problems because it returns null
	
			//for (var idx=0; idx < textboxes.length; idx++) {
			for (var idx = textboxes.length - 1; idx > -1; idx--) {
				if (textboxes[idx].value != "") {
					// use numBuys variable to "batch" requests
					this._numBuys++;
				
					dojo.io.bind({
    					method:  "GET",
    					//url: "/proxy/SoapProxy/buy?p1=" + uid + "&p2=" + textboxes[idx].id + "&p3=" + textboxes[idx].value + "&p4=0&format=json",
      					url: "/daytraderProxy/doProxy/buy?p1=" + uid + "&p2=" + textboxes[idx].id + "&p3=" + textboxes[idx].value + "&p4=0",
      					mimetype: "text/json",
    					load: dojo.lang.hitch(this, this.handleBuyStocks),
    					error: dojo.lang.hitch(this, this.handleError),
    					useCache: false,
                		preventCache: true
  					});
  					
  					// remove row from list and refresh the color scheme of the remaining rows
  					// only redraw the color scheme after all have finished
  					var row = textboxes[idx].parentNode.parentNode;
  					row.parentNode.removeChild(row);
				}
			}
		},

		handleBuyStocks: function (type, data, event) {
			var order = data.buyReturn;
			var message = "Buy order completed (OrderId: " + order.orderID + " - $" + order.price + ")";	

			dojo.event.topic.publish("/messages", {event: "addMessage", message: message});
			
			this._numBuys--;
			
			if (this._numBuys == 0) {
				//redraw the table color scheme
				this.redoTableColorScheme(this.bqQuoteTable, 1, 0);
				if (this.bqQuoteTable.rows.length == 1) 
					this.bqQuotesDisplay.style.display = "none";
				
				// don't refresh holdings table until all buys are completed
				dojo.event.topic.publish("/portfolio", {event: "updateHoldings"});
			}
		},

		clearQuotes: function () {
			// this method is used to cleanup the buy stocks pane when a user logs out
			
			// clear text box
			this._symbolTextBox.setValue("");
	
			// remove old rows
			for (idx=0; idx < this.bqQuoteTable.rows.length - 1;) {
				this.bqQuoteTable.deleteRow(1);
			}
	
			// hide the table
			if (this.bqQuotesDisplay.style.display == "")
				this.bqQuotesDisplay.style.display = "none";
		}
	}
);
