package com.mybills.home.fragments.BillList;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mybills.R;
import com.mybills.databinding.FragmentTabBillListBinding;
import com.mybills.home.HomeActivity;
import com.mybills.home.fragments.BillList.adapter.BillListAdapter;

public class TabBillListFragment extends Fragment {

    FragmentTabBillListBinding binding;

    ViewPager2 viewPager2;
    TabLayout tabLayout;

    HomeActivity homeActivity;

    public TabBillListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTabBillListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setup();
        listeners();
    }

    private void listeners() {
        binding.addBillFab.setOnClickListener(view -> homeActivity.showAddBillAlert());
    }

    public void setup() {
        homeActivity = (HomeActivity) getActivity();
        homeActivity.showProgressBar();

        //Cambia el titulo de la toolbar
        homeActivity.toolbarTitle(getString(R.string.toolbarTitleBills));

        //ViewPager2 setup
        viewPager();
    }

    //ViewPager2 setup
    private void viewPager() {
        BillListAdapter adapter = new BillListAdapter(requireActivity());
        viewPager2 = binding.viewpager2;
        viewPager2.setAdapter(adapter);
        tabLayout = binding.tabLayout;

        //Configurar tabs
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->{
            switch (position){
                case 0:
                    tab.setText("Gastos");
                    break;
                case 1:
                    tab.setText("Proximos Gastos");
                    break;
            }
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                homeActivity.hideNoRegistry();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    //Cambia la posicion del ViewPager
    public void setViewPagerPosition(int position){
        viewPager2.setCurrentItem(position);
    }

    //Obtiene la posicion del ViewPager
    public int getViewPagerPosition(){
        return viewPager2.getCurrentItem();
    }


}