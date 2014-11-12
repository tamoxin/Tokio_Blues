package com.tumblr.tamoxin.tokioblues;

import android.app.Activity;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;

public class accelerometerInfo extends Activity implements SensorListener{

    final String sensorLog = "Sensor Log: ";
    //Variables for the interface
    public float yViewPositive,yViewNegative,zViewPositive,zViewNegative;
    private String messageY, messageZ;
    private TextView x,y,z,direction,xDegrees,yDegrees,zDegrees;
    //variables for sensors
    private SensorManager sensManager;
    //Variables to communicate thorough classes
    public Bundle ipRecover;
    public String ipServer;
    public int portServer;
    //Variables for sending data
    private informationSender sender;
    private String degrees,accelerometer;
    private String[] dataPackage;
    private Thread senderThread;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_info);

        yViewPositive = 20;
        yViewNegative = -20;
        zViewPositive = 20;
        zViewNegative = -20;

        ipRecover = getIntent().getExtras();
        ipServer = ipRecover.getString("ipServer");
        portServer = Integer.parseInt(ipRecover.getString("ipPort"));

        direction = (TextView) findViewById(R.id.direction_data);
        x = (TextView) findViewById(R.id.x_axis_data);
        y = (TextView) findViewById(R.id.y_axis_data);
        z = (TextView) findViewById(R.id.z_axis_data);
        xDegrees = (TextView) findViewById(R.id.x_axis_data_gyro);
        yDegrees = (TextView) findViewById(R.id.y_axis_data_gyro);
        zDegrees = (TextView) findViewById(R.id.z_axis_data_gyro);

        sensManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Log.d("IP", ipServer);
        Log.d("Port", String.valueOf(portServer));

        Toast.makeText(this, this.getString(R.string.successful_connection)
                +"\n" +this.getString(R.string.ip) +ipServer
                +"\n" +this.getString(R.string.port) +portServer, Toast.LENGTH_LONG).show();

        dataPackage = new String[2];

        sender = new informationSender(ipServer,portServer);
        sender.setMessage();
        senderThread = new Thread(sender);
        senderThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accelerometer_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        sensManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        sensManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensManager.registerListener(this, SensorManager.SENSOR_ORIENTATION
                | SensorManager.SENSOR_ACCELEROMETER
                , SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(int i, float[] floats)
    {
        synchronized (this)
        {
            //Log.d(sensorLog, "On sensor changed: " + i + " X:" + floats[0] + " Y:" + floats[1] + " Z:" + floats[2]);
            if(i == SensorManager.SENSOR_ORIENTATION){

                degrees = Arrays.toString(floats);
                //Log.d("Degrees:", degrees);
                if(floats[1] >= yViewPositive){
                    messageY = "UP";
                    //this.packager.setSensorsMessage(message);
                    //Log.d("Direction:", messageY +"-----" +floats[1]);
                }else if(floats[1] <= yViewNegative){
                    messageY = "DOWN";
                    //this.packager.setSensorsMessage(message);
                    //Log.d("Direction:", messageY + "-----" + floats[1]);
                }else messageY = "";

                if(floats[2] >= zViewPositive){
                    messageZ = "RIGHT";
                    //this.packager.setSensorsMessage(message);
                    //Log.d("Direction:", messageZ+"-----" +floats[2]);
                }
                else if(floats[2] <= zViewNegative){
                    messageZ = "LEFT";
                    //this.packager.setSensorsMessage(message);
                    //Log.d("Direction:", messageZ+"-----" +floats[2]);
                }else messageZ = "";

                dataPackage[0]= degrees;
                direction.setText(""+messageY+","+messageZ);
                xDegrees.setText(""+(int)floats[0]);
                yDegrees.setText(""+(int)floats[1]);
                zDegrees.setText(""+(int)floats[2]);
            }

            if(i == SensorManager.SENSOR_ACCELEROMETER){
                accelerometer = Arrays.toString(floats);
                //Log.d("Accelerom:", accelerometer);
                x.setText(""+floats[0]);
                y.setText(""+floats[1]);
                z.setText(""+floats[2]);
                dataPackage[1] = accelerometer;
            }

            if(dataPackage[0] != null && dataPackage[1] != null){
                sender.setMessage(dataPackage);
                sender.run();
            }else{
                Log.d("FOOOOOOOOO:", "The string returned null");
                return;
            }
        }
    }

    @Override
    public void onAccuracyChanged(int i, int i2)
    {
    }
}
