package com.minhtu.firesocialmedia.feature.search.di

import com.minhtu.firesocialmedia.feature.search.navigation.SearchNavGraphImpl
import com.minhtu.firesocialmedia.feature.search.presentation.search.SearchViewModel
import com.minhtu.firesocialmedia.presentation.navigation.SearchNavGraph
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun searchModule() = module {
    viewModel { SearchViewModel() }
    single<SearchNavGraph> { SearchNavGraphImpl() }
}

