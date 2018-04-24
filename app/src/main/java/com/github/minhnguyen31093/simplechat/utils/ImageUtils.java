package com.github.minhnguyen31093.simplechat.utils;

import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class ImageUtils {

    private static String checkUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        if (url.contains("http:")) {
            url = url.replace("http:", "https:");
        }
        return url;
    }

    public static void loadChatContent(SimpleDraweeView simpleDraweeView, String url) {
        if (url != null && !url.isEmpty()) {
            int size = 256;
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(checkUrl(url))).setResizeOptions(new ResizeOptions(size, size)).setLocalThumbnailPreviewsEnabled(true).build();
            DraweeController controller = Fresco.newDraweeControllerBuilder().setLowResImageRequest(request).setOldController(simpleDraweeView.getController()).setAutoPlayAnimations(true).build();
            simpleDraweeView.setController(controller);
        }
    }
}
