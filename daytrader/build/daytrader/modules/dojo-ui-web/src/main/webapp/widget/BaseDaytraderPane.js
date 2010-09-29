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

dojo.provide("dojotrader.widget.BaseDaytraderPane");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");

dojo.widget.defineWidget(
	"dojotrader.widget.BaseDaytraderPane",
	dojo.widget.HtmlWidget, {
		templatePath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlBaseTemplate.html"),
		templateCssPath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlBase.css"),
		widgetType: "BaseDaytraderPane",

		label: "Base DayTrader Pane",
		open: true,
		debug: false,
		
		postCreate: function() {
			//alert("BaseDaytraderPane>>postCreate - " + this.templateCssPath);
		
			if (!this.open) {
				with(this.containerNode.style) {
					visibility = "hidden";
					position = "absolute";
				}
				//dojo.lfx.wipeOut(this.containerNode,0).play();
			}
		},
		
		fillInTemplate: function(args, frag) {
			//alert("BaseDaytraderPane>>fillInTemplate - " + this.templateCssPath);
		},
		
		handleExternalEvents: function (args) {
			if (args.event == "toggleHistory") {
				if (this.msHistory.style.display == "none")
					this.msHistory.style.display = "";
				else
					this.msHistory.style.display = "none";
			}
		},
		
		onLabelClick: function() {
			if (this.open) {
				var callback = function(node, anim) {
					dojo.html.setStyle(node, "position", "absolute");
				};
				dojo.lfx.fadeOut(this.containerNode, 500).play();
				dojo.lfx.wipeOut(this.containerNode, 600, 5, callback).play();
				dojo.html.setClass(this.paneControl, "daytrader-maximize-image");
				this.open=false;
			} else {
				dojo.html.setStyle(this.containerNode, "position", "relative");
				dojo.lfx.wipeIn(this.containerNode, 500).play();
				dojo.lfx.fadeIn(this.containerNode, 600).play();
				dojo.html.setClass(this.paneControl, "daytrader-minimize-image");
				this.open=true;
			}
		},
	
		createTimeStampStr: function() {
			var dataStr = "";
			var currentTime = new Date()
			var month = currentTime.getMonth() + 1
			var day = currentTime.getDate() 
			var year = currentTime.getFullYear()
		
			dateStr = month + "/" + day + "/" + year;

			var hours = currentTime.getHours()
			var minutes = currentTime.getMinutes()
			if (minutes < 10)
				minutes = "0" + minutes;
			var seconds = currentTime.getSeconds();
			if (seconds < 10)
				seconds = "0" + seconds;
	
			dateStr += " " + hours + ":" + minutes + ":" + seconds + " "; 
	
			if(hours > 11){
				dateStr += "PM";
			} else {
				dateStr += "AM";
			}
	
			return dateStr;
		},
		
		createShortTimeStampStr: function() {
			var dataStr = "";
			var currentTime = new Date();

			var hours = currentTime.getHours();
			var minutes = currentTime.getMinutes();
			if (minutes < 10)
				minutes = "0" + minutes;
			var seconds = currentTime.getSeconds();
			if (seconds < 10)
				seconds = "0" + seconds;
	
			dateStr = hours + ":" + minutes + ":" + seconds + " "; 
	
			if(hours > 11){
				dateStr += "PM";
			} else {
				dateStr += "AM";
			}
	
			return dateStr;
		},

	
		handleError: function (type, error) {
			dlg = dojo.widget.manager.getWidgetById("errorMessage");
			dlg.show();
			alert(error);
		},
		
		appendTextNode: function (parent, text) {
			var textNode = document.createTextNode(text);
			parent.appendChild(textNode);
		},
		
		replaceTextNode: function (parent, text) {
			var textNode = document.createTextNode(text);
			
			if (parent.childNodes[0] != null)
				parent.replaceChild(textNode, parent.childNodes[0]);
			else
				parent.appendChild(textNode);
		},
		
		addValueWithArrow: function (parent, value, text) {
	  		table = document.createElement("TABLE");
	  		dojo.html.setClass(table, "dt-table-no-padding");
	  		parent.appendChild(table);
	  		row = table.insertRow(0);
	  		textCell = row.insertCell(0);
	  		imageCell = row.insertCell(1);
	  		
	  		// append the text value
	  		txtNode = document.createTextNode(text);
  			textCell.appendChild(txtNode);
  			// create the div-based image 
  			imageNode = document.createElement("DIV");
  			imageCell.appendChild(imageNode);

  			// set the style characteristics
  			if (value < 0) {
  				dojo.html.setClass(imageNode, "arrowdown");
  				textCell.style.color = "red";
  			} else {
  				dojo.html.setClass(imageNode, "arrowup");
  				textCell.style.color = "green";
  			}
		},
		
		addCommas: function (nStr) {
			nStr += '';
			x = nStr.split('.');
			x1 = x[0];
			x2 = x.length > 1 ? '.' + x[1] : '';
			var rgx = /(\d+)(\d{3})/;
			while (rgx.test(x1)) {
				x1 = x1.replace(rgx, '$1' + ',' + '$2');
			}
			return x1 + x2;
		},

		removeCommas: function (nStr) {
			nStr=nStr.replace(/,/g,"");
			nStr=nStr.replace(/\s/g,"");
			return nStr;
		},
		
		redoTableColorScheme: function (table, headerOffset, footerOffset) {
			count = table.rows.length;
			if (footerOffset > 0)
				count = count - footerOffset;
			
			for (idx=headerOffset; idx < count; idx++) {
				var row = table.rows[idx];
				if (idx % 2 == 0)
					row.className = "row-even";
				else
					row.className = "row-odd";
			}
		}
		
	}
);
