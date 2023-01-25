/*
Copyright (C) 2022-2023 by k3b

This file is part of de.k3b.android.lossless_jpg_crop (https://github.com/k3b/losslessJpgCrop/)

This program is free software: you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see <http://www.gnu.org/licenses/>
 */
package de.k3b.android.lossless_jpg_crop;

import android.app.DialogFragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Define the aspect ratio of the result. (i.e 10x15).
 *
 * Use the {@link DefineAspectRatioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DefineAspectRatioFragment extends DialogFragment {

    private static final String PARAM_WIDTH = "paramWidth";
    private static final String PARAM_HEIGHT = "paramHeight";

    private String mParamWidth;
    private String mParamHeight;

    private EditText editWidth;
    private EditText editHeight;

    /** must be implemented by calling activity to receive change in AspectRatio */
    public interface AspectRatioHandler {
        void onDefineAspectRatio(String width, String height);
    }

    public DefineAspectRatioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param paramXY aspect ratio width and height
     * @return A new instance of fragment DefineAspectRatioFragment.
     */
    public static DefineAspectRatioFragment newInstance(String... paramXY) {
        DefineAspectRatioFragment fragment = new DefineAspectRatioFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_WIDTH, paramXY[0]);
        args.putString(PARAM_HEIGHT, paramXY[1]);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamWidth = getArguments().getString(PARAM_WIDTH);
            mParamHeight = getArguments().getString(PARAM_HEIGHT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_define_aspect_ratio, container, false);
        if (getShowsDialog()) {
            onCreateViewDialog(view);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveDialog();
        outState.putString(PARAM_WIDTH, mParamWidth);
        outState.putString(PARAM_HEIGHT, mParamHeight);
    }

    /** handle init for dialog-only controlls: cmdOk, cmdCancel, status */
    private void onCreateViewDialog(View view) {
        view.<Button>findViewById(R.id.cmd_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOk();
                dismiss();
            }
        });
        view.<Button>findViewById(R.id.cmd_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        view.<Button>findViewById(R.id.cmd_swap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDialog();
                String temp = mParamHeight;
                mParamHeight = mParamWidth;
                mParamWidth = temp;
                loadDialog();
            }
        });

        editWidth = view.findViewById(R.id.editWidth);
        editHeight = view.findViewById(R.id.editHeight);
        loadDialog();
    }

    private void loadDialog() {
        editWidth.setText(this.mParamWidth);
        editHeight.setText(this.mParamHeight);
    }
    private void saveDialog() {
        this.mParamWidth = editWidth.getText().toString();
        this.mParamHeight = editHeight.getText().toString();
    }

    private void onOk() {
        saveDialog();
        ((AspectRatioHandler) getActivity()).onDefineAspectRatio(mParamWidth, mParamHeight);
    }

}