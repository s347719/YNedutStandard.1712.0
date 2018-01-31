//***************************************************************
//*    2015-8-5  下午2:54:18
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.bean.imgpicker;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 贺毅柳
 */
public class ImageFolder implements Parcelable
{
    private ArrayList<ImageFile> images;
    private File directory;

    public ImageFolder(@NonNull ImageFolder src)
    {
        this.images = new ArrayList<>(src.getImages());
        this.directory = new File(src.getDirectory().getPath());
    }

    public ImageFolder()
    {

    }

    /**
     * 根据已有的对象创建并返回一个新的相同内容的对象<br>
     * 相当于调用 {@link ImageFolder#ImageFolder(ImageFolder)} 构造方法创建新对象
     *
     * @param src 源对象
     * @return 新对象，包含相同内容
     */
    public static ImageFolder from(ImageFolder src)
    {
        return new ImageFolder(src);
    }

    /**
     * @return the images
     */
    public ArrayList<ImageFile> getImages()
    {
        return images;
    }

    public void setImages(ArrayList<ImageFile> images)
    {
        this.images = images;
    }

    /**
     * @return the directory
     */
    public File getDirectory()
    {
        return directory;
    }

    /**
     * @param directory the directory to set
     */
    public void setDirectory(File directory)
    {
        this.directory = directory;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeList(this.images);
        dest.writeSerializable(this.directory);
    }

    protected ImageFolder(Parcel in)
    {
        this.images = new ArrayList<>();
        in.readList(this.images, List.class.getClassLoader());
        this.directory = (File) in.readSerializable();
    }

    public static final Creator<ImageFolder> CREATOR = new Creator<ImageFolder>()
    {
        @Override
        public ImageFolder createFromParcel(Parcel source) {return new ImageFolder(source);}

        @Override
        public ImageFolder[] newArray(int size) {return new ImageFolder[size];}
    };
}
