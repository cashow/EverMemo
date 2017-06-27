package com.cashow.adapters;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cashow.cashowevermemo.R;
import com.cashow.data.Memo;
import com.cashow.data.MemoColor;
import com.cashow.data.MemoProvider;
import com.cashow.evermemo.MemoActivity;
import com.cashow.sync.Evernote;
import com.cashow.utils.DateHelper;

public class MemosAdapter extends CursorAdapter implements OnClickListener,
		OnLongClickListener {

	private LayoutInflater mLayoutInflater;

	private ItemLongPressedLisener mItemLongPressedLisener;

	public MemosAdapter(Context context, Cursor c, int flags,
			ItemLongPressedLisener itemLongPressedLisener) {
		super(context, c, flags);
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext.getContentResolver().registerContentObserver(
				MemoProvider.MEMO_URI, false,
				new UpdateObserver(mUpdateHandler));
		mItemLongPressedLisener = itemLongPressedLisener;
	}

	@SuppressLint("HandlerLeak")
	private Handler mUpdateHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			notifyDataSetChanged();
		};
	};

	class UpdateObserver extends ContentObserver {
		private Handler mHandler;

		public UpdateObserver(Handler handler) {
			super(handler);
			mHandler = handler;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			mHandler.sendEmptyMessage(0);
		}

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (!mDataValid) {
			throw new IllegalStateException(
					"this should only be called when the cursor is valid");
		}
		if (!mCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position "
					+ position);
		}
		View v;
		if (convertView == null) {
			v = newView(mContext, mCursor, parent);
		} else {
			v = convertView;
		}

        v.findViewById(R.id.hover).setTag(R.string.memo_position, position);
		bindView(v, mContext, mCursor);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int _id = cursor.getInt(cursor.getColumnIndex("_id"));
		if (cursor != null && view != null) {
			Memo memo = new Memo(cursor);
			TextView content = (TextView) view.findViewById(R.id.content);
			TextView date = (TextView) view.findViewById(R.id.date);
			content.setText(Html.fromHtml(memo.getContent()));
			date.setText(DateHelper.getGridDate(mContext, memo.getCreatedTime()));
			View hoverView = view.findViewById(R.id.hover);
			View uploadView = view.findViewById(R.id.uploading);
			hoverView.setTag(R.string.memo_data, memo);
			hoverView.setTag(R.string.memo_id, _id);
			if (memo.isSyncingUp()) {
				uploadView.setVisibility(View.VISIBLE);
			} else {
				uploadView.setVisibility(View.INVISIBLE);
			}
			String memoColor = memo.getColor();
            int colorId = MemoColor.getColorId(memoColor);
            hoverView.setBackgroundColor(context.getResources().getColor(colorId));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View commonView = mLayoutInflater.inflate(R.layout.memo_item,
                parent, false);
        final View hover = commonView.findViewById(R.id.hover);
        hover.setOnClickListener(this);
        hover.setOnLongClickListener(this);
        return commonView;
	}

	@Override
	public void onClick(View v) {
        switch (v.getId()) {
        case R.id.hover:
            Memo memo = (Memo) v.getTag(R.string.memo_data);
            Intent intent = new Intent(mContext, MemoActivity.class);
            intent.putExtra("memo", memo);
            mContext.startActivity(intent);
            break;
        default:
            break;
        }
	}

	public interface ItemLongPressedLisener {
		public void onLongPressed(Memo memo);
	}

	@Override
	public boolean onLongClick(View v) {
        Memo memo = (Memo) v.getTag(R.string.memo_data);
        mItemLongPressedLisener.onLongPressed(memo);
		return true;
	}

	public void deleteSelectedMemos(int memoId) {
        mContext.getContentResolver().delete(
                ContentUris.withAppendedId(MemoProvider.MEMO_URI, memoId), null, null);
        new Evernote(mContext).sync(true, false, null);
	}
}
