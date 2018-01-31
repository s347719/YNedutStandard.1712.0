package com.yineng.ynmessager.activity.picker.picture;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.imgpicker.ImageFolder;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by 贺毅柳 on 2015/12/7 17:21.
 */
public class GalleryHelper
{
    @NonNull
    public static List<ImageFolder> list(@NonNull ContentResolver contentResolver, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor cur = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);

        // Cursor为null或者没有查询出数据则返回空列表
        int count;
        if (cur == null || (count = cur.getCount()) == 0) {
            return Collections.emptyList();
        }

        List<ImageFile> allImages = new ArrayList<>(count);
        while (cur.moveToNext())
        {
            String path = cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            // TODO: 2016/1/20 跳过/过滤 了gif文件
            if (StringUtils.endsWithIgnoreCase(path, ".gif")) {
                continue;
            }

            ImageFile img = new ImageFile(path);
            allImages.add(img);
        }
        cur.close();
        cur = null;

        // 遍历得到所有的不重复的父目录列表
        Set<File> paths = new LinkedHashSet<>();
        for (ImageFile img : allImages)
        {
            File path = img.getParentFile();
            paths.add(path);
        }

        // 把各个ImageFile重新组合到对应的ImageFolder
        List<ImageFolder> imageFolderList = new ArrayList<>(paths.size());
        for (File folder : paths)
        {
            ImageFolder imageFolder = new ImageFolder();
            imageFolder.setDirectory(folder);
            ArrayList<ImageFile> imageFiles = new ArrayList<>();
            for (ImageFile img : allImages)
            {
                if (img.getParentFile().equals(folder)) {
                    imageFiles.add(img);
                }
            }
            imageFolder.setImages(imageFiles);

            imageFolderList.add(imageFolder);
        }

        return imageFolderList;
    }

    @NonNull
    public static List<ImageFolder> list(@NonNull ContentResolver contentResolver)
    {
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.MIME_TYPE};
        String selection = MediaStore.Images.ImageColumns.MIME_TYPE + " like ?";
        String[] selectionArgs = new String[]{"image/%"};

        return list(contentResolver, projection, selection, selectionArgs, null);
    }
}
