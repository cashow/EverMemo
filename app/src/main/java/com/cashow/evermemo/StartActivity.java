package com.cashow.evermemo;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cashow.adapters.MemosAdapter;
import com.cashow.adapters.MemosAdapter.ItemLongPressedLisener;
import com.cashow.cashowevermemo.R;
import com.cashow.custom.MemoPopupWindow;
import com.cashow.data.Memo;
import com.cashow.data.MemoColor;
import com.cashow.data.MemoDB;
import com.cashow.data.MemoProvider;
import com.cashow.sync.Evernote;
import com.cashow.utils.Logger;
import com.cashow.utils.MarginAnimation;
import com.evernote.client.android.EvernoteSession;
import com.huewu.pla.lib.MultiColumnListView;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends ActionBarActivity implements
        LoaderCallbacks<Cursor>, View.OnClickListener, ItemLongPressedLisener {

    private MultiColumnListView mMemosGrid;
    private Context mContext;
    private MemosAdapter mMemosAdapter;
    private LinearLayout mBindEvernotePanel;
    private SharedPreferences mSharedPreferences;
    private Button mBindEvernote;
    private int mBindEvernotePandelHeight;
    private Button buttonNew;
    public static Evernote mEvernote;
    public static String sStartCount = "StartCount";

    private MemoPopupWindow memoPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setLogo(R.drawable.ab_logo);
        mContext = this;
        mEvernote = new Evernote(mContext);

        setContentView(R.layout.activity_start);
        mMemosGrid = (MultiColumnListView) findViewById(R.id.memos);
        mBindEvernotePanel = (LinearLayout) findViewById(R.id.evernote_panel);
        mBindEvernote = (Button) findViewById(R.id.bind_evernote);
        mBindEvernotePandelHeight = mBindEvernotePanel.getLayoutParams().height;
        buttonNew = (Button) findViewById(R.id.button_new);

        LoaderManager manager = getSupportLoaderManager();
        mMemosAdapter = new MemosAdapter(mContext, null,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, this);
        mMemosGrid.setAdapter(mMemosAdapter);

        manager.initLoader(1, null, this);
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        if (mSharedPreferences.getInt(sStartCount, 1) == 1) {
            // 底部显示绑定evernote的提示
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
            mSharedPreferences.edit().putInt(sStartCount,mSharedPreferences.getInt(sStartCount, 1) + 1)
                    .commit();
            mBindEvernote.setOnClickListener(this);
        }

        if (mSharedPreferences.getBoolean(
                SettingActivity.OPEN_MEMO_WHEN_START_UP, false)) {
            startActivity(new Intent(this, MemoActivity.class));
        }

        mEvernote.sync(true, true, null);

        buttonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, MemoActivity.class));
            }
        });

        Typeface mRobotoThin = Typeface.createFromAsset(mContext.getAssets(),
                "fonts/Roboto-Thin.ttf");
        buttonNew.setTypeface(mRobotoThin);
    }

    private Menu mMenu;

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cursorLoader = new CursorLoader(mContext,
                MemoProvider.MEMO_URI, null, null, null, MemoDB.UPDATEDTIME
                + " desc");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"_id"});
        Cursor c = new MergeCursor(new Cursor[]{matrixCursor, cursor});
        mMemosAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mMemosAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bind_evernote) {
            mEvernote.auth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EvernoteSession.REQUEST_CODE_OAUTH:
                mEvernote.onAuthFinish(resultCode);
                break;
        }
    }

    private Timer mSyncTimer;

    @Override
    protected void onResume() {
        super.onResume();

        if (mMenu != null) {
            MenuItem syncItem = mMenu.findItem(R.id.sync);
            if (!mEvernote.isLogin()) {
                syncItem.setTitle(R.string.menu_bind);
            } else {
                syncItem.setTitle(R.string.menu_sync);
            }
        }

        mSyncTimer = new Timer();
        Logger.e("启动自动更新任务");
        mSyncTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                mEvernote.sync(true, true, null);
            }
        }, 30000, 50000);
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
        if (!mEvernote.isLogin()) {
            syncItem.setTitle(R.string.menu_bind);
        } else {
            syncItem.setTitle(R.string.menu_sync);
        }
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
                if (mEvernote.isLogin() == false) {
                    mEvernote.auth();
                } else {
                    mEvernote.sync(true, true, new SyncHandler());
                }
                break;
            default:
                break;
        }
        return false;
    }

    @SuppressLint("HandlerLeak")
    class SyncHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Evernote.SYNC_START:
                    findViewById(R.id.sync_progress).setVisibility(View.VISIBLE);
                    break;
                case Evernote.SYNC_END:
                    findViewById(R.id.sync_progress).setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            findViewById(R.id.more).performClick();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onLongPressed(Memo memo) {
        if (memoPopupWindow == null) {
            memoPopupWindow = new MemoPopupWindow(mContext, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    memoPopupWindow.dismiss();
                    switch (v.getId()) {
                        case R.id.image_normal:
                            updateMemoColor(memoPopupWindow.getMemo(), MemoColor.NORMAL);
                            break;
                        case R.id.image_green:
                            updateMemoColor(memoPopupWindow.getMemo(), MemoColor.GREEN);
                            break;
                        case R.id.image_blue:
                            updateMemoColor(memoPopupWindow.getMemo(), MemoColor.BLUE);
                            break;
                        case R.id.image_grey:
                            updateMemoColor(memoPopupWindow.getMemo(), MemoColor.GREY);
                            break;
                        case R.id.image_yellow:
                            updateMemoColor(memoPopupWindow.getMemo(), MemoColor.YELLOW);
                            break;
                        case R.id.image_red:
                            updateMemoColor(memoPopupWindow.getMemo(), MemoColor.RED);
                            break;
                        case R.id.image_delete:
                            deleteMemo(memoPopupWindow.getMemo().getId());
                            break;
                    }
                }
            });
        }
        memoPopupWindow.setMemo(memo);
        memoPopupWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content),
                Gravity.CENTER, 0, 0);
    }

    private void updateMemoColor(Memo memo, String color) {
        memo.setColor(color);
        ContentValues values = memo.toContentValues();
        getContentResolver().update(
                ContentUris.withAppendedId(MemoProvider.MEMO_URI,
                        memo.getId()), values, null, null);
    }

    private void deleteMemo(final int memoId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.delete_confirm)
                .setTitle(R.string.delete_title)
                .setPositiveButton(R.string.delete_sure,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMemosAdapter.deleteSelectedMemos(memoId);
                            }
                        })
                .setNegativeButton(R.string.delete_cancel, null)
                .create().show();
    }
}
