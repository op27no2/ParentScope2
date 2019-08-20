package op27no2.parentscope2;

import android.support.v4.app.Fragment;
import android.widget.ImageView;

public interface FragmentChangeListener
{
    public void replaceFragment(Fragment fragment, Boolean addToStack);
    public void replaceFragmentWithTransition(Fragment fragment, Boolean addToStack, ImageView img);
}