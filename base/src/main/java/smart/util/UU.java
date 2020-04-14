package smart.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.log.Log;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class UU {
    public Bitmap getContackPhoto(Context context, long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri, new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                final byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public static long getContactIDFromNumber(Context context, String contactNumber) {
        long id = 0;
        final String UriContactNumber = Uri.encode(contactNumber);
        final Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, UriContactNumber), new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);

        while (cursor.moveToNext()) {
            long _id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            String display_name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            Log.e(_id, display_name, contactNumber);
        }
        cursor.close();
        return id;
    }

    public static String pwComplexError(byte[] password) {
        int N = password.length;
        if (!(10 <= N && N <= 30))
            return ("비밀번호의 자릿수는 10자리 이상, 30자리 이하입니다.");

        byte prev_2 = 0;
        byte prev_1 = 0;
        boolean s = false;//특수문자
        boolean a = false;//문자
        boolean n = false;//숫자
        int nc = 0;//숫자카운트

        for (int i = 0; i < N; i++) {
            byte c = password[i];

            {//영문자 숫자 특수문자를 포함하는지
                if (!s && (!('a' <= c && c <= 'z') && !('A' <= c && c <= 'Z') && !('0' <= c && c <= '9')))
                    s = true;
                if (!a && ('a' <= c && c <= 'z' || 'A' <= c && c <= 'Z'))
                    a = true;
                if (!n && ('0' <= c && c <= '9')) {
                    n = true;
                }
            }

            if ('\'' == c || '\"' == c || '\\' == c || '|' == c)
                return ("사용할 수 없는 특수문자가 포함되어 있습니다. (예 : ', \", \\, |)");

            if (prev_2 == prev_1 && prev_1 == c)
                return ("동일한 영문, 숫자, 특수문자를 3자리 이상 사용할 수 없습니다.");

            if ((prev_2 + 1 == prev_1 && prev_1 + 1 == c) || (prev_2 - 1 == prev_1 && prev_1 - 1 == c))
                return ("연속된 영문, 숫자를 3자리 이상 사용할 수 없습니다. (예 : 123, 321, abc, cba)");
            prev_2 = prev_1;
            prev_1 = c;
        }

        if (!(s && a && n))
            return ("비밀번호에는 숫자, 영문, 특수문자가 포함되어야 합니다. (제외 : ', \", \\, |)");

        return "";
    }

}
