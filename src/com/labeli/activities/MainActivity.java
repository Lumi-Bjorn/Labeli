package com.labeli.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import net.labeli.APIConnection;

import com.labeli.R;
import com.labeli.user.UserProperties;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static EditText vEditTextLogin;
	private static EditText vEditTextPassword;
	public static int PERMISSION_LEVEL;
	private ProgressDialog pDialog;
	private File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		vEditTextLogin = (EditText) findViewById(R.id.editTextLogin);
		vEditTextPassword = (EditText) findViewById(R.id.editTextPassword);
		((Button)findViewById(R.id.buttonConnexion)).setOnClickListener(connexion);

		file = new File(getFilesDir(), "logs.ini");
		if (file.exists()){
			try {
				Scanner sc = new Scanner(file);
				vEditTextLogin.setText(sc.nextLine());
				vEditTextPassword.setText(sc.nextLine());
				sc.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private OnClickListener connexion = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!champVide(vEditTextLogin) && !champVide(vEditTextPassword)){
				if (MainActivity.isConnected(getApplicationContext()))
					new GetPermission().execute();
				else
					Toast.makeText(MainActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(MainActivity.this, "Veuillez remplir les champs", Toast.LENGTH_SHORT).show();
			}

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.show_local) {
			new GetState().execute();

			return true;
		} 

		return false;
	}

	private boolean champVide(EditText e){
		boolean estVide = true;

		if (!e.getText().toString().equals("")) estVide = false;

		return estVide;		
	}

	/**
	 * SetCount
	 * */
	private class GetPermission extends AsyncTask<String, String, String> {

		private int tmpLevel;
		public GetPermission() {
			tmpLevel = -1;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Connexion en cours");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			tmpLevel = APIConnection.getPermissionLevelForUser(vEditTextLogin.getText().toString(), vEditTextPassword.getText().toString());

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();

			UserProperties.setPermissionLevel(tmpLevel);
			if (UserProperties.getPermissionLevel() == UserProperties.PERMISSION_ERROR){
				Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
			}
			else if (UserProperties.getPermissionLevel() == UserProperties.PERMISSION_DENIED){
				Toast.makeText(MainActivity.this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
			}
			else {
				// Enregistrement des logs de connexion
				try {
					if (! new File("logs.ini").exists()) new File("logs.ini").createNewFile();
					FileOutputStream print = openFileOutput("logs.ini", Context.MODE_PRIVATE);
					String logs = vEditTextLogin.getText().toString() + "\n" + vEditTextPassword.getText().toString() + "\n";
					print.write(logs.getBytes());
					print.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent intent = new Intent(getApplicationContext(), SelectionActivity.class);
				startActivity(intent);
			}
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
				Toast.makeText(MainActivity.this, "Le local est ouvert", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(MainActivity.this, "Le local est fermé", Toast.LENGTH_SHORT).show();
		}

	}

	public static boolean isConnected(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null)
		{
			State networkState = networkInfo.getState();
			if (networkState.compareTo(State.CONNECTED) == 0)
				return true;
		}
		return false;
	}

}
