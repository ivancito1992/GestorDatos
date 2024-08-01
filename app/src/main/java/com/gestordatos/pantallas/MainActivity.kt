package com.gestordatos.pantallas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.gestordatos.BBDD.AppDatabase
import com.gestordatos.BBDD.IncidentDao
import com.gestordatos.R




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonReadFile: Button = findViewById(R.id.buttonReadFile)
        val buttonForm: Button = findViewById(R.id.buttonForm)

        buttonReadFile.setOnClickListener {
            val intent = Intent(this, ReadFileActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonForm.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.buttonVisor).setOnClickListener {
            startActivity(Intent(this, IncidentsByProvinceActivity::class.java))
        }

        findViewById<Button>(R.id.buttonGestion).setOnClickListener {
            startActivity(Intent(this, TableManagementActivity::class.java))
        }
    }
}
