package com.votors.runningx;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class FriendsFragment extends Fragment {
	
	public FriendsFragment(){}
    Context context;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        /*final Button button_share = (Button) rootView.findViewById(R.id.share);

        button_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShare();
            }
        });*/

        return rootView;
    }


}
