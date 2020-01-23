/*
 * Copyright 2019 Pranav Pandey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pranavpandey.android.dynamic.support.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.pranavpandey.android.dynamic.support.R;
import com.pranavpandey.android.dynamic.support.activity.DynamicActivity;
import com.pranavpandey.android.dynamic.support.adapter.DynamicFragmentStateAdapter;
import com.pranavpandey.android.dynamic.support.listener.DynamicViewPagerCallback;

/**
 * An abstract {@link ViewPager} fragment to display multiple fragments inside the view pager
 * along with the tabs.
 * <p>Just extend this fragment and implement the necessary methods to use it inside an activity.
 */
public abstract class DynamicViewPager2Fragment extends
        DynamicFragment implements DynamicViewPagerCallback {

    /**
     * Fragment argument key to set the initial view pager page.
     */
    public static String ADS_ARGS_VIEW_PAGER_PAGE = "ads_args_view_pager_page";

    /**
     * View pager used by this fragment.
     */
    private ViewPager2 mViewPager;

    /**
     * Tab layout used by this fragment.
     */
    private TabLayout mTabLayout;

    /**
     * View pager adapter used by this fragment.
     */
    private ViewPagerAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ads_fragment_view_pager_2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = view.findViewById(R.id.ads_view_pager);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((DynamicActivity) getActivity()).addHeader(R.layout.ads_tabs, true);
        mTabLayout = getActivity().findViewById(R.id.ads_tab_layout);

        mAdapter = new ViewPagerAdapter(this, this);
        mViewPager.setOffscreenPageLimit(mAdapter.getItemCount());
        mViewPager.setAdapter(mAdapter);

        new TabLayoutMediator(mTabLayout, mViewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(getTitle(position));
                    }
                }).attach();

        if (getArguments() != null && getArguments().containsKey(ADS_ARGS_VIEW_PAGER_PAGE)) {
            setPage(getArguments().getInt(ADS_ARGS_VIEW_PAGER_PAGE));
        }
    }

    /**
     * Get the tab layout used by this fragment.
     *
     * @return The tab layout used by this fragment.
     */
    public TabLayout getTabLayout() {
        return mTabLayout;
    }

    /**
     * Get the view pager used by this fragment.
     *
     * @return The view pager used by this fragment.
     */
    public ViewPager2 getViewPager() {
        return mViewPager;
    }

    /**
     * Returns the currently selected view pager page or position.
     *
     * @return The currently selected view pager page or position.
     */
    public int getCurrentPage() {
        return mViewPager.getCurrentItem();
    }

    /**
     * Set the current page or position for the view pager.
     *
     * @param page The current position for the view pager.
     */
    public void setPage(final int page) {
        mViewPager.setCurrentItem(page);
    }

    /**
     * View pager adapter to display the supplied fragments with tab titles.
     */
    static class ViewPagerAdapter extends DynamicFragmentStateAdapter {

        private final DynamicViewPagerCallback dynamicViewPagerCallback;

        /**
         * Constructor to initialize an object of this class.
         *
         * @param fragment The fragment manager to get the child fragment manager.
         * @param dynamicViewPagerCallback The view pager callback to return the data.
         */
        ViewPagerAdapter(@NonNull Fragment fragment,
                @NonNull DynamicViewPagerCallback dynamicViewPagerCallback) {
            super(fragment);

            this.dynamicViewPagerCallback = dynamicViewPagerCallback;
        }

        @Override
        public @NonNull Fragment createFragment(int position) {
            return dynamicViewPagerCallback.createFragment(position);
        }

        @Override
        public int getItemCount() {
            return dynamicViewPagerCallback.getItemCount();
        }
    }
}
