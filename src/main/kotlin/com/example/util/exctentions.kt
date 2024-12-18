package com.example.util

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor


inline fun <reified T : Enum<T>> Int.toEnum(): T? {
    return enumValues<T>().firstOrNull { it.ordinal == this }
}

fun addOneMoth() : Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.add(Calendar.MONTH, 1)
    val newTimeInMillis = calendar.timeInMillis
    return  newTimeInMillis;
}
fun addSixMoth() : Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.add(Calendar.MONTH, 6)
    val newTimeInMillis = calendar.timeInMillis
    return  newTimeInMillis;
}
fun addOneYear() : Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.add(Calendar.YEAR, 1)
    val newTimeInMillis = calendar.timeInMillis
    return  newTimeInMillis;
}

inline fun <reified T : Any, reified R : Any> T.convertTo(): R {
    val sourceProperties = T::class.memberProperties.associateBy { it.name }
    val targetConstructor = R::class.primaryConstructor!!

    val args = targetConstructor.parameters.associateWith { parameter ->
        sourceProperties[parameter.name]?.get(this)
    }

    return targetConstructor.callBy(args)
}

fun Long.toFormattedDateString(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.of("Asia/Amman"))
        .format(formatter)
}
fun Long.toFormattedDashDateString(): String {
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.of("Asia/Amman"))
        .format(formatter)
}


fun Long.toFormattedDashMonthDateString(): String {
    val formatter = DateTimeFormatter.ofPattern("MM", Locale.ENGLISH)
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.of("Asia/Amman"))
        .format(formatter)
}

fun Long.toFormattedDashYearDateString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH)
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.of("Asia/Amman"))
        .format(formatter)
}

suspend inline fun <reified T : Any> ApplicationCall.receiveModel(): T {
    // Receive the raw JSON from the request
    val rawString = receiveText()

    // Deserialize the JSON string into the model object of type T
    return Json.decodeFromString<T>(rawString)
}


fun String?.isNullOrBlank(): Boolean {
    return this == null || this.trim().isEmpty() || this.equals("null", ignoreCase = true)
}

// Extension function for Double
fun Double?.isNullOrBlank(): Boolean {
    return this == null || this.toString().isNullOrBlank() || this == 0.0
}

// Extension function for Long
fun Long?.isNullOrBlank(): Boolean {
    return this == null || this.toString().isNullOrBlank()
}

// Extension function for Int
fun Int?.isNullOrBlank(): Boolean {
    return this == null || this.toString().isNullOrBlank()
}

fun Boolean?.isNullOrBlank(): Boolean {
    return this == null || this.toString().isNullOrBlank()
}

fun String.removeQueryParams(): String {
    return this.substringBefore("?").removePrefix("/")
}

fun String?.toSafeInt(): Int? {
    return if (this == null || this == "null") null else this.toIntOrNull()
}

// Extension function for String to Long conversion with "null" handling
fun String?.toSafeLong(): Long? {
    return if (this == null || this == "null") null else this.toLongOrNull()
}

// Extension function for String to Boolean conversion with "null" handling
fun String?.toSafeBoolean(): Boolean? {
    return if (this == null || this == "null") null else this.toBooleanStrictOrNull()
}

// Extension function for String to Double conversion with "null" handling
fun String?.toSafeDouble(): Double? {
    return if (this == null || this == "null") null else this.toDoubleOrNull()
}

fun Double.toDoubleAmount(): Double {
    return String.format("%.2f", this).toDouble()
}

fun String?.toSafeString(): String? {
    return if (this == null || this == "null") null else this.toString()
}