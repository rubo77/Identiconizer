/*
 * Original work Copyright (C) 2013 The ChameleonOS Open Source Project
 * Modified work Copyright (C) 2013-2014 GermainZ@xda-developers.com
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

import com.germainz.identiconizer.Config;

/**
 * A factory to instantiate an identicon based on the type
 */
public class IdenticonFactory {

    public static final int IDENTICON_STYLE_RETRO = 0;
    public static final int IDENTICON_STYLE_CONTEMPORARY = 1;
    public static final int IDENTICON_STYLE_SPIROGRAPH = 2;
    public static final int IDENTICON_STYLE_DOTMATRIX = 3;
    public static final int IDENTICON_STYLE_GMAIL = 4;
    public static final int IDENTICON_STYLE_UNICORNIFY = 5;
    public static final int IDENTICON_STYLE_VISIGLYPHS = 6;

    /**
     * Get the appropriate identicon class based on the type passed in
     *
     * @param type
     * @param size
     * @param bgColor
     * @param serif
     * @param length
     * @return
     */
    public static Identicon makeIdenticon(int type, int size, int bgColor, boolean serif, int length) {
        return makeIdenticon(null, type, size, bgColor, serif, length);
    }
    
    /**
     * Get the appropriate identicon class based on the type passed in, with optional context
     *
     * @param context Optional context, required for UnicornifyIdenticon
     * @param type Identicon type
     * @param size Size of the identicon
     * @param bgColor Background color
     * @param serif Whether to use serif font
     * @param length Length of text
     * @return Appropriate Identicon implementation
     */
    public static Identicon makeIdenticon(Context context, int type, int size, int bgColor, boolean serif, int length) {
        Identicon.SIZE = size;
        Identicon.BG_COLOR = bgColor;
        Identicon.SERIF = serif;
        Identicon.LENGTH = length;
        switch (type) {
            case IDENTICON_STYLE_RETRO:
                return new RetroIdenticon();
            case IDENTICON_STYLE_CONTEMPORARY:
                return new NineBlockIdenticon();
            case IDENTICON_STYLE_SPIROGRAPH:
                return new SpirographIdenticon();
            case IDENTICON_STYLE_DOTMATRIX:
                return new DotMatrixIdenticon();
            case IDENTICON_STYLE_GMAIL:
                return new LetterTile();
            case IDENTICON_STYLE_UNICORNIFY:
                return new UnicornifyIdenticon(context);
            case IDENTICON_STYLE_VISIGLYPHS:
                return new VisiglyphsIdenticon();
            default:
                throw new IllegalArgumentException("Unknown identicon type.");
        }
    }

    /**
     * Get the appropriate identicon class by looking up the user selected
     * setting.
     *
     * @param context
     * @return
     */
    public static Identicon makeIdenticon(Context context) {
        Config config = Config.getInstance(context);
        return makeIdenticon(context, config.getRandomIdenticonStyle(), config.getIdenticonSize(), config.getIdenticonBgColor(), config.isIdenticonSerif(), config.getIdenticonLength());
    }

    /**
     * Get the appropriate identicon class using consistent style selection for a contact
     *
     * @param context Application context
     * @param contactName Contact name for consistent style selection
     * @return Appropriate Identicon implementation
     */
    public static Identicon makeIdenticonForContact(Context context, String contactName) {
        Config config = Config.getInstance(context);
        int styleId = config.getConsistentIdenticonStyleForContact(contactName);
        return makeIdenticon(context, styleId, config.getIdenticonSize(), config.getIdenticonBgColor(), config.isIdenticonSerif(), config.getIdenticonLength());
    }

    /**
     * Creates an identicon with style metadata embedded
     *
     * @param context Application context
     * @param contactName Contact name for consistent style selection
     * @param key The key to generate identicon for
     * @return Identicon byte array with style metadata
     */
    public static byte[] makeIdenticonWithStyleMetadata(Context context, String contactName, String key) {
        Config config = Config.getInstance(context);
        int styleId = config.getConsistentIdenticonStyleForContact(contactName);
        String styleName = config.getStyleNameForId(styleId);
        
        Identicon identicon = makeIdenticon(context, styleId, config.getIdenticonSize(), 
                config.getIdenticonBgColor(), config.isIdenticonSerif(), config.getIdenticonLength());
        
        // Generate the identicon bitmap
        android.graphics.Bitmap bitmap = identicon.generateIdenticonBitmap(key);
        if (bitmap == null) return null;
        
        // Convert to byte array and add style metadata
        java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        try {
            stream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        
        if (bytes != null) {
            return Identicon.makeTaggedIdenticonWithStyle(bytes, styleName);
        }
        
        return bytes;
    }
}

