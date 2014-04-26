/*
 * Original work Copyright (C) 2013 The ChameleonOS Open Source Project
 * Modified work Copyright (C) 2013 GermainZ@xda-developers.com
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
import com.germainz.identiconizer.identicons.DotMatrixIdenticon;
import com.germainz.identiconizer.identicons.Identicon;
import com.germainz.identiconizer.identicons.NineBlockIdenticon;
import com.germainz.identiconizer.identicons.RetroIdenticon;
import com.germainz.identiconizer.identicons.SpirographIdenticon;

/**
 * A factory to instantiate an identicon based on the type
 */
public class IdenticonFactory {

    public static final int IDENTICON_STYLE_RETRO = 0;
    public static final int IDENTICON_STYLE_CONTEMPORARY = 1;
    public static final int IDENTICON_STYLE_SPIROGRAPH = 2;
    public static final int IDENTICON_STYLE_DOTMATRIX = 3;

    /**
     * Get the appropriate identicon class based on the type passed in
     * @param type
     * @return
     */
    public static Identicon makeIdenticon(int type, int size) {
        Identicon.DEFAULT_SIZE = size;
        switch (type) {
            case IDENTICON_STYLE_RETRO:
                return new RetroIdenticon();
            case IDENTICON_STYLE_CONTEMPORARY:
                return new NineBlockIdenticon();
            case IDENTICON_STYLE_SPIROGRAPH:
                return new SpirographIdenticon();
            case IDENTICON_STYLE_DOTMATRIX:
                return new DotMatrixIdenticon();
            default:
                throw new IllegalArgumentException("Unkown identicon type.");
        }
    }

    /**
     * Get the appropriate identicon class by looking up the user selected
     * setting.
     * @param context
     * @return
     */
    public static Identicon makeIdenticon(Context context) {
        Config config = Config.getInstance(context);
        return makeIdenticon(config.getIdenticonStyle(), config.getIdenticonSize());
    }
}
