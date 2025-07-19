/*
 * Copyright (C) 2013-2014 GermainZ@xda-developers.com
 * Based on Visiglyphs by Charles Darke @ digitalconsumption.com
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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.io.ByteArrayOutputStream;

public class VisiglyphsIdenticon extends Identicon {

    /**
     * Generates a visiglyphs identicon bitmap using the provided hash
     *
     * @param hash A 16 byte hash used to generate the identicon
     * @return The bitmap of the identicon created
     */
    @Override
    public Bitmap generateIdenticonBitmap(byte[] hash) {
        if (hash.length != 16)
            return null;

        // Convert hash to hex string
        StringBuilder hexHash = new StringBuilder();
        for (byte b : hash) {
            hexHash.append(String.format("%02x", b & 0xff));
        }
        String hexString = hexHash.toString();

        // Extract pattern and color information from hash
        int pattern1 = Integer.parseInt(hexString.substring(0, 1), 16) % 16 + 1;
        int pattern2 = Integer.parseInt(hexString.substring(1, 2), 16) % 16 + 1;
        int centerPattern = Integer.parseInt(hexString.substring(2, 3), 16) & 7;
        int rotation1 = Integer.parseInt(hexString.substring(3, 4), 16) & 3;
        int rotation2 = Integer.parseInt(hexString.substring(4, 5), 16) & 3;

        // Extract colors from hash (ensure contrast by AND'ing foreground with 239)
        int fgR = Integer.parseInt(hexString.substring(5, 7), 16) & 239;
        int fgG = Integer.parseInt(hexString.substring(7, 9), 16) & 239;
        int fgB = Integer.parseInt(hexString.substring(9, 11), 16) & 239;
        int fgR2 = Integer.parseInt(hexString.substring(11, 13), 16) & 239;
        int fgG2 = Integer.parseInt(hexString.substring(13, 15), 16) & 239;
        int fgB2 = Integer.parseInt(hexString.substring(15, 17), 16) & 239;
        int bgR = Integer.parseInt(hexString.substring(17, 19), 16);
        int bgG = Integer.parseInt(hexString.substring(19, 21), 16);
        int bgB = Integer.parseInt(hexString.substring(21, 23), 16);

        return generateVisiglyphBitmap(SIZE, pattern1, pattern2, centerPattern, 
                                     rotation1, rotation2, fgR, fgG, fgB, 
                                     fgR2, fgG2, fgB2, bgR, bgG, bgB);
    }

    /**
     * Generates an identicon bitmap, as a byte array, using the provided hash
     *
     * @param hash A 16 byte hash used to generate the identicon
     * @return The bitmap byte array of the identicon created
     */
    @Override
    public byte[] generateIdenticonByteArray(byte[] hash) {
        Bitmap bitmap = generateIdenticonBitmap(hash);
        if (bitmap == null) {
            return null;
        }
        return convertBitmapToByteArray(bitmap);
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
     * @return The bitmap byte array of the identicon created
     */
    @Override
    public byte[] generateIdenticonByteArray(String key) {
        Bitmap bitmap = generateIdenticonBitmap(key);
        if (bitmap == null) {
            return null;
        }
        return convertBitmapToByteArray(bitmap);
    }

    /**
     * Converts a bitmap to a byte array
     */
    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return makeTaggedIdenticon(stream.toByteArray());
    }

    /**
     * Generates the actual visiglyphs bitmap
     */
    private Bitmap generateVisiglyphBitmap(int blockSize, int pattern1, int pattern2, int centerPattern,
                                          int rotation1, int rotation2, int fgR, int fgG, int fgB,
                                          int fgR2, int fgG2, int fgB2, int bgR, int bgG, int bgB) {
        
        int imgSize = blockSize * 3;
        Bitmap bitmap = Bitmap.createBitmap(imgSize, imgSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Fill background
        canvas.drawColor(Color.rgb(bgR, bgG, bgB));
        
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(fgR, fgG, fgB));
        
        // Draw patterns in 4 corners (mirrored)
        drawPattern(canvas, paint, pattern1, 0, 0, blockSize, 0);
        drawPattern(canvas, paint, pattern1, blockSize * 2, 0, blockSize, 1);
        drawPattern(canvas, paint, pattern1, 0, blockSize * 2, blockSize, 3);
        drawPattern(canvas, paint, pattern1, blockSize * 2, blockSize * 2, blockSize, 2);
        
        // Draw center pattern if needed
        if (centerPattern > 0) {
            paint.setColor(Color.rgb(fgR2, fgG2, fgB2));
            drawPattern(canvas, paint, centerPattern, blockSize, blockSize, blockSize, rotation1);
        }
        
        return bitmap;
    }

    /**
     * Draws a specific pattern at the given position with rotation
     */
    private void drawPattern(Canvas canvas, Paint paint, int patternType, int originX, int originY, 
                           int blockSize, int rotation) {
        
        float quarter = blockSize / 4.0f;
        float quarter3 = quarter * 3;
        float half = blockSize / 2.0f;
        float third = blockSize / 3.0f;
        
        Path path = new Path();
        
        switch (patternType) {
            case 1: // Mountains
                // First mountain
                path.moveTo(originX, originY);
                path.lineTo(originX + quarter, originY + blockSize);
                path.lineTo(originX + half, originY);
                path.close();
                canvas.drawPath(path, paint);
                
                // Second mountain
                path.reset();
                path.moveTo(originX + half, originY);
                path.lineTo(originX + quarter3, originY + blockSize);
                path.lineTo(originX + blockSize, originY);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 2: // Half triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + blockSize, originY);
                path.lineTo(originX, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 3: // Center triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY + blockSize);
                path.lineTo(originX + blockSize, originY);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 4: // Half block
                path.moveTo(originX, originY);
                path.lineTo(originX, originY + blockSize);
                path.lineTo(originX + half, originY + blockSize);
                path.lineTo(originX + half, originY);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 5: // Half diamond
                path.moveTo(originX + quarter, originY);
                path.lineTo(originX, originY + half);
                path.lineTo(originX + quarter, originY + blockSize);
                path.lineTo(originX + half, originY + half);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 6: // Spike
                path.moveTo(originX, originY);
                path.lineTo(originX + blockSize, originY + half);
                path.lineTo(originX + blockSize, originY + blockSize);
                path.lineTo(originX + half, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 7: // Quarter triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY + blockSize);
                path.lineTo(originX, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 8: // Diagonal triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + blockSize, originY + half);
                path.lineTo(originX + half, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 9: // Center mini triangle
                path.moveTo(originX + quarter, originY + quarter);
                path.lineTo(originX + quarter3, originY + quarter);
                path.lineTo(originX + quarter, originY + quarter3);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 10: // Diagonal mountains
                // First part
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY);
                path.lineTo(originX + half, originY + half);
                path.close();
                canvas.drawPath(path, paint);
                
                // Second part
                path.reset();
                path.moveTo(originX + half, originY + half);
                path.lineTo(originX + blockSize, originY + half);
                path.lineTo(originX + blockSize, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 11: // Quarter block
                path.moveTo(originX, originY);
                path.lineTo(originX, originY + half);
                path.lineTo(originX + half, originY + half);
                path.lineTo(originX + half, originY);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 12: // Point out triangle
                path.moveTo(originX, originY + half);
                path.lineTo(originX + half, originY + blockSize);
                path.lineTo(originX + blockSize, originY + half);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 13: // Point in triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY + half);
                path.lineTo(originX + blockSize, originY);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 14: // Diagonal point in
                path.moveTo(originX + half, originY + half);
                path.lineTo(originX, originY + half);
                path.lineTo(originX + half, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 15: // Diagonal point out
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY);
                path.lineTo(originX, originY + half);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 16: // Diagonal side point out
            default:
                path.moveTo(originX, originY);
                path.lineTo(originX + blockSize, originY);
                path.lineTo(originX + half, originY + half);
                path.close();
                canvas.drawPath(path, paint);
                break;
        }
        
        // Apply rotation if needed
        if (rotation > 0) {
            canvas.save();
            canvas.rotate(rotation * 90, originX + half, originY + half);
            canvas.restore();
        }
    }
}
