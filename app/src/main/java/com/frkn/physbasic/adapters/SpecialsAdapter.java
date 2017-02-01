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
        public ImageView image;
        public TextView title, definiton;

        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            definiton = (TextView) view.findViewById(R.id.definition);
        }
    }

    public SpecialsAdapter(List<Specials> specialList) {
        this.specialList = specialList;
    }

    public void setAccountType(int _accountType){
        this.accountType = _accountType;
    }

    @Override
    public SpecialsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_row, parent, false);
        if(accountType  == 3){
            itemView.setBackgroundResource(R.drawable.images);
        } else{
            itemView.setBackgroundResource(R.drawable.images2);
        }

        return new SpecialsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SpecialsAdapter.MyViewHolder holder, int position) {
        Specials special = specialList.get(position);
        holder.image.setImageResource(special.getImageId());
        holder.title.setText(special.getTitle());
        holder.definiton.setText(special.getDefinition());
    }

    @Override
    public int getItemCount() {
        return specialList.size();
    }

}
