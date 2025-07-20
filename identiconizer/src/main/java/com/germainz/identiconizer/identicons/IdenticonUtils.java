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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class IdenticonUtils {

    private static final byte[] JPG_HEADER = new byte[]{(byte) 0xFF, (byte) 0xD8};
    private static final byte[] PNG_HEADER = new byte[]{(byte) 137, (byte) 80, (byte) 78,
            (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10};
    private static final int JPG_FORMAT = 0;
    private static final int PNG_FORMAT = 1;
    private static final int OTHER_FORMAT = 2;

    public static boolean isIdenticon(byte[] data) {
        int format = getDataFormat(data);
        if (data == null || format == OTHER_FORMAT)
            return false;

        if (format == JPG_FORMAT) {
            // Handle JPG format with fixed position
            int start = data.length - 18;
            int end = data.length - 2;
            String charSet = "US-ASCII";
            
            if (start < 0 || end <= start || end > data.length) {
                return false;
            }
            
            byte[] tag = Arrays.copyOfRange(data, start, end);
            try {
                String tagString = new String(tag, charSet);
                return Identicon.IDENTICON_MARKER.equals(tagString);
            } catch (UnsupportedEncodingException e) {
                return false;
            }
        } else {
            // Handle PNG format - try multiple approaches to be robust
            
            // Try different possible marker lengths to handle both old and new identicons
            String[] possibleMarkers = {
                Identicon.IDENTICON_MARKER, // Old format: "identicon_marker"
            };
            
            // Try the original fixed-length approach first
            for (String expectedMarker : possibleMarkers) {
                int start = data.length - (expectedMarker.length() + 1);
                int end = data.length - 1;
                
                if (start >= 0 && end > start && end <= data.length) {
                    byte[] tag = Arrays.copyOfRange(data, start, end);
                    try {
                        String tagString = new String(tag, "ISO-8859-1");
                        if (expectedMarker.equals(tagString)) {
                            return true;
                        }
                    } catch (UnsupportedEncodingException e) {
                        // Continue to next approach
                    }
                }
            }
            
            // Try a broader search approach - look for the marker anywhere in the last part of the file
            int searchStart = Math.max(0, data.length - 200); // Search last 200 bytes
            try {
                String endOfFile = new String(Arrays.copyOfRange(data, searchStart, data.length), "ISO-8859-1");
                if (endOfFile.contains(Identicon.IDENTICON_MARKER)) {
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                // Ignore and return false
            }
            
            return false;
        }
    }

    /**
     * Extracts the style from an existing identicon's metadata
     *
     * @param imageData The identicon image data
     * @return The style name, or null if not found or not an identicon
     */
    public static String getStyleFromIdenticon(byte[] imageData) {
        if (!isIdenticon(imageData)) {
            return null;
        }
        
        String markerString = extractMarkerString(imageData);
        if (markerString != null && markerString.contains(Identicon.STYLE_MARKER_PREFIX)) {
            String[] parts = markerString.split("\\|");
            for (String part : parts) {
                if (part.startsWith(Identicon.STYLE_MARKER_PREFIX)) {
                    return part.substring(Identicon.STYLE_MARKER_PREFIX.length());
                }
            }
        }
        
        return null;
    }

    /**
     * Extracts the full marker string from identicon metadata
     *
     * @param data The identicon image data
     * @return The marker string, or null if not found
     */
    private static String extractMarkerString(byte[] data) {
        int format = getDataFormat(data);
        if (data == null || format == OTHER_FORMAT)
            return null;

        int start, end;
        String charSet;
        if (format == JPG_FORMAT) {
            start = data.length - 18;
            end = data.length - 2;
            charSet = "US-ASCII";
        } else {
            // For PNG, we need to find the actual marker length dynamically
            // since it can now contain style information
            start = findMarkerStart(data);
            if (start == -1) return null;
            end = data.length - 1;
            charSet = "ISO-8859-1";
        }
        
        if (start < 0 || end <= start) return null;
        
        byte[] tag = Arrays.copyOfRange(data, start, end);
        
        try {
            return new String(tag, charSet);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Finds the start position of the marker in PNG data
     */
    private static int findMarkerStart(byte[] data) {
        // Search backwards for the identicon marker
        String marker = Identicon.IDENTICON_MARKER;
        byte[] markerBytes = marker.getBytes();
        
        for (int i = data.length - markerBytes.length - 1; i >= 0; i--) {
            boolean found = true;
            for (int j = 0; j < markerBytes.length; j++) {
                if (data[i + j] != markerBytes[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    private static int getDataFormat(byte[] data) {
        boolean isPng = true;
        if (data.length < PNG_HEADER.length) {
            isPng = false;
        } else {
            for (int i = 0; i < PNG_HEADER.length; i++) {
                if (data[i] != PNG_HEADER[i])
                    isPng = false;
            }
        }
        if (isPng)
            return PNG_FORMAT;

        boolean isJpg = true;
        if (data.length < JPG_HEADER.length) {
            isJpg = false;
        } else {
            for (int i = 0; i < JPG_HEADER.length; i++) {
                if (data[i] != JPG_HEADER[i])
                    isJpg = false;
            }
        }
        if (isJpg)
            return JPG_FORMAT;

        return OTHER_FORMAT;
    }
}
