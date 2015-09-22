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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shane- on 09/09/2015.
 */
public class Fragment_RecentAnime extends Fragment {
    LinearLayoutManager mLayoutManager;
    RecyclerView mRecyclerView;
    Context context = null;
    Menu menuInstance;
    RecyclerAdapter mRecyclerAdapter = null;
    View layout;
    boolean loading = true;
    boolean ranOnce = false;
    boolean doneMaxPage = false;
    ProgressDialog dialog;
    Integer curPage = 1;
    boolean viewShown = false;

    public Fragment_RecentAnime(Context context, Menu menuInstance) {
        this.context = context;
        this.menuInstance = menuInstance;
        mRecyclerAdapter = new RecyclerAdapter(context,0);
    }
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutManager = new LinearLayoutManager(getActivity());
        dialog = new ProgressDialog(context);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View v = inflater.inflate(R.layout.anime_recent, container, false);
        if (layout == null)
            layout = inflater.inflate(R.layout.recycler, container, false);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view);
        if (mRecyclerView.getLayoutManager() != mLayoutManager) {
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }
        if (!viewShown) setUserVisibleHint(true);
        return layout;
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
                curPage++;
            } catch (IOException e) {
                e.printStackTrace();
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

    public void seriesAttempt(String responseBody) {
        if (doneMaxPage)
            return;
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
        String respStr = StringEscapeUtils.unescapeJava(responseBody.substring(1, responseBody.length() - 1));
        Document doc = Jsoup.parse(respStr);
        Elements elems = doc.select("div[class=column]");
        for (Element elem : elems) {
            Elements epThumb_Elements = elem.select("div.image img"); // Image URL
            if (epThumb_Elements.size() == 0) continue;
            String[] splitThumb = epThumb_Elements.get(0).attr("src").split("/");
            String hum_id = splitThumb[5] + splitThumb[6] + splitThumb[7];
            hum_id = StringUtils.stripStart(hum_id, "0");
            String episode_num = elem.select("div.ui.mini.statistic div.value").get(0).text().trim();
            OkHttpClient client = new OkHttpClient();
            Request synopsisGrab_Req = new Request.Builder()
                    .url("https://hummingbird.me/api/v1/anime/" + hum_id)
                    .header("User-Agent", getString(R.string.app_name) + " Android App")
                    .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                    .build();
            Response synopsisResponse = null;
            try {
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
                stringMap.put("ep_number", episode_num);
                ((Activity_Main) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            this.notify();
                        }
                        mRecyclerAdapter.addItemToList(stringMap);
                    }
                });
        }catch(IOException | JSONException e){
            backgroundShortToast("Hummingbird isn't loading...\n\nNuck?");
            e.printStackTrace();
        }
    }
        ((Activity_Main)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerAdapter.getItemCount() % 12 != 0) doneMaxPage = true;
                mRecyclerAdapter.notifyDataSetChanged();
                dialog.hide();
            }
        });
        loading = true;
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
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            Integer visibleItemCount, totalItemCount, pastVisiblesItems;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                if (loading && !doneMaxPage) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        OkHttpClient client = new OkHttpClient();
                        Request seriesGet_Req = new Request.Builder()
                                .url("http://www.masterani.me/?page=" + Integer.toString(curPage))
                                .header("User-Agent", getString(R.string.app_name) + " Android App")
                                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                                .addHeader("X-Requested-With", "XMLHttpRequest")
                                .build();
                        client.newCall(seriesGet_Req).enqueue(seriesGet_Callback);
                        loading = false;
                    }
                }
            }
        });
        OkHttpClient client = new OkHttpClient();
        if (!ranOnce) {
            ranOnce = true;
            final ViewPager viewPager = (ViewPager) ((Activity_Main)context).findViewById(R.id.pager);
            final TabLayout tabLayout = (TabLayout) ((Activity_Main)context).findViewById(R.id.tab_layout);
            viewPager.clearOnPageChangeListeners();
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                }
            });
            Request seriesGet_Req = new Request.Builder()
                    .url("http://www.masterani.me/?page=" + Integer.toString(curPage))
                    .header("User-Agent", getString(R.string.app_name) + " Android App")
                    .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .build();
            client.newCall(seriesGet_Req).enqueue(seriesGet_Callback);
        }
    }
}