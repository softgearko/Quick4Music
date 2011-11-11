package softgearko.quick4music;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Quick4MusicActivity extends Activity implements OnClickListener,OnCompletionListener,OnLongClickListener {	
		
	static final String TAG="Quick4";
	
	static final int[] buttonIds = {
		R.id.ibutton1,
		R.id.ibutton2,
		R.id.ibutton3,
		R.id.ibutton4
	};	
	
	static final int[] bgImgOn = {
		R.drawable.quick4_1o,
		R.drawable.quick4_2o,
		R.drawable.quick4_3o,
		R.drawable.quick4_4o,
	};
	
	static final int[] bgImgOff = {
		R.drawable.quick4_1x,
		R.drawable.quick4_2x,
		R.drawable.quick4_3x,
		R.drawable.quick4_4x,
	};
	
	static final int[] soundIds = {
		R.raw.s1,
		R.raw.s2,
		R.raw.s3,
		R.raw.s4,
	};
	
	static final int[] defaultNamesId = {
		R.string.name1,
		R.string.name2,
		R.string.name3,
		R.string.name4,
	};
	
	public static final String PREFS_NAME = "MyPrefsFile";	
	static final int CHOOSE_MEDIA_FILE=0;
	private int longClickedButtonId=-1;
	public Uri choosedUri = null;
	
	private int playingId = 0;
	
	Button btns[] = new Button[buttonIds.length];
	private Uri uris[] = new Uri[buttonIds.length];
	private String names[] = new String[buttonIds.length];
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	Log.i(TAG, "Quick4 Started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
                
        for(int i=0; i<buttonIds.length; i++) {        	
        	Button btn = (Button)findViewById(buttonIds[i]);
        	btns[i] = btn;
        	btn.setOnClickListener(this); 
        	btn.setOnLongClickListener(this);        	
        	setName(i, null, null); // set default
        }
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        for(int i=0; i<buttonIds.length; i++) {
        	String parName = "name" + Integer.toString(i);
        	String name = settings.getString( parName, null );
        	if(name!=null) {
        		String parUri = "uri" +  Integer.toString(i);
        		String sUri = settings.getString(parUri, null);
        		if(sUri!=null) {
        			Uri uri = Uri.parse(sUri);
        			if(uri !=null) {
        				setName(i, name, uri);
        			}
        		}
        	}
        }
        longClickedButtonId = settings.getInt("longClickedButtonId", longClickedButtonId); 
                
        Log.i(TAG, "Quick4 ready");        
    }
    
    public void onResume() {
    	boolean noSong = true;
    	super.onResume();
    	this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    	for(int i=0;i<buttonIds.length; i++) { 
    		if(playingId == i+1)
    			btns[i].setBackgroundResource(bgImgOn[i]);
    		else
    			btns[i].setBackgroundResource(bgImgOff[i]);
    		if(uris[i]!=null) {
    			noSong = false;
    		}
    	}
    	
    	if(noSong) {
    		Toast.makeText(this, "Please, Do Long click", Toast.LENGTH_LONG).show();
    	}
    }
    
    public void onPause() {
    	super.onPause();
    	stopSound();
    	playingId = 0;
    	this.setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
    }

    public void onStop() {
    	super.onStop();
    	
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        for(int i=0; i<buttonIds.length; i++) {
        	String parName = "name" + Integer.toString(i);        	
        	editor.putString(parName, names[i]);
        	String parUri = "uri" + Integer.toString(i);
        	if(uris[i]!=null) {
        		editor.putString(parUri, uris[i].toString());	
        	} else {
        		editor.putString(parUri, null);
        	}
        	
        }
        editor.putInt("longClickedButtonId", longClickedButtonId);
        editor.commit();
    }
    
	@Override
	public void onClick(View v) {
		int resourceId = 0;
		int bid = -1;		
				
		int vid = v.getId();		
		for(int i=0; i<buttonIds.length; i++) {
			if(vid == buttonIds[i]) { 
				bid = i;
				break;
			}
		}

		if(player!=null) {			
			Log.i(TAG, "Click, stop id="+playingId);
			stopSound();			
			if(playingId!=0) {
				btns[playingId-1].setBackgroundResource(bgImgOff[playingId-1]);
			}			
		}
		
		if(playingId != bid+1) {		
							
			btns[bid].setBackgroundResource(bgImgOn[bid]);
			playingId = bid+1;
			if(names[bid]!=null && uris[bid]!=null) {
				playSound(uris[bid]);
			} else {
				resourceId = soundIds[bid];
				playSound(resourceId);
			}
		} else {
			playingId = 0;			
		}
				
		Log.i(TAG, "Click, playingId="+playingId);	
    }
	
	// Media Player
	private MediaPlayer player=null;
	
	public void playSound(Uri uri) {
		try {			
			stopSound();
			
			Log.i(TAG, "Start Music");
			player = MediaPlayer.create(this, uri );
			
			player.seekTo(0);
			player.start();
			
			player.setOnCompletionListener(this);
		} catch (Exception e) {
			//
		}
	}
	
	public void playSound(int resourceId) {
		try {			
			stopSound();
			
			Log.i(TAG, "Start Music");
			player = MediaPlayer.create(this, resourceId );
			
			player.seekTo(0);
			player.start();
			
			player.setOnCompletionListener(this);
		} catch (Exception e) {
			//
		}
	}
	
	public void stopSound() {
		Log.i(TAG, "Stop Music");
		try {
			 if(player==null)
				 	return;
			 
			 player.stop();
			 player.setOnCompletionListener(null);
			 player.release();
			 player = null;
		} catch (Exception e) {
			//
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg) {		
		stopSound();	
		
		if(playingId!=0) {
			btns[playingId-1].setBackgroundResource(bgImgOff[playingId-1]);
		}
		playingId = 0;
	}

	@Override
	public boolean onLongClick(View v) {
		int bid = -1;		
		
		int vid = v.getId();		
		for(int i=0; i<buttonIds.length; i++) {
			if(vid == buttonIds[i]) { 
				bid = i;
				break;
			}
		}
		Log.i(TAG, "Long Click !! bid="+bid);
		
		longClickedButtonId = bid;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("audio/*");
		startActivityForResult(Intent.createChooser(intent, getString (R.string.mediaChooserTitle)), CHOOSE_MEDIA_FILE);
		
		return true;
	}
	
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case CHOOSE_MEDIA_FILE:
				if(resultCode == RESULT_OK){
					Uri uri = data.getData();
					if(uri!=null) {
						choosedUri = uri;
						String titleName=null;
						Log.i(TAG,"URI=" + uri.toString());
						
						String scheme = uri.getScheme();
						if (scheme.equals("file")) {
						    titleName = uri.getLastPathSegment();
						}
						else if (scheme.equals("content")) {
						    String[] proj = { MediaStore.Images.Media.TITLE };
						    Cursor cursor = this.getContentResolver().query(uri, proj, null, null, null);
						    if (cursor != null && cursor.getCount() != 0) {
						        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
						        cursor.moveToFirst();
						        titleName = cursor.getString(columnIndex);
						    }
						}
						if(titleName!=null) {
							Log.i(TAG,"Title = " + titleName);

							final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.setname, null);
							final EditText editTextName = (EditText)linear.findViewById(R.id.name);							
							editTextName.setText(titleName);
							
							AlertDialog.Builder ad = new AlertDialog.Builder(this);
							ad.setTitle("Set Music Name");
							ad.setView(linear);
							ad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									String name = editTextName.getEditableText().toString();
									if(name!=null && name.length()>0) {
										dialog.dismiss();
										setName(longClickedButtonId, name, choosedUri);
									}									
								}
							});
							ad.setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 									
								}								
							});	
							ad.setNeutralButton("Default",  new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									setName(longClickedButtonId, null, null); 									
								}								
							});	

							ad.show();
						}
					}
				}
			break;
		}
	}

	protected void setName(int bid, String name, Uri uri) {		
		
		if(name!=null) {
			names[bid] = name;
			uris[bid] = uri;			
		} else {
			names[bid] = getString(defaultNamesId[bid]);
			uris[bid] = null;
		}		
		
		setTitleOnButton(bid);
	}	
	
	protected void setTitleOnButton(int bid)
	{
		btns[bid].setText(names[bid]);
	}
}