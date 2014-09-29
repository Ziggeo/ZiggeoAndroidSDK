package com.ziggeo.androidsdk.helper;


import java.util.ArrayList;

import com.ziggeo.androidsdk.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class GridViewAdapter extends ArrayAdapter<ImageItem> {
	
    private Context mContext;
    private int layoutResourceId;
    private ArrayList<ImageItem> data = new ArrayList<ImageItem>();
 
    public GridViewAdapter(Context context, int layoutResourceId, ArrayList<ImageItem> data) {
        super(context, layoutResourceId, data);
        this.data = data;
        this.mContext = context;
        this.layoutResourceId = layoutResourceId;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       
    	View row = convertView;
        ViewHolder holder = null;
   
        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder((ImageView) row.findViewById(R.id.grid_image));
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
  
        ImageItem item = data.get(position);
        holder.image.setImageBitmap(item.getImage());
        return row;
    
    }
 
    
	public static class ViewHolder {
		public final ImageView image;

		public ViewHolder(ImageView image) {
			this.image = image;
		}
	}
    
}