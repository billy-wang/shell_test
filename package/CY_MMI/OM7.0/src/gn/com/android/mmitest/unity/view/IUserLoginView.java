package gn.com.android.mmitest.unity.view;

import gn.com.android.mmitest.unity.bean.User;

/**
 * Created by qiang on 4/14/17.
 */
public interface IUserLoginView
{
    void showUnityResult(User user);

    void showLoading();

    void hideLoading();

    void showrevertResult(String msg);
}