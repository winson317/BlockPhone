package com.example.blockphone;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.android.internal.telephony.ITelephony;

public class BlockPhone extends Activity {
	
	ArrayList<String> blockList = new ArrayList<String>(); //记录黑名单的List
	TelephonyManager telephonyManager;
	CustomPhoneCallListener customPhoneListener; //监听通话状态的监听器
	
	public class CustomPhoneCallListener extends PhoneStateListener
	{

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			switch (state) 
			{
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
				//当电话呼入时
			case TelephonyManager.CALL_STATE_RINGING:
				//如果该号码属于黑名单
				if (isBlock(incomingNumber))
				{
					System.out.println("--挂断电话--");
					
					try
					{
						Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
						//获取远程TELEPHONY_SERVICE的IBinder对象的代理
						IBinder binder = (IBinder)method.invoke(null, new Object[] {TELEPHONY_SERVICE});
						//将IBinder对象的代理转换为ITelephony对象
						ITelephony telephony = ITelephony.Stub.asInterface(binder);
						telephony.endCall();  //挂断电话
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				
				break;
			}

			super.onCallStateChanged(state, incomingNumber);
		}
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); //获取系统的TelephonyManager管理器
        customPhoneListener = new CustomPhoneCallListener();
        //通过TelephonyManager监听通话状态的改变
        telephonyManager.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        Button button = (Button)findViewById(R.id.managerBlock);
        
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//查询联系人的电话号码
				final Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
						null, null, null, null);
				
				BaseAdapter adapter = new BaseAdapter() 
				{
					
					@Override
					public View getView(int position, View convertView, ViewGroup parent) 
					{
						// TODO Auto-generated method stub
						cursor.moveToPosition(position);
						CheckBox checkBox = new CheckBox(BlockPhone.this);
						//获取联系人的电话号码，并去掉中间的中划线、空格
						String number = cursor.getString(cursor.getColumnIndex
								(ContactsContract.CommonDataKinds.Phone.NUMBER))
								.replace("_", "")
								.replace(" ", "");
						checkBox.setText(number);
						//如果该号码已经被加入黑名单、默认勾选该号码
						if (isBlock(number))
						{
							checkBox.setChecked(true);
						}
						
						return checkBox;
					}
					
					@Override
					public long getItemId(int position) {
						// TODO Auto-generated method stub
						return position;
					}
					
					@Override
					public Object getItem(int position) {
						// TODO Auto-generated method stub
						return position;
					}
					
					@Override
					public int getCount() {
						// TODO Auto-generated method stub
						return cursor.getCount();
					}
				};
				
				//加载list.xml布局文件对应的View
				View selectView = getLayoutInflater().inflate(R.layout.list, null);
				//获取selectView中的名为list的ListView组件
				final ListView listView = (ListView)selectView.findViewById(R.id.list);
				listView.setAdapter(adapter);
				
				new AlertDialog.Builder(BlockPhone.this).setView(selectView).setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								blockList.clear(); //清空blockList集合
								//遍历listView组件的每个列表项
								for (int i = 0; i < listView.getCount(); i++)
								{
									CheckBox checkBox2 = (CheckBox)listView.getChildAt(i);
									//如果该列表项被勾选
									if (checkBox2.isChecked())
									{
										blockList.add(checkBox2.getText().toString()); //添加该列表项的电话号码
									}
								}
								System.out.println(blockList);
							}
						}).show();
			}
		}); 
    }
    
    //判断某个电话号码是否在黑名单之内
    public boolean isBlock (String phone)
    {
    	System.out.println("呼入号码：" + phone);
    	System.out.println("------" + blockList);
    	
    	for (String s1 : blockList)
    	{
    		if (s1.equals(phone))
    		{
    			return true;
    		}
    	}
		return false;
    }
    
}
