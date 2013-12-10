package com.labeli.activities;

import net.labeli.APIConnection;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.labeli.R;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddProductActivity extends Activity {

	private EditText vEditTextAddProductName;
	private EditText vEditTextAddProductCount;
	private EditText vEditTextAddProductCode;
	private EditText vEditTextAddProductPrice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_product);

		vEditTextAddProductName = (EditText) findViewById(R.id.editTextAddProductName);
		vEditTextAddProductCount = (EditText) findViewById(R.id.editTextAddProductCount);
		vEditTextAddProductCode = (EditText) findViewById(R.id.editTextAddProductCode);
		vEditTextAddProductPrice = (EditText) findViewById(R.id.editTextAddProductPrice);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_product, menu);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home)
			onBackPressed();
		return true;
	}

	public void ajouterCanette(View v) {
		if (MainActivity.isConnected(getApplicationContext())){
			new AddProduct().execute();
			finish();
		}
		else
			Toast.makeText(AddProductActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();

	}

	public void scanner(View v) {
		IntentIntegrator scanIntegrator = new IntentIntegrator(this);
		scanIntegrator.initiateScan();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanningResult != null) {
			String scanContent = scanningResult.getContents();
			vEditTextAddProductCode.setText(scanContent);
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),"No scan data received!", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	/**
	 * AddCanettes
	 * */
	private class AddProduct extends AsyncTask<String, String, String> {
		private boolean success;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			success = false;
		}

		@Override
		protected String doInBackground(String... args) {
			String type = vEditTextAddProductName.getText().toString();
			String nombre = vEditTextAddProductCount.getText().toString();
			String code = vEditTextAddProductCode.getText().toString();
			String price = vEditTextAddProductPrice.getText().toString();

			success = APIConnection.addProduct(type, nombre, code, price);

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			if (success)
				Toast.makeText(AddProductActivity.this,	"Le produit a été rajoutée à la base de données.", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(AddProductActivity.this, "Un problème est survenu.", Toast.LENGTH_SHORT).show();
		}

	}

}
