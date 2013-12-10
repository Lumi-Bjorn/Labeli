package com.labeli.activities;

import java.util.Vector;

import net.labeli.APIConnection;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.labeli.R;
import com.labeli.user.UserProperties;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.TextView;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SelectionActivity extends Activity implements OnClickListener {
	private ProgressDialog pDialog;
	private Button scanBtn;
	private TextView textViewDispo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (UserProperties.getPermissionLevel()== UserProperties.PERMISSION_ADMIN){
			setContentView(R.layout.activity_selection_admin);
		}
		else {
			setContentView(R.layout.activity_selection_user);
		}
		textViewDispo = (TextView) findViewById(R.id.textViewCanettesDisponibles);
		scanBtn = (Button) findViewById(R.id.scan_button);
		scanBtn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.selection, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.show_local) {
			new GetState().execute();

			return true;
		} 

		return false;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.scan_button) {
			IntentIntegrator scanIntegrator = new IntentIntegrator(this);
			scanIntegrator.initiateScan();
		}
	}

	public void gerer(View v) {
		Intent intent = new Intent(getApplicationContext(),
				ProductManagementActivity.class);
		startActivity(intent);
	}
	
	public void gererLocal(View v){
		Intent intent = new Intent(getApplicationContext(),
				LocalActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		new LoadAllCanettes().execute();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		if (scanningResult != null) {
			String scanContent = scanningResult.getContents();
			new GetCanetteWithID(scanContent).execute();
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Aucun code-barre détécté.", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	/**
	 * GetCanetteWithID
	 * */
	class GetCanetteWithID extends AsyncTask<String, String, String> {

		private String code;
		private Intent intent;
		private boolean productFound;

		public GetCanetteWithID(String code) {
			productFound = false;
			intent = new Intent(getApplicationContext(),
					ProductInformationActivity.class);
			this.code = code;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... args) {
			Product tmp = APIConnection.getProductWithCode(code);

			if (tmp != null) {
				productFound = true;	
				intent.putExtra(APIConnection.TAG_NAME, tmp.getName());
				intent.putExtra(APIConnection.TAG_COUNT, tmp.getCount());
				intent.putExtra(APIConnection.TAG_CODE, tmp.getCode());
				intent.putExtra(APIConnection.TAG_PRICE, tmp.getPrice());
			}

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			if (productFound)
				startActivity(intent);
			else {
				Toast.makeText(SelectionActivity.this, "Aucune canette trouvée", Toast.LENGTH_SHORT).show();;
			}
		}

	}

	/**
	 * LoadAllCanettes
	 * */
	private class LoadAllCanettes extends AsyncTask<String, String, String> {

		String dispo;

		public LoadAllCanettes() {
			dispo = "Produits disponibles : ";
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SelectionActivity.this);
			pDialog.setMessage("Chargement des produits en cours ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			Vector<Product> products =  APIConnection.getProducts();

			if (products.size()==0)
				dispo += "Aucune";
			else{
				for (int i = 0; i < products.size(); i++) {
					if (Integer.valueOf(products.get(i).getCount()) > 0)
						dispo += "\n\t\t"+ products.get(i).getName() + " (" + products.get(i).getCount() + ")";
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			textViewDispo.setText(dispo);
			pDialog.dismiss();
		}
	}

	/**
	 * SetCount
	 * */
	private class GetState extends AsyncTask<String, String, String> {

		private boolean isOpened;
		
		public GetState() {
			isOpened = false;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... args) {
			isOpened = APIConnection.getState();

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			if (isOpened)
				Toast.makeText(SelectionActivity.this, "Le local est ouvert", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(SelectionActivity.this, "Le local est fermé", Toast.LENGTH_SHORT).show();
		}

	}
}
