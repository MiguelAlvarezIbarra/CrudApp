package mx.edu.utng.crudapp

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

class LoginActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        val usuarioInput = findViewById<EditText>(R.id.editUsuario)
        val contrasenaInput = findViewById<EditText>(R.id.editContrasena)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val fondoGif = findViewById<GifImageView>(R.id.fondoGif)

        // Cargar el GIF de fondo
        try {
            val gifDrawable: Drawable = GifDrawable(resources, R.drawable.fondo)
            fondoGif.setImageDrawable(gifDrawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Verificar si el usuario ya está autenticado
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            val usuario = usuarioInput.text.toString()
            val contrasena = contrasenaInput.text.toString()

            if (usuario.isNotEmpty() && contrasena.isNotEmpty()) {
                val user = db.getUserByCredentials(usuario, contrasena)

                if (user != null) {
                    // Guardar sesión
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isLoggedIn", true)
                    editor.putString("usuario", usuario)
                    editor.apply()

                    // Redirigir al CRUD
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}