package cy.com.android.mmitest.unity.bean;
import cy.com.android.mmitest.utils.DswLog;
/**
 * Created by qiang on 4/14/17.
 */
public class User
{
    public static final String TAG = User.class.getName();
    public static final int ERROR_IS_INIT = -1;
    public static final int ERROR_IS_SUCCED = 1;
    public static final int ERROR_IS_SAME_AREA = 2;
    public static final int ERROR_IS_NOT_SUPPORT_UNITY = 3;
    public static final int ERROR_IS_CONFIGXML_NOT_EXISTS = 4;
    private String curCountry ;
    private String idCountry;
    private String unityResult;
    private String idConfigPath;

    public String getIdConfigPath() {
        return idConfigPath;
    }

    public void setIdConfigPath(String idConfigPath) {
        this.idConfigPath = idConfigPath;
    }

    private int error;



    public void setIdCountry(String idCountry) {
        this.idCountry = idCountry;
    }

    public String getIdCountry() {
        return idCountry;
    }

    public boolean isSameArea() {
        if (getIdCountry().equals(getCurCountry())) {

            DswLog.d(TAG,"is SameArea--getIdCountry="+getIdCountry()+"  getCurCountry="+getCurCountry());
            return true;
        }
        DswLog.d(TAG,"not SameArea--getIdCountry="+getIdCountry()+"  getCurCountry="+getCurCountry());
        return false;
    }
    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public void setCurCountry(String curCountry) {
        this.curCountry = curCountry;
    }

    public String getCurCountry() {
        return curCountry;
    }

    public String getUnityResult() {
        if (error == ERROR_IS_SUCCED) {
            //初始化成功
            unityResult += "";
        }else if (error == ERROR_IS_SAME_AREA) {
            //当前地区与选择地区相同
            unityResult += "当前地区与选择地区相同";
        }else if (error == ERROR_IS_NOT_SUPPORT_UNITY) {
            //没有对应的地区参数
            unityResult += "该版本不支持一体化";
        }else if (error == ERROR_IS_INIT) {
            unityResult += "一体化化未完成，异常退出";
        }else if (error == ERROR_IS_CONFIGXML_NOT_EXISTS) {
            unityResult += "没有找到选中地区的MMI配置项文件";
        }else {
            unityResult += "未知错误参数参数";
        }
        return unityResult;
    }

    public void setUnityResult(String unityResult) {
        this.unityResult = unityResult;
    }
}