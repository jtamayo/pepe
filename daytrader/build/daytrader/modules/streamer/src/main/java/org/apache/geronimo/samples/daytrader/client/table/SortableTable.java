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
package org.apache.geronimo.samples.daytrader.client.table;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.apache.geronimo.samples.daytrader.client.*;

public class SortableTable extends JTable {
	public SortableTable(TableModel tm) {
		super(tm);
		addMouseListenerToTableHeader();
	}

	public void addMouseListenerToTableHeader() {
		setColumnSelectionAllowed(false);                
		JTableHeader th = getTableHeader(); 
		th.addMouseListener(new listMouseListener ()); 
	}

	class listMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			TableColumnModel columnModel = getColumnModel();
			int viewColumn = columnModel.getColumnIndexAtX (e.getX()); 
			int columnIndex = convertColumnIndexToModel(viewColumn); 
			if (e.getClickCount() == 1 && columnIndex != -1) {                
				((TradeQuoteAuditStats)getModel ()).sort(columnIndex, true);
			}
		}
	}
}
