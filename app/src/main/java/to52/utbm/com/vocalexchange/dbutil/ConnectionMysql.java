package to52.utbm.com.vocalexchange.dbutil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class ConnectionMysql extends AsyncTask<String,String,String> {
    String classs = "com.mysql.jdbc.Driver";
    // 172.17.0.99 192.168.1.11 172.20.10.3
    String ip = "192.168.1.11";
    //String url = "jdbc:mysql://"+ip+":3306/vocaldb";
    String url2 = "jdbc:mysql://"+ip+"/vocaldb?"+"user=androidMysql&password=1231";
    //

    String user = "androidMysql";
    String password = "1231";
    String TAG = "ConnectionMySql";
    String z="";
    boolean isSuccess=false;
    Connection conn = null;

    public ConnectionMysql(Context context) {
        super();
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    private Context context;
    //in constructor:


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.i(TAG+"postExecute",s);
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            try {
                Class.forName(classs);
                //conn = DriverManager.getConnection(url, user, password);
                conn = DriverManager.getConnection(url2);

                //conn = DriverManager.getConnection(ConnURL);
                Log.e(TAG, "ConnectionSuccess");
            } catch (SQLException se) {
                Log.e(TAG+"Esql", se.getMessage());
            } catch (ClassNotFoundException e) {
                Log.e(TAG+"ENotfound", e.getMessage());
            } catch (Exception e) {
                Log.e(TAG+"Erro", e.getMessage());
            }

               // String query="insert into question values('"+namestr+"','"+emailstr+"','"+passstr+"')";
                String query="select * from question ";
                Statement stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(query);
            int rn = 0;
            while(rs.next()) {
                rn++;
                String item1 = rs.getString(1);
                String item2  = rs.getString(2);
                String item3  = rs.getString(3);

                Log.i(TAG, item1+item2+item3);
            }
               //stmt.executeUpdate(query);
                z = "Register successfull";
                isSuccess=true;
        }
        catch (Exception ex)
        {
            isSuccess = false;
            z = "Exceptions"+ex;
        }
        return z;
    }
}
