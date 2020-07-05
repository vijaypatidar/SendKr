package com.vkpapps.thunder.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vkpapps.thunder.R;
import com.vkpapps.thunder.connection.ClientHelper;
import com.vkpapps.thunder.model.User;
import com.vkpapps.thunder.utils.StorageManager;

import java.io.File;
import java.util.List;

/**
 * @author VIJAY PATIDAR
 */
public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.MyHolder> {

    private List<ClientHelper> users;
    private File profiles;
    private View view;

    public ClientAdapter(List<ClientHelper> users, View view) {
        this.users = users;
        this.view = view;
        profiles = new StorageManager(view.getContext()).getProfiles();
    }

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
        File file = new File(profiles, user.getUserId());
        if (file.exists()) {
            Picasso.get().load(file).into(holder.profilePic);
        }
        holder.btnDisconnect.setOnClickListener(v -> clientHelper.shutDown());
    }

    @Override
    public int getItemCount() {
        return (users == null) ? 0 : users.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private ImageView profilePic;
        private ImageButton btnDisconnect;
        MyHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            profilePic = itemView.findViewById(R.id.profilePic);
            btnDisconnect = itemView.findViewById(R.id.btnDisconnect);
        }
    }

    public void notifyDataSetChangedAndHideIfNull() {
        if (users.size() == 0) {
            view.findViewById(R.id.emptyClient).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.emptyClient).setVisibility(View.GONE);
            notifyDataSetChanged();
        }
    }
}
