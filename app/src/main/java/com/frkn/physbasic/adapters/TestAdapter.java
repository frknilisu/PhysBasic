package com.frkn.physbasic.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frkn.physbasic.R;
import com.frkn.physbasic.Test;

import java.util.List;

/**
 * Created by frkn on 21.01.2017.
 */

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.MyViewHolder> {

    private List<Test> testList;
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

    public TestAdapter(List<Test> testList) {
        this.testList = testList;
    }

    public void setAccountType(int _accountType){
        this.accountType = _accountType;
    }

    @Override
    public TestAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_row, parent, false);
        if(accountType >= 2){
            itemView.setBackgroundResource(R.drawable.images);
        } else{
            itemView.setBackgroundResource(R.drawable.images2);
        }

        return new TestAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TestAdapter.MyViewHolder holder, int position) {
        Test test = testList.get(position);
        holder.image.setImageResource(test.getImageId());
        holder.title.setText(test.getTitle());
        holder.definiton.setText(test.getDefinition());
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    public String getAdapterName(){
        return "TestAdapter";
    }
}
