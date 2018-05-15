
package gn.com.android.mmitest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class SingleTestGridView extends BaseActivity implements GridView.OnItemClickListener {
    /**
     * Called when the activity is first created.
     */
    private GridView mGrid;
    private String[] mItems;
    private String[] mItemKeys;
    private Intent mIt;
    private Button mQuitBtn;
    private String TAG = "GnMMITest";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_grid_view);
        mQuitBtn = (Button) findViewById(R.id.quit_btn);
        mQuitBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        mGrid = (GridView) findViewById(R.id.single_gv);
        mIt = new Intent();
        mItems = TestUtils.getSingleTestItems(this);
        //Gionee zhangke 20160104 add for CR01617258 start
        if (mItems == null || mItems.length == 0) {
            TestUtils.configTestItemArrays(this);
            mItems = TestUtils.getSingleTestItems(this);
        }
        //Gionee zhangke 20160104 add for CR01617258 end
        mItemKeys = TestUtils.getSingleTestKeys(this);
        mGrid.setAdapter(new SingleGvAdapter());
        mGrid.setOnItemClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TestUtils.releaseWakeLock();
    }

    public class SingleGvAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mItems[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView == null) {
                convertView = SingleTestGridView.this.getLayoutInflater().inflate(
                        R.layout.gridview_item, parent, false);
            }
            ((TextView) convertView).setText(mItems[position]);
            return convertView;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        try {
            mIt.setClass(this, Class.forName("gn.com.android.mmitest.item." + mItemKeys[position]));
            Log.e(TAG, "in hardware mmi test you choose item is =  " + mItemKeys[position]);
            startActivity(mIt);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
