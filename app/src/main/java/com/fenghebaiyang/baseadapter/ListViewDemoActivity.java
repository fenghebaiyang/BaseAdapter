package com.fenghebaiyang.baseadapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.fenghebaiyang.library.BaseXAdapter;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ListViewDemoActivity extends AppCompatActivity {

    private SwipeRefreshLayout refreshLayout;
    private ListView listView;
    private BaseXAdapter adapter;
    private int maxPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_demo);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        listView = (ListView) findViewById(R.id.list_view);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(false);
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            private boolean mIsEnd = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (mIsEnd) {
                        getData(true);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
                    mIsEnd = true;
                } else {
                    mIsEnd = false;
                }
            }
        });

        adapter = new BaseXAdapter<HuaBanBean.PinsBean>(this) {
            @Override
            public int getConvertViewRes(int position, int type) {
                return R.layout.item_huaban;
            }

            @Override
            public View getItemView(int position, View convertView, ViewGroup parent, ViewHolder viewHolder) {
                String imgUrl = "";
                imgUrl = "http://hbimg.b0.upaiyun.com/" + list.get(position).getFile().getKey();
                AspectRatioImageView imageView = viewHolder.getView(R.id.img_pro);
                imageView.setRatio(MathUtils.str2Float(list.get(position).getFile().getWidth()) / MathUtils.str2Float(list.get(position).getFile().getHeight()));
                Glide.with(mContext).load(imgUrl).asBitmap().into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        super.setResource(resource);
                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                ((CardView) view.getParent()).setCardBackgroundColor(palette.getMutedColor(Color.parseColor("#ffffff")));
                            }
                        });
                    }
                });
                return convertView;
            }
        };
        listView.setAdapter(adapter);
        getData(false);
    }

    private void getData(final boolean needMore) {
        String url = "http://huaban.com/partner/uc/aimeinv/pins/";
        if (needMore) {
            //加载更多
            url += "?max=" + maxPin;
        }
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                refreshLayout.setRefreshing(false);
                HuaBanBean bean = new Gson().fromJson(response, HuaBanBean.class);
                if (bean.getPins().size() > 0) {
                    maxPin = bean.getPins().get(bean.getPins().size() - 1).getPinId();
                }
                if (!needMore) {
                    adapter.deleteAll();
                }
                adapter.addAll(bean.getPins());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                refreshLayout.setRefreshing(false);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("X-Requested-With", "XMLHttpRequest");
                return header;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }
}
