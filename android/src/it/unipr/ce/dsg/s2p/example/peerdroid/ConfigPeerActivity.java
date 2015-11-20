package it.unipr.ce.dsg.s2p.example.peerdroid;

/*
 * Copyright (C) 2010 University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Designer(s):
 * Marco Picone (picone@ce.unipr.it)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * Michele Amoretti (michele.amoretti@unipr.it)
 * 
 * Developer(s)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * 
 */

import it.unipr.ce.dsg.s2p.example.peerdroid.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Class <code>ConfigPeerActivity</code> save the configuratione of a peer.
 * 
 * 
 * @author Fabrizio Caramia
 *
 */


public class ConfigPeerActivity extends Activity {

	private Button saveBut;
	private EditText bootstrapEdit;
	private EditText sbcEdit;
	private CheckBox checkbox;
	private String checkReach;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		 setContentView(R.layout.config_peer);
		 
		 checkReach="no";
		
		 //nameEdit = (EditText)findViewById(R.id.nameEdit);
		 bootstrapEdit = (EditText)findViewById(R.id.bootstrapEdit);
		 sbcEdit = (EditText)findViewById(R.id.sbcEdit);
		 bootstrapEdit.setText("bootstrap@ipaddress:port");
		 
		 checkbox = (CheckBox) findViewById(R.id.reachCheckBox);
		 checkbox.setOnClickListener(new OnClickListener() {
		     public void onClick(View v) {
		         // Perform action on clicks, depending on whether it's now checked
		         if (((CheckBox) v).isChecked()) {
		        	 checkReach="yes";
		         } else {
		        	 checkReach="no";
		         }
		     }
		 });
		 
		 
		 saveBut = (Button)findViewById(R.id.save);
		 
		 saveBut.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				String bootstrapPeer = bootstrapEdit.getText().toString();
				String sbcAddress = sbcEdit.getText().toString();
				if(bootstrapPeer.contentEquals("bootstrap@ipaddress:port")){
					Toast toast = Toast.makeText(getApplicationContext(),"Please type a BootstrapPeer (ex. bootstrap@192.168.1.2:5080)" ,Toast.LENGTH_LONG);
     		 		toast.show();
     		 		finish();
				}
				else{
					
					if(PeerActivity.peer!=null){
						PeerActivity.peer.setConfiguration(sbcAddress, bootstrapPeer, checkReach);	
						finish();
					
					}		
				}
		}
			
		 });
	}
	
}
