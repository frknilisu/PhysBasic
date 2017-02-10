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

    public TestAdapter(List<Test> testList) {
        this.testList = testList;
    }

    public void setAccountType(int _accountType) {
        this.accountType = _accountType;
    }

    @Override
    public TestAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_row, parent, false);

        return new TestAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TestAdapter.MyViewHolder holder, int position) {
        Test test = testList.get(position);
        holder.icon_image.setImageResource(test.getImageId());
        holder.title.setText(test.getTitle());
        holder.definiton.setText(test.getDefinition());
        if (accountType >= 2) {
            holder.lock_image.setImageResource(R.drawable.ic_lock_open);
        } else {
            holder.lock_image.setImageResource(R.drawable.ic_lock);
        }
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

}
