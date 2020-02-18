package com.osamaq;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.net.MalformedURLException;
import java.net.URL;

public class ImageColorsModule extends ReactContextBaseJavaModule {
    private boolean getAvg;
    private boolean getDominant;
    private boolean getVib;
    private boolean getDarkVibrant;
    private boolean getLightVibrant;
    private boolean getDarkMuted;
    private boolean getLightMuted;
    private boolean getMuted;

    ImageColorsModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "ImageColors";
    }


    /**
     * https://gist.github.com/maxjvh/a6ab15cbba9c82a5065d
     * pixelSpacing tells how many pixels to skip each pixel.
     * If pixelSpacing > 1: the average color is an estimate, but higher values mean better performance.
     * If pixelSpacing == 1: the average color will be the real average.
     * If pixelSpacing < 1: the method will most likely crash (don't use values below 1).
     */
    private int calculateAverageColor(@NonNull Bitmap bitmap) {
        int R = 0;
        int G = 0;
        int B = 0;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int n = 0;
        int[] pixels = new int[width * height];

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // Pixel spacing is 5
        for (int i = 0; i < pixels.length; i += 5) {
            int color = pixels[i];
            R += Color.red(color);
            G += Color.green(color);
            B += Color.blue(color);
            n++;
        }
        return Color.rgb(R / n, G / n, B / n);
    }

    private String getHex(int rgb) {
        return String.format("#%06X", (0xFFFFFF & rgb));
    }

    private int parseColorFromHex(String colorHex) {
        return Color.parseColor(colorHex);
    }

    private void falsifyAll() {
        getAvg = false;
        getDominant = false;
        getVib = false;
        getDarkVibrant = false;
        getLightVibrant = false;
        getDarkMuted = false;
        getLightMuted = false;
        getMuted = false;
    }


    private void getBooleans(ReadableMap config) {
        if (config.hasKey("average")) {
            getAvg = config.getBoolean("average");
        }
        if (config.hasKey("dominant")) {
            getDominant = config.getBoolean("dominant");
        }
        if (config.hasKey("vibrant")) {
            getVib = config.getBoolean("vibrant");
        }
        if (config.hasKey("darkVibrant")) {
            getDarkVibrant = config.getBoolean("darkVibrant");
        }
        if (config.hasKey("lightVibrant")) {
            getLightVibrant = config.getBoolean("lightVibrant");
        }
        if (config.hasKey("darkMuted")) {
            getDarkMuted = config.getBoolean("darkMuted");
        }
        if (config.hasKey("lightMuted")) {
            getLightMuted = config.getBoolean("lightMuted");
        }
        if (config.hasKey("muted")) {
            getMuted = config.getBoolean("muted");
        }
    }


    @ReactMethod
    public void getColors(String url, ReadableMap config, Promise promise) {
        try {
            String defColor;
            if (config.hasKey("defaultColor")) {
                defColor = config.getString("defaultColor");
            } else {
                defColor = "#000000";
            }
            int defColorInt = parseColorFromHex(defColor);

            falsifyAll();
            getBooleans(config);

            WritableMap resultMap = Arguments.createMap();
            URL parsedURL = new URL(url);
            Bitmap image = BitmapFactory.decodeStream(parsedURL.openConnection().getInputStream());
            if (image == null) throw new Exception("Invalid image");
            
            if (getAvg) {
                int rgbAvg = calculateAverageColor(image);
                String hexAvg = getHex(rgbAvg);
                resultMap.putString("average", hexAvg);
            }
            if (getDominant || getVib || getDarkVibrant || getLightVibrant || getDarkMuted || getLightMuted || getMuted) {
                Palette.Builder builder = new Palette.Builder(image);
                builder.generate(palette -> {
                    try {
                        if (palette != null) {
                            if (getDominant) {
                                int rgb = palette.getDominantColor(defColorInt);
                                String hex = getHex(rgb);
                                resultMap.putString("dominant", hex);
                            }
                            if (getVib) {
                                int rgb = palette.getVibrantColor(defColorInt);
                                String hex = getHex(rgb);
                                resultMap.putString("vibrant", hex);
                            }
                            if (getDarkVibrant) {
                                int rgb = palette.getDarkVibrantColor(defColorInt);
                                String hex = getHex(rgb);
                                resultMap.putString("darkVibrant", hex);
                            }
                            if (getLightVibrant) {
                                int rgb = palette.getLightVibrantColor(defColorInt);
                                String hex = getHex(rgb);
                                resultMap.putString("lightVibrant", hex);
                            }
                            if (getDarkMuted) {
                                int rgb = palette.getDarkMutedColor(defColorInt);
                                String hex = getHex(rgb);
                                resultMap.putString("darkMuted", hex);
                            }
                            if (getLightMuted) {
                                int rgb = palette.getLightMutedColor(defColorInt);
                                String hex = getHex(rgb);
                                resultMap.putString("lightMuted", hex);
                            }
                            if (getMuted) {
                                int rgb = palette.getMutedColor(defColorInt);
                                String hex = getHex(rgb);
                                resultMap.putString("muted", hex);
                            }
                            promise.resolve(resultMap);
                        } else {
                            throw new Exception("Palette was null");
                        }
                    } catch (Exception e) {
                        handleException(e, promise);
                    }
                });
            } else {
                promise.resolve(resultMap);
            }
        } catch (MalformedURLException e) {
            handleException(new Exception("Invalid URL"), promise);
        } catch (Exception e) {
            handleException(e, promise);
        }

    }


    private void handleException(Exception e, Promise promise) {
        e.printStackTrace();
        promise.reject("Error", "ImageColors: " + e.getMessage());
    }
}
