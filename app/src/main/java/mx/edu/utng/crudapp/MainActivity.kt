package mx.edu.utng.crudapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var claveInput: EditText
    private lateinit var usuarioInput: EditText
    private lateinit var gmailInput: EditText
    private lateinit var contrasenaInput: EditText
    private lateinit var edadInput: EditText
    private lateinit var ciudadInput: EditText
    private lateinit var sexoSpinner: Spinner
    private lateinit var insertButton: Button
    private lateinit var consultarButton: Button
    private lateinit var mostrarTodosButton: Button
    private lateinit var borrarButton: Button
    private lateinit var modificarButton: Button
    private lateinit var logoutButton: Button
    private lateinit var backgroundGif: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db = DatabaseHelper(this)
        initUI()
        setListeners()

        // Cargar el GIF de fondo
        Glide.with(this).asGif().load(R.drawable.fonde).into(backgroundGif)
    }

    private fun initUI() {
        backgroundGif = findViewById(R.id.backgroundGif)
        claveInput = findViewById(R.id.editClave)
        usuarioInput = findViewById(R.id.editUsuario)
        gmailInput = findViewById(R.id.editGmail)
        contrasenaInput = findViewById(R.id.editContrasena)
        edadInput = findViewById(R.id.editEdad)
        ciudadInput = findViewById(R.id.editCiudad)
        sexoSpinner = findViewById(R.id.spinnerSexo)

        insertButton = findViewById(R.id.btnInsertar)
        consultarButton = findViewById(R.id.btnConsultar)
        mostrarTodosButton = findViewById(R.id.btnMostrarTodos)
        borrarButton = findViewById(R.id.btnBorrar)
        modificarButton = findViewById(R.id.btnModificar)
        logoutButton = findViewById(R.id.btnLogout)

        // Configurar Spinner de Sexo
        val adapter = ArrayAdapter.createFromResource(this, R.array.sexo_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sexoSpinner.adapter = adapter
    }

    private fun setListeners() {
        insertButton.setOnClickListener { insertarUsuario() }
        consultarButton.setOnClickListener { consultarUsuario() }
        mostrarTodosButton.setOnClickListener { mostrarUsuarios() }
        borrarButton.setOnClickListener { borrarUsuario() }
        modificarButton.setOnClickListener { modificarUsuario() }
        logoutButton.setOnClickListener { cerrarSesion() }
    }
    private fun borrarUsuario() {
        val clave = claveInput.text.toString().trim()
        if (clave.isEmpty()) {
            showToast("Ingrese la clave para borrar")
            return
        }

        val user = db.getUser(clave)
        if (user == null) {
            showToast("Usuario no encontrado")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Seguro que deseas eliminar a ${user["usuario"]}?")
            .setPositiveButton("Sí") { _, _ ->
                val deleted = db.deleteUser(clave)
                if (deleted) {
                    showToast("Usuario eliminado")
                    claveInput.text.clear()
                    usuarioInput.text.clear()
                    gmailInput.text.clear()
                    contrasenaInput.text.clear()
                    edadInput.text.clear()
                    ciudadInput.text.clear()
                } else {
                    showToast("Error al eliminar usuario")
                }
            }
            .setNegativeButton("No", null)
            .show()
    }


    private fun insertarUsuario() {
        val clave = claveInput.text.toString().trim()
        val usuario = usuarioInput.text.toString().trim()
        val gmail = gmailInput.text.toString().trim()
        val contrasena = contrasenaInput.text.toString().trim()
        val ciudad = ciudadInput.text.toString().trim()
        val sexo = sexoSpinner.selectedItem.toString()
        val edad = edadInput.text.toString().trim().toIntOrNull()
        if (edad == null) {
            showToast("Edad debe ser un número válido")
            return
        }


        if (clave.isEmpty() || usuario.isEmpty() || gmail.isEmpty() || contrasena.isEmpty() || edad.toString().isEmpty() || ciudad.isEmpty()) {
            showToast("Todos los campos son obligatorios")
            return
        }

        val inserted = db.insertData(clave, usuario, gmail, contrasena, edad, ciudad, sexo)
        showToast(if (inserted) "Insertado con éxito" else "Error al insertar")
    }

    private fun consultarUsuario() {
        val clave = claveInput.text.toString().trim()
        if (clave.isEmpty()) {
            showToast("Ingrese la clave para consultar")
            return
        }

        val user = db.getUser(clave)
        if (user != null) {
            usuarioInput.setText(user["usuario"])
            gmailInput.setText(user["gmail"])
            contrasenaInput.setText(user["contrasena"])
            edadInput.setText(user["edad"])
            ciudadInput.setText(user["ciudad"])

            val sexoPosition = (sexoSpinner.adapter as ArrayAdapter<String>).getPosition(user["sexo"])
            sexoSpinner.setSelection(sexoPosition)
        } else {
            showToast("Usuario no encontrado")
        }
    }

    private fun mostrarUsuarios() {
        val users = db.getAllUsers()
        if (users.isEmpty()) {
            showToast("No hay usuarios registrados")
            return
        }

        val listView = ListView(this)
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            users.map { "${it["clave"]} - ${it["usuario"]}, ${it["edad"]} años, ${it["ciudad"]}, ${it["sexo"]}" }
        )
        listView.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Usuarios Registrados")
            .setView(listView)
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun modificarUsuario() {
        val clave = claveInput.text.toString().trim()
        val user = db.getUser(clave)

        if (user == null) {
            showToast("Usuario no encontrado")
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update, null)
        val dialogUsuario = dialogView.findViewById<EditText>(R.id.dialogUsuario)
        val dialogGmail = dialogView.findViewById<EditText>(R.id.dialogGmail)
        val dialogContrasena = dialogView.findViewById<EditText>(R.id.dialogContrasena)
        val dialogEdad = dialogView.findViewById<EditText>(R.id.dialogEdad)
        val dialogCiudad = dialogView.findViewById<EditText>(R.id.dialogCiudad)
        val dialogSexo = dialogView.findViewById<Spinner>(R.id.dialogSexo)

        dialogUsuario.setText(user["usuario"])
        dialogGmail.setText(user["gmail"])
        dialogContrasena.setText(user["contrasena"])
        dialogEdad.setText(user["edad"])
        dialogCiudad.setText(user["ciudad"])

        val sexoPosition = (dialogSexo.adapter as ArrayAdapter<String>).getPosition(user["sexo"])
        dialogSexo.setSelection(sexoPosition)

        AlertDialog.Builder(this)
            .setTitle("Modificar Usuario")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoUsuario = dialogUsuario.text.toString().trim()
                val nuevoGmail = dialogGmail.text.toString().trim()
                val nuevaContrasena = dialogContrasena.text.toString().trim()
                val nuevaEdad = dialogEdad.text.toString().trim()
                val nuevaCiudad = dialogCiudad.text.toString().trim()
                val nuevoSexo = dialogSexo.selectedItem.toString()

                db.updateUser(clave, nuevoUsuario, nuevoGmail, nuevaContrasena, nuevaEdad, nuevaCiudad, nuevoSexo)
                showToast("Usuario actualizado")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit().putBoolean("isLoggedIn", false).apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}