/*
 * Copyright (C) 2013-2014 GermainZ@xda-developers.com
 * Based on unicornify.pictures by @balpha
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

package com.germainz.identiconizer.identicons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;

public class UnicornifyIdenticon extends Identicon {
    
    private static final String TAG = "UnicornifyIdenticon";
    private static final String CACHE_DIR = "unicornify_cache";
    private boolean offlineWarningShown = false;
    private Context mContext;
    
    /**
     * Constructor that takes a Context for file operations and toast messages
     *
     * @param context Application context
     */
    public UnicornifyIdenticon(Context context) {
        this.mContext = context;
    }
    
    /**
     * Default constructor (required for compatibility)
     * Note: Cache operations and toast messages will not work without setting context
     */
    public UnicornifyIdenticon() {
        // Context must be set manually if using this constructor
    }
    
    /**
     * Sets the context for this instance
     * @param context Application context
     */
    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * Generates a unicornify identicon bitmap using the provided hash
     *
     * @param hash A 16 byte hash used to generate the identicon
     * @return The bitmap of the identicon created or null if offline and no cached version available
     */
    @Override
    public Bitmap generateIdenticonBitmap(byte[] hash) {
        // Convert hash to hex string
        StringBuilder hexHash = new StringBuilder();
        for (byte b : hash) {
            hexHash.append(String.format("%02x", b & 0xff));
        }
        String hexHashStr = hexHash.toString();
        
        // Determine the appropriate size (power of 2 between 32 and 128)
        int size = SIZE;
        // Round to nearest power of 2 between 32 and 128
        if (size < 32) {
            size = 32;
        } else if (size > 128) {
            size = 128;
        } else {
            // Find the nearest power of 2
            size = 32;
            while (size * 2 <= SIZE && size * 2 <= 128) {
                size *= 2;
            }
        }
        
        // Try to load from cache first
        Bitmap cachedBitmap = loadFromCache(hexHashStr, size);
        if (cachedBitmap != null) {
            return cachedBitmap;
        }
        
        try {
            // Build URL with hash and size
            String urlString = String.format(Locale.US, 
                    "https://unicornify.pictures/avatar/%s?s=%d", 
                    hexHashStr, size);
            
            // Download the image
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            connection.disconnect();
            
            // Save to cache for offline use
            saveToCache(hexHashStr, size, bitmap);
            
            return bitmap;
        } catch (UnknownHostException e) {
            // Network is unreachable - offline
            if (!offlineWarningShown) {
                showOfflineWarning();
                offlineWarningShown = true;
            }
            Log.w(TAG, "Device is offline. Cannot download unicorn avatar.");
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error downloading unicorn avatar: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generates an identicon bitmap, as a byte array, using the provided hash
     *
     * @param hash A 16 byte hash used to generate the identicon
     * @return The bitmap byte array of the identicon created or null if offline and no cached version available
     */
    @Override
    public byte[] generateIdenticonByteArray(byte[] hash) {
        Bitmap bitmap = generateIdenticonBitmap(hash);
        if (bitmap == null) {
            return null;
        }
        return bitmapToByteArray(bitmap);
    }
    
    /**
     * Generates an identicon bitmap using the provided key to generate a hash
     *
     * @param key A non empty string used to generate a hash when creating the identicon
     * @return The bitmap of the identicon created
     */
    @Override
    public Bitmap generateIdenticonBitmap(String key) {
        return generateIdenticonBitmap(generateHash(saltedKey(key)));
    }

    /**
     * Generates an identicon bitmap, as a byte array, using the provided key to generate a hash
     *
     * @param key A non empty string used to generate a hash when creating the identicon
     * @return The bitmap byte array of the identicon created or null if offline and no cached version available
     */
    @Override
    public byte[] generateIdenticonByteArray(String key) {
        Bitmap bitmap = generateIdenticonBitmap(key);
        if (bitmap == null) {
            return null;
        }
        return bitmapToByteArray(bitmap);
    }
    
    /**
     * Saves a bitmap to the local cache
     *
     * @param hexHash The hex hash string used as filename
     * @param size The size of the bitmap
     * @param bitmap The bitmap to save
     */
    private void saveToCache(String hexHash, int size, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        
        File cacheDir = new File(mContext.getCacheDir(), CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        
        String fileName = String.format("%s_%d.png", hexHash, size);
        File cacheFile = new File(cacheDir, fileName);
        
        try (FileOutputStream out = new FileOutputStream(cacheFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.d(TAG, "Saved unicorn avatar to cache: " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "Error saving unicorn avatar to cache: " + e.getMessage());
        }
    }
    
    /**
     * Loads a bitmap from the local cache
     *
     * @param hexHash The hex hash string used as filename
     * @param size The size of the bitmap
     * @return The cached bitmap or null if not found
     */
    private Bitmap loadFromCache(String hexHash, int size) {
        File cacheDir = new File(mContext.getCacheDir(), CACHE_DIR);
        if (!cacheDir.exists()) {
            return null;
        }
        
        String fileName = String.format("%s_%d.png", hexHash, size);
        File cacheFile = new File(cacheDir, fileName);
        
        if (cacheFile.exists()) {
            try (FileInputStream in = new FileInputStream(cacheFile)) {
                Log.d(TAG, "Loaded unicorn avatar from cache: " + fileName);
                return BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                Log.e(TAG, "Error loading unicorn avatar from cache: " + e.getMessage());
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Shows a warning toast when device is offline
     */
    private void showOfflineWarning() {
        Toast.makeText(mContext, 
                "Device is offline. Using cached unicorn avatars if available.", 
                Toast.LENGTH_LONG).show();
    }
}
