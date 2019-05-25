package op27no2.parentscope;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
  //  private ArrayList<String> mDataset = new ArrayList<String>();
    private ArrayList<FileObject> mFileArray = new ArrayList<FileObject>();
    private Context mContext;
    public ClickListener clickListener;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtHeader;
        public TextView txtFooter;
        public ImageView imageView;
        public View layout;



        public MyViewHolder(View v) {
            super(v);
            layout = v;
            txtHeader = (TextView) v.findViewById(R.id.firstLine);
            txtFooter = (TextView) v.findViewById(R.id.secondLine);
            imageView = (ImageView) v.findViewById(R.id.icon);
        }
    }

    //fix to make work
    public void add(int position, String item) {
      //  mDataset.add(position, item);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public void remove(int position) {
        mFileArray.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
        notifyDataSetChanged();
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context, ArrayList<FileObject> fileArray , ArrayList<String> myDataset, ClickListener mClickListener) {
     //   mDataset = myDataset;
        mContext = context;
        mFileArray = fileArray;
        clickListener = mClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v = inflater.inflate(R.layout.row_layout, parent, false);

        v.getLayoutParams().height = parent.getMeasuredHeight() / 4;
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(mFileArray.get(position).getSelected() == 1) {
            holder.layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightAccent));
        }
        else {
            holder.layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightGray));
        }


        holder.txtHeader.setText(mFileArray.get(position).getFile().getName());
        holder.txtFooter.setText(Long.toString(mFileArray.get(position).getFile().length()/1024/1024)+" MB");
        final int here = position;

        final Uri uri = Uri.fromFile(new File(mFileArray.get(position).getFile().getAbsolutePath()));
        Glide.with(mContext).load(uri).thumbnail().into(holder.imageView);


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // remove(here);

                holder.imageView.setTransitionName("thumbnailTransition");
                Pair<View, String> pair1 = Pair.create((View) holder.imageView, holder.imageView.getTransitionName());


              /*  Intent intent = new Intent(mContext, VideoActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, pair1);
                intent.putExtra("bitmap_uri", uri.toString());
                mContext.startActivity(intent, optionsCompat.toBundle());*/


                VideoActivity frag = new VideoActivity();
                Bundle args = new Bundle();
                args.putString("bitmap_uri", uri.toString());
                frag.setArguments(args);
                showOtherFragment(frag, true, holder.imageView);

            }
        });

        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clickListener.onLongClick(position);
            //    remove(position);

                return false;
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFileArray.get(position).getSelected() ==0) {
                    mFileArray.get(position).setSelected(1);
                }
                else {
                    mFileArray.get(position).setSelected(0);
                }
                notifyDataSetChanged();
            }
        });


        System.out.println("bitmap null "+position);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mFileArray.size();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public void showOtherFragment(Fragment fr, Boolean addToStack, ImageView img)
    {
        NavActivity myActivity = (NavActivity)mContext;
        FragmentChangeListener fc=(FragmentChangeListener)myActivity;
        fc.replaceFragmentWithTransition(fr,addToStack, img);
    }



}