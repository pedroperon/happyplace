package com.example.happyplace.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.happyplace.LocalShoppingList
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object ShoppingListSerializer : Serializer<LocalShoppingList> {

    override val defaultValue: LocalShoppingList
        get() = LocalShoppingList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): LocalShoppingList {
        try {
            return LocalShoppingList.parseFrom(input)
        }
        catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: LocalShoppingList, output: OutputStream) {
        t.writeTo(output)
    }
}
