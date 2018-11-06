package cy.com.android.mmitest.unity.presenter;

import android.os.Handler;

import cy.com.android.mmitest.unity.bean.User;
import cy.com.android.mmitest.unity.biz.IUserBiz;
import cy.com.android.mmitest.unity.biz.OnUnityListener;
import cy.com.android.mmitest.unity.biz.OnUnityRevertListener;
import cy.com.android.mmitest.unity.biz.UserBiz;
import cy.com.android.mmitest.unity.view.IUserLoginView;


/**
 * Created by qiang on 4/14/17.
 */
public class UserLoginPresenter {
    private IUserBiz userBiz;
    private IUserLoginView userLoginView;
    private Handler mHandler = new Handler();

    public UserLoginPresenter(IUserLoginView userLoginView) {
        this.userLoginView = userLoginView;
        this.userBiz = new UserBiz();
    }

    public void unityStart(final User user) {


        userBiz.login(user, new OnUnityListener() {
            @Override
            public void loginSuccess(final User mUser) {
                //需要在UI线程执行
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        userLoginView.hideLoading();//取消load
                        // 可以查看User
                        userLoginView.showUnityResult(mUser);

                    }
                });

            }

        });
    }

    public void revertFactory(final User user) {
        userLoginView.showLoading();//显示正在进行一体化设置
        userBiz.revert(user, new OnUnityRevertListener() {
            @Override
            public void revertResult(String msg) {
                //需要在UI线程执行
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        userLoginView.showrevertResult(msg);
                    }
                });

            }

        });
    }
    public void clear() {
        //清除user
    }


}