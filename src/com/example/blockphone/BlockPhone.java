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
	
	ArrayList<String> blockList = new ArrayList<String>(); //��¼��������List
	TelephonyManager telephonyManager;
	CustomPhoneCallListener customPhoneListener; //����ͨ��״̬�ļ�����
	
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
				//���绰����ʱ
			case TelephonyManager.CALL_STATE_RINGING:
				//����ú������ں�����
				if (isBlock(incomingNumber))
				{
					System.out.println("--�Ҷϵ绰--");
					
					try
					{
						Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
						//��ȡԶ��TELEPHONY_SERVICE��IBinder����Ĵ���
						IBinder binder = (IBinder)method.invoke(null, new Object[] {TELEPHONY_SERVICE});
						//��IBinder����Ĵ���ת��ΪITelephony����
						ITelephony telephony = ITelephony.Stub.asInterface(binder);
						telephony.endCall();  //�Ҷϵ绰
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

        telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); //��ȡϵͳ��TelephonyManager������
        customPhoneListener = new CustomPhoneCallListener();
        //ͨ��TelephonyManager����ͨ��״̬�ĸı�
        telephonyManager.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        Button button = (Button)findViewById(R.id.managerBlock);
        
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//��ѯ��ϵ�˵ĵ绰����
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
						//��ȡ��ϵ�˵ĵ绰���룬��ȥ���м���л��ߡ��ո�
						String number = cursor.getString(cursor.getColumnIndex
								(ContactsContract.CommonDataKinds.Phone.NUMBER))
								.replace("_", "")
								.replace(" ", "");
						checkBox.setText(number);
						//����ú����Ѿ��������������Ĭ�Ϲ�ѡ�ú���
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
				
				//����list.xml�����ļ���Ӧ��View
				View selectView = getLayoutInflater().inflate(R.layout.list, null);
				//��ȡselectView�е���Ϊlist��ListView���
				final ListView listView = (ListView)selectView.findViewById(R.id.list);
				listView.setAdapter(adapter);
				
				new AlertDialog.Builder(BlockPhone.this).setView(selectView).setPositiveButton("ȷ��",
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								blockList.clear(); //���blockList����
								//����listView�����ÿ���б���
								for (int i = 0; i < listView.getCount(); i++)
								{
									CheckBox checkBox2 = (CheckBox)listView.getChildAt(i);
									//������б����ѡ
									if (checkBox2.isChecked())
									{
										blockList.add(checkBox2.getText().toString()); //��Ӹ��б���ĵ绰����
									}
								}
								System.out.println(blockList);
							}
						}).show();
			}
		}); 
    }
    
    //�ж�ĳ���绰�����Ƿ��ں�����֮��
    public boolean isBlock (String phone)
    {
    	System.out.println("������룺" + phone);
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
