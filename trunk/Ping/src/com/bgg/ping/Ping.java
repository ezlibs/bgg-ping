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
import android.widget.ScrollView;
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
		// 控件
		final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
		final Button ping = (Button) this.findViewById(R.id.pingButton);
		final TextView pingResult = (TextView) findViewById(R.id.pingResult);
		final TextView ipInfo = (TextView) findViewById(R.id.ipInfo);
		final EditText userHost = (EditText) findViewById(R.id.userHost);
		final Spinner times = (Spinner) findViewById(R.id.timesSpinner);
		final Spinner hosts = (Spinner) findViewById(R.id.hostsSpinner);
		//
		final StringBuffer s = new StringBuffer("");
		final Handler resultHandler = new Handler();
		final Runnable mUpdateResults = new Runnable() {
			@Override
			public void run() {
				setText(pingResult, s);
				scrollView.scrollTo(0, scrollView.getHeight());
			}
		};

		ipInfo.setText("本机IP地址为：" + getLocalIpAddress());
		pingResult.setMovementMethod(ScrollingMovementMethod.getInstance());

		// 监听
		hosts.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int postion, long id) {
				if (hosts.getSelectedItem().toString()
						.equalsIgnoreCase("other")) {
					userHost.setVisibility(View.VISIBLE);

				} else {
					userHost.setVisibility(View.GONE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				userHost.setVisibility(View.INVISIBLE);
			}
		});
		// 点击
		ping.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ping.setEnabled(false);
				// 提示框
				final ProgressDialog dialog = ProgressDialog.show(Ping.this,
						"Ping! now", "Please Wait...", true);
				final Handler handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						dialog.dismiss();
					}
				};

				Thread process = new Thread() {
					@Override
					public void run() {
						// 获取文本框以及下拉框的值
						s.delete(0, s.length());
						String selectHost = hosts.getSelectedItem().toString();
						String host = selectHost;
						if (selectHost.equalsIgnoreCase("other")) {
							host = userHost.getText().toString();
						} else if (selectHost.indexOf(": ") != -1) {
							host = selectHost.split(" ")[1];
						} else {
							host = selectHost.split(" ")[0];
						}
						if (host.equalsIgnoreCase(getString(R.string.host))) {
							resultHandler.post(mUpdateResults);
							return;
						}
						String count = times.getSelectedItem().toString();

						// 处理
						try {
							InputStream is = Runtime.getRuntime()
									.exec("ping -c " + count + " " + host)
									.getInputStream();
							resultHandler.post(mUpdateResults);
							BufferedReader in = new BufferedReader(
									new InputStreamReader(is));
							String str = "";
							// 关闭滚动条
							handler.sendEmptyMessage(0);
							int i = 1;
							while ((str = in.readLine()) != null) {
								s.append(str);
								String oCount = (i > Integer.valueOf(count)) ? ""
										: "第" + String.valueOf(i) + "次: ";
								s.append("\n" + oCount);
								i++;
								// 逐行打印
								resultHandler.post(mUpdateResults);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						// 重新启用ping按钮
						resultHandler.post(new Runnable() {

							@Override
							public void run() {
								ping.setEnabled(true);
							}
						});
					}
				};
				process.start();
			}// end onClick
		});// end setOnClickListener
	}

	@Override
	public void onResume() {
		super.onResume();
		final Spinner hostsSpinner = (Spinner) findViewById(R.id.hostsSpinner);
		ArrayAdapter<String> adapter;
		String[] hostsList = { "www.google.com", "www.baidu.com", "Other" };
		/*
		 * String[] hostsList = { "移动1: 221.130.195.94", "移动2: 221.130.195.19",
		 * "移动3: 221.130.195.102", "电信1: 211.151.87.21", "电信2: 211.151.87.28",
		 * "联通1: 210.51.30.31", "联通2: 210.51.30.48", "Other" };
		 */
		String hosts = getConfig();
		if (!hosts.equals(null) && !hosts.equals("\n") && !hosts.equals("")) {
			hostsList = hosts.split("\n");
		}
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, hostsList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		hostsSpinner.setAdapter(adapter);
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