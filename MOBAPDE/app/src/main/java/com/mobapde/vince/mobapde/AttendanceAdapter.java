package com.mobapde.vince.mobapde;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Vince on 3/22/2017.
 */

public class AttendanceAdapter extends FirebaseRecyclerAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder>{

    static Context context;

    public AttendanceAdapter(Class<Attendance> modelClass, int modelLayout, Class<AttendanceViewHolder> viewHolderClass, DatabaseReference ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = context;
    }

    @Override
    protected void populateViewHolder(final AttendanceViewHolder viewHolder, Attendance model, int position) {
        viewHolder.setFacultyName(model.getFacultyName());
        viewHolder.setRoomName(model.getRoom());
        if(model.getPic() != null)
            viewHolder.setFacultyPic(model.getPic());
        viewHolder.mView.setTag(model);
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Attendance model = (Attendance) view.getTag();
                    onItemClickListener.onItemClick(model);
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

        public void setFacultyName(String facultyName){
            TextView tvFacultyName = (TextView) mView.findViewById(R.id.lbl_item_sub_title);
            tvFacultyName.setText(facultyName);
        }

        public void setRoomName(String roomName){
            TextView tvRoomName = (TextView) mView.findViewById(R.id.lbl_item_text);
            tvRoomName.setText(roomName);
        }

        public void setFacultyPic(String url){
            CircleImageView profPic = (CircleImageView) mView.findViewById(R.id.im_item_icon);
            Picasso.with(context).load(url).into(profPic);
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(Attendance model);
    }
}
