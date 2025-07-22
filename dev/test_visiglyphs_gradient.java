import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

// Simple test to generate a Visiglyphs identicon with gradient
public class TestVisiglyphsGradient {
    public static void main(String[] args) {
        // Test hash that should produce visible colors
        String testInput = "test@example.com";
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(testInput.getBytes());
            
            // Convert to hex string like the Android implementation
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            System.out.println("Test hash: " + hexString.toString());
            System.out.println("This would generate a Visiglyphs identicon with gradient overlay");
            System.out.println("The gradient should now use the identicon's own foreground colors");
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
