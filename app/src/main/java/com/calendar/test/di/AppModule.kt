package com.calendar.test.di

import android.app.Application
import androidx.room.Room
import com.calendar.test.repository.local.AppDatabase
import com.calendar.test.repository.local.EventDAO
import com.calendar.test.repository.local.LocalRepository
import com.calendar.test.viewmodel.AddEventViewModel
import com.calendar.test.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val appModule = module {
    single { provideDataBase(androidApplication()) }
    single { provideDao(get()) }

    viewModel {
        MainViewModel(get())
    }
    viewModel {
        AddEventViewModel(get())
    }
    factory { LocalRepository(get()) }
}
fun provideDataBase(application: Application): AppDatabase {
    return Room.databaseBuilder(application, AppDatabase::class.java, "CAL_EVENTS")
        .fallbackToDestructiveMigration()
        .build()
}

fun provideDao(dataBase: AppDatabase): EventDAO {
    return dataBase.eventsDao()
}