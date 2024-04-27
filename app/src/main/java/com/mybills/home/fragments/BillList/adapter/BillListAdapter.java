package com.mybills.home.fragments.BillList.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mybills.home.fragments.BillList.FutureBillListFragment;
import com.mybills.home.fragments.BillList.PastBillListFragment;

public class BillListAdapter extends FragmentStateAdapter {


    public BillListAdapter(@NonNull FragmentActivity fragmentActivity) { super(fragmentActivity); }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            default:
            case 0:
                return new PastBillListFragment();
            case 1:
                //Proporciona la posicion al el fragment
                return new FutureBillListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
