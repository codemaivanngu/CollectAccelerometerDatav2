package com.example.collectaccelerometerdatav2
//https://www.youtube.com/watch?v=6_wK_Ud8--0
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
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
import com.example.collectaccelerometerdatav2.ui.theme.CollectAccelerometerDatav2Theme
import java.net.Socket


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CollectAccelerometerDatav2Theme {
//                var count = remember{
//                    mutableStateOf(0)
//                }
//                Column(
//                    modifier = Modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
////                    Text(text=count.value.toString(), fontSize = 30.sp)
//                    Button(onClick = { /*TODO*/
//                        count.value++
//                    }) {
//                        Text(text="Start record data")
//                    }
//                    Button(onClick = {}){
//                        Text(text="Stop recording")
//                    }
//                    Button(onClick = {}){
//                        Text(text="Connect to server and send data")
//                    }
//                    Button(onClick = {}){
//                        Text(text="Disconnect")
//                    }
//
//                }
                MainActi()
            }
        }
    }
}


@Composable
fun MainActi(){
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
    var isConnect = remember {mutableStateOf(0) }
    val dataArray = remember{mutableStateListOf<FloatArray>()}
    val socket = Socket("127.168.1.110",8000)
    Column (modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Button(onClick = {
            sensorManager.registerListener(context as SensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }){ Text(text = "Start Button")}
        Button(onClick = {
            sensorManager.unregisterListener(context as SensorEventListener, accelerometer)
        }){ Text(text = "Stop Button")}
        Button(onClick = {isConnect.value = 1},){
            Text(text = "Connect")

//            // Handle socket connection using Ktor or WorkManager (recommended)
//            // For simplicity, a coroutine is used here (not ideal for production)
//            viewModelScope.launch {
//                val socket = Socket(serverInetAddress, portNumber)
//                // ... data sending logic using the socket ...
//            }
        }
//        Button(isConnect = isConnect, onClick = {
//            isConnect = 0
//           // Assuming socket is available from the coroutine scope
//        }){ Text(text = "Disconnect")}
        Button(onClick = {isConnect.value = 0,
            socket.close()
        }) {
            Text(text = "Disconnect")
        }
        Text(text = "x,y,z")

        // Optional DataDisplay composable to show collected data
    }

    DisposableEffect(Unit) {
        val listener = context as SensorEventListener
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose {
            sensorManager.unregisterListener(listener, accelerometer)
        }
    }
}