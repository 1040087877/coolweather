package com.coolweather.app.activity;

import java.security.PublicKey;

import com.coolweather.app.R;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.HttpUtil.HttpCallbackListener;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	private LinearLayout weatherInfoLayout;
	/*
	 * 用于显示城市名
	 */
	private TextView cityNameText;
	/*
	 * 用于显示发布时间
	 */
	private TextView publishText;
	/*
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;
	/*
	 * 用于显示气温1
	 */
	private TextView temp1Text;
	/*
	 * 用于显示发布气温2
	 */
	private TextView temp2Text;
	/*
	 * 用于显示当前日期
	 */
	private TextView currentDateText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各控件
		weatherInfoLayout=(LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText=(TextView) findViewById(R.id.city_name);
		publishText=(TextView) findViewById(R.id.publish_text);
		weatherDespText=(TextView) findViewById(R.id.weather_desp);
		temp1Text=(TextView) findViewById(R.id.temp1);
		temp2Text=(TextView) findViewById(R.id.temp2);
		currentDateText=(TextView) findViewById(R.id.current_data);
		String countyCode=getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode)){
			//有县级代号时就去查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			//没有县级代号就直接显示本地天气
			showWeather();
		}
	}
	/*
	 * 查询县级代码所对应的天气代号
	 */
	private void queryWeatherCode(String countyCode){
		String address="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");
	}
	/*
	 * 查询天气代号所对应的天气
	 */
	private void queryWeatherInfo(String weatherCode){
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address,"weatherCode");
	}
	/*
	 * 根据传入的地址和类型去向服务器查询代码或天气
	 */
	private void queryFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(type)){
						//从服务器返回的数据中解析出天气代号
						String[]array=response.split("\\|");
						if(array!=null && array.length==2){
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);
						}
					}
					//weatherCode是天气代号不是城市代号
				}else if("weatherCode".equals(type)){
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
			}
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				publishText.setText("同步失败");
			}
		});
	}
	/*
	 * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
	 */
	private void showWeather(){
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}
}
