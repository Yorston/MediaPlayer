package com.zjm.music;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static MusicService.musicbinder musicbinder;
	private Boolean isPause = true;
	private Boolean isplayed = false;
	private Button playB;
	private Button nextB;
	private ListView musiclistview;
	static int itemid = -1;
	static int listsize;
	static List musiclist = new ArrayList();
	SimpleDateFormat timeexchange = new SimpleDateFormat("mm:ss",Locale.getDefault());
	String filename = null;
	String PREF_NAME = "com.zjm.music.musicpref";
	static SharedPreferences musicPref;
	SimpleAdapter musicadapter;
	static Editor musiceditor;
	private ServiceConnection musicconnection = new ServiceConnection() {	
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub	
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			musicbinder = (MusicService.musicbinder)service;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		timeexchange.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		getMusic();
		playB = (Button) findViewById(R.id.play);
		nextB = (Button) findViewById(R.id.next);
		//pauseB = (Button) findViewById(R.id.pause);
		musiclistview = (ListView) findViewById(R.id.musiclist);
		playB.setBackgroundResource(R.drawable.play);
		nextB.setBackgroundResource(R.drawable.next);
		Intent startservice = new Intent(MainActivity.this,MusicService.class);
		listsize = musiclist.size();
		System.out.println(listsize);
		Bundle datebundle = new Bundle();
		startService(startservice);
		bindService(startservice, musicconnection, BIND_AUTO_CREATE);
		musicPref = MainActivity.this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		musiceditor = musicPref.edit();
		musicadapter = new SimpleAdapter(MainActivity.this, musiclist, R.layout.listitem, new String[]{"title","artist","durationms"}, new int[]{R.id.title,R.id.artist,R.id.duration});
		musiclistview.setAdapter(musicadapter);
		musiclistview.setOnItemClickListener(new musicitemlistener());
		playB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					if(isPause&&!isplayed){
						Toast.makeText(MainActivity.this, "即将开始播放...", Toast.LENGTH_SHORT).show();
						itemid = musicPref.getInt("LastPlayItem",0);
						sendurl(itemid);
						((Button)v).setBackgroundResource(R.drawable.pause);
						isPause = false;
						isplayed = true;
					}else if(!isPause&&isplayed){
						musicbinder.pause();
						isPause = true;
						((Button)v).setBackgroundResource(R.drawable.play);
						isplayed = true;
					}else if(isplayed&&isPause){
						musicbinder.start();
						((Button)v).setBackgroundResource(R.drawable.pause);
						isPause = false;
					}
			}
		});
		
		nextB.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				playB.setBackgroundResource(R.drawable.pause);
				isPause = false;
				isplayed = true;
				if(itemid == musiclist.size() - 1){
					itemid = -1;
				}
				itemid = itemid + 1;
				musiceditor.putInt("LastPlayItem", itemid);
				musiceditor.commit();
				sendurl(itemid);
			}
		});		
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		musiceditor.putInt("LastPlayItem", itemid);
		musiceditor.commit();
		System.out.println(musicPref.getInt("LastPlayItem", 0));
		Log.d("MyService", "已经存放进去了!!");
		Intent stopservice = new Intent(MainActivity.this,MusicService.class);
		stopService(stopservice);
		Log.d("MyService", "已经执行Destory方法!");
		super.onDestroy();
	}
	
	public void next(Boolean complete){
		//System.out.println(complete);
		//System.out.println(itemid);
		//System.out.println(listsize);
		if(complete == true){
			if(itemid == listsize - 1){
				System.out.println("已经重置了!!!");
				itemid = -1;
			}
			itemid = itemid + 1;
			//System.out.println(itemid);
			musiceditor.putInt("LastPlayItem", itemid);
			musiceditor.commit();
			sendurl(itemid);
		}else{
			System.out.println("还没播放完毕");
		}
	}
	
	public void sendurl(int itemid){
		HashMap songmap = (HashMap) musiclist.get(itemid);
		String fileurl = (String) songmap.get("musicurl");
		//System.out.println(fileurl);
		musicbinder.play(fileurl);
	}
	
	class musicitemlistener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			playB.setBackgroundResource(R.drawable.pause);
			isPause = false;
			isplayed = true;
			itemid = (int) musiclistview.getItemIdAtPosition(position);
			System.out.println(itemid);
			musiceditor.putInt("LastPlayItem", itemid);
			musiceditor.commit();
			sendurl(itemid);
		}
	}
	
	private void getMusic(){
		Cursor cursor = MainActivity.this.getContentResolver().
				query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, 
						MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if (cursor.moveToFirst()){
			while (!cursor.isAfterLast()){
				HashMap musicmap = new HashMap();
				String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				// 歌曲文件的路径 ：MediaStore.Audio.Media.DATA
				String musicurl = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				// 歌曲的总播放时长：MediaStore.Audio.Media.DURATION
				int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				String durationms = timeexchange.format(duration);
				// 歌曲文件显示名字
				String disName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
				// 歌曲标题
				String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				musicmap.put("title", title);
				musicmap.put("artist", artist);
				musicmap.put("durationms", durationms);
				musicmap.put("musicurl", musicurl);
				musiclist.add(musicmap);
				cursor.moveToNext();
			}
			cursor.close();
		}
	}
	
}
