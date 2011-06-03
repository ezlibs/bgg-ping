/**
 * 
 */
package com.bgg.ping;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.bgg.ping.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author zhangbin.bin
 * 
 */
public class Config extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);

		Button save = (Button) this.findViewById(R.id.button1);
		Button cancel = (Button) this.findViewById(R.id.button2);
		final EditText hostList = (EditText) findViewById(R.id.editText1);

		hostList.setText(getConfig());
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				writeConfig(hostList.getText().toString());
				Config.this.finish();
			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Config.this.finish();
			}
		});
	}

	//
	public void writeConfig(String text) {
		FileOutputStream os;
		try {
			os = this.openFileOutput("hostConfig.txt", MODE_PRIVATE);
			OutputStreamWriter outWriter = new OutputStreamWriter(os);
			text = text.toLowerCase();
			while (text.indexOf("\n\n") != -1) {
				text = text.replace("\n\n", "\n");
			}
			if (text.equals("") || text.equals("\n")) {
				text = null;
			} else if (text.indexOf("\nother") == -1) {
				text += "\nother";
			}
			outWriter.write(text);
			outWriter.flush();
			outWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			return null;
		}
	}
}
