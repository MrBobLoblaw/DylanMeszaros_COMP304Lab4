package com.dylan.dylanmeszaros_comp304lab4_ex1.di

import com.dylan.dylanmeszaros_comp304lab4_ex1.data.RouteRepository
import com.dylan.dylanmeszaros_comp304lab4_ex1.data.RouteRepositoryImpl
import com.dylan.dylanmeszaros_comp304lab4_ex1.viewmodel.RouteViewModel
import org.koin.dsl.module

val appModules = module {
    single<RouteRepository> { RouteRepositoryImpl() }
    single { RouteViewModel(get()) }
}