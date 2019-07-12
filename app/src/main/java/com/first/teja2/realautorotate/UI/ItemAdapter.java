package com.first.teja2.realautorotate.UI;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.first.teja2.realautorotate.Model.AppsInfo;
import com.first.teja2.realautorotate.R;
import com.first.teja2.realautorotate.Service.realAutorotateService;
import com.first.teja2.realautorotate.ViewModel.MainViewModel;

import java.util.List;

/**
 * Adapter for the RecyclerView
 * -
 * Created by
 * Bala Guna Teja Karlapudi
 */


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    List<AppsInfo> itemList;
    Context context;
    MainViewModel mainViewModel;

    public ItemAdapter(List<AppsInfo> itemList, Context context, MainViewModel mainViewModel) {
        this.itemList = itemList;
        this.context = context;
        this.mainViewModel = mainViewModel;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.name.setText(itemList.get(position).getAppName());

//        Drawable icon=null;
//
//        try {
//            icon = context.getPackageManager().getApplicationIcon(itemList.get(position).getAppPackageName());
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }

        //holder.appIcon.setImageDrawable(icon);

        //Glide.with(context).load(icon).into(holder.appIcon);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mainViewModel.setSelectedApps(context, itemList, itemList.get(position));
                context.stopService(new Intent(context, realAutorotateService.class));
                context.startService(new Intent(context, realAutorotateService.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private ImageView delete;
        private ImageView appIcon;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.title);
            delete = itemView.findViewById(R.id.deleteApp);
            appIcon = itemView.findViewById(R.id.appIcon);

        }
    }

}


