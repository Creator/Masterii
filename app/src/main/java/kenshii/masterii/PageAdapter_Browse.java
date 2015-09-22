package kenshii.masterii;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.Menu;

/**
 * Created by shane- on 09/09/2015.
 */
public class PageAdapter_Browse extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    Context context;
    Fragment_RecentAnime f0;
    Fragment_OtherAnime f1;
    Fragment_OtherAnime f2;
    Fragment_OtherAnime f3;
    Fragment_OtherAnime f4;

    public PageAdapter_Browse(FragmentManager fm, int NumOfTabs, Context context, Menu menuInstance) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.context = context;
        f0 = new Fragment_RecentAnime(context,menuInstance);
        f1 = new Fragment_OtherAnime(context,menuInstance,"0");
        f2 = new Fragment_OtherAnime(context,menuInstance,"2");
        f3 = new Fragment_OtherAnime(context,menuInstance,"1");
        f4 = new Fragment_OtherAnime(context,menuInstance,"3");
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
            case 4:
                return f4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
