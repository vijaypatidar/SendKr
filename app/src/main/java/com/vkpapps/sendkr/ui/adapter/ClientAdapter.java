package com.vkpapps.sendkr.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vkpapps.sendkr.R;
import com.vkpapps.sendkr.connection.ClientHelper;
import com.vkpapps.sendkr.model.User;
import com.vkpapps.sendkr.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.MyHolder> {

    private List<ClientHelper> users = new ArrayList<>();


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list_item, parent, false);
        return new MyHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final ClientHelper clientHelper = users.get(position);
        final User user = clientHelper.getUser();
        holder.userName.setText(user.getName());
        byte[] profileByteArray = user.getProfileByteArray();
        if (profileByteArray.length > 0) {
            holder.profilePic.setImageBitmap(BitmapUtils.INSTANCE.byteArrayToBitmap(user.getProfileByteArray()));
        }
        holder.btnDisconnect.setOnClickListener(v -> clientHelper.shutDown());
    }

    @Override
    public int getItemCount() {
        return (users == null) ? 0 : users.size();
    }

    public void setUsers(List<ClientHelper> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final ImageView profilePic;
        private final ImageButton btnDisconnect;

        MyHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            profilePic = itemView.findViewById(R.id.profilePic);
            btnDisconnect = itemView.findViewById(R.id.btnDisconnect);
        }
    }
}
