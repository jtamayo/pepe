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

dojo.provide("dojotrader.widget.LoginLogout");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.ValidationTextbox");
dojo.require("dojo.storage.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.PopupContainer");
dojo.require("dojotrader.widget.BaseDaytraderPane");

dojo.widget.defineWidget(
	"dojotrader.widget.LoginLogout", 
	[dojo.widget.HtmlWidget, dojotrader.widget.BaseDaytraderPane], 
	{
		templatePath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlLoginLogout.html"),
		//templateCssPath: dojo.uri.dojoUri("/dojotrader/widget/templates/HtmlLoginLogout.css"),
		widgetType: "LoginLogout",

		_register: false,
		
		_doLoginButton: "",
		_doShowRegisterButton: "",
		
		_registerContainer: "",
		
		_useridTxtBox: "",
		_passwordTxtBox: "",
		_regUseridTxtBox: "",
		_regPasswordTxtBox: "",
		_regConfirmTxtBox: "",
		_regFullnameTxtBox: "",
		_regAddressTxtBox: "",
		_regEmailTxtBox: "",
		_regCreditcardTxtBox: "",
		_regBalanceTxtBox: "",
		
		
		postCreate: function() {
			dojotrader.widget.LoginLogout.superclass.postCreate.call(this);
			
			//alert("in postCreate");
		},
		
		fillInTemplate: function(args, frag) {
			this._doLoginButton = dojo.widget.createWidget("Button", {caption: "Login"}, this.loginButtonNode);
			dojo.event.connect(this._doLoginButton, "onClick", this, "onLoginClick");
	
			this._useridTxtBox = dojo.widget.createWidget("ValidationTextBox", {type: "text", validation: "false", size: "10", value: "uid:0"}, this.loginUseridTextBoxNode);
			this._passwordTxtBox = dojo.widget.createWidget("ValidationTextBox", {type: "password", validation: "false", size: "10", value: "xxx"}, this.loginPasswordTextBoxNode);
			
			this._doShowRegisterButton = dojo.widget.createWidget("Button", {caption: "Register"}, this.showRegisterButtonNode);
			dojo.event.connect(this._doShowRegisterButton, "onClick", this, "onShowRegisterClick");
			
			ref = dojo.widget.createWidget("Button", {}, this.logoutButtonNode);
			dojo.event.connect(ref, "onClick", this, "onLogoutClick");
			
			this._registerContainer = dojo.widget.createWidget("PopupContainer", {toggle: "plain", toggleDuration: 150});
			this.domNode.appendChild(this._registerContainer.domNode);
			this._registerContainer.domNode.appendChild(this.registerNode);
			// need the following to handle when the user clicks outside the registration box
			// - either of the two will work
			//dojo.event.connect(dojo.widget.PopupManager, "onClick", this, "onCancelRegisterClick");
			dojo.event.connect(this._registerContainer, "close", this, "onCancelRegisterClick");
			
			var ref = dojo.widget.createWidget("Button", {}, this.registerButtonNode);
			dojo.event.connect(ref, "onClick", this, "onRegisterClick");
			ref = dojo.widget.createWidget("Button", {}, this.cancelRegisterButtonNode);
			dojo.event.connect(ref, "onClick", this, "onCancelRegisterClick");
			this._regUseridTxtBox = dojo.widget.createWidget("ValidationTextBox", {type: "text", required: "true", size: "30", missingMessage: "* Required", missingClass: "error"}, this.registerUseridTextBoxNode);
			this._regPasswordTxtBox = dojo.widget.createWidget("ValidationTextBox", {type: "password", required: "true", size: "30", missingMessage: "* Required", missingClass: "error"}, this.registerPasswordTextBoxNode);
			this._regConfirmTxtBox = dojo.widget.createWidget("ValidationTextBox", {type: "password", required: "true", size: "30", missingMessage: "* Required", missingClass: "error"}, this.registerConfirmTextBoxNode);
			this._regFullnameTxtBox = dojo.widget.createWidget("ValidationTextBox", {type: "text", required: "true", size: "30", missingMessage: "* Required", missingClass: "error"}, this.registerFullnameTextBoxNode);
			this._regAddressTxtBox = dojo.widget.createWidget("ValidationTextBox", {type: "text", required: "true", size: "30", missingMessage: "* Required", missingClass: "error"}, this.registerAddressTextBoxNode);
			this._regEmailTxtBox = dojo.widget.createWidget("EmailTextBox", {type: "text", required: "true", size: "30", missingMessage: "* Required", invalidMessage: "* Not valid", missingClass: "error", invalidClass: "error"}, this.registerEmailTextBoxNode);
			this._regCreditcardTxtBox = dojo.widget.createWidget("ValidationTextBox", {type: "text", required: "true", size: "30", missingMessage: "* Required", missingClass: "error"}, this.registerCreditcardTextBoxNode);
			this._regBalanceTxtBox = dojo.widget.createWidget("RealNumberTextBox", {type: "text", required: "true", size: "30", missingMessage: "* Required", invalidMessage: "* Not valid", missingClass: "error", invalidClass: "error"}, this.registerBalanceTextBoxNode);
		},
		
		onShowRegisterClick: function() {
			if (!this._register) {
				// clear register form values
				this.clearRegisterValues();
				// display the account registration form
				this._registerContainer.open(this.domNode, null, this.domNode);	
				
				dojo.event.topic.publish("/daytrader", {event: "register"});
	
				// disble the login controls
				this._doLoginButton.setDisabled(true);
				this._doShowRegisterButton.setDisabled(true);
				this._passwordTxtBox.textbox.disabled = true;
				this._useridTxtBox.textbox.disabled = true;		
				this._register=true;
			}
		},
				
		onCancelRegisterClick: function () {
			if (this._register) {
				// hide the account registration form
				//alert(this._registerContainer.isShowingNow);
				if (this._registerContainer.isShowingNow)
					this._registerContainer.close();
				
				dojo.event.topic.publish("/daytrader", {event: "cancelRegister"});

				// enable the login controls
				this._doLoginButton.setDisabled(false);
				this._doShowRegisterButton.setDisabled(false);
				this._passwordTxtBox.textbox.disabled = false;
				this._useridTxtBox.textbox.disabled = false;
				this._register=false;
			}
		},
		
		onLoginClick: function() {
			var password = this._passwordTxtBox.getValue();
			var userid = this._useridTxtBox.getValue();
			
			this.doLogin(userid, password);
		},
		
		doLogin: function(userid, passwd) {
			if (userid != "" && passwd != "") {		
				dojo.io.bind({
    				method:  "GET",
    				//url: "/proxy/SoapProxy/login?p1=" + userid + "&p2=" +  passwd + "&format=json",
       				url: "/daytraderProxy/doProxy/login?p1=" + userid + "&p2=" +  passwd,
       				mimetype: "text/json",
    				load: dojo.lang.hitch(this, this.handleLogin),
    				error: dojo.lang.hitch(this, this.handleError),
    				useCache: false,
                	preventCache: true
  				});
  			}
		},
		
		handleLogin: function (type, data, evt) {	
			var uid = data.loginReturn.profileID;
			//dojo.storage.put("uid",uid,storageHandler);
			dojo.storage.put("uid",uid);
			
			// hide the login controls and unhide the logout controls
			this.loginNode.style.display = "none";
			this.logoutNode.style.display = "";
			
			
			// clear old messages from messages pane
			dojo.event.topic.publish("/messages", {event: "clearMessages"});
			// clear quotes from buy stocks pane
			dojo.event.topic.publish("/messages", {event: "clearQuotes"});
			
			// publish message to the messages pane
			var message = "User (" + uid + ") logged in";
			dojo.event.topic.publish("/messages", {event: "addMessage", message: message});
			// update account summary pane
			dojo.event.topic.publish("/accountSummary", {event: "getAccountSummary"});
			// update portfolio pane
			dojo.event.topic.publish("/portfolio", {event: "getHoldings"});
			
			dojo.event.topic.publish("/daytrader", {event: "handleLogin"});
			
			message = "Welcome " + uid + "!";
			// publish Welcome message to the top banner
			dojo.event.topic.publish("/daytrader", {event: "msgWelcome", message: message});
			
			dojo.event.topic.publish("/marketSummary", {event: "toggleHistory"});
		},
		
		onLogoutClick: function() {
			var uid = dojo.storage.get("uid");
	
			// TODO: need to investigate logout service in proxy - encounters NPE during serializeBean method
			/*dojo.io.bind({
   	 			method:  "GET",
    			url: "/proxy/SoapProxy/logout?p1=" + uid + "&format=json",
       			mimetype: "text/json",
    			load: handleLogout,
    			error: handleError
  			});*/
	
			// TODO: Fill in code for logout
			this.handleLogout();
		},
		
		handleLogout: function (type, data, evt) {	
			var uid = dojo.storage.get("uid");
	
			if (dojo.storage.hasKey("uid")) {
				//dojo.storage.remove("uid");
				dojo.storage.put("uid",null);
			}
			
			// hide the logout controls and unhide the login controls
			this.logoutNode.style.display = "none";
			this.loginNode.style.display = "";
			
			var message = "User (" + uid + ") logged out";
			// publish message to the messages pane
			dojo.event.topic.publish("/messages", {event: "addMessage", message: message});
			
			dojo.event.topic.publish("/daytrader", {event: "handleLogout"});
			
			message = "This is the top banner...";
			// revert the Welcome message to the top banner
			dojo.event.topic.publish("/daytrader", {event: "msgWelcome", message: message});
			
			dojo.event.topic.publish("/marketSummary", {event: "toggleHistory"});
		},
		
		clearRegisterValues: function () {
			this._regUseridTxtBox.setValue("");
			this._regPasswordTxtBox.setValue("");
			this._regConfirmTxtBox.setValue("");
			this._regFullnameTxtBox.setValue("");
			this._regAddressTxtBox.setValue("");
			this._regEmailTxtBox.setValue("");
			this._regCreditcardTxtBox.setValue("");
			this._regBalanceTxtBox.setValue("");
			this.showRegisterMessage("");
		},
			
		showRegisterMessage: function(message) {
			var node = this.registerMessageNode;
			var mNode = document.createTextNode(message);
	
			if (node.hasChildNodes()) {
				node.removeChild(node.childNodes[0]);
			}
	
			node.appendChild(mNode);
		},
		
		onRegisterClick: function () {
			var userid = this._regUseridTxtBox.getValue();
			var password = this._regPasswordTxtBox.getValue();
			var confirm = this._regConfirmTxtBox.getValue();
			var fullname = this._regFullnameTxtBox.getValue();
			var address = this._regAddressTxtBox.getValue();
			var email = this._regEmailTxtBox.getValue();	
			var creditcard = this._regCreditcardTxtBox.getValue();
			var balance = this._regBalanceTxtBox.getValue();
		
			if (password == "" || confirm == "" || userid == "" || fullname == "" ||
				!this._regEmailTxtBox.isValid() || address == "" || creditcard == "" || !this._regBalanceTxtBox.isValid())
				return; 
	
			if (password != confirm) {
				//displayStatusMessage("Passwords do not match!");
				this.showRegisterMessage("Passwords do not match!");
				return;
			}
			
			var queryStr = "p1=" + userid + "&p2=" + password + "&p3=" + fullname + "&p4=" + address + "&p5=" + email + "&p6=" + creditcard + "&p7=" + balance; 
		
			dojo.io.bind({
   			 	method:  "GET",
   		 		//url: "/proxy/SoapProxy/register?" + queryStr + "&format=json",
   		   		url: "/daytraderProxy/doProxy/register?" + queryStr,
   		   		mimetype: "text/json",
	   	 		load: dojo.lang.hitch(this, this.handleRegisterAccount),
	   	 		error: dojo.lang.hitch(this, this.handleError),
	   	 		useCache: false,
                preventCache: true
  			});
		},

		handleRegisterAccount: function (type, data, evt) {		
			var message = "";
			var password = this._regPasswordTxtBox.getValue();
			var userid = this._regUseridTxtBox.getValue();
		
			if (data != null) {
				var accountid = data.registerReturn.accountID;
				var balance = data.registerReturn.balance;
	
				message = "Successfully created account " + accountid + " with a balance of $" + balance;
				// display message on the registration form
				this.showRegisterMessage(message);
				// publish message to the messages pane
				dojo.event.topic.publish("/messages", {event: "addMessage", message: message});
				
				// hide the register form after 1.5 seconds
				// setTimeout(this.onCancelRegisterClick, 1500);
				this.onCancelRegisterClick();
				
				// perform the login after 1.6 seconds
				//var login = dojo.lang.hitch(this, this.doLogin);
				//setTimeout(login, 1600, userid, password);
				this.doLogin(userid, password);
			} else {
				message = "Error registering user";
				
				this.showRegisterMessage(message); 
			}
		}
	}
);
