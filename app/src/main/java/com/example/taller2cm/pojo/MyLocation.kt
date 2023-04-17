package com.example.taller2cm.pojo

import org.json.JSONException
import org.json.JSONObject

class MyLocation {
    lateinit var fecha: String
    lateinit var latitud: String
    lateinit var longitud: String

    fun toJSON(): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("latitud", latitud)
            obj.put("longitud", longitud)
            obj.put("date", fecha)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return obj
    }

}