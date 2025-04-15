package com.example.sobadguys

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sobadguys.ui.theme.SobadguysTheme
import android.app.role.RoleManager
import android.app.role.RoleManager.ROLE_CALL_SCREENING
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : ComponentActivity() {

    private var isAppEnabled = false
    private lateinit var mServiceConnection: ServiceConnection
    private lateinit var requestRoleLauncher: ActivityResultLauncher<Intent>
    private lateinit var intentRole: Intent
    private lateinit var mCallServiceIntent: Intent
    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CallScreening.STOP_WORKING = true;
        val appImage = findViewById<ImageView>(R.id.app_image)
        val appStatusText = findViewById<TextView>(R.id.app_description)

        appImage.setOnClickListener {
            isAppEnabled = !isAppEnabled
            if (isAppEnabled) {
                CallScreening.STOP_WORKING = false;
                Toast.makeText(this, "Приложение запускается", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Приложение останавливается", Toast.LENGTH_LONG).show()
            }
            //toggleApp(appImage)
            manageService()
        }

        requestRoleLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    bindMyService(appStatusText)
                } else {
                    Toast.makeText(
                        this,
                        "Не удалось получить доступ к роли Call Screening",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun manageService() {
        if (isAppEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
                    intentRole =
                        roleManager.createRequestRoleIntent(ROLE_CALL_SCREENING)
                    requestRoleLauncher.launch(intentRole)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        "Ошибка при управлении приложением: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            CallScreening.STOP_WORKING = true;
            stopService(mCallServiceIntent)
            unbindMyService()
        }
    }

    private fun bindMyService(appStatusText: TextView) {
        if (!isBound) {
            mCallServiceIntent = Intent("android.telecom.CallScreeningService")
            mCallServiceIntent.setPackage(applicationContext.packageName)
            mServiceConnection = object : ServiceConnection {
                override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {

                    Toast.makeText(this@MainActivity, "Приложение запущено", Toast.LENGTH_LONG).show()
                    isBound = true
                    appStatusText.text = "Приложение работает"
                }

                override fun onServiceDisconnected(componentName: ComponentName) {

                    Toast.makeText(this@MainActivity, "Приложение остановлено", Toast.LENGTH_LONG).show()
                    isBound = false
                    appStatusText.text = "Приложение выключено";

                }

                override fun onBindingDied(name: ComponentName) {
                    Toast.makeText(
                        this@MainActivity,
                        "Связь с приложением оборвалась",
                        Toast.LENGTH_LONG
                    ).show()
                    isBound = false
                    appStatusText.text = "Сбой в работе приложения"
                }
            }

            bindService(mCallServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun toggleApp(appImage: ImageView) {
        if (isAppEnabled) {
            appImage.setImageResource(R.drawable.app_on)
        } else {
            appImage.setImageResource(R.drawable.app_off)

        }
    }

    private fun unbindMyService() {
        if (isBound) {
            unbindService(mServiceConnection)
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindMyService()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SobadguysTheme {
        Greeting("Android")
    }
}