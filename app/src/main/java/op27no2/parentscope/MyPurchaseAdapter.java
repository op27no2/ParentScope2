package op27no2.parentscope;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rilixtech.materialfancybutton.MaterialFancyButton;

import java.util.ArrayList;

/**
 * Created by CristMac on 11/3/17.
 */

public class MyPurchaseAdapter extends RecyclerView.Adapter<MyPurchaseAdapter.ViewHolder> {
    private ArrayList<String> mDataset;
    private ArrayList<String> mDataset2;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyPurchaseAdapter(ArrayList<String> myDataset, ArrayList<String> myDataset2) {
        mDataset = myDataset;
        mDataset2 = myDataset2;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rowview_premium, parent, false);
        // set the view's size, margins, paddings and layout parameters
        final ViewHolder holder = new ViewHolder(v);



        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final String mTitle = mDataset.get(position);
        final String mTitle2 = mDataset2.get(position);

        System.out.println("position test"+position);
        TextView mText = holder.mView.findViewById(R.id.text_view);
        TextView mText2 = holder.mView.findViewById(R.id.text2);
        mText.setText(mTitle);
        mText2.setText(mTitle2);

        MaterialFancyButton mButton = holder.mView.findViewById(R.id.purchase_button);
      /*  if(position==0){
            mButton.setText("Info:");
            mText2.setVisibility(View.GONE);

        }*/
    }




    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }





}



