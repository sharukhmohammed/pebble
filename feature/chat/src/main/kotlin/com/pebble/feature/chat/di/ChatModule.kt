package com.pebble.feature.chat.di

import com.pebble.feature.chat.ui.ChatViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    viewModel { ChatViewModel(aiClient = get()) }
}
