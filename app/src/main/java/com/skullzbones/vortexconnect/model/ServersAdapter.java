package com.skullzbones.vortexconnect.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.skullzbones.vortexconnect.R;
import com.skullzbones.vortexconnect.Utils.MCClient;
import com.skullzbones.vortexconnect.interfaces.MyServerClickListener;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ServerViewHolder> {


  //this context we will use to inflate the layout
  private Context mCtx;
  private MyServerClickListener listener;
  //we are storing all the products in a list
  private List<Server> productList;

  //getting the context and product list with constructor
  public ServersAdapter(Context mCtx, List<Server> productList, MyServerClickListener listener) {
    this.mCtx = mCtx;
    this.productList = productList;
    this.listener = listener;
  }

  @Override
  public ServerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    //inflating and returning our view holder
    LayoutInflater inflater = LayoutInflater.from(mCtx);
    View view = inflater.inflate(R.layout.server_cards_item, null);
    return new ServerViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ServerViewHolder holder, int position) {
    //getting the product of the specified position
    Server product = productList.get(position);

    //binding the data with the viewholder views
    holder.textViewTitle.setText(product.getTitle());
    holder.textViewShortDesc.setText(product.getShortdesc());
    holder.textViewRating.setText(product.getPlayer());

    Picasso.get()
        .load(product.getImage())
        .fit()
        .centerCrop()
        .into(holder.imageView);
  }


  @Override
  public int getItemCount() {
    return productList.size();
  }


  class ServerViewHolder extends RecyclerView.ViewHolder {

    TextView textViewTitle, textViewShortDesc, textViewRating;
    ImageView imageView;

    public ServerViewHolder(View itemView) {
      super(itemView);
      itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Server s = productList.get(getBindingAdapterPosition());
          listener.onServerClick(s);
        }
      });
      textViewTitle = itemView.findViewById(R.id.textViewTitle);
      textViewShortDesc = itemView.findViewById(R.id.textViewShortDesc);
      textViewRating = itemView.findViewById(R.id.textViewPlayers);
      imageView = itemView.findViewById(R.id.imageView);
    }
  }
}