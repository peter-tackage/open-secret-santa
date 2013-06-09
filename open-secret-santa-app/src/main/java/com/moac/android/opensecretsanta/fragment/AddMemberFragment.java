package com.moac.android.opensecretsanta.fragment;

import android.app.Fragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import com.moac.android.opensecretsanta.R;

import java.util.List;

public class AddMemberFragment extends Fragment {

    private static final String TAG = AddMemberFragment.class.getSimpleName();

    SearchView mSearchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.add_member_fragment, container, false);

        mSearchView = (SearchView)view.findViewById(R.id.memberSearchView);
        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            Log.i(TAG, "onCreateView() - setting SearchAbleInfo. getComponentName() = " + getActivity().getComponentName());
            setupSearchView(mSearchView, mQueryTextListener);
        }
        return view;
    }
    SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            Log.i(TAG, "onQueryTextSubmit() - query: " + query);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Log.i(TAG, "onQueryTextChange() - newText: " + newText);
            return false;
        }
    };

    private void setupSearchView(SearchView _searchView, SearchView.OnQueryTextListener _listener) {

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();
            // Try to use the "contacts" global search provider
            SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null && inf.getSuggestAuthority().equals("com.android.contacts")) {
                    info = inf;
                    break;
                }
            }
            _searchView.setSearchableInfo(info);
        }
        _searchView.setOnQueryTextListener(_listener);
    }
}