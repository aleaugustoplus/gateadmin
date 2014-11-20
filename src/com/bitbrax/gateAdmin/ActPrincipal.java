package com.bitbrax.gateAdmin;

import java.io.IOException;




import java.util.Date;

import com.bitbrax.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;



public class ActPrincipal extends Activity 
{
    
    IntentFilter[] mFilters;
    String[][] mTechLists; 
    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    String Saldo;
    Controller controller = null;
    RadioButton rbPerso;
	RadioButton rbClear;
	RadioButton rbRead;
    EditText edName;
    EditText edDTEntry;
    
    CheckBox chkMultipleEntry;
    CheckBox chkMultipleSlipper;
    DBGate Db;
    
    
    final Handler handler = new Handler() 
    {
        public void handleMessage(Message msg) 
        {
            // Get the current value of the variable total from the message data
            // and update the progress bar.
        	  TextView status = (TextView) findViewById(R.id.tvStatus);
	      	  int Operation = msg.getData().getInt("Operation");
	      	  
             
	      	  Guest guest=null;
	    	  switch(Operation)
	    	  {
  	  				case Controller.OP_CLEAR:				             			              			             
  	  					status.setText("Limpou UID: " + byteArrayToHex(msg.getData().getByteArray("Uid")));
	              	break;

	    	        case Controller.OP_PERSON:				             			              			             
			              status.setText("Person UID: " + byteArrayToHex(msg.getData().getByteArray("Uid")));
		            break;
	    	  		case Controller.OP_READ:	  			
	    	  			
	    	  			  guest=(Guest)msg.getData().getSerializable("guest");
			              status.setText("Leu UID: " + bytearray2int(msg.getData().getByteArray("Uid")));
			              System.out.println("Leu UID: " + bytearray2int(msg.getData().getByteArray("Uid")) + 
			            		             " Guest: " + guest);
			              
			     
			              edName.setText(guest.getName());
			              if(guest.getDHEntry()!=0)
			            	  edDTEntry.setText(new Date(guest.getDHEntry()).toString());
			              else
			            	  edDTEntry.setText("");
			              
			              chkMultipleEntry.setChecked(guest.isMultipleEntries());
			              chkMultipleSlipper.setChecked(guest.isMultipleSlippers());
			              GuestsDS Ds= new GuestsDS(getApplicationContext());
			              
			              guest.setUid(bytearray2int(msg.getData().getByteArray("Uid")));
			              Ds.InsertGuestIfNot(guest);
			              
			              edName.setText(guest.getName());
			              
		            break;
	    	  		case Controller.OP_ENTRY:
	    	  			
	    	  		break;
	    	  		case Controller.OP_SLIPPER:
	    	  			
		    	  	break;		    	  		
		            default:
		            	 // showDialog(1);
		                  status.setText("Falha: " + msg.getData().getString("Message"));
		            break;	
	    	  }
	    	  
	    	  ActPrincipal.this.controller=null;
	    	  dismissDialog(0);
	    	  
        }
    };
    
    
    
    
    public static String byteArrayToHex(byte[] a) {
    	   StringBuilder sb = new StringBuilder(a.length * 2);
    	   for(byte b: a)
    	      sb.append(String.format("%02x", b & 0xff));
    	   return sb.toString();
    	}
    public static int bytearray2int(byte[] by)
    {
	    int value = 0;
	    for (int i = 0; i < by.length; i++)
	    {
	       value = (value << 8) + (by[i] & 0xff);
	    }
	    return value;
    }
//----------------------------------------------------------------------------------------------------------------------
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        Db=new DBGate(this.getApplicationContext());
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        mAdapter = NfcAdapter.getDefaultAdapter(this);                
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
         
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] {
                ndef,
        };

        // Setup a tech list for all NfcF tags
       mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };
        
        Intent intent = getIntent();
        
        resolveIntent(intent); 
        
       rbPerso = (RadioButton) findViewById(R.id.rbPerso);
   	   rbClear = (RadioButton) findViewById(R.id.rbClear);
   	   rbRead = (RadioButton) findViewById(R.id.rbRead);
       edName= (EditText) findViewById(R.id.edtName);
       edDTEntry= (EditText) findViewById(R.id.edtDateTimeEntry);       
       chkMultipleEntry= (CheckBox) findViewById(R.id.chkEntries);
       chkMultipleSlipper= (CheckBox) findViewById(R.id.chkSlipper);
       
        
        
    }
//----------------------------------------------------------------------------------------------------------------------
    private void resolveIntent(Intent intent) 
    {
        
      
            // 1) Parse the intent and get the action that triggered this intent
         String action = intent.getAction();
            // 2) Check if it was triggered by a tag discovered interruption.
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) 
         {
                    //  3) Get an instance of the TAG from the NfcAdapter
             Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
               // 4) Get an instance of the Mifare classic card from this TAG intent
            
             
                    
            try 
            {   
            	 
            	 Guest guest = new Guest();
            	 
            	 guest.setName(edName.getEditableText().toString());
                 guest.setMultipleEntries(chkMultipleEntry.isChecked());
                 guest.setMultipleSlippers(chkMultipleSlipper.isChecked());
                 
                 
                 System.out.println("Date in long: " + System.currentTimeMillis());
            	 if(rbPerso.isChecked())
            	 {	 	                              	               
	                 controller = new Controller(MifareClassic.get(tagFromIntent), handler, Controller.OP_PERSON, guest);
            		 //controller = new Controller(MifareClassic.get(tagFromIntent), handler, Controller.OP_ENTRY, guest);
            	 }
            	 else if(rbClear.isChecked())
            	 {            		 	                                  	                                  
            		 guest.setDHEntry(0);
                     guest.setDHSlipper(0);
            		 controller = new Controller(MifareClassic.get(tagFromIntent), handler, Controller.OP_CLEAR, guest);            		 
            	 }
            	 else if(rbRead.isChecked())
            	 {            		 
            		 controller = new Controller(MifareClassic.get(tagFromIntent), handler, Controller.OP_READ, null);            		 
            	 }
            	 
            	 showDialog(0);
            	 
            	 controller.start();
            	 
            	 //controller.join();
                   
            }
            catch(Exception e) 
            { 
                    //Log.e(TAG, e.getLocalizedMessage());
                    //showAlert(3);
            }
        } 
    }
 //---------------------------------------------------------------------------------------------------------------------   
    @Override
    protected Dialog onCreateDialog(int id) 
    {
        switch(id)
        {
            case 0:
                ProgressDialog progDialog = new ProgressDialog(this);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setMessage("Loading...");
            return progDialog;
            case 1:
                AlertDialog.Builder tmp = new AlertDialog.Builder(getBaseContext());
                tmp.setMessage(Saldo).show();
                return null;
            default:
                return null;
        }
    }
//----------------------------------------------------------------------------------------------------------------------
    
        @Override
        public void onResume() 
        {
            super.onResume();
            
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
//----------------------------------------------------------------------------------------------------------------------
        @Override
        public void onNewIntent(Intent intent) 
        {
            Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
            resolveIntent(intent);            
        }
//----------------------------------------------------------------------------------------------------------------------
        @Override
        public void onPause()
        {
            super.onPause();
            mAdapter.disableForegroundDispatch(this);
        }
//----------------------------------------------------------------------------------------------------------------------        
   
    	
    	public void onRadioButtonClicked(View view) {
    	    // Is the button now checked?
    	    boolean checked = ((RadioButton) view).isChecked();
    	    
    	    // Check which radio button was clicked
    	    switch(view.getId()) {
    	    	case R.id.rbClear:
    	        case R.id.rbRead:
    	            if (checked)
    	            {
    	            	//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    	            	//((TextView)findViewById(R.id.edtName)).setEnabled(false);
    	            	//((TextView)findViewById(R.id.edtDateTimeEntry)).setEnabled(false);
    	            	//((CheckBox)findViewById(R.id.chkEntries)).setEnabled(false);
    	            	//((CheckBox)findViewById(R.id.chkSlipper)).setEnabled(false);
    	            }
    	        break;
    	        case R.id.rbPerso:
    	            if (checked)
                    {
    	            	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    	            	//((TextView)findViewById(R.id.edtName)).setEnabled(true);
    	            	//((TextView)findViewById(R.id.edtDateTimeEntry)).setEnabled(true);
    	            	//((CheckBox)findViewById(R.id.chkEntries)).setEnabled(true);
    	            	//((CheckBox)findViewById(R.id.chkSlipper)).setEnabled(true);
    	            }
    	       break;
    	    }
    	}        
    
    
}