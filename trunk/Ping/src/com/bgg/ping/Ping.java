package com.bgg.ping;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.bgg.ping.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author zhangbin.bin
 * 
 */
public class Ping extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//
		final StringBuffer s = new StringBuffer("");

		// 控件
		Button button = (Button) this.findViewById(R.id.button1);
		final TextView tv = (TextView) findViewById(R.id.textView1);
		final TextView tv2 = (TextView) findViewById(R.id.textView2);
		final EditText et = (EditText) findViewById(R.id.editText1);
		final Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);

		//
		final Handler cwjHandler = new Handler();
		final Runnable mUpdateResults = new Runnable() {
			@Override
			public void run() {
				setText(tv, s);
			}
		};

		tv2.setText("本机IP地址为：" + getLocalIpAddress());
		tv.setMovementMethod(ScrollingMovementMethod.getInstance());

		// 监听
		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int postion, long id) {
				if (spinner2.getSelectedItem().toString()
						.equalsIgnoreCase("other")) {
					et.setVisibility(View.VISIBLE);
				} else {
					et.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				et.setVisibility(View.INVISIBLE);
			}
		});
		// 点击
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// 提示框
				final ProgressDialog dialog = ProgressDialog.show(Ping.this,
						"Ping! now", "Please Wait...", true);
				final Handler handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						dialog.dismiss();
					}
				};

				Thread checkUpdate = new Thread() {
					@Override
					public void run() {
						// 获取文本框以及下拉框的值
						s.delete(0, s.length());
						String selectHost = spinner2.getSelectedItem()
								.toString();
						String host = selectHost;
						if (selectHost.equalsIgnoreCase("other")) {
							host = et.getText().toString();
						} else {
							host = selectHost.split(" ")[0];
						}
						if (host.equalsIgnoreCase(getString(R.string.host))) {
							cwjHandler.post(mUpdateResults);
							return;
						}
						String count = spinner1.getSelectedItem().toString();

						// 处理
						try {
							InputStream is = Runtime.getRuntime()
									.exec("ping -c " + count + " " + host)
									.getInputStream();
							// s.append("ping -c " + count + " " + host + "\n");
							cwjHandler.post(mUpdateResults);
							BufferedReader in = new BufferedReader(
									new InputStreamReader(is));
							String str = "";
							while ((str = in.readLine()) != null) {
								s.append(str);
								s.append("\n");
							}
							cwjHandler.post(mUpdateResults);
						} catch (IOException e) {
							e.printStackTrace();
						}
						handler.sendEmptyMessage(0);
					}
				};
				checkUpdate.start();
			}// end onClick
		});// end setOnClickListener
	}

	@Override
	public void onResume() {
		super.onResume();
		final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
		ArrayAdapter<String> adapter;
		String[] hostsList = { "www.google.com", "www.baidu.com", "Other" };
		String hosts = getConfig();
		if (!hosts.equals(null) && !hosts.equals("\n") && !hosts.equals("")) {
			hostsList = hosts.split("\n");
		}
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, hostsList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter);
	}

	private void setText(TextView tv, StringBuffer s) {
		tv.setText(s);
	}

	// 获得当前IP
	private String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
			return "get local IP failed!";

		}
		return null;
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, Menu.FIRST + 1, 0, "编辑服务器").setIcon(
				android.R.drawable.ic_menu_edit);

		menu.add(Menu.NONE, Menu.FIRST + 2, 1, "帮助").setIcon(
				android.R.drawable.ic_menu_help);

		menu.add(Menu.NONE, Menu.FIRST + 3, 2, "退出").setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case Menu.FIRST + 1:
			Intent intent = new Intent(this, Config.class);
			startActivity(intent);
			break;

		case Menu.FIRST + 2:
			Toast toast = Toast.makeText(this, "请联系binx1440@126.com",
					Toast.LENGTH_LONG);
			View textView = toast.getView();
			LinearLayout lay = new LinearLayout(this);
			lay.setOrientation(LinearLayout.VERTICAL);
			ImageView view = new ImageView(this);
			view.setImageResource(R.drawable.head);
			lay.addView(view);
			lay.addView(textView);
			toast.setView(lay);
			toast.show();
			break;

		case Menu.FIRST + 3:
			System.exit(0);
			break;

		}
		return false;
	}

	// 读取服务器配置
	public String getConfig() {
		FileInputStream os = null;
		try {
			os = this.openFileInput("hostConfig.txt");
			InputStreamReader inReader = new InputStreamReader(os);
			BufferedReader br = new BufferedReader(inReader);
			String data = null;
			StringBuffer sb = new StringBuffer();
			while ((data = br.readLine()) != null) {
				sb.append(data);
				sb.append("\n");
			}
			return sb.toString();

		} catch (Exception e) {
			return "";
		}
	}

}