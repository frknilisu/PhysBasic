package com.frkn.physbasic.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frkn.physbasic.R;
import com.frkn.physbasic.Specials;

import java.util.List;

/**
 * Created by frkn on 21.01.2017.
 */

public class SpecialsAdapter extends RecyclerView.Adapter<SpecialsAdapter.MyViewHolder> {

    private List<Specials> specialList;
    private int accountType;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon_image;
        public TextView title, definiton;
        public ImageView lock_image;

        public MyViewHolder(View view) {
            super(view);
            icon_image = (ImageView) view.findViewById(R.id.item_icon);
            title = (TextView) view.findViewById(R.id.item_title);
            definiton = (TextView) view.findViewById(R.id.item_definition);
            lock_image = (ImageView) view.findViewById(R.id.item_status);
        }
    }

    public SpecialsAdapter(List<Specials> specialList) {
        this.specialList = specialList;
    }

    public void setAccountType(int _accountType) {
        this.accountType = _accountType;
    }

    @Override
    public SpecialsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_row, parent, false);

        return new SpecialsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SpecialsAdapter.MyViewHolder holder, int position) {
        Specials special = specialList.get(position);
        holder.icon_image.setImageResource(special.getImageId());
        holder.title.setText(special.getTitle());
        holder.definiton.setText(special.getDefinition());
        if (accountType == 3) {
            holder.lock_image.setImageResource(R.drawable.ic_lock_open);
        } else {
            holder.lock_image.setImageResource(R.drawable.ic_lock);
        }
    }

    @Override
    public int getItemCount() {
        return specialList.size();
    }

}
