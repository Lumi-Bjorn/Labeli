package com.labeli.activities;

import net.labeli.APIConnection;

import com.labeli.R;
import com.labeli.user.UserProperties;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ProductInformationActivity extends Activity {

	private String name, code;
	private int count;
	private TextView textViewInfosName, textViewInfosCount, textViewInfosPrice;
	private Double price;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = this.getIntent().getExtras();
		this.name = bundle.getString(APIConnection.TAG_NAME);
		this.count = bundle.getInt(APIConnection.TAG_COUNT);
		this.code = bundle.getString(APIConnection.TAG_CODE);
		this.price = bundle.getDouble(APIConnection.TAG_PRICE);

		if (UserProperties.getPermissionLevel() == UserProperties.PERMISSION_ADMIN) {
			setContentView(R.layout.activity_product_information_admin);

			((Button) findViewById(R.id.btnInfos_add))
			.setOnClickListener(mAddButtonClickListener);
			((Button) findViewById(R.id.btnInfos_delete))
			.setOnClickListener(mDeleteButtonClickListener);
		}
		else {
			setContentView(R.layout.activity_product_information_user);

			if (count == 0){
				((Button) findViewById(R.id.buttonInfosYes)).setVisibility(View.GONE);
				((Button) findViewById(R.id.buttonInfosNo)).setVisibility(View.GONE);
				((TextView) findViewById(R.id.textViewInfosQuestion)).setText("Il n'y a plus de canettes de "+ name +" en stock.");
			}
			else {
				((Button) findViewById(R.id.buttonInfosYes)).setOnClickListener(mTakeACanetteButton);
				((Button) findViewById(R.id.buttonInfosNo)).setOnClickListener(mReturnButton);
			}
		}

		textViewInfosName = (TextView) findViewById(R.id.textViewInfosName);
		textViewInfosCount = (TextView) findViewById(R.id.textViewInfosCount);
		textViewInfosPrice = (TextView) findViewById(R.id.textViewInfosPrice);

		textViewInfosName.setText(name);
		textViewInfosCount.setText(String.valueOf(count));
		textViewInfosPrice.setText(String.valueOf(price));

	}

	private OnClickListener mAddButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			count++;
			textViewInfosCount.setText(String.valueOf(count));
			new SetCount(code, count).execute();
		}
	};

	private OnClickListener mDeleteButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (count>0){
				count--;
				textViewInfosCount.setText(String.valueOf(count));
				new SetCount(code, count).execute();
			}
			else
				Toast.makeText(ProductInformationActivity.this, name + " est déjà nul.", Toast.LENGTH_SHORT).show();
		}
	};

	private OnClickListener mTakeACanetteButton = new OnClickListener() {
		@Override
		public void onClick(View v) {
			count--;
			textViewInfosCount.setText(String.valueOf(count));
			new SetCount(code, count).execute();

			((Button) findViewById(R.id.buttonInfosYes)).setVisibility(View.GONE);
			((Button) findViewById(R.id.buttonInfosNo)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.textViewInfosQuestion)).setText("Bonne dégustation !");
		}
	};

	private OnClickListener mReturnButton = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	/**
	 * SetCount
	 * */
	private class SetCount extends AsyncTask<String, String, String> {

		private boolean success;
		private String code;
		private int count;

		public SetCount(String code, int count) {
			this.code = code;
			this.count = count;
			success = false;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... args) {
			success = APIConnection.setCount(code, String.valueOf(count));

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			if (!success)
				Toast.makeText(ProductInformationActivity.this, "Un problème est survenu.", Toast.LENGTH_SHORT).show();
		}

	}

}
