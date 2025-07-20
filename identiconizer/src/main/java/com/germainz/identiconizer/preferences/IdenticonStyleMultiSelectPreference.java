/*
 * Copyright (C) 2013-2014 GermainZ@xda-developers.com
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

package com.germainz.identiconizer.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.germainz.identiconizer.Config;
import com.germainz.identiconizer.R;
import com.germainz.identiconizer.identicons.Identicon;
import com.germainz.identiconizer.identicons.IdenticonFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import android.util.Log;

public class IdenticonStyleMultiSelectPreference extends MultiSelectListPreference {
    
    private Context mContext;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private Set<String> mValues = new HashSet<>();
    private boolean[] mCheckedItems;
    private Random mRandom = new Random();
    private String[] mSampleNames = {"Alice", "Bob", "Charlie", "Diana", "Emma", "Frank", "Grace", "Henry", "Ivy", "Jack"};
    
    public IdenticonStyleMultiSelectPreference(Context context) {
        super(context);
        init(context);
    }
    
    public IdenticonStyleMultiSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        mContext = context;
    }
    
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        mEntries = getEntries();
        mEntryValues = getEntryValues();
        
        if (mEntries == null || mEntryValues == null || mEntries.length != mEntryValues.length) {
            throw new IllegalStateException("Entries and entryValues must be non-null and have the same length");
        }
        
        mValues = getValues();
        mCheckedItems = new boolean[mEntries.length];
        
        for (int i = 0; i < mEntryValues.length; i++) {
            mCheckedItems[i] = mValues.contains(mEntryValues[i].toString());
        }
        
        IdenticonStyleAdapter adapter = new IdenticonStyleAdapter();
        ListView listView = new ListView(mContext);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        builder.setView(listView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Set<String> newValues = new HashSet<>();
                for (int i = 0; i < mCheckedItems.length; i++) {
                    if (mCheckedItems[i]) {
                        newValues.add(mEntryValues[i].toString());
                    }
                }
                
                if (callChangeListener(newValues)) {
                    setValues(newValues);
                }
            }
        });
        
        builder.setNegativeButton(android.R.string.cancel, null);
    }
    
    private class IdenticonStyleAdapter extends BaseAdapter {
        
        @Override
        public int getCount() {
            return mEntries.length;
        }
        
        @Override
        public Object getItem(int position) {
            return mEntries[position];
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                view = inflater.inflate(R.layout.identicon_style_preference_item, parent, false);
            }
            
            ImageView iconView = view.findViewById(R.id.identicon_preview);
            TextView titleView = view.findViewById(R.id.style_title);
            CheckBox checkBox = view.findViewById(R.id.style_checkbox);
            
            // Set the style name
            titleView.setText(mEntries[position]);
            
            // Set checkbox state
            checkBox.setChecked(mCheckedItems[position]);
            
            // Generate preview identicon
            int styleId = Integer.parseInt(mEntryValues[position].toString());
            generatePreviewIdenticon(iconView, styleId);
            
            // Handle item clicks
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCheckedItems[position] = !mCheckedItems[position];
                    checkBox.setChecked(mCheckedItems[position]);
                }
            });
            
            return view;
        }
        
        private void generatePreviewIdenticon(ImageView imageView, int styleId) {
            // Set a loading placeholder first
            imageView.setImageResource(R.drawable.ic_settings_identicons);
            
            // Generate random sample name for variety
            String sampleName = mSampleNames[mRandom.nextInt(mSampleNames.length)] + mRandom.nextInt(1000);
            
            // Handle Unicornify (styleId 5) with special fallback logic
            if (styleId == 5) { // Unicornify
                generateUnicornifyPreviewWithFallback(imageView, sampleName);
            } else {
                // Generate other identicons synchronously (they're fast)
                generateRegularPreview(imageView, styleId, sampleName);
            }
        }
        
        private void generateRegularPreview(ImageView imageView, int styleId, String sampleName) {
            try {
                Config config = Config.getInstance(mContext);
                
                // Create identicon with smaller size for preview
                Identicon identicon = IdenticonFactory.makeIdenticon(mContext, styleId, 
                        64, config.getIdenticonBgColor(), config.isIdenticonSerif(), config.getIdenticonLength());
                
                Bitmap bitmap = identicon.generateIdenticonBitmap(sampleName);
                
                if (bitmap != null) {
                    imageView.setImageDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
                } else {
                    // Fallback to default icon if generation fails
                    imageView.setImageResource(R.drawable.ic_settings_identicons);
                }
            } catch (Exception e) {
                // Fallback to default icon on error
                imageView.setImageResource(R.drawable.ic_settings_identicons);
            }
        }
        
        private void generateUnicornifyPreviewWithFallback(ImageView imageView, String sampleName) {
            Log.d("IdenticonPreview", "Generating Unicornify preview with fallback");
            
            // First try to get any random cached Unicornify image (fast)
            try {
                com.germainz.identiconizer.identicons.UnicornifyIdenticon unicornify = 
                    new com.germainz.identiconizer.identicons.UnicornifyIdenticon(mContext);
                
                // Try to get any random cached image first
                android.graphics.Bitmap cachedBitmap = unicornify.getRandomCachedImage(64);
                
                if (cachedBitmap != null) {
                    Log.d("IdenticonPreview", "Using random cached Unicornify image");
                    imageView.setImageDrawable(new BitmapDrawable(mContext.getResources(), cachedBitmap));
                    return;
                }
                
                Log.d("IdenticonPreview", "No cached images found, using drawable fallback");
            } catch (Exception e) {
                Log.w("IdenticonPreview", "Cache check failed: " + e.getMessage());
            }
            
            // If no cache, use the embedded drawable fallback
            imageView.setImageResource(R.drawable.unicorn_preview_fallback);
        }
        

    }
}
