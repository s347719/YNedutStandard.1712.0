package com.yineng.ynmessager.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.db.CommonDB;
import com.yineng.ynmessager.util.L;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yutang date 2014-12-28 保存用户的登陆信息
 */
public class LoginUserDao {
    private SQLiteDatabase mDB;
    private final String TABLE_NAME = "LoginUser";

    /**
     * @param context
     */
    public LoginUserDao(Context context) {
        mDB = (CommonDB.getInstance(context)).getWritableDatabase();
    }

    /**
     * 保存登陆信息
     *
     * @param loginUser
     */
    public void saveLoginUser(LoginUser loginUser) {
        if (isExists(loginUser.getAccount())) {

            updateLoginUser(loginUser);

        } else {
            insertNewLoginUser(loginUser);
        }
    }

    /**
     * 获取最近登陆的帐号信息
     *
     * @return
     */
    public List<LoginUser> getLoginUsers() {
        List<LoginUser> list = new ArrayList<LoginUser>();
        LoginUser user = null;
        String sql = "select * from LoginUser Order By lastLoginDate desc";
        Cursor cursor = mDB.rawQuery(sql.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                user = new LoginUser();
                user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                user.setAccount(cursor.getString(cursor
                        .getColumnIndex("account")));
                user.setUserNo(cursor.getString(cursor
                        .getColumnIndex("userNo")));
                user.setPassWord(cursor.getString(cursor
                        .getColumnIndex("passWord")));
                user.setLastLoginDate(cursor.getString(cursor
                        .getColumnIndex("lastLoginDate")));
                user.setFileSavePath(cursor.getString(cursor.getColumnIndex("fileSavePath")));
                user.setLastLoginType(cursor.getInt(cursor.getColumnIndex("lastLoginType")));
                user.setFirstLoginDate(cursor.getString(cursor.getColumnIndex("firstLoginDate")));
                user.setLastLoginPhoneNum(cursor.getString(cursor.getColumnIndex("lastLoginPhoneNum")));
                user.setIsUserAccountType(cursor.getInt(cursor.getColumnIndex("isUserAccountType")));
                list.add(user);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    /**
     * 插入一条用户登录新记录
     *
     * @param loginUser 登录用户
     */
    private void insertNewLoginUser(LoginUser loginUser) {
        L.v("LoginUserDao", "LoginUserDao:insertNewLoginUser->");
        ContentValues userContentValues = new ContentValues();
        userContentValues.put("userNo", loginUser.getUserNo());
        userContentValues.put("account", loginUser.getAccount());
        userContentValues.put("passWord", loginUser.getPassWord());
        userContentValues.put("firstLoginDate", loginUser.getFirstLoginDate());
        userContentValues.put("lastLoginDate", loginUser.getLastLoginDate());
        userContentValues.put("fileSavePath", loginUser.getFileSavePath());
        userContentValues.put("lastLoginType", loginUser.getLastLoginType());
        userContentValues.put("lastLoginPhoneNum", loginUser.getLastLoginPhoneNum());
        userContentValues.put("isUserAccountType", loginUser.getIsUserAccountType());
        mDB.insert(TABLE_NAME, null, userContentValues);
    }

    /**
     * 更新登陆信息
     *
     * @param loginUser
     */
    private void updateLoginUser(LoginUser loginUser) {
        L.v("LoginUserDao", "LoginUserDao:updateLoginUser->");
        ContentValues userContentValues = new ContentValues();
        userContentValues.put("userNo", loginUser.getUserNo());
        userContentValues.put("account", loginUser.getAccount());
        userContentValues.put("passWord", loginUser.getPassWord());
        userContentValues.put("firstLoginDate", loginUser.getFirstLoginDate());
        userContentValues.put("lastLoginDate", loginUser.getLastLoginDate());
        userContentValues.put("fileSavePath", loginUser.getFileSavePath());
        userContentValues.put("lastLoginPhoneNum", loginUser.getLastLoginPhoneNum());
        userContentValues.put("lastLoginType", loginUser.getLastLoginType());
        //如果是0就修改
        LoginUser user = getLoginUserByAccount(loginUser.getAccount());
        if (user.getIsUserAccountType() == 0) {
            userContentValues.put("isUserAccountType", loginUser.getIsUserAccountType());
        }
        mDB.update(TABLE_NAME, userContentValues, "account = ?",
                new String[]{loginUser.getAccount()});
    }

    /**
     * 判断帐号是否存在
     *
     * @param account
     * @return
     */
    public boolean isExists(String account) {
        boolean isExists = false;
        String sql = "select * from LoginUser where account ='" + account + "'";

        Cursor cursor = mDB.rawQuery(sql.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {

            isExists = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        L.v("LoginUserDao", "LoginUserDao:isExists:sql->" + sql + " " + isExists);
        return isExists;
    }

    /**
     * 根据unsrNo 来判断是否存在当前帐号
     *
     * @param userno
     * @return
     */
    public boolean isExistUserNo(String userno) {
        boolean isExistUserNo = false;
        String sql = "select * from LoginUser where userNo ='" + userno + "'";

        Cursor cursor = mDB.rawQuery(sql.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {

            isExistUserNo = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        L.v("LoginUserDao", "LoginUserDao:isExists:sql->" + sql + " " + isExistUserNo);
        return isExistUserNo;
    }

    /**
     * 根据用户ID删除用户登陆记录
     *
     * @param account
     */
    public void deleteByAccount(String account) {
        mDB.delete("LoginUser", "account = ?", new String[]{account});
    }

    /**
     * 根据帐号id找到该user对象
     *
     * @param mUserAccount 登陆帐号
     */
    public LoginUser getLoginUserByAccount(String mUserAccount) {
        LoginUser user = null;
        String sql = "select * from LoginUser where account = ?";
        Cursor cursor = mDB.rawQuery(sql.toString(), new String[]{mUserAccount});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                user = new LoginUser();
                user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                user.setAccount(cursor.getString(cursor
                        .getColumnIndex("account")));
                user.setUserNo(cursor.getString(cursor
                        .getColumnIndex("userNo")));
                user.setPassWord(cursor.getString(cursor
                        .getColumnIndex("passWord")));
                user.setLastLoginDate(cursor.getString(cursor
                        .getColumnIndex("lastLoginDate")));
                user.setFileSavePath(cursor.getString(cursor.getColumnIndex("fileSavePath")));
                user.setFirstLoginDate(cursor.getString(cursor.getColumnIndex("firstLoginDate")));
                user.setLastLoginType(cursor.getInt(cursor.getColumnIndex("lastLoginType")));
                user.setLastLoginPhoneNum(cursor.getString(cursor.getColumnIndex("lastLoginPhoneNum")));
                user.setIsUserAccountType(cursor.getInt(cursor.getColumnIndex("isUserAccountType")));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return user;
    }

    /**
     * 根据帐号userNo找到该user对象
     *
     * @param userNo 登陆帐号
     */
    public LoginUser getLoginUserByUserNo(String userNo) {
        LoginUser user = null;
        String sql = "select * from LoginUser where userNo = ?";
        Cursor cursor = mDB.rawQuery(sql.toString(), new String[]{userNo});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                user = new LoginUser();
                user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                user.setAccount(cursor.getString(cursor
                        .getColumnIndex("account")));
                user.setUserNo(cursor.getString(cursor
                        .getColumnIndex("userNo")));
                user.setPassWord(cursor.getString(cursor
                        .getColumnIndex("passWord")));
                user.setLastLoginDate(cursor.getString(cursor
                        .getColumnIndex("lastLoginDate")));
                user.setFileSavePath(cursor.getString(cursor.getColumnIndex("fileSavePath")));
                user.setFirstLoginDate(cursor.getString(cursor.getColumnIndex("firstLoginDate")));
                user.setLastLoginType(cursor.getInt(cursor.getColumnIndex("lastLoginType")));
                user.setLastLoginPhoneNum(cursor.getString(cursor.getColumnIndex("lastLoginPhoneNum")));
                user.setIsUserAccountType(cursor.getInt(cursor.getColumnIndex("isUserAccountType")));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return user;
    }
}
