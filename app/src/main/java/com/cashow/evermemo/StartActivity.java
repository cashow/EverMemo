package com.cashow.evermemo;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cashow.cashowevermemo.R;
import com.huewu.pla.lib.MultiColumnListView;
import com.cashow.adapters.MemosAdapter;
import com.cashow.adapters.MemosAdapter.ItemLongPressedLisener;
import com.cashow.adapters.MemosAdapter.onItemSelectLisener;
import com.cashow.data.MemoDB;
import com.cashow.data.MemoProvider;
import com.cashow.utils.Logger;
import com.cashow.utils.MarginAnimation;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends ActionBarActivity implements
		LoaderCallbacks<Cursor>, ItemLongPressedLisener,
		onItemSelectLisener {

	private MultiColumnListView mMemosGrid;
	private Context mContext;
	private MemosAdapter mMemosAdapter;
	private LinearLayout mBindEvernotePanel;
	private SharedPreferences mSharedPreferences;
	private int mBindEvernotePandelHeight;
	public static String sShownRate = "ShownRate";
	public static String sStartCount = "StartCount";
	private Menu mMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setLogo(R.drawable.ab_logo);
		mContext = this;
		setContentView(R.layout.activity_start);
		mMemosGrid = (MultiColumnListView) findViewById(R.id.memos);
		mBindEvernotePanel = (LinearLayout) findViewById(R.id.evernote_panel);
		mBindEvernotePandelHeight = mBindEvernotePanel.getLayoutParams().height;

		LoaderManager manager = getSupportLoaderManager();
		mMemosAdapter = new MemosAdapter(mContext, null,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, this, this);
		mMemosGrid.setAdapter(mMemosAdapter);

		manager.initLoader(1, null, this);
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		if (mSharedPreferences.getInt(sStartCount, 1) == 1) {
			mBindEvernotePanel.startAnimation(new MarginAnimation(
					mBindEvernotePanel, 0, 0, 0, 0, 600));
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {

					StartActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mBindEvernotePanel
									.startAnimation(new MarginAnimation(
											mBindEvernotePanel, 0, 0, 0,
											-mBindEvernotePandelHeight));
						}
					});
				}
			}, 5000);
			mSharedPreferences
					.edit()
					.putInt(sStartCount,
							mSharedPreferences.getInt(sStartCount, 1) + 1)
					.commit();
		}

		if (mSharedPreferences.getBoolean(
				SettingActivity.OPEN_MEMO_WHEN_START_UP, false)) {
			startActivity(new Intent(this, MemoActivity.class));
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader cursorLoader = new CursorLoader(mContext,
				MemoProvider.MEMO_URI, null, null, null, MemoDB.UPDATEDTIME
						+ " desc");
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		MatrixCursor matrixCursor = new MatrixCursor(new String[] { "_id" });
		matrixCursor.addRow(new String[] { "0" });
		Cursor c = new MergeCursor(new Cursor[] { matrixCursor, cursor });
		mMemosAdapter.swapCursor(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mMemosAdapter.swapCursor(null);
	}

	private Timer mSyncTimer;

	@Override
	protected void onResume() {
		super.onResume();

		if (mMenu != null) {
			MenuItem syncItem = mMenu.findItem(R.id.sync);
		}

		if (mSharedPreferences.getInt(MemoActivity.sEditCount, 0) == 5
				&& mSharedPreferences.getBoolean(sShownRate, false) == false) {

			Builder builder = new Builder(mContext);
			builder.setMessage(R.string.rate_for_evernote)
					.setPositiveButton(R.string.rate_rate,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Uri uri = Uri.parse("market://details?id="
											+ mContext.getPackageName());
									Intent goToMarket = new Intent(
											Intent.ACTION_VIEW, uri);
									try {
										startActivity(goToMarket);
									} catch (ActivityNotFoundException e) {
										Toast.makeText(mContext,
												R.string.can_not_open_market,
												Toast.LENGTH_SHORT).show();
									}
								}
							})
					.setNegativeButton(R.string.rate_feedback,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent Email = new Intent(
											Intent.ACTION_SEND);
									Email.setType("text/email");
									Email.putExtra(
											Intent.EXTRA_EMAIL,
											new String[] { getString(R.string.team_email) });
									Email.putExtra(Intent.EXTRA_SUBJECT,
											getString(R.string.feedback));
									Email.putExtra(Intent.EXTRA_TEXT,
											getString(R.string.email_title));
									startActivity(Intent.createChooser(Email,
											getString(R.string.email_chooser)));
								}
							}).create().show();
			mSharedPreferences.edit().putBoolean(sShownRate, true).commit();
		}
		mSyncTimer = new Timer();
		Logger.e("启动自动更新任务");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSyncTimer != null) {
			Logger.e("结束定时同步任务");
			mSyncTimer.cancel();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.start, menu);
		mMenu = menu;
		MenuItem syncItem = menu.findItem(R.id.sync);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settiing:
			Intent intent = new Intent(mContext, SettingActivity.class);
			startActivity(intent);
			break;
		case R.id.sync:
			break;
		case R.id.feedback:
			Intent Email = new Intent(Intent.ACTION_SEND);
			Email.setType("text/email");
			Email.putExtra(Intent.EXTRA_EMAIL,
					new String[] { getString(R.string.team_email) });
			Email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback));
			Email.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_title));
			startActivity(Intent.createChooser(Email,
					getString(R.string.email_chooser)));
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			findViewById(R.id.more).performClick();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private Menu mContextMenu;
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onActionItemClicked(ActionMode arg0, MenuItem menuItem) {
			switch (menuItem.getItemId()) {
			case R.id.delete:
				if (mMemosAdapter.getSelectedCount() == 0) {
					Toast.makeText(mContext, R.string.delete_select_nothing,
							Toast.LENGTH_SHORT).show();
				} else {
					Builder builder = new Builder(mContext);
					builder.setMessage(R.string.delete_all_confirm)
							.setTitle(R.string.delete_title)
							.setPositiveButton(R.string.delete_sure,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mMemosAdapter.deleteSelectedMemos();
											if (mActionMode != null) {
												mActionMode.finish();
											}
										}
									})
							.setNegativeButton(R.string.delete_cancel, null)
							.create().show();
				}
				break;
			default:
				break;
			}
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.context_menu, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			mActionMode = null;
			mContextMenu = null;
			mMemosAdapter.setCheckMode(false);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode arg0, Menu menu) {
			mContextMenu = menu;
			updateActionMode();
			return false;
		}

	};

	private ActionMode mActionMode;

	@Override
	public void startActionMode() {
		if (mActionMode != null) {
			return;
		}
		mActionMode = startSupportActionMode(mActionModeCallback);
	}

	public void updateActionMode() {
		if (mMemosAdapter.getSelectedCount() <= 1) {
			mContextMenu.findItem(R.id.selected_counts).setTitle(
					mContext.getString(R.string.selected_one_count,
							mMemosAdapter.getSelectedCount()));
		} else {
			mContextMenu.findItem(R.id.selected_counts).setTitle(
					mContext.getString(R.string.selected_more_count,
							mMemosAdapter.getSelectedCount()));
		}
	}

	@Override
	public void onSelect() {
		updateActionMode();
	}

	@Override
	public void onCancelSelect() {
		updateActionMode();
	}

}
