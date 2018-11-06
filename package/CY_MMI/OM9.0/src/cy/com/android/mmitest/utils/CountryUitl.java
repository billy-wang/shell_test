package cy.com.android.mmitest.utils;

import android.content.Context;
import cy.com.android.mmitest.R;
public class CountryUitl {

    public enum Pcountry {
        GI("GI"),
        GN("GN"),
        GM("GM"),
        GP("GP"),
        GS("GS"),
        GL("GL"),
        GD("GD"),
        GH("GH"),
        GA("GA"),
        GE("GE"),
        GK("GK"),
        GC("GC"),
        GU("GU");
        Pcountry(String pName) {
        }
    }

    public static String getCountry(Context mContext, String area) {
        String country = null;
        try {
            switch (Pcountry.valueOf(area)) {
                case GI:
                    country = mContext.getResources().getString(R.string.country_gi);
                    break;
                case GN:
                    country = mContext.getResources().getString(R.string.country_gn);
                    break;
                case GM:
                    country = mContext.getResources().getString(R.string.country_gm);
                    break;
                case GP:
                    country = mContext.getResources().getString(R.string.country_gp);
                    break;
                case GS:
                    country = mContext.getResources().getString(R.string.country_gs);
                    break;
                case GL:
                    country = mContext.getResources().getString(R.string.country_gl);
                    break;
                case GD:
                    country = mContext.getResources().getString(R.string.country_gd);
                    break;
                case GH:
                    country = mContext.getResources().getString(R.string.country_gh);
                    break;
                case GA:
                    country = mContext.getResources().getString(R.string.country_ga);
                    break;
                case GE:
                    country = mContext.getResources().getString(R.string.country_ge);
                    break;
                case GK:
                    country = mContext.getResources().getString(R.string.country_gk);
                    break;
                case GC:
                    country = mContext.getResources().getString(R.string.country_gc);
                    break;
                case GU:
                    country = mContext.getResources().getString(R.string.country_gu);
                    break;
            }

        } catch (IllegalArgumentException e) {

        }
        return country;
    }

}