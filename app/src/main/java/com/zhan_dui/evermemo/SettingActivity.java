package com.zhan_dui.evermemo;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cashow.cashowevermemo.R;
import com.umeng.analytics.MobclickAgent;

public class SettingActivity extends ActionBarActivity implements
		OnClickListener, OnCheckedChangeListener {
	public static final String OPEN_MEMO_WHEN_START_UP = "OPEN_MEMO_WHEN_START_UP";

	private ToggleButton mToggleButton;
	private Context mContext;
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.in_push_right_to_left,
				R.anim.in_stable);
		mContext = this;
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activtiy_setting);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setTitle(getString(R.string.setting));
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		mToggleButton = (ToggleButton) findViewById(R.id.open_toggle);
		mToggleButton.setOnCheckedChangeListener(this);
		mToggleButton.setChecked(mSharedPreferences.getBoolean(
				OPEN_MEMO_WHEN_START_UP, false));
		findViewById(R.id.rate).setOnClickListener(this);
		findViewById(R.id.setting_start).setOnClickListener(this);
	}

	private void bindSuccess() {
		findViewById(R.id.bind_arrow).setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(R.anim.in_stable,
					R.anim.out_push_left_to_right);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bind_evernote:
			break;
		case R.id.rate:
			Uri uri = Uri.parse("market://details?id="
					+ mContext.getPackageName());
			Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
			try {
				startActivity(goToMarket);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(mContext, R.string.can_not_open_market,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.setting_start:
			mToggleButton.performClick();
		default:
			break;
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			MobclickAgent.onEvent(mContext, "open_quick_launch");
		} else {
			MobclickAgent.onEvent(mContext, "close_quick_launch");
		}
		mSharedPreferences.edit()
				.putBoolean(OPEN_MEMO_WHEN_START_UP, isChecked).commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
