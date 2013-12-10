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
import android.widget.ImageView;

public class SelectionActivity extends Activity implements OnClickListener {
	private ProgressDialog pDialog;
	private TextView textViewImgLocalText;
	private ImageView imgLocal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selection);


		imgLocal = (ImageView)findViewById(R.id.imgLocal);
		((ImageView)findViewById(R.id.imgProducts)).setOnClickListener(gerer);

		if (UserProperties.getPermissionLevel()== UserProperties.PERMISSION_ADMIN){
			imgLocal.setOnClickListener(changeState);
		}

		textViewImgLocalText = (TextView)findViewById(R.id.imgLocalText);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.selection, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_refresh) {
			if (MainActivity.isConnected(getApplicationContext())){
				new GetState().execute();
			}
			else
				Toast.makeText(SelectionActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();

			return true;
		} 

		return false;
	}

	private OnClickListener gerer = new OnClickListener() {
		@Override
		public void onClick(View v) {
			gerer(v);
		}
	};

	private OnClickListener changeState = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (MainActivity.isConnected(getApplicationContext())){
				new ChangeState().execute();
			}
			else
				Toast.makeText(SelectionActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();
		}
	};

	public void gerer(View v) {
		Intent intent = new Intent(getApplicationContext(),
				ProductManagementActivity.class);
		startActivity(intent);
	}

	@Override
	public void onResume(){
		super.onResume();
		if (MainActivity.isConnected(getApplicationContext())){
			new GetState().execute();
		}
		else
			Toast.makeText(SelectionActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();
	}

	/**
	 * SetCount
	 * */
	private class GetState extends AsyncTask<String, String, String> {

		private boolean isOpened;

		public GetState() {
			isOpened = false;
			pDialog = new ProgressDialog(SelectionActivity.this);
			pDialog.setMessage("Chargement du local");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
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
			if (isOpened){
				imgLocal.setImageResource(R.drawable.vert);
				textViewImgLocalText.setText(R.string.text_local_state_open);
			}
			else {
				imgLocal.setImageResource(R.drawable.rouge);
				textViewImgLocalText.setText(R.string.text_local_state_close);
			}
			pDialog.dismiss();
		}

	}

	private class ChangeState extends AsyncTask<String, String, String> {

		protected void onPreExecute(){
			pDialog = new ProgressDialog(SelectionActivity.this);
			pDialog.setMessage("Modification du local");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			APIConnection.toggleState();
			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();

			new GetState().execute();
		}

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}
}
