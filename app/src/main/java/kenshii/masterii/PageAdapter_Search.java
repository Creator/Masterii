package kenshii.masterii;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.Menu;

/**
 * Created by shane- on 09/09/2015.
 */
public class PageAdapter_Search extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    Context context;
    Fragment_SearchedAnime f0;
    Fragment_SearchedAnime f1;
    Fragment_SearchedAnime f2;
    Fragment_SearchedAnime f3;

    public PageAdapter_Search(FragmentManager fm, int NumOfTabs, Context context, Menu menuInstance) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.context = context;
        f0 = new Fragment_SearchedAnime(context,menuInstance,"0");
        f1 = new Fragment_SearchedAnime(context,menuInstance,"2");
        f2 = new Fragment_SearchedAnime(context,menuInstance,"1");
        f3 = new Fragment_SearchedAnime(context,menuInstance,"3");
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return f0;
            case 1:
                return f1;
            case 2:
                return f2;
            case 3:
                return f3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
