package com.example.collectaccelerometerdatav2
//https://www.youtube.com/watch?v=6_wK_Ud8--0
//import androidx.compose.ui.unit.TextUnit
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.collectaccelerometerdatav2.ui.theme.CollectAccelerometerDatav2Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket


public class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CollectAccelerometerDatav2Theme {
                Box {
                    MainActi()
                }
            }
        }
    }
    @Composable
    fun MainActi(){
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        var isConnecting = remember {mutableStateOf(false) }
        var isRecording = remember {mutableStateOf(false)}
        var currentData = remember {mutableStateOf(FloatArray(3))}
        val dataArray = remember{mutableStateListOf<FloatArray>()}
        val socket = remember {  // Remember the socket instance
            Socket()
        }

        fun sendDataToServer(data: FloatArray){
            val currentTimeMillis = SystemClock.elapsedRealtime()

            val str= "["+data[0].toString()+","+data[1].toString()+","+data[1].toString()+"]" + "time: "+currentTimeMillis.toString()
            Log.e("send string",str)
            coroutineScope.launch {
                withContext(Dispatchers.Default) {// Launch coroutine for network operations
                    try {
                        val outputStream = socket.outputStream
                        outputStream.write(str.toByteArray())
                        outputStream.flush()
                    } catch (e: Exception) {
                        Log.e(
                            "Connection Error",
                            "in sendDataToSever - Error connecting to server: ${e.message}"
                        )
                    } finally {
                        // Consider closing the socket here (optional)
                    }
                }
            }
        }

        val sensorEventListener = object : SensorEventListener{
            override fun onSensorChanged(event: SensorEvent?) {
                if (event !=null){
                    currentData.value = event.values.clone()
                    dataArray.add(currentData.value)
                    if(isConnecting.value)
                    {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                sendDataToServer(currentData.value)
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//                TODO("Not yet implemented")
            }
        }

        Column (modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
            Button(onClick = {
                isRecording.value = !isRecording.value
            if(isRecording.value)
             sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            else
                sensorManager.unregisterListener(sensorEventListener, accelerometer)
            },
                modifier = Modifier.size(width=200.dp, height = 50.dp),
//            colors = if(isRecording.value)ButtonDefaults.disabledButtonColors() else ButtonDefaults.buttonColors()
                colors = if (isRecording.value) {
                    ButtonDefaults.buttonColors(contentColor = Color.LightGray) // Set custom color for disabled state
                } else {
                    ButtonDefaults.buttonColors()
                }
            ){ if(!isRecording.value)Text(text = "Start")else Text(text = "Stop")}

            Button(
                onClick = {
                    isConnecting.value = !isConnecting.value
                    if (isConnecting.value) {
                        dataArray.clear()
                        coroutineScope.launch {
                            withContext(Dispatchers.Default) {// Launch coroutine for connection
                                try {
                                    socket.setSoTimeout(500)
                                    socket.connect(InetSocketAddress("192.168.1.110", 8000))
                                    val message = "connected".encodeToByteArray()
                                    socket.outputStream.write(message)
                                    socket.outputStream.flush()

                                    // Update isConnecting state based on success (optional)
                                } catch (e: Exception) {
                                    Log.e(
                                        "Connection Error",
                                        "Button Connect - Error connecting to server: ${e.message}"
                                    )
                                    // Update isConnecting state based on failure (optional)
                                    e.printStackTrace()
                                }
                            }
                        }
                    } else
                        socket.close()
//                pass
                },
                modifier = Modifier.size(width = 200.dp, height = 50.dp),
                colors = if (isRecording.value) {
                    ButtonDefaults.buttonColors(contentColor = Color.LightGray) // Set custom color for disabled state
                } else {
                    ButtonDefaults.buttonColors()
                },
            ){
                if(!isConnecting.value)Text(text = "Connect")else Text(text="Disconnect")

//            // Handle socket connection using Ktor or WorkManager (recommended)
//            // For simplicity, a coroutine is used here (not ideal for production)
//            viewModelScope.launch {
//                val socket = Socket(serverInetAddress, portNumber)
//                // ... data sending logic using the socket ...
//            }
            }
            Button(
                onClick = {
                    // Generate random values for currentData
                    currentData.value = floatArrayOf(generateRandomFloat(), generateRandomFloat(), generateRandomFloat())
                    dataArray.add(currentData.value)
                    sendDataToServer(currentData.value)
                },
                modifier = Modifier.size(width = 200.dp, height = 50.dp)
            ) {
                Text(text = "Generate Trash Data")
            }

            Text(text = "x= ${currentData.value[0]},y= ${currentData.value[1]},z = ${currentData.value[2]}")
            LazyColumn {
                items(dataArray) { currentData ->
                    Text(text = currentData.contentToString()) // Display each FloatArray
                }
            }

            // Optional DataDisplay composable to show collected data
        }

        DisposableEffect(Unit) {
            onDispose {
                if(isRecording.value)sensorManager.unregisterListener(sensorEventListener,accelerometer)
            }
        }

    }

    fun generateRandomFloat():Float{
        return Math.random().toFloat()
    }

}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////