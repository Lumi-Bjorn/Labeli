package com.labeli.activities;

import java.util.Locale;
import java.util.Vector;

import net.labeli.APIConnection;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.labeli.R;
import com.labeli.user.UserProperties;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ProductManagementActivity extends ListActivity {

	private static final boolean REFRESH_LIST = true;
	private static final boolean NO_REFRESH_LIST = false;

	private ProgressDialog pDialog;
	private CanettesAdapter adapter;
	private Vector<Product> stockList, stockListOrigin;
	private EditText editTextSearch;

	private TextWatcher refreshWithSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_management);

		refreshWithSearch = new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				recreateList(editTextSearch.getText().toString());
			}

		};

		editTextSearch = (EditText) findViewById(R.id.editTextProductSearch);
		editTextSearch.addTextChangedListener(refreshWithSearch);

		((Button)findViewById(R.id.buttonScan)).setOnClickListener(scan);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.product_management, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_compose) {
			Intent intent = new Intent(ProductManagementActivity.this, AddProductActivity.class);
			this.startActivity(intent);

			return true;
		} else if (item.getItemId() == R.id.action_refresh) {
			if (MainActivity.isConnected(getApplicationContext())){
				new LoadAllProducts().execute();
			}
			else
				Toast.makeText(ProductManagementActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();

			return true;
		}

		return false;
	}

	/*protected void onListItemClick(ListView l, View v, int position, long id){
		final int position = getListView().getPositionForView(v);
		if (position != ListView.INVALID_POSITION) {
			stockList.get(position).setCount(stockList.get(position).getCount() + 1);
			adapter.notifyDataSetChanged();
			new UpdateCanette(stockList.get(position).getCode(), stockList.get(position).getName(), stockList.get(position).getCount()).execute();
		}
	}*/

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if(UserProperties.getPermissionLevel() == UserProperties.PERMISSION_ADMIN){
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.product_management_context, menu);

			menu.setHeaderTitle("Que faire ?");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		if (item.getItemId() == R.id.delete){
			if (MainActivity.isConnected(getApplicationContext())){
				new DeleteProduct(stockList.get(info.position).getCode()).execute();
			}
			else
				Toast.makeText(ProductManagementActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();

		}
		else if (item.getItemId() == R.id.add_count){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			// alert.setTitle("Title");
			alert.setMessage("Entrez le nombre à ajouter : ");

			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString();
					if (Integer.valueOf(value) != null){
						if (MainActivity.isConnected(getApplicationContext())){
							stockList.get(info.position).setCount(stockList.get(info.position).getCount() + Integer.valueOf(value));
							adapter.notifyDataSetChanged();
							new SetCount(stockList.get(info.position).getCode(), stockList.get(info.position).getCount()).execute();
						}
						else
							Toast.makeText(ProductManagementActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();

					}
				}
			});
			alert.show();

		}

		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		stockList = new Vector<Product>();
		if (MainActivity.isConnected(getApplicationContext())){
			new LoadAllProducts().execute();
		}
		else
			Toast.makeText(ProductManagementActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		if (scanningResult != null) {
			String scanContent = scanningResult.getContents();
			if (MainActivity.isConnected(getApplicationContext())){
				new GetProductWithID(scanContent).execute();
			}
			else
				Toast.makeText(ProductManagementActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Aucun code-barre détécté.", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private OnClickListener scan = new OnClickListener() {
		@Override
		public void onClick(View v) {
			IntentIntegrator scanIntegrator = new IntentIntegrator(ProductManagementActivity.this);
			scanIntegrator.initiateScan();
		}
	};
	/**
	 * CanettesAdapter & CanettesViewHolder
	 */

	private static class CanettesViewHolder {
		public TextView content;
		public TextView contentNb;
	}

	public class CanettesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return stockList.size();
		}

		@Override
		public String getItem(int position) {
			return stockList.get(position).getName();
		}

		public int getItemCount(int position) {
			return stockList.get(position).getCount();
		}

		public void setItemNb(int position, int value) {
			stockList.get(position).setCount(value);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CanettesViewHolder holder = null;

			if (convertView == null) {
				if (UserProperties.getPermissionLevel() == UserProperties.PERMISSION_ADMIN){
					convertView = getLayoutInflater().inflate(R.layout.list_products_admin, parent, false);
					convertView.setLongClickable(true);

					holder = new CanettesViewHolder();

					holder.content = (TextView) convertView.findViewById(R.id.content);
					holder.contentNb = (TextView) convertView.findViewById(R.id.contentNb);

					((Button) convertView.findViewById(R.id.btn_add)).setOnClickListener(mAddButtonClickListener);
					((Button) convertView.findViewById(R.id.btn_delete)).setOnClickListener(mDeleteButtonClickListener);

					convertView.setTag(holder);
				}
				else {
					convertView = getLayoutInflater().inflate(R.layout.list_products_user, parent, false);
					convertView.setLongClickable(true);

					holder = new CanettesViewHolder();

					holder.content = (TextView) convertView.findViewById(R.id.content);
					holder.contentNb = (TextView) convertView.findViewById(R.id.contentNb);

					convertView.setTag(holder);
				}
			} else {
				holder = (CanettesViewHolder) convertView.getTag();
			}
			holder.content.setText(stockList.get(position).getName());
			holder.contentNb.setText(String.valueOf(stockList.get(position).getCount()));

			return convertView;
		}

		private void showMessage(String message) {
			Toast.makeText(ProductManagementActivity.this, message, Toast.LENGTH_SHORT).show();
		}

		private OnClickListener mAddButtonClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int position = getListView().getPositionForView(v);
				if (position != AdapterView.INVALID_POSITION) {
					if (MainActivity.isConnected(getApplicationContext())){
						stockList.get(position).setCount(stockList.get(position).getCount() + 1);
						adapter.notifyDataSetChanged();
						new SetCount(stockList.get(position).getCode(), stockList.get(position).getCount()).execute();
					}
					else
						Toast.makeText(ProductManagementActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();

				}

			}
		};

		private OnClickListener mDeleteButtonClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int position = getListView().getPositionForView(v);
				if (position != AdapterView.INVALID_POSITION) {
					if (stockList.get(position).getCount() > 0) {
						if (MainActivity.isConnected(getApplicationContext())){
							stockList.get(position).setCount(stockList.get(position).getCount() - 1);
							adapter.notifyDataSetChanged();
							new SetCount(stockList.get(position).getCode(), stockList.get(position).getCount()).execute();
						}
						else
							Toast.makeText(ProductManagementActivity.this, "Aucune connexion détectée", Toast.LENGTH_SHORT).show();
					} else
						showMessage(stockList.get(position).getName() + " est déjà nul.");
				}
			}
		};

	}

	public void refreshList(boolean notifyChange){
		if (!notifyChange){
			adapter = new CanettesAdapter();
			setListAdapter(adapter);
		}
		if (notifyChange) adapter.notifyDataSetChanged();
	}

	public void recreateList(String search){
		stockList.clear();

		if (search.length()==0)
			stockList = (Vector<Product>) stockListOrigin.clone();
		else {
			search = search.toLowerCase(Locale.FRANCE);
			for (int i=0; i < stockListOrigin.size(); i++){
				String tmpName = stockListOrigin.get(i).getName().toLowerCase(Locale.FRANCE);
				if (tmpName.length()>=search.length()){
					if (tmpName.substring(0, search.length()).equals(search)) stockList.add(stockListOrigin.get(i));
				}
			}
		}

		refreshList(REFRESH_LIST);
	}


	/**
	 * LoadAllCanettes
	 * */
	private class LoadAllProducts extends AsyncTask<String, String, String> {

		public LoadAllProducts() {

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ProductManagementActivity.this);
			pDialog.setMessage("Chargement des canettes en cours ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			stockListOrigin = APIConnection.getProducts();
			if (stockListOrigin==null)
				stockListOrigin = new Vector<Product>();
			stockList = (Vector<Product>) stockListOrigin.clone();

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			refreshList(NO_REFRESH_LIST);
			registerForContextMenu(getListView());

			if (stockListOrigin.size()==0) {
				AlertDialog.Builder alert = new AlertDialog.Builder(ProductManagementActivity.this);

				alert.setTitle("Informations");
				alert.setMessage("Pas d'entrées trouvées.");

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});

				alert.show();
			} else
				adapter.notifyDataSetChanged();

			pDialog.dismiss();
		}
	}

	/**
	 * UpdateCanette
	 * N'est plus utilisée pour le moment
	 * Sera utilisée plus tard pour modifier les caractéristiques d'un produit
	 * */
	private class UpdateCanette extends AsyncTask<String, String, String> {

		private boolean reussite;
		private String type, code;
		private int nb;

		public UpdateCanette(String code, String type, int nb) {
			this.code = code;
			this.type = type;
			this.nb = nb;
			reussite = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected String doInBackground(String... args) {
			reussite = APIConnection.updateProduct(code, type, String.valueOf(nb));

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			refreshList(REFRESH_LIST);

			if (!reussite) {
				Toast.makeText(ProductManagementActivity.this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
			}
		}
	}

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
			refreshList(REFRESH_LIST);

			if (!success) {
				Toast.makeText(ProductManagementActivity.this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * DeleteProduct
	 * */
	private class DeleteProduct extends AsyncTask<String, String, String> {

		private boolean success;
		private String code;

		public DeleteProduct(String code) {
			this.code = code;
			success = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected String doInBackground(String... args) {
			success = APIConnection.deleteProduct(code);

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			new LoadAllProducts().execute();
			if (!success) {
				Toast.makeText(ProductManagementActivity.this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show();
			}

		}

	}

	/**
	 * GetProductWithID
	 * */
	class GetProductWithID extends AsyncTask<String, String, String> {

		private String code;
		private Intent intent;
		private boolean productFound;

		public GetProductWithID(String code) {
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
				Toast.makeText(ProductManagementActivity.this, "Aucune canette trouvée", Toast.LENGTH_SHORT).show();;
			}
		}

	}
}
