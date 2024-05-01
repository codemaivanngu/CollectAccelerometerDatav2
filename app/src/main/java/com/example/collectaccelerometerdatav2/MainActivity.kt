package com.example.collectaccelerometerdatav2
//https://www.youtube.com/watch?v=6_wK_Ud8--0
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.collectaccelerometerdatav2.ui.theme.CollectAccelerometerDatav2Theme
import org.w3c.dom.Text
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CollectAccelerometerDatav2Theme {
                Box (){
//                    Button(onClick = { /*TODO*/ },
//                        modifier = Modifier.fillMaxSize(),
//                        colors = ButtonDefaults.buttonColors(),
//                        ) {
//
//                    }
                    MainActi()

                }
            }
        }
    }
}


@Composable
fun MainActi(){
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
    var isConnecting = remember {mutableStateOf(false) }
    var isRecording = remember {mutableStateOf(false)}
    var currentData = remember {mutableStateOf(FloatArray(3))}
    val dataArray = remember{mutableStateListOf<FloatArray>()}
    var socket:Socket = Socket()

    fun sendDataToServer(data: FloatArray){
        val str = data.toString()
        try {
            val outputStream = socket.outputStream
            outputStream.write(str.toByteArray())
            outputStream.flush()
        }catch(e:Exception){

        }
    }

    val sensorEventListener = object : SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            if (event !=null){
                currentData.value = event.values.clone()
                dataArray.add(currentData.value)
                if(isConnecting.value)
                {
                    sendDataToServer(currentData.value)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            TODO("Not yet implemented")
        }
    }
    Column (modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
        Button(onClick = {
            isRecording.value = !isRecording.value
//            if(isRecording.value)
//             sensorManager.registerListener(context as SensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
//            else
//                sensorManager.unregisterListener(context as SensorEventListener, accelerometer)
        },
            modifier = Modifier.size(width=200.dp, height = 50.dp),
//            colors = if(isRecording.value)ButtonDefaults.disabledButtonColors() else ButtonDefaults.buttonColors()
            colors = if (isRecording.value) {
                ButtonDefaults.buttonColors(contentColor = Color.LightGray) // Set custom color for disabled state
            } else {
                ButtonDefaults.buttonColors()
            }
            ){ if(!isRecording.value)Text(text = "Start")else Text(text = "Stop")}
//        Button(onClick = {
////
//        },
//            modifier = Modifier.size(width=200.dp, height = 50.dp)){ Text(text = "Stop Button")}
        Button(onClick = {
            isConnecting.value = !isConnecting.value
            if(isConnecting.value)dataArray.clear()
            if(isConnecting.value)
            {
                try{
                    socket.connect(InetSocketAddress("192.168.1.110", 8000))
                    val message = "connected".encodeToByteArray() // Encode message to bytes
                    socket.outputStream.write(message) // Send message
                    socket.outputStream.flush()
                }catch (e:Exception){
                    Log.e("Connection Error", "Error connecting to server: ${e.message}")
                }
            }
            else
                socket.close()
                         },
            modifier = Modifier.size(width=200.dp, height = 50.dp),
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
                try{
                    socket.connect(InetSocketAddress("192.168.1.110", 8000))
                    val message = "connected".encodeToByteArray() // Encode message to bytes
                    socket.outputStream.write(message) // Send message
                    socket.outputStream.flush()
                    socket.close()
                }catch (e:Exception){

                    Log.e("Connection Error", "Error connecting to server troll troll vn: ${e.message}")
                }
//                if(isConnecting.value)
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
    return Math.random().toFloat();
}