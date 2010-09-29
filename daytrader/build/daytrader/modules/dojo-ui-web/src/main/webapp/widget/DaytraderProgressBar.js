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

dojo.provide("dojotrader.widget.DaytraderProgressBar");

dojo.require("dojo.widget.*");
dojo.require("dojo.lang.timing.Timer");

dojo.widget.defineWidget(
	"dojotrader.widget.DaytraderProgressBar", 
	dojo.widget.HtmlWidget, 
	{
		templatePath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlProgressBar.html"),
		templateCssPath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlProgressBar.css"),
		widgetType: "ProgressBar",
		period: "",
		cycle: false,
		_timer: null,
		_tickInterval: 0,
		_tickCount: 0,
		
		postCreate: function() {
			//alert(this.interval);
			this._tickInterval = (+(this.period) / 6).toFixed(0);	
		},
		
		fillInTemplate: function(args, frag) {
			//alert(this.interval);
		},
		
		incrementBars: function() {
			if (this._tickCount < 6) {
				this.progressBarRow.cells[this._tickCount].firstChild.style.backgroundColor = "green";
				this._tickCount++;
			} else {
				if (!this.cycle) {
					this._timer.stop();
					this._timer = null;
				}
				
				this.reset();
				this._tickCount = 0;
				this.onComplete();
			}
		},
		
		onComplete: function() {},
		
		start: function () {
			this.reset();
			this._timer = new dojo.lang.timing.Timer(this._tickInterval);
			//this._timer = new dojo.animation.Timer(this._tickInterval);
			this._timer.onTick = dojo.lang.hitch(this,this.incrementBars);
			this.progressBarUpdate.style.visibility = "visible";
			this._timer.start();
		},
		
		stop: function () {
			if (this._timer != null) {
				this._timer.stop();
				this.progressBarUpdate.style.visibility = "hidden";
				this._timer = null;
				this._tickCount = 0;
			}
		},
		
		setPeriod: function (value) {
			this.period = value;
			this._tickInterval = (+(value) / 6).toFixed(0);
		},
		
		setCycle: function (value) {
			this.cycle = value;
		},
		
		reset: function () {
			for (idx=0; idx < this.progressBarRow.cells.length; idx++) {
				this.progressBarRow.cells[idx].firstChild.style.backgroundColor = "#eeeeee";
			}
		}
	}
);
