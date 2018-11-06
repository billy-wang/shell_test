package cy.com.android.mmitest.unity.biz;

import cy.com.android.mmitest.unity.bean.User;
import cy.com.android.mmitest.unity.biz.OnUnityListener;
import cy.com.android.mmitest.unity.biz.OnUnityRevertListener;
/**
 * Created by qiang on 4/14/17.
 */
public interface IUserBiz
{
    void login(User user, OnUnityListener loginListener);
    void revert(User user, OnUnityRevertListener revertListener);
}