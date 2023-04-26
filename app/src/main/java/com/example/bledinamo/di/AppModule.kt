package com.example.bledinamo.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.room.Room
import com.example.bledinamo.ble.GripBLEReceiveManager
import com.example.bledinamo.data.GripReceiveManager
import com.example.bledinamo.persistence.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context):BluetoothAdapter{
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter
    }
    @Provides
    @Singleton
    fun provideGripReceiveManager(
        @ApplicationContext context: Context,
        bluetoothAdapter: BluetoothAdapter
    ):GripReceiveManager{
        return GripBLEReceiveManager(bluetoothAdapter,context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ):AppDatabase{
        return Room.databaseBuilder(context,AppDatabase::class.java, "profile_database")
                .build()

    }
}