package com.itrane.dynamicview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "DynamicView";
	
	private static final int INIT_CHILD_COUNT = 3;
	private static final String KEY_INPUT_DATA = "input.data";
	private static final String KEY_FIELD_COUNT = "fld.count";
	private static final String KEY_SELECT_POS = "select.pos";
	private static final String TYPE_PHONE = "電話：";
	private static final String TYPE_MAIL = "メール：";
	private static final String TYPE_ADDRESS = "住所：";
	private static final String[] ITEM_TYPES = { TYPE_PHONE, TYPE_MAIL, TYPE_ADDRESS };

	class EditItem {
		String type;
		LinearLayout layout;
		List<View> fields;

		EditItem() {
			fields = new ArrayList<View>();
		}
	}

	// すべての項目と項目追加ボタンの親ビュー
	private LinearLayout mContainerView;
	// 追加項目選択ダイアログ
	private AlertDialog mItemSelectDialog;

	// 追加項目のマップ
	private Map<String, EditItem> fItems = new HashMap<String, EditItem>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContainerView = (LinearLayout) findViewById(R.id.root_view);
	}


	/**
	 * デバイスの回転など、アクティビティの再構築前に現在の状態を保存する.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		for (EditItem item : fItems.values()) {
			outState.putInt(item.type + KEY_FIELD_COUNT, item.fields.size());
			for (int i = 0; i < item.fields.size(); i++) {
				View v = item.fields.get(i);
				EditText et = findEditText(item.type, v);
				Spinner sp = findSpinner(item.type, v);
				outState.putString(item.type + KEY_INPUT_DATA + i, et.getText()
						.toString());
				outState.putInt(item.type + KEY_SELECT_POS + i,
						sp.getSelectedItemPosition());
			}
		}
	}

	/**
	 * アクティビティが再構築された後、保存状態で画面を更新する。
	 */
	@Override
	protected void onRestoreInstanceState(Bundle inState) {
		super.onRestoreInstanceState(inState);
		for (String itemType : ITEM_TYPES) {
			int fieldCnt = inState.getInt(itemType + KEY_FIELD_COUNT);
			if (fieldCnt > 0) {
				inflateEditItem(itemType);
				for (int i = 0; i < fieldCnt; i++) {
					int pos = inState.getInt(itemType + KEY_SELECT_POS + i);
					String data = inState.getString(itemType + KEY_INPUT_DATA
							+ i);
					inflateEditRow(itemType, data, pos);
				}
			}
		}
	}

	/**
	 * 「新規項目追加」ボタンの onClickハンドラ.
	 */
	public void onAddNewItemClicked(View v) {
		final int checkedItem = -1;
		mItemSelectDialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("追加する項目を選択してください")
				.setSingleChoiceItems(ITEM_TYPES, checkedItem,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String type = ITEM_TYPES[which];
								mItemSelectDialog.dismiss();
								// 　選択されたタイプの項目を親コンテナに挿入
								inflateEditItem(type);
								// 挿入項目に最初の１行追加
								inflateEditRow(type, "", 0);
							}
						}).create();
		mItemSelectDialog.show();
	}

	/**
	 * 「新規追加」ボタンの onClick ハンドラ.
	 */
	public void onAddNewClicked(View v) {
		// 親ビューのテキストビューから項目タイプを取得
		View rowContainer = (View) v.getParent();
		TextView textv = (TextView) rowContainer.findViewById(R.id.textv_item);
		String itemType = textv.getText().toString();

		// 新規項目を取得して
		inflateEditRow(itemType, "", 0);
	}

	// 各行の "X" ボタンの onClick ハンドラ
	public void onDeleteClicked(View v) {
		// ボタンの親 : rowView を取得
		View rowView = (View) v.getParent();
		// その親 : 項目レイアウトを取得
		LinearLayout rowContainer = (LinearLayout) rowView.getParent();
		String type = ((TextView) rowContainer.findViewById(R.id.textv_item))
				.getText().toString();

		if (rowContainer.getChildCount() == INIT_CHILD_COUNT) {
			// 行が１つの場合トップコンテナから項目を削除する
			mContainerView.removeView(rowContainer);
			fItems.remove(type);
		} else {
			// 項目から行を削除する
			rowContainer.removeView(rowView);
			fItems.get(type).fields.remove(rowView);
		}
	}

	// 項目を取得するためのヘルパー
	private void inflateEditItem(String type) {
		EditItem editItem = new EditItem();

		// レイアウトXMLから項目ビューを取得
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View itemView = inflater.inflate(R.layout.row_container, null);
		editItem.layout = (LinearLayout) itemView;
		editItem.type = type;
		fItems.put(type, editItem);

		// 追加項目のラベルに、項目タイプを設定
		final TextView textv = (TextView) itemView
				.findViewById(R.id.textv_item);
		textv.setText(type);

		// すべての行の最後で「新規項目追加」ボタンの前に入れる
		mContainerView.addView(itemView, mContainerView.getChildCount() - 1);
	}

	// 行を取得するためのヘルパー
	private void inflateEditRow(String itemType, String data, int select) {

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflateRowView(itemType);
		final EditText editText = findEditText(itemType, rowView);
		final Spinner spinner = findSpinner(itemType, rowView);
		LinearLayout itemLayout = fItems.get(itemType).layout;
		fItems.get(itemType).fields.add(rowView);

		if (data != null && !data.equals("")) {
			editText.setText(data);
		}
		if (select > 0) {
			spinner.setSelection(select);
		}

		// すべての行の最後で「新規追加」ボタンの前に入れる
		itemLayout.addView(rowView, itemLayout.getChildCount() - 1);
	}

	private View inflateRowView(String itemType) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;
		if (itemType.equals(TYPE_PHONE)) {
			rowView = inflater.inflate(R.layout.phone_row, null);
		} else if (itemType.equals(TYPE_MAIL)) {
			rowView = inflater.inflate(R.layout.mail_row, null);
		} else {
			rowView = inflater.inflate(R.layout.address_row, null);
		}
		return rowView;
	}

	private EditText findEditText(String itemType, View rowView) {
		EditText editText = null;
		if (itemType.equals(TYPE_PHONE)) {
			editText = (EditText) rowView.findViewById(R.id.edit_phone);
		} else if (itemType.equals(TYPE_MAIL)) {
			editText = (EditText) rowView.findViewById(R.id.edit_mail);
		} else {
			editText = (EditText) rowView.findViewById(R.id.edit_address);
		}
		return editText;
	}

	private Spinner findSpinner(String itemType, View rowView) {
		Spinner spinner = null; 
		if (itemType.equals(TYPE_PHONE)) {
			spinner = (Spinner) rowView.findViewById(R.id.spinner_phone);
		} else if (itemType.equals(TYPE_MAIL)) {
			spinner = (Spinner) rowView.findViewById(R.id.spinner_mail);
		} else {
			spinner = (Spinner) rowView.findViewById(R.id.spinner_address);
		}
		return spinner;
	}

}