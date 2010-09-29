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

dojo.provide("dojotrader.widget.MarketSummary");

dojo.require("dojo.widget.*");
dojo.require("dojo.lang.timing.Timer");
dojo.require("dojotrader.widget.DaytraderProgressBar");
dojo.require("dojo.widget.Chart");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojotrader.widget.BaseDaytraderPane");

dojo.widget.defineWidget(
	"dojotrader.widget.MarketSummary", 
	[dojo.widget.HtmlWidget, dojotrader.widget.BaseDaytraderPane], 
	{
		templatePath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlMarketSummary.html"),
		widgetType: "MarketSummary",

		label: "Market Summary",
		chartEnabled: false,
		
		_refreshButton: null,
		_changeRefreshComboBox: null,
		_timer: null,
		_progressBar: null,
		_chart: null,
		_series: null,
		_chartIdx: 0,
		_previousMS: null,
		
		postCreate: function() {
			dojotrader.widget.MarketSummary.superclass.postCreate.call(this);
			
			this.refreshMarketSummary();
		},
		
		fillInTemplate: function(args, frag) {
			//dojotrader.widget.MarketSummary.superclass.fillInTemplate.call(this, args, frag);
		
			this._refreshButton = dojo.widget.createWidget("Button", {caption: "Refresh Now"}, this.buttonNode);
			dojo.event.connect(this._refreshButton, "onClick", this, "refreshMarketSummary");
			
			this._progressBar = dojo.widget.createWidget("dojotrader:DaytraderProgressBar", {cycle: true}, this.progressBar);
			this._progressBar.onComplete = dojo.lang.hitch(this, this.refreshMarketSummary);
			
			// create the chart and hide it (note: chart does not work on ie)
			// connsequently, only creating chart under mozilla
			if (dojo.render.html.moz && this.chartEnabled) {
				this._chart = dojo.widget.createWidget("dojo:Chart", {}, this.chart);
				this._series = new dojo.widget.Chart.DataSeries("SeriesA", "First series", "area");
				this._chart.series.push(this._series);
				this._chart.properties.height=120;
				this._chart.properties.width=220;
				this._chart.properties.padding={top:5, bottom: 20, left:30, right: 5};
				
				this._chart.properties.axes.x.range={min:0, max:100};
				this._chart.properties.axes.y.range={min:60, max:120};
				//this._chart.fillInTemplate(args, frag);
			}
			this.msHistory.style.display = "none";
			
			// setup subscription channel for external event listener
			dojo.event.topic.subscribe("/marketSummary", this, "handleExternalEvents");
		},
		
		handleExternalEvents: function (args) {
			if (args.event == "toggleHistory") {
				if (this.msHistory.style.display == "none" && this._chart != null)
					this.msHistory.style.display = "";
				else
					this.msHistory.style.display = "none";
			}
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

		startTimer: function () {
			var ms_timer = new dojo.animation.Timer(20000);
			ms_timer.onTick = dojo.lang.hitch(this,this.refreshMarketSummary);
			ms_timer.start();
		},
	
		refreshMarketSummary: function () {
			this.replaceTextNode(this.msLastUpdated, this.createShortTimeStampStr());
			
			// attempt async request to service
			dojo.io.bind({
			   	method:  "GET",
			   	//url: "/proxy/SoapProxy/getMarketSummary?format=json",
			   	url: "/daytraderProxy/doProxy/getMarketSummary",
   		    	mimetype: "text/json",
			   	load: dojo.lang.hitch(this,this.handleMarketSummary),
			   	error: dojo.lang.hitch(this, this.handleError),
			   	useCache: false,
                preventCache: true
			});
			
			//addMessage("Performing refresh of Market Summary");
			//dojo.event.topic.publish("/messages", {content: "Performing refresh of Market Summary"});
		},	
		
		toggleChart: function() {
			if (this.chart.style.display == "none")
				this.chart.style.display = "";
			else	
				this.chart.style.display = "none";
		},
	
		handleMarketSummary: function (type, data, evt) {

			var tsia = +data.getMarketSummaryReturn.TSIA;
			var open_tsia = +data.getMarketSummaryReturn.openTSIA;
			var volume = +data.getMarketSummaryReturn.volume;
			var change = eval((tsia - open_tsia)/open_tsia * 100);
			
			// update the chart if it exists
			if (this._chart != null) {
				//this._chart.series[0].add({x: this._chartIdx++, value: +(tsia.toFixed(0))});
				this._series.add({x: this._chartIdx++, value: +(tsia.toFixed(0))});
				this._chart.render();
			}
			
			if (this.previousMS && this.previousMS.TSIA != tsia) {
				dojo.lfx.html.highlight([this.msTSIA, this.msVolume, this.msChange],"red",2000,5).play();
			}
			
			this.replaceTextNode(this.msTSIA, tsia.toFixed(3));
			this.replaceTextNode(this.msVolume, volume);
				
			// in order to use CSS and div tags to control the images have
			// to encapsulate in a table to keep the image and value on the
			// same line
  		
  			// first, remove child nodes of attachpoint
				
			if (this.msChange.childNodes[0] != null) {
	  			this.msChange.removeChild(this.msChange.childNodes[0]);
	  		}
	  		this.addValueWithArrow(this.msChange, change, change.toFixed(2));
			
  			// handle the top gainers
  			
  			table = this.msGainerTable;
  			// delete old rows
  			while (table.rows.length > 2)
  				table.deleteRow(table.rows.length - 1);
  			
  			for (i = 0; i < data.getMarketSummaryReturn.topGainers.QuoteDataBean.length; i++) {
  				var row = table.insertRow(table.rows.length);
  				if (table.rows.length % 2 == 0)
  					dojo.html.addClass(row, "row-even");
  				else 	
					dojo.html.addClass(row, "row-odd");
  			
  				var cell = row.insertCell(0);
  				dojo.html.addClass(cell, "data-indent");
	  			var text = document.createTextNode(data.getMarketSummaryReturn.topGainers.QuoteDataBean[i].symbol);
				cell.appendChild(text);
			
				cell = row.insertCell(1);
				dojo.html.addClass(cell, "data-indent");
  				text = document.createTextNode(this.addCommas("$" + data.getMarketSummaryReturn.topGainers.QuoteDataBean[i].price));
				cell.appendChild(text);
			
				cell = row.insertCell(2);
				dojo.html.addClass(cell, "data-indent");
				
				this.addValueWithArrow(cell, data.getMarketSummaryReturn.topGainers.QuoteDataBean[i].change, data.getMarketSummaryReturn.topGainers.QuoteDataBean[i].change);
  			}
			
			// handle the top losers
			
			table = this.msLoserTable;
  			// delete old rows
  			while (table.rows.length > 2)
  				table.deleteRow(table.rows.length - 1);
			
			for (i = 0; i < data.getMarketSummaryReturn.topLosers.QuoteDataBean.length; i++) {
  				var row = table.insertRow(table.rows.length);
  				if (table.rows.length % 2 == 0)
  					dojo.html.addClass(row, "row-even");
  				else 	
					dojo.html.addClass(row, "row-odd");
					
				cell = row.insertCell(0);
				dojo.html.addClass(cell, "data-indent");
  				text = document.createTextNode(data.getMarketSummaryReturn.topLosers.QuoteDataBean[i].symbol);
				cell.appendChild(text);
				
				cell = row.insertCell(1);
				dojo.html.addClass(cell, "data-indent");
  				text = document.createTextNode(this.addCommas("$" + data.getMarketSummaryReturn.topLosers.QuoteDataBean[i].price));
				cell.appendChild(text);
			
				cell = row.insertCell(2);
				dojo.html.addClass(cell, "data-indent");
				
				this.addValueWithArrow(cell, data.getMarketSummaryReturn.topLosers.QuoteDataBean[i].change, data.getMarketSummaryReturn.topLosers.QuoteDataBean[i].change);
  			}
  			
  			this.previousMS = data.getMarketSummaryReturn;
		}
	}
);
