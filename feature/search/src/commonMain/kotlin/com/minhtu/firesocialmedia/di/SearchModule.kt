package com.minhtu.firesocialmedia.di

import com.minhtu.firesocialmedia.navigation.SearchNavGraphImpl
import com.minhtu.firesocialmedia.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.presentation.navigation.SearchNavGraph
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun searchModule() = module {
    viewModel { SearchViewModel() }
    single<SearchNavGraph> { SearchNavGraphImpl() }
}

