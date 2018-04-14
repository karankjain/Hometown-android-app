package com.example.karanjain.weconnect;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by karanjain on 4/12/17.
 */
public class Utility {

    public static ProgressDialog ProgressDialog;



    public static void dialogBox(String title, String message, boolean isError, Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(isError){
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
        }else{
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                }
            });
        }
        builder.setMessage(message);
        builder.setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static String[] toArray(String listString)
    {
        String[] list = listString.split("\",\"");
        String[] realList = new String[list.length+1];
        realList[0]=" ";
        int lastElement = list.length-1;

        list[0]=list[0].substring(2);
        list[lastElement]=list[lastElement].substring(0,list[lastElement].length()-2);

        for (int i=1;i<list.length;i++) {
            realList[i]=list[i-1];
        }
        return list;
    }

    public static boolean checkIsNullOrIsEmpty(String string){
        if( null != string && !string.isEmpty()){
            return true;
        }
        return  false;
    }


    public static void displayProgressDialog(Context context) {
        if (ProgressDialog == null) {
            ProgressDialog = new ProgressDialog(context);
            ProgressDialog.setMessage("Loading..");
            ProgressDialog.setIndeterminate(true);
        }

        ProgressDialog.show();
    }

    public static void removeProgressDialog() {
        if (ProgressDialog != null && ProgressDialog.isShowing()) {
            ProgressDialog.dismiss();
        }
    }


}
