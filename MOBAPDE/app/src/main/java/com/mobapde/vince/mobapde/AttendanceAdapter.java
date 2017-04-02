package com.mobapde.vince.mobapde;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by Vince on 3/22/2017.
 */

public class AttendanceAdapter extends FirebaseRecyclerAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder>{


    public AttendanceAdapter(Class<Attendance> modelClass, int modelLayout, Class<AttendanceViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(final AttendanceViewHolder viewHolder, Attendance model, int position) {
        viewHolder.setName(model.getName());
        viewHolder.mView.setTag(model.getName());
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = (String) view.getTag();
                onItemClickListener.onItemClick(name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public AttendanceViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name){
            TextView tvName = (TextView) mView.findViewById(R.id.lbl_item_text);
            tvName.setText(name);
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(String name);
    }
}
