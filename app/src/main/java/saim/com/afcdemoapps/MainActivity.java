package saim.com.afcdemoapps;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import saim.com.afcdemoapps.Adapter.AdapterPost;
import saim.com.afcdemoapps.Model.ModelImage;
import saim.com.afcdemoapps.Utility.ApiURL;
import saim.com.afcdemoapps.Utility.MySingleton;

public class MainActivity extends AppCompatActivity {

    public static Toolbar toolbar;
    public SearchView searchView;
    ProgressBar progressBar;

    ArrayList<ModelImage> arrayListImage = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManagerRecyclerView;
    RecyclerView.Adapter recyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void init(){
        toolbar = (Toolbar) findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Home");

        progressBar = findViewById(R.id.progressBar);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        layoutManagerRecyclerView = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManagerRecyclerView);
        recyclerView.setHasFixedSize(true);

        //Getting Web Request
        try {
            GetRequest();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void GetRequest() throws JSONException {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", "IDDL");
        jsonBody.put("mobile_no", "01823824732938");
        final String mRequestBody = jsonBody.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiURL.link_json,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        recyclerView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Log.d("SAIM RESPONSE", response);
                        try {
                            PopulateView(response);
                        }catch (Exception e){
                            Log.d("HDHD 1", e.toString() + "\n" + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("APPTOKEN", "4fb8744b8easjdaslkdasjdjksg7c4f45e6dd");
                params.put("APPAUTHID", "iddl_interview");
                params.put("Content-Type", "application/json");

                return params;
            }
        };
        stringRequest.setShouldCache(false);
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }


    public void PopulateView(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        int status = jsonObject.getInt("status");
        String message = jsonObject.getString("message");

        if (status == 200){
            JSONObject jsonObjectData = jsonObject.getJSONObject("data");
            JSONArray jsonArrayImages = jsonObjectData.getJSONArray("images");
            for (int i=0; i<jsonArrayImages.length(); i++){
                JSONObject jsonObjectImages = jsonArrayImages.getJSONObject(i);

                String url = jsonObjectImages.getString("url");
                String title = jsonObjectImages.getString("title");
                int height = jsonObjectImages.getInt("height");
                int width = jsonObjectImages.getInt("width");
                String upload_at = jsonObjectImages.getString("upload_at");

                Log.d("SAIM SAIM", url + "\n" + title + "\n" + height + "\n" + width + "\n" + upload_at + "\n\n");
                ModelImage modelImage = new ModelImage(url, title, upload_at, height, width);
                arrayListImage.add(modelImage);

            }
        } else if (status == 400) {
            showDialog(message);
        } else if (status == 480) {
            showDialog(message);
        }

        recyclerViewAdapter = new AdapterPost(arrayListImage);
        recyclerView.setAdapter(recyclerViewAdapter);
    }


    public void showDialog(String message){

        LayoutInflater factory = LayoutInflater.from(this);
        final View deleteDialogView = factory.inflate(R.layout.dialog_message, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
        deleteDialog.setView(deleteDialogView);
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        TextView txtMessage = (TextView) deleteDialogView.findViewById(R.id.txtDialog);
        txtMessage.setText(message);
        deleteDialogView.findViewById(R.id.btnDialogYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        deleteDialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        } else if(id == android.R.id.home){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    void filter(String text){
        ArrayList<ModelImage> temp = new ArrayList();
        for(ModelImage d: arrayListImage){
            if(d.title.toLowerCase().contains(text.toLowerCase())){
                temp.add(d);
            }
        }
        recyclerViewAdapter = new AdapterPost(temp);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();
    }
}
