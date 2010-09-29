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

dojo.provide("dojotrader.widget.AccountSummary");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojotrader.widget.BaseDaytraderPane");

dojo.widget.defineWidget(
	"dojotrader.widget.AccountSummary", 
	[dojo.widget.HtmlWidget, dojotrader.widget.BaseDaytraderPane], 
	{
		templatePath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlAccountSummary.html"),
		widgetType: "MarketSummary",

		label: "Account Summary",
		
		_holdingsValue: 0,
		_accountSummary: null,
		
		postCreate: function() {
			dojotrader.widget.Messages.superclass.postCreate.call(this);
			
			dojo.event.topic.subscribe("/accountSummary", this, "handleExternalEvents");
		},
		
		fillInTemplate: function(args, frag) {
			if (this.debug) {
				var ref = dojo.widget.createWidget("Button", {id: "somebutton", caption: "Get Account Summary"}, this.buttonNode);
				dojo.event.connect(ref, "onClick", this, "getAccountSummary");
				this.asDebug.style.display = "";
			}
		},
		
		handleExternalEvents: function (args) {
			if (args.event == "updateHoldingsValue") {
				this.updateHoldingsValue(args.value);
				this.getAccountSummary();
			} else if (args.event == "getAccountSummary") {
				this.getAccountSummary();
			}
		},
		
		updateHoldingsValue: function (value) {
			this._holdingsValue = +(value);
		},
	
		getAccountSummary: function () {
			var uid = dojo.storage.get("uid");
			if (uid == null || uid == "") {
				alert("Unable to find uid in storage, using uid:0");
				uid = "uid:0";
			}		
	
			dojo.io.bind({
    			method:  "GET",
   			 	//url: "/proxy/SoapProxy/getAccountData?p1=" + uid + "&format=json",
      			url: "/daytraderProxy/doProxy/getAccountData?p1=" + uid,
      			mimetype: "text/json",
    			load: dojo.lang.hitch(this, this.handleAccountSummaryData),
    			error: dojo.lang.hitch(this, this.handleError),
    			useCache: false,
                preventCache: true
  			});
		},

		handleAccountSummaryData: function (type, data, evt) {
			this._accountSummary = data.getAccountDataReturn;
			
			var balance = +this._accountSummary.balance;
			var openBalance = +this._accountSummary.openBalance;
			var total = balance + this._holdingsValue;
			
			this.replaceTextNode(this.balance, this.addCommas("$" + balance.toFixed(2)));
			this.replaceTextNode(this.openBalance, this.addCommas("$" + openBalance.toFixed(2)));
			this.replaceTextNode(this.holdingsValue, this.addCommas("$" + this._holdingsValue.toFixed(2)));
			this.replaceTextNode(this.total, this.addCommas("$" + total.toFixed(2)));
			this.replaceTextNode(this.change, ((total - openBalance)/openBalance * 100).toFixed(2) + "%");
		}
	}
);
