package com.dimas.testtask;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dimas.testtask.Model.CoinModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder> {

    //Create variables
    private ArrayList<CoinModel> coinModelArrayList;
    private Context context;
    private static DecimalFormat df = new DecimalFormat("#.##");

    public CurrencyAdapter(ArrayList<CoinModel> coinModelArrayList, Context context) {
        this.coinModelArrayList = coinModelArrayList;
        this.context = context;

    }

    @NonNull
    @Override
    public CurrencyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.coin_layout,parent,false);
        return new CurrencyAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyAdapter.ViewHolder holder, int position) {

        //Get data form  Array list

        CoinModel coinModel = coinModelArrayList.get(position);
        holder.currencyName.setText(coinModel.getName());
        holder.currencySymbol.setText(coinModel.getSymbol());
        holder.currencyPrice.setText("$ "+df.format(coinModel.getPrice()));

    }

    @Override
    public int getItemCount() {

        return coinModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView currencyName, currencySymbol, currencyPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            currencyName = itemView.findViewById(R.id.idName);
            currencySymbol = itemView.findViewById(R.id.idSymbol);
            currencyPrice = itemView.findViewById(R.id.idPrice);

        }
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context,ChartActivity.class);
            context.startActivity(intent);

        }
    }
}
