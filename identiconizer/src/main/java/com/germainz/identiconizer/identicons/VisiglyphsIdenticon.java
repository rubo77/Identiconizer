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
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

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

        // Extract pattern and color information from hash - exactly matching PHP
        int i = Integer.parseInt(hexString.substring(0, 1), 16); // block 1 pattern
        int j = Integer.parseInt(hexString.substring(1, 2), 16); // block 2 pattern  
        int k = Integer.parseInt(hexString.substring(2, 3), 16) & 7; // center pattern
        int rot1 = Integer.parseInt(hexString.substring(3, 4), 16) & 3; // rotation 1
        int rot2 = Integer.parseInt(hexString.substring(4, 5), 16) & 3; // rotation 2

        // Extract colors from hash (ensure contrast by AND'ing foreground with 239)
        int fgR = Integer.parseInt(hexString.substring(5, 7), 16) & 239;
        int fgG = Integer.parseInt(hexString.substring(7, 9), 16) & 239;
        int fgB = Integer.parseInt(hexString.substring(9, 11), 16) & 239;
        int fgR2 = Integer.parseInt(hexString.substring(11, 13), 16) & 239;
        int fgG2 = Integer.parseInt(hexString.substring(13, 15), 16) & 239;
        int fgB2 = Integer.parseInt(hexString.substring(15, 17), 16) & 239;
        
        // Background color - using white as in PHP default
        int bgR = 255;
        int bgG = 255;
        int bgB = 255;

        return generateVisiglyphBitmap(SIZE, i, j, k, rot1, rot2, 
                                     fgR, fgG, fgB, fgR2, fgG2, fgB2, 
                                     bgR, bgG, bgB);
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
     * Generates the actual visiglyphs bitmap - exactly matching the PHP glyph() function
     */
    private Bitmap generateVisiglyphBitmap(int blockSize, int i, int j, int k,
                                          int rot1, int rot2, int fgR, int fgG, int fgB,
                                          int fgR2, int fgG2, int fgB2, int bgR, int bgG, int bgB) {
        
        // set a minimum blocksize below which we draw bigger and then resample downwards for a better look
        int resize = 0;
        int minblocksize = 24;
        if (blockSize < minblocksize) {
            resize = blockSize;
            blockSize = minblocksize;
        }
        
        int imgSize = blockSize * 3;
        float quarter = blockSize / 4.0f;
        float quarter3 = quarter * 3;
        float half = blockSize / 2.0f;
        float third = blockSize / 3.0f;
        float center = imgSize / 2.0f;
        
        // Create main bitmap
        Bitmap bitmap = Bitmap.createBitmap(imgSize, imgSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.rgb(bgR, bgG, bgB));
        
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        
        // Create temporary bitmaps for rotation
        Bitmap tempBlock = Bitmap.createBitmap(blockSize * 2, blockSize, Bitmap.Config.ARGB_8888);
        Bitmap rotateTemp = Bitmap.createBitmap(blockSize, blockSize, Bitmap.Config.ARGB_8888);
        
        // Draw first pattern at origin (0,0)
        paint.setColor(Color.rgb(fgR, fgG, fgB));
        drawPatternOnCanvas(canvas, paint, i, 0, 0, blockSize);
        
        // rotate block
        Canvas rotateTempCanvas = new Canvas(rotateTemp);
        rotateTempCanvas.drawColor(Color.rgb(bgR, bgG, bgB));
        rotateTempCanvas.drawBitmap(bitmap, new Rect(0, 0, blockSize, blockSize), new Rect(0, 0, blockSize, blockSize), null);
        
        // Rotate first block
        if (rot1 > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rot1 * 90, blockSize / 2.0f, blockSize / 2.0f);
            rotateTemp = Bitmap.createBitmap(rotateTemp, 0, 0, blockSize, blockSize, matrix, true);
        }
        
        // Clear and redraw rotated first block
        canvas.drawColor(Color.rgb(bgR, bgG, bgB));
        canvas.drawBitmap(rotateTemp, 0, 0, null);
        
        // Draw second pattern at (blockSize, 0)
        paint.setColor(Color.rgb(fgR2, fgG2, fgB2));
        drawPatternOnCanvas(canvas, paint, j, blockSize, 0, blockSize);
        
        // rotate block
        rotateTempCanvas.drawColor(Color.rgb(bgR, bgG, bgB));
        rotateTempCanvas.drawBitmap(bitmap, new Rect(blockSize, 0, blockSize * 2, blockSize), new Rect(0, 0, blockSize, blockSize), null);
        
        // Rotate second block
        if (rot2 > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rot2 * 90, blockSize / 2.0f, blockSize / 2.0f);
            rotateTemp = Bitmap.createBitmap(rotateTemp, 0, 0, blockSize, blockSize, matrix, true);
        }
        
        // Redraw second block rotated
        canvas.drawBitmap(rotateTemp, blockSize, 0, null);
        
        // copy blocks to form radial pattern
        Canvas tempBlockCanvas = new Canvas(tempBlock);
        for (int roundabout = 0; roundabout < 3; roundabout++) {
            // Copy current blocks
            tempBlockCanvas.drawColor(Color.rgb(bgR, bgG, bgB));
            tempBlockCanvas.drawBitmap(bitmap, new Rect(0, 0, blockSize * 2, blockSize), new Rect(0, 0, blockSize * 2, blockSize), null);
            
            // rotate
            Matrix matrix = new Matrix();
            matrix.postRotate(90, imgSize / 2.0f, imgSize / 2.0f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgSize, imgSize, matrix, true);
            canvas = new Canvas(bitmap);
            
            // paste back
            canvas.drawBitmap(tempBlock, 0, 0, null);
        }
        
        // Draw center pattern
        paint.setColor(Color.rgb(fgR, fgG, fgB));
        int centerX = blockSize;
        int centerY = blockSize;
        // draw centre
        
        switch (k) {
            case 1: // circle
                canvas.drawCircle(center, center, quarter3 / 2.0f, paint);
                break;
                
            case 2: // quarter square
                canvas.drawRect(centerX + quarter, centerY + quarter, 
                              centerX + quarter3, centerY + quarter3, paint);
                break;
                
            case 3: // full square
                canvas.drawRect(centerX, centerY, centerX + blockSize, centerY + blockSize, paint);
                break;
                
            case 4: // quarter diamond
                Path path = new Path();
                path.moveTo(centerX + half, centerY + quarter);
                path.lineTo(centerX + quarter3, centerY + half);
                path.lineTo(centerX + half, centerY + quarter3);
                path.lineTo(centerX + quarter, centerY + half);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 5: // diamond
                Path diamondPath = new Path();
                diamondPath.moveTo(centerX + half, centerY);
                diamondPath.lineTo(centerX, centerY + half);
                diamondPath.lineTo(centerX + half, centerY + blockSize);
                diamondPath.lineTo(centerX + blockSize, centerY + half);
                diamondPath.close();
                canvas.drawPath(diamondPath, paint);
                break;
                
            default:
                // empty space
                break;
        }
        
        // Apply radial gradient overlay (matching PHP Visiglyphs finish)
        applyRadialGradientOverlay(canvas, imgSize, fgR, fgG, fgB, fgR2, fgG2, fgB2);
        
        // if we need to resample down
        if (resize > 0) {
            blockSize = resize;
            int imgsizeR = blockSize * 3;
            Bitmap imresize = Bitmap.createBitmap(imgsizeR, imgsizeR, Bitmap.Config.ARGB_8888);
            Canvas resizeCanvas = new Canvas(imresize);
            resizeCanvas.drawColor(Color.rgb(bgR, bgG, bgB));
            
            // Scale down the bitmap
            Matrix matrix = new Matrix();
            float scale = (float) imgsizeR / imgSize;
            matrix.postScale(scale, scale);
            resizeCanvas.drawBitmap(bitmap, matrix, null);
            
            return imresize;
        }
        
        return bitmap;
    }
    
    /**
     * Applies a radial gradient overlay to create the classic Visiglyphs finish effect
     * Uses the identicon's own foreground colors to create a visible gradient effect
     */
    private void applyRadialGradientOverlay(Canvas canvas, int imgSize, int fgR, int fgG, int fgB, int fgR2, int fgG2, int fgB2) {
        float centerX = imgSize / 2.0f;
        float centerY = imgSize / 2.0f;
        float radius = imgSize * 0.7f; // Slightly larger radius for better coverage
        
        // Create radial gradient using the identicon's own foreground colors
        // Center: Semi-transparent first foreground color (lighter)
        // Edge: Semi-transparent second foreground color (darker)
        int centerColor = Color.argb(30, fgR, fgG, fgB);     // 12% opacity - subtle center
        int edgeColor = Color.argb(80, fgR2, fgG2, fgB2);    // 31% opacity - more visible edge
        
        RadialGradient gradient = new RadialGradient(
            centerX, centerY, radius,
            centerColor, edgeColor,  // Center to edge: light to dark
            Shader.TileMode.CLAMP
        );
        
        Paint gradientPaint = new Paint();
        gradientPaint.setShader(gradient);
        gradientPaint.setAntiAlias(true);
        
        // Use multiply blend mode for better color interaction
        gradientPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        
        // Draw the gradient overlay
        canvas.drawCircle(centerX, centerY, radius, gradientPaint);
    }

    /**
     * Draws a specific pattern exactly matching the PHP switch statement
     */
    private void drawPatternOnCanvas(Canvas canvas, Paint paint, int patternType, int originX, int originY, int blockSize) {
        
        float quarter = blockSize / 4.0f;
        float quarter3 = quarter * 3;
        float half = blockSize / 2.0f;
        
        Path path = new Path();
        
        switch (patternType) {
            case 1: // #1 mountains
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
                
            case 2: // #2 half triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + blockSize, originY);
                path.lineTo(originX, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 3: // #3 centre triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY + blockSize);
                path.lineTo(originX + blockSize, originY);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 4: // #4 half block
                canvas.drawRect(originX, originY, originX + half, originY + blockSize, paint);
                break;
                
            case 5: // #5 half diamond
                path.moveTo(originX + quarter, originY);
                path.lineTo(originX, originY + half);
                path.lineTo(originX + quarter, originY + blockSize);
                path.lineTo(originX + half, originY + half);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 6: // #6 spike
                path.moveTo(originX, originY);
                path.lineTo(originX + blockSize, originY + half);
                path.lineTo(originX + blockSize, originY + blockSize);
                path.lineTo(originX + half, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 7: // #7 quarter triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY + blockSize);
                path.lineTo(originX, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 8: // #8 diag triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + blockSize, originY + half);
                path.lineTo(originX + half, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 9: // #9 centre mini triangle
                path.moveTo(originX + quarter, originY + quarter);
                path.lineTo(originX + quarter3, originY + quarter);
                path.lineTo(originX + quarter, originY + quarter3);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 10: // #10 diag mountains
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
                
            case 11: // #11 quarter block
                canvas.drawRect(originX, originY, originX + half, originY + half, paint);
                break;
                
            case 12: // #12 point out triangle
                path.moveTo(originX, originY + half);
                path.lineTo(originX + half, originY + blockSize);
                path.lineTo(originX + blockSize, originY + half);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 13: // #13 point in triangle
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY + half);
                path.lineTo(originX + blockSize, originY);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 14: // #14 diag point in
                path.moveTo(originX + half, originY + half);
                path.lineTo(originX, originY + half);
                path.lineTo(originX + half, originY + blockSize);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 15: // #15 diag point out
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY);
                path.lineTo(originX, originY + half);
                path.close();
                canvas.drawPath(path, paint);
                break;
                
            case 0: // #16 diag side point out (case 16 becomes 0 after modulo)
            default:
                path.moveTo(originX, originY);
                path.lineTo(originX + half, originY);
                path.lineTo(originX + half, originY + half);
                path.close();
                canvas.drawPath(path, paint);
                break;
        }
    }
}
