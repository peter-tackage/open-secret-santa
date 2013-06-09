package com.moac.android.opensecretsanta.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import com.moac.android.opensecretsanta.R;

public class AddMemberFragment extends Fragment {

    SearchView mSearchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.add_member_fragment, container, false);
        mSearchView = (SearchView)view.findViewById(R.id.memberSearchView);
        return view;
    }

}