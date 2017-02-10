package com.frkn.physbasic.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frkn.physbasic.Chapter;
import com.frkn.physbasic.R;

import java.util.List;

/**
 * Created by frkn on 08.11.2016.
 */

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.MyViewHolder> {

    private List<Chapter> chapterList;
    public static final int ITEM_UNLOCKED = 0;
    public static final int ITEM_LOCKED = 1;
    private int accountType = 0;

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

    public ChapterAdapter(List<Chapter> chapterList) {
        this.chapterList = chapterList;
    }

    public void setAccountType(int _accountType) {
        this.accountType = _accountType;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);
        holder.icon_image.setImageResource(chapter.getImageId());
        holder.title.setText(chapter.getTitle());
        holder.definiton.setText(chapter.getDefinition());
        if(accountType == 0 && chapter.isLock()) {
            holder.lock_image.setImageResource(R.drawable.ic_lock);
        } else{
            holder.lock_image.setImageResource(R.drawable.ic_lock_open);
        }
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chapterList.get(position).isLock())
            return ITEM_LOCKED;
        else
            return ITEM_UNLOCKED;
    }
}
