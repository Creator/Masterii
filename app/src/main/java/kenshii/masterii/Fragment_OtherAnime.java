package kenshii.masterii;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shane- on 09/09/2015.
 */
public class Fragment_OtherAnime extends Fragment {
    LinearLayoutManager mLayoutManager;
    RecyclerView mRecyclerView;
    Context context = null;
    Menu menuInstance;
    RecyclerAdapter mRecyclerAdapter = null;
    View layout;
    boolean loading = true;
    boolean ranOnce = false;
    ProgressDialog dialog;
    int curNum = 0;
    JSONArray seriesArray;
    boolean viewShown = true;
    String seriesType = "-1";

    public Fragment_OtherAnime(Context context, Menu menuInstance, String seriesType) {
        this.context = context;
        this.menuInstance = menuInstance;
        mRecyclerAdapter = new RecyclerAdapter(context,1);
        this.seriesType = seriesType;
    }
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutManager = new LinearLayoutManager(getActivity());
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View v = inflater.inflate(R.layout.anime_recent, container, false);
        dialog = new ProgressDialog(context);
        if (layout == null)
            layout = inflater.inflate(R.layout.recycler, container, false);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view);
        if (mRecyclerView.getLayoutManager()!=mLayoutManager) {
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }
        if (!viewShown) setUserVisibleHint(true);
        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_filter, menu);
        // Get dynamic menu item
    }

    Callback seriesGet_Callback = new Callback() { // Required in order to get the 'Remember me' cookie.
        @Override
        public void onFailure(Request request, IOException e) {
            backgroundShortToast("Anime request failed with exception: " + e.getMessage());
        }

        @Override
        public void onResponse(Response response) {
            if (response.code() != 200) {
                backgroundShortToast("Anime request failed with error code: " + Integer.toString(response.code()));
                return;
            }
            try {
                seriesAttempt(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                ((Activity_Main)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                    }
                });
                ranOnce = false;
            }
        }
    };

    public void backgroundShortToast(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void seriesAttempt(String responseBody) {
        ((Activity_Main)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        dialog.setMessage("Loading. Please wait...");
                        dialog.setIndeterminate(true);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                });
            }
        });
        try {
            if (((Activity_Main)context).jsonArray_anime == null)
            ((Activity_Main) context).jsonArray_anime = new JSONArray(responseBody);
            JSONArray tmpArray = new JSONArray();
            for (int i = 0;i<((Activity_Main)context).jsonArray_anime.length();i++) {
                if (((Activity_Main)context).jsonArray_anime.getJSONObject(i).getString("type").equals(seriesType))
                    tmpArray.put(((Activity_Main)context).jsonArray_anime.getJSONObject(i));
            }
            seriesArray = tmpArray;
            for (int i = curNum; i < curNum + 12; i++) {
                if (curNum >= seriesArray.length()) break;
                JSONObject jsonObject = seriesArray.getJSONObject(i);
                OkHttpClient client = new OkHttpClient();
                Request synopsisGrab_Req = new Request.Builder()
                        .url("https://hummingbird.me/api/v1/anime/" + jsonObject.getString("hum_id"))
                        .header("User-Agent", getString(R.string.app_name) + " Android App")
                        .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                        .build();
                Response synopsisResponse = null;
                synopsisResponse = client.newCall(synopsisGrab_Req).execute();
                if (synopsisResponse.code() != 200) {
                    backgroundShortToast("Synopsis request failed with error code: " + Integer.toString(synopsisResponse.code()));
                    continue;
                }
                JSONObject synopsisJSON = new JSONObject(synopsisResponse.body().string());
                final Map<String, String> stringMap = new HashMap<>();
                stringMap.put("ep_title", synopsisJSON.getString("title"));
                stringMap.put("ep_thumb", synopsisJSON.getString("cover_image"));
                stringMap.put("ep_desc", synopsisJSON.getString("synopsis"));
                stringMap.put("ep_number", "0");
                stringMap.put("ep_rating", String.format ("%.2f", round((double)synopsisJSON.get("community_rating"),2)));
                ((Activity_Main) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            this.notify();
                        }
                        mRecyclerAdapter.addItemToList(stringMap);
                    }
                });
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        ((Activity_Main)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecyclerAdapter.notifyDataSetChanged();
                dialog.hide();
                curNum += 12;
                loading = true;
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) return;
        if (mRecyclerView == null) {
            viewShown = false;
            return;
        }
        viewShown = true;
        mRecyclerView.clearOnScrollListeners();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            Integer visibleItemCount, totalItemCount, pastVisiblesItems;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        new Thread(new Runnable() {
                            public void run() {
                                if (curNum < seriesArray.length()) {
                                    seriesAttempt(null);
                                }
                            }
                        }).start();
                    }
                }
            }
        });
        if (!ranOnce) {
            ranOnce = true;
            if (((Activity_Main) context).jsonArray_anime == null) {
                OkHttpClient client = new OkHttpClient();
                Request seriesGet_Req = new Request.Builder()
                        .url("http://www.masterani.me/api/anime-all")
                        .header("User-Agent", getString(R.string.app_name) + " Android App")
                        .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                        .build();
                client.newCall(seriesGet_Req).enqueue(seriesGet_Callback);
            } else if (((Activity_Main) context).jsonArray_anime != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            seriesAttempt(null);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }
}