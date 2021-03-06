package com.richard.abigayle.hotelfinder.Activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.richard.abigayle.hotelfinder.Activities.DetailsActivity.HotelDetailsActivity;
import com.richard.abigayle.hotelfinder.Helpers.HotelDao;
import com.richard.abigayle.hotelfinder.Helpers.HotelDatabase;
import com.richard.abigayle.hotelfinder.Helpers.Hotels;
import com.richard.abigayle.hotelfinder.R;
import com.richard.abigayle.hotelfinder.UiHelpers.HotelAdapter;
import com.richard.abigayle.hotelfinder.UiHelpers.HotelListViewModel;
import com.richard.abigayle.hotelfinder.UiHelpers.HotelTouchListerner;

import java.util.List;

public class HotelList extends AppCompatActivity {
    RecyclerView recyclerView;

    HotelDao hotelDao;
    List<Hotels> hotelsList;
    private HotelListViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_list);
        final HotelAdapter hotelAdapter = new HotelAdapter(this);
        hotelDao = HotelDatabase.getInstance(getApplication()).hotelDao();
        ProgressBar progressBar = findViewById(R.id.list_progress);

        recyclerView = findViewById(R.id.recycle_view);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(hotelAdapter);
        recyclerView.addOnItemTouchListener(new HotelTouchListerner(getApplicationContext(), recyclerView, new HotelTouchListerner.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(HotelList.this,HotelDetailsActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        viewModel = ViewModelProviders.of(this).get(HotelListViewModel.class);
        viewModel.getAllHotel().observe(this, new Observer<List<Hotels>>() {
            @Override
            public void onChanged(@Nullable List<Hotels> hotels) {
                progressBar.setVisibility(View.GONE);
                hotelAdapter.setHotels(hotels);
            }
        });







    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {

                hotelDao.deleteAll();

                return null;
            }
        }.execute();


    }
}
