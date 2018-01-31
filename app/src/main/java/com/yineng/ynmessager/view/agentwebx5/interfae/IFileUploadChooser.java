package com.yineng.ynmessager.view.agentwebx5.interfae;

import android.content.Intent;

/**
 * Created by cenxiaozhong on 2017/5/22.
 * source CODE  https://github.com/Justson/AgentWebX5
 */

public interface IFileUploadChooser {



    void openFileChooser();

    void fetchFilePathFromIntent(int requestCode, int resultCode, Intent data);
}
