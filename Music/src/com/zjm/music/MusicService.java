package com.zjm.music;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service{
	
	private File mp3;
	private MainActivity musicactivity = new MainActivity();
	private MediaPlayer player = new MediaPlayer();
	private musicbinder musicb = new musicbinder();
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return musicb;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		player.setOnCompletionListener(new OnCompletionListener() {			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				musicactivity.next(true);
			}
		});
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(player.isPlaying()){
			player.stop();
		}
		player.release();
		Log.d("MyService", "服务已经停止!!!!");
		super.onDestroy();
	}
	
	class musicbinder extends Binder{
		public void play(String fileurl){
			System.out.println(fileurl);
			mp3 = new File(fileurl);
			if(mp3.exists()){
				//Toast.makeText(MusicService.this, "文件获取成功!", Toast.LENGTH_SHORT).show();
				try {
					if(player.isPlaying()){
						player.stop();
					}
					player.reset();
					player.setDataSource(mp3.getAbsolutePath());
					player.prepare();
					player.start();
				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				Toast.makeText(MusicService.this, "文件不存在!", Toast.LENGTH_SHORT).show();
			}
		}
		
		public void pause(){
			if(player.isPlaying()){
				player.pause();
			}
		}
		public void start(){
			player.start();
		}
	}
}
