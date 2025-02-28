package mx.edu.utng.crudapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
z
internal class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, "Users.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE users (clave TEXT PRIMARY KEY, usuario TEXT, gmail TEXT, contrasena TEXT, edad INTEGER, ciudad TEXT, sexo TEXT)")

        // Insertar usuario predeterminado "Admin Miguel"
        val values = ContentValues()
        values.put("clave", "1")
        values.put("usuario", "Admin Miguel")
        values.put("gmail", "miguelangelalvarezibarrautng@gmail.com")
        values.put("contrasena", "12345678")
        values.put("edad", 19)
        values.put("ciudad", "Ciudad de México")
        values.put("sexo", "Masculino")
        db.insert("users", null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE users ADD COLUMN edad INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE users ADD COLUMN ciudad TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE users ADD COLUMN sexo TEXT DEFAULT ''")
        }
    }

    fun insertData(
        clave: String,
        usuario: String?,
        gmail: String?,
        contrasena: String?,
        edad: Int,
        ciudad: String?,
        sexo: String?
    ): Boolean {
        if (clave == "1") return false // No permitir insertar otro Admin Miguel


        val db = this.writableDatabase
        val values = ContentValues()
        values.put("clave", clave)
        values.put("usuario", usuario)
        values.put("gmail", gmail)
        values.put("contrasena", contrasena)
        values.put("edad", edad)
        values.put("ciudad", ciudad)
        values.put("sexo", sexo)

        return db.insert("users", null, values) != -1L
    }

    fun updateUser(
        clave: String,
        usuario: String?,
        gmail: String?,
        contrasena: String?,
        edad: String,
        ciudad: String?,
        sexo: String?
    ): Boolean {
        if (clave == "1") return false // No permitir actualizar a Admin Miguel


        val db = this.writableDatabase
        val values = ContentValues()
        values.put("usuario", usuario)
        values.put("gmail", gmail)
        values.put("contrasena", contrasena)
        values.put("edad", edad)
        values.put("ciudad", ciudad)
        values.put("sexo", sexo)

        return db.update("users", values, "clave=?", arrayOf(clave)) > 0
    }

    fun getUser(clave: String): Map<String, String>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE clave=?", arrayOf(clave))

        if (cursor.moveToFirst()) {
            val user: MutableMap<String, String> = HashMap()
            user["clave"] = cursor.getString(0)
            user["usuario"] = cursor.getString(1)
            user["gmail"] = cursor.getString(2)
            user["contrasena"] = cursor.getString(3)
            user["edad"] = cursor.getString(4)
            user["ciudad"] = cursor.getString(5)
            user["sexo"] = cursor.getString(6)
            cursor.close()
            return user
        }
        cursor.close()
        return null
    }
    fun validateUser(usuario: String, contrasena: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE usuario = ? AND contrasena = ?",
            arrayOf(usuario, contrasena)
        )
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }
    fun deleteUser(clave: String): Boolean {
        if (clave == "1") return false // No permitir eliminar a Admin Miguel

        val db = this.writableDatabase
        return db.delete("users", "clave=?", arrayOf(clave)) > 0
    }
    fun getUserByCredentials(usuario: String, contrasena: String): Map<String, String>? {
        val db = this.readableDatabase
        val query = "SELECT * FROM users WHERE usuario = ? AND contrasena = ?"
        val cursor = db.rawQuery(query, arrayOf(usuario, contrasena))

        return if (cursor.moveToFirst()) {
            val user = mapOf(
                "clave" to cursor.getString(cursor.getColumnIndexOrThrow("clave")),
                "usuario" to cursor.getString(cursor.getColumnIndexOrThrow("usuario")),
                "gmail" to cursor.getString(cursor.getColumnIndexOrThrow("gmail")),
                "contrasena" to cursor.getString(cursor.getColumnIndexOrThrow("contrasena"))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }
    fun getAllUsers(): List<Map<String, String>> {
        val usersList = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users", null)

        if (cursor.moveToFirst()) {
            do {
                val user = mapOf(
                    "clave" to cursor.getString(cursor.getColumnIndexOrThrow("clave")),
                    "usuario" to cursor.getString(cursor.getColumnIndexOrThrow("usuario")),
                    "gmail" to cursor.getString(cursor.getColumnIndexOrThrow("gmail")),
                    "contrasena" to cursor.getString(cursor.getColumnIndexOrThrow("contrasena")),
                    "edad" to cursor.getString(cursor.getColumnIndexOrThrow("edad")),
                    "ciudad" to cursor.getString(cursor.getColumnIndexOrThrow("ciudad")),
                    "sexo" to cursor.getString(cursor.getColumnIndexOrThrow("sexo"))
                )
                usersList.add(user)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return usersList
    }

}