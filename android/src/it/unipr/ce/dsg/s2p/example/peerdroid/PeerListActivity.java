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
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;

/**
 * Class <code>PeerListActivity</code> manage a list of peers.
 * 
 * 
 * @author Fabrizio Caramia
 *
 */


public class PeerListActivity extends ListActivity {
	
	
	public static final String PEER_ADDRESS="address";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  

		  

	 setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, PeerActivity.peer.getListAddressPeer()));
	 
	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
	    	
	    	Bundle bundle = new Bundle();
	    	
	    	bundle.putString(PEER_ADDRESS, ((TextView) view).getText().toString());
	    	
	    	Intent mIntent = new Intent();
	    	mIntent.putExtras(bundle);
	    	setResult(RESULT_OK, mIntent);
	    	finish();
	    }
	  });
	}

}
